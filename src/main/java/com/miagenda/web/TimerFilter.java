package com.miagenda.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.Locale;

/** Equivalente a TimerMiddleware: añade el header X-Process-Time (segundos). */
@Component
public class TimerFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        long start = System.nanoTime();
        // Se bufferiza la respuesta para poder fijar el header antes de confirmarla.
        ContentCachingResponseWrapper wrapper = new ContentCachingResponseWrapper(response);
        try {
            chain.doFilter(request, wrapper);
        } finally {
            double seconds = (System.nanoTime() - start) / 1_000_000_000.0;
            wrapper.setHeader("X-Process-Time", String.format(Locale.ROOT, "%.4f", seconds));
            wrapper.copyBodyToResponse();
        }
    }
}
