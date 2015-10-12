package com.squareup.otto;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.AbstractElementVisitor6;
import javax.tools.Diagnostic;

/**
 * Annotation processor that detects some cases of misusing Otto at compile time.
 *
 * @author balazsbanyai
 */
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedAnnotationTypes({ "com.squareup.otto.Subscribe" })
public class OttoAnnotationProcessor extends AbstractProcessor {
    private AbstractMethodVisitor[] checks = {
            new VisibilityCheckerVisitor(),
            new ArgumentListLengthCheckerVisitor()
    };

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {

        for (Element element : roundEnvironment.getElementsAnnotatedWith(Subscribe.class)) {
            for (AbstractMethodVisitor check : checks) {
                element.accept(check, null);
            }
        }

        return true;
    }

    private class ArgumentListLengthCheckerVisitor extends AbstractMethodVisitor {

        @Override
        public Void visitExecutable(ExecutableElement element, Void aVoid) {
            if (element.getParameters().size() != 1) {
                String methodName = getQualifiedMethodName(element);
                int argumentListSize = element.getParameters().size();
                String message = ErrorMessages.newInvalidArgumentListMessage(methodName, argumentListSize);
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message);
            }
            return null;
        }
    }

    private class VisibilityCheckerVisitor extends AbstractMethodVisitor {

        @Override
        public Void visitExecutable(ExecutableElement element, Void aVoid) {
            if (!element.getModifiers().contains(Modifier.PUBLIC)) {
                String methodName = getQualifiedMethodName(element);
                String eventTypeName = element.getParameters().get(0).asType().toString();
                String message = ErrorMessages.newNotVisibleMessage(methodName, eventTypeName);
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message);
            }
            return null;
        }
    }

    private String getQualifiedMethodName(ExecutableElement element) {
        TypeElement typeElement = (TypeElement) element.getEnclosingElement();
        String className = typeElement.getQualifiedName().toString();
        String methodName = element.toString();
        return className + "." + methodName;
    }

    private abstract class AbstractMethodVisitor extends AbstractElementVisitor6<Void, Void> {

        @Override
        public Void visitPackage(PackageElement e, Void aVoid) {
            return null;
        }

        @Override
        public Void visitType(TypeElement e, Void aVoid) {
            return null;
        }

        @Override
        public Void visitVariable(VariableElement e, Void aVoid) {
            return null;
        }

        @Override
        public Void visitTypeParameter(TypeParameterElement e, Void aVoid) {
            return null;
        }
    }

}
