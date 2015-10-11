package com.squareup.otto;

import junit.framework.TestCase;

import org.fest.assertions.util.ArrayWrapperList;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.util.collections.Sets;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class OttoAnnotationProcessorTest {

    private static final String METHOD_ARGUMENT_TYPE_NAME = "TestEvent";

    @Mock
    private ProcessingEnvironment env;

    @Mock
    private Messager messager;

    @Mock
    private RoundEnvironment roundEnvironment;

    @Mock
    private Set<? extends TypeElement> annotations;

    private OttoAnnotationProcessor processor;

    @Mock
    private ExecutableElement executableElement;


    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        when(env.getMessager()).thenReturn(messager);

        processor = new OttoAnnotationProcessor();
        processor.init(env);

        Set<ExecutableElement> elements = Sets.newSet(executableElement);
        when(roundEnvironment.getElementsAnnotatedWith(Subscribe.class)).thenReturn((Set) elements);
        setupVisitableMockElement();
    }

    private void setupVisitableMockElement() {
        when(executableElement.accept(any(ElementVisitor.class), anyObject())).then(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                ElementVisitor visitor = invocationOnMock.getArgumentAt(0, ElementVisitor.class);
                Object parameter = invocationOnMock.getArguments()[1];
                visitor.visitExecutable(executableElement, parameter);
                return null;
            }
        });
    }

    private VariableElement mockMethodArgument() {
        VariableElement mockEvent = mock(VariableElement.class);
        TypeMirror mockTypeMirror = mock(TypeMirror.class);
        when(mockTypeMirror.toString()).thenReturn(METHOD_ARGUMENT_TYPE_NAME);
        when(mockEvent.asType()).thenReturn(mockTypeMirror);
        return mockEvent;
    }

    @Test
    public void process_validSignature_noErrorReported() {
        VariableElement argument1 = mockMethodArgument();
        List args = Arrays.asList(new VariableElement[]{argument1});
        when(executableElement.getModifiers()).thenReturn(EnumSet.of(Modifier.PUBLIC));
        when(executableElement.getParameters()).thenReturn(args);

        processor.process(annotations, roundEnvironment);

        verifyZeroInteractions(messager);
    }

    @Test
    public void process_wrongArgumentListLength_errorReported() {
        VariableElement argument1 = mockMethodArgument();
        VariableElement argument2 = mockMethodArgument();
        List args = Arrays.asList(new VariableElement[]{argument1, argument2});
        when(executableElement.getModifiers()).thenReturn(EnumSet.of(Modifier.PUBLIC));
        when(executableElement.getParameters()).thenReturn(args);

        processor.process(annotations, roundEnvironment);

        verifyWrongArgumentListLengthMessage();
    }

    private void verifyWrongArgumentListLengthMessage() {
        Diagnostic.Kind expectedKind = Diagnostic.Kind.ERROR;
        String expectedMessage = ErrorMessages.newInvalidArgumentListMessage(executableElement.toString(), executableElement.getParameters().size());
        verify(messager).printMessage(eq(expectedKind), eq(expectedMessage));
    }

    @Test
    public void process_wrongModifier_errorReported() {
        VariableElement argument1 = mockMethodArgument();
        List args = Arrays.asList(new VariableElement[]{argument1});
        when(executableElement.getModifiers()).thenReturn(EnumSet.of(Modifier.PRIVATE));
        when(executableElement.getParameters()).thenReturn(args);

        processor.process(annotations, roundEnvironment);

        verifyNotVisibleMessage();
    }

    private void verifyNotVisibleMessage() {
        Diagnostic.Kind expectedKind = Diagnostic.Kind.ERROR;
        String expectedMessage = ErrorMessages.newNotVisibleMessage(executableElement.toString(), METHOD_ARGUMENT_TYPE_NAME);
        verify(messager).printMessage(eq(expectedKind), eq(expectedMessage));
    }

    @Test
    public void process_wrongModifierAndParameterLength_bothErrorsReported() {
        VariableElement argument1 = mockMethodArgument();
        VariableElement argument2 = mockMethodArgument();
        List args = Arrays.asList(new VariableElement[]{argument1, argument2});
        when(executableElement.getModifiers()).thenReturn(EnumSet.of(Modifier.PRIVATE));
        when(executableElement.getParameters()).thenReturn(args);

        processor.process(annotations, roundEnvironment);

        verifyNotVisibleMessage();
        verifyWrongArgumentListLengthMessage();
    }
}
