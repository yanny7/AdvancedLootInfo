#!/usr/bin/env python3
"""Minimal Java class file reader: enough for a type hierarchy and member shapes.

No dependencies on purpose — this runs against a few hundred thousand class entries and
must not need a jvm or a pip install. Only the constant pool, the hierarchy header and the
field/method descriptors are decoded; code bodies are skipped.
"""
import struct

ACC_PUBLIC = 0x0001
ACC_FINAL = 0x0010
ACC_INTERFACE = 0x0200
ACC_ABSTRACT = 0x0400
ACC_SYNTHETIC = 0x1000
ACC_ANNOTATION = 0x2000
ACC_ENUM = 0x4000
ACC_STATIC = 0x0008

# tag -> (byte length after the tag, does it consume two pool slots)
FIXED_TAGS = {
    3: (4, False), 4: (4, False), 5: (8, True), 6: (8, True), 7: (2, False), 8: (2, False),
    9: (4, False), 10: (4, False), 11: (4, False), 12: (4, False), 15: (3, False),
    16: (2, False), 17: (4, False), 18: (4, False), 19: (2, False), 20: (2, False),
}


class ClassFileError(Exception):
    pass


class ClassInfo:
    __slots__ = ("name", "supername", "interfaces", "access", "fields", "methods", "strings")

    def __init__(self, name, supername, interfaces, access, fields, methods, strings):
        self.name = name
        self.supername = supername
        self.interfaces = interfaces
        self.access = access
        self.fields = fields
        self.methods = methods
        self.strings = strings

    @property
    def is_interface(self):
        return bool(self.access & ACC_INTERFACE)

    @property
    def is_abstract(self):
        return bool(self.access & ACC_ABSTRACT)

    @property
    def is_public(self):
        return bool(self.access & ACC_PUBLIC)

    @property
    def is_enum(self):
        return bool(self.access & ACC_ENUM)

    @property
    def binary_name(self):
        return self.name.replace("/", ".")

    def constructors(self):
        return [m["descriptor"] for m in self.methods if m["name"] == "<init>"]


def _read_constant_pool(data, offset, count):
    utf8 = {}
    class_ref = {}
    string_ref = {}
    index = 1
    while index < count:
        tag = data[offset]
        offset += 1
        if tag == 1:
            length = struct.unpack_from(">H", data, offset)[0]
            offset += 2
            utf8[index] = data[offset:offset + length].decode("utf-8", "replace")
            offset += length
            index += 1
            continue
        if tag not in FIXED_TAGS:
            raise ClassFileError(f"unknown constant pool tag {tag}")
        size, wide = FIXED_TAGS[tag]
        if tag == 7:
            class_ref[index] = struct.unpack_from(">H", data, offset)[0]
        elif tag == 8:
            string_ref[index] = struct.unpack_from(">H", data, offset)[0]
        offset += size
        index += 2 if wide else 1
    return offset, utf8, class_ref, string_ref


def _skip_attributes(data, offset):
    count = struct.unpack_from(">H", data, offset)[0]
    offset += 2
    for _ in range(count):
        length = struct.unpack_from(">I", data, offset + 2)[0]
        offset += 6 + length
    return offset


def _read_members(data, offset, utf8):
    count = struct.unpack_from(">H", data, offset)[0]
    offset += 2
    out = []
    for _ in range(count):
        access, name_index, descriptor_index = struct.unpack_from(">HHH", data, offset)
        offset = _skip_attributes(data, offset + 6)
        out.append({
            "name": utf8.get(name_index, ""),
            "descriptor": utf8.get(descriptor_index, ""),
            "access": access,
        })
    return offset, out


def parse(data, want_strings=False):
    """Parse one class file. Set want_strings to also keep its string constants."""
    if len(data) < 10 or struct.unpack_from(">I", data, 0)[0] != 0xCAFEBABE:
        raise ClassFileError("not a class file")
    pool_count = struct.unpack_from(">H", data, 8)[0]
    offset, utf8, class_ref, string_ref = _read_constant_pool(data, 10, pool_count)

    access, this_index, super_index = struct.unpack_from(">HHH", data, offset)
    offset += 6
    interface_count = struct.unpack_from(">H", data, offset)[0]
    offset += 2
    interfaces = []
    for position in range(interface_count):
        index = struct.unpack_from(">H", data, offset + position * 2)[0]
        interfaces.append(utf8.get(class_ref.get(index, 0), ""))
    offset += interface_count * 2

    offset, fields = _read_members(data, offset, utf8)
    offset, methods = _read_members(data, offset, utf8)

    strings = sorted({utf8.get(i, "") for i in string_ref.values()}) if want_strings else []
    return ClassInfo(
        name=utf8.get(class_ref.get(this_index, 0), ""),
        supername=utf8.get(class_ref.get(super_index, 0), "") if super_index else "",
        interfaces=[i for i in interfaces if i],
        access=access,
        fields=fields,
        methods=methods,
        strings=strings,
    )


def field_types(info):
    """Declared instance fields as (name, descriptor), synthetic ones dropped."""
    return [(f["name"], f["descriptor"]) for f in info.fields
            if not f["access"] & (ACC_SYNTHETIC | ACC_STATIC)]
