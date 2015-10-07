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

@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedAnnotationTypes({ "com.squareup.otto.Subscribe" })
public class OttoAnnotationProcessor extends AbstractProcessor {
    private AbstractMethodVisitor[] checks = { new VisibilityCheckerVisitor(), new ArgumentListLengthCheckerVisitor() };

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
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, ErrorMessages.newInvalidArgumentListMessage(element.toString(), ((ExecutableElement) element).getParameters().size()));
            }
            return null;
        }
    }

    private class VisibilityCheckerVisitor extends AbstractMethodVisitor {

        @Override
        public Void visitExecutable(ExecutableElement element, Void aVoid) {
            if (!element.getModifiers().contains(Modifier.PUBLIC)) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, ErrorMessages.newNotVisibleMessage(element.toString(), ((ExecutableElement) element).getParameters().get(0).asType().toString()));
            }
            return null;
        }
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
