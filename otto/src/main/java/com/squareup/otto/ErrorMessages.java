package com.squareup.otto;

import java.util.Locale;

public class ErrorMessages {

    private static final String INVALID_ARGUMENT_LIST = "Method %s has @Subscribe annotation but requires %d arguments. Methods must require a single argument.";
    private static final String NOT_VISIBLE =  "Method %s has @Subscribe annotation on %s but is not 'public'.";

    public static String newInvalidArgumentListMessage(String methodName, int argumentCount) {
        return String.format(Locale.US, INVALID_ARGUMENT_LIST, methodName, argumentCount);
    }


    public static String newNotVisibleMessage(String name, String eventType) {
        return String.format(Locale.US, NOT_VISIBLE, name, eventType);
    }
}
