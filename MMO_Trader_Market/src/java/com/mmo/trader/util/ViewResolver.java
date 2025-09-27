package com.mmo.trader.util;

/**
 * Resolves logical view names to JSP paths. Keeping the mapping in a single
 * place simplifies changing the folder structure later.
 */
public final class ViewResolver {

    private static final String PREFIX = "/WEB-INF/views/";
    private static final String SUFFIX = ".jsp";

    private ViewResolver() {
    }

    public static String resolve(String viewName) {
        return PREFIX + viewName + SUFFIX;
    }
}
