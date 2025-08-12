package com.saborclick.auth.common.trace;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class TraceIdFilter extends OncePerRequestFilter {

    public static final String TRACE_ID = "traceId";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String traceId = UUID.randomUUID().toString();
        MDC.put(TRACE_ID, traceId); // Agrega traceId al contexto para los logs
        response.setHeader("X-Trace-Id", traceId); // También lo devolvemos en la respuesta

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(TRACE_ID); // Limpia para evitar errores en hilos reutilizados
        }
    }
}

