package com.yanny.ali.processor;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SupportedAnnotationTypes(AccessorProcessor.FIELD_ACCESSOR)
public class AccessorProcessor extends AbstractProcessor {
    static final String FIELD_ACCESSOR = "com.yanny.alicompat.accessor.FieldAccessor";
    private static final String CLASS_ACCESSOR = "com.yanny.alicompat.accessor.ClassAccessor";
    private static final String BASE_ACCESSOR = "com.yanny.alicompat.accessor.BaseAccessor";
    private static final String OBJECT = "java.lang.Object";

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Elements elements = processingEnv.getElementUtils();
        TypeElement fieldAccessor = elements.getTypeElement(FIELD_ACCESSOR);

        if (fieldAccessor == null) {
            return false;
        }

        Map<TypeElement, List<VariableElement>> perAccessor = new LinkedHashMap<>();

        for (Element element : roundEnv.getElementsAnnotatedWith(fieldAccessor)) {
            if (element.getKind() == ElementKind.FIELD && element.getEnclosingElement() instanceof TypeElement owner) {
                perAccessor.computeIfAbsent(owner, (k) -> new ArrayList<>()).add((VariableElement) element);
            }
        }

        perAccessor.forEach(this::validate);
        return false;
    }

    private void validate(TypeElement accessor, List<VariableElement> fields) {
        TypeMirror target = resolveTarget(accessor);

        if (target == null) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Target class of accessor "
                    + accessor.getQualifiedName() + " is not on the compile classpath, skipping field validation", accessor);
            return;
        }

        Map<String, VariableElement> targetFields = collectFields(target);

        for (VariableElement field : fields) {
            validateField(accessor, field, target, targetFields);
        }
    }

    private void validateField(TypeElement accessor, VariableElement field, TypeMirror target, Map<String, VariableElement> targetFields) {
        String name = field.getSimpleName().toString();

        if (field.getModifiers().contains(Modifier.STATIC)) {
            error(field, "@FieldAccessor field " + name + " must not be static");
            return;
        }
        if (field.getModifiers().contains(Modifier.FINAL)) {
            error(field, "@FieldAccessor field " + name + " must not be final, reflection cannot assign it");
            return;
        }

        VariableElement targetField = targetFields.get(name);

        if (targetField == null) {
            error(field, "No field named " + name + " in " + target + " (accessor " + accessor.getQualifiedName() + ")");
            return;
        }

        Types types = processingEnv.getTypeUtils();
        TypeMirror fieldType = types.erasure(field.asType());
        TypeMirror targetType = types.erasure(targetField.asType());
        TypeMirror nested = nestedAccessor(field);

        if (nested == null) {
            if (!types.isAssignable(targetType, fieldType)) {
                error(field, "Field " + name + " is " + fieldType + " but " + target + "." + name + " is " + targetType);
            }
            return;
        }

        if (!types.isAssignable(types.erasure(nested), fieldType)) {
            error(field, "Field " + name + " is " + fieldType + " but is filled with " + nested);
            return;
        }

        TypeElement nestedElement = (TypeElement) types.asElement(nested);
        TypeMirror nestedTarget = nestedElement != null ? resolveTarget(nestedElement) : null;

        if (nestedTarget != null && !types.isAssignable(targetType, types.erasure(nestedTarget))) {
            error(field, "Accessor " + nested + " reads " + nestedTarget + " but " + target + "." + name + " is " + targetType);
        }
    }

    private TypeMirror resolveTarget(TypeElement accessor) {
        String declared = classAccessorValue(accessor);

        if (declared != null) {
            TypeElement element = processingEnv.getElementUtils().getTypeElement(declared);
            return element != null ? element.asType() : null;
        }

        Types types = processingEnv.getTypeUtils();
        Deque<TypeMirror> queue = new ArrayDeque<>(List.of(accessor.asType()));
        Set<String> seen = new LinkedHashSet<>();

        while (!queue.isEmpty()) {
            TypeMirror current = queue.poll();

            if (!(current instanceof DeclaredType declaredType) || !seen.add(types.erasure(current).toString())) {
                continue;
            }

            if (types.erasure(current).toString().equals(BASE_ACCESSOR)) {
                List<? extends TypeMirror> arguments = declaredType.getTypeArguments();

                if (arguments.size() != 1) {
                    return null;
                }

                TypeMirror argument = arguments.get(0);

                if (argument.getKind() != TypeKind.DECLARED || argument.toString().equals(OBJECT)) {
                    return null;
                }

                return argument;
            }

            queue.addAll(types.directSupertypes(current));
        }

        return null;
    }

    private String classAccessorValue(TypeElement accessor) {
        for (AnnotationMirror mirror : accessor.getAnnotationMirrors()) {
            if (!mirror.getAnnotationType().toString().equals(CLASS_ACCESSOR)) {
                continue;
            }

            Object value = annotationValue(mirror, "value");

            if (value instanceof String string) {
                return string;
            }
        }

        return null;
    }

    private TypeMirror nestedAccessor(VariableElement field) {
        for (AnnotationMirror mirror : field.getAnnotationMirrors()) {
            if (!mirror.getAnnotationType().toString().equals(FIELD_ACCESSOR)) {
                continue;
            }

            Object value = annotationValue(mirror, "clazz");

            if (value instanceof TypeMirror type && !type.toString().equals(OBJECT)) {
                return type;
            }
        }

        return null;
    }

    private Object annotationValue(AnnotationMirror mirror, String name) {
        Map<? extends ExecutableElement, ? extends AnnotationValue> values =
                processingEnv.getElementUtils().getElementValuesWithDefaults(mirror);

        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : values.entrySet()) {
            if (entry.getKey().getSimpleName().contentEquals(name)) {
                return entry.getValue().getValue();
            }
        }

        return null;
    }

    private Map<String, VariableElement> collectFields(TypeMirror target) {
        Map<String, VariableElement> result = new LinkedHashMap<>();
        Types types = processingEnv.getTypeUtils();
        TypeMirror current = target;

        while (current instanceof DeclaredType declaredType && !types.erasure(current).toString().equals(OBJECT)) {
            TypeElement element = (TypeElement) declaredType.asElement();

            for (Element member : element.getEnclosedElements()) {
                if (member.getKind() == ElementKind.FIELD) {
                    result.putIfAbsent(member.getSimpleName().toString(), (VariableElement) member);
                }
            }

            current = element.getSuperclass();
        }

        return result;
    }

    private void error(Element element, String message) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message, element);
    }
}
