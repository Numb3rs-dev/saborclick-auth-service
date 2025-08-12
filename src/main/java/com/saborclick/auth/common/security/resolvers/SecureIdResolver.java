package com.saborclick.auth.common.security.resolvers;
import com.saborclick.auth.common.security.JwtService;
import com.saborclick.auth.common.security.SecureIdService;
import com.saborclick.auth.common.security.annotations.SecureId;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@RequiredArgsConstructor
public class SecureIdResolver implements HandlerMethodArgumentResolver {

    private final JwtService jwtService;
    private final HttpServletRequest request;
    private final SecureIdService secureIdService;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(SecureId.class) && parameter.getParameterType().equals(String.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        String secureId = webRequest.getParameter(parameter.getParameterName());
        if (secureId == null) {
            throw new IllegalArgumentException("El ID firmado es requerido");
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalStateException("Token JWT no presente en cabecera Authorization");
        }
        String jwt = authHeader.substring(7);
        String sessionHash = jwtService.getTokenHash(jwt);

        return secureIdService.verifyAndExtract(secureId, sessionHash);
    }
}