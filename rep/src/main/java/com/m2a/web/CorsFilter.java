package com.m2a.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public final class CorsFilter extends OncePerRequestFilter {

    private static final Set<String> ALLOWED_METHODS = Set.of(
            "GET",
            "POST",
            "PUT",
            "DELETE",
            "OPTIONS",
            "PATCH",
            "HEAD"
    );
    private static final List<String> ALLOWED_HEADERS = List.of(
            "Authorization",
            "Content-Type",
            "X-XSRF-TOKEN",
            "Cache-Control",
            "X-Requested-With",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
    );
    private static final List<String> EXPOSED_HEADERS = List.of(
            "X-XSRF-TOKEN",
            "Location",
            "Content-Disposition"
    );
    private static final String MAX_AGE = "86400"; // 24 hours

    private final boolean enableCors;
    private final List<String> allowedOrigins;

    /**
     * Creates a CORS filter with default settings (enabled, all origins allowed)
     */
    public CorsFilter() {
        this(true, "*");
    }

    /**
     * Creates a CORS filter with configurable settings
     *
     * @param enableCors whether CORS should be enabled
     */
    public CorsFilter(boolean enableCors) {
        this(enableCors, enableCors ? "*" : "");
    }

    /**
     * Creates a CORS filter with specific origin restrictions
     *
     * @param enableCors     whether CORS should be enabled
     * @param allowedOrigins comma-separated list of allowed origins
     */
    public CorsFilter(boolean enableCors, String allowedOrigins) {
        this.enableCors = enableCors;
        this.allowedOrigins = parseAllowedOrigins(allowedOrigins);
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        if (!enableCors) {
            filterChain.doFilter(request, response);
            return;
        }

        String origin = request.getHeader("Origin");
        if (origin != null && isOriginAllowed(origin)) {
            response.setHeader("Access-Control-Allow-Origin", allowedOrigins.contains("*") ? "*" : origin);
            response.setHeader("Vary", "Origin");
        }

        response.setHeader("Access-Control-Allow-Methods", String.join(", ", ALLOWED_METHODS));
        response.setHeader("Access-Control-Max-Age", MAX_AGE);
        response.setHeader("Access-Control-Allow-Headers", String.join(", ", ALLOWED_HEADERS));
        response.setHeader("Access-Control-Expose-Headers", String.join(", ", EXPOSED_HEADERS));
        response.setHeader("Access-Control-Allow-Credentials", "true");

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isOriginAllowed(String origin) {
        if (allowedOrigins.contains("*")) {
            return true;
        }
        return allowedOrigins.stream()
                .anyMatch(allowed -> allowed.equalsIgnoreCase(origin) ||
                        (allowed.startsWith("*.") &&
                                origin.matches("^(.+\\.)?" + allowed.substring(2) + "$")));
    }

    private static List<String> parseAllowedOrigins(String origins) {
        if (origins == null || origins.trim().isEmpty())
            return List.of();
        return Stream.of(origins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return false;
    }

    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return false;
    }
}