package com.saborclick.auth.common.config;

import com.saborclick.auth.common.security.resolvers.SecureIdResolver;
import com.saborclick.auth.common.security.resolvers.SessionHashArgumentResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final SecureIdResolver secureIdResolver;
    private final SessionHashArgumentResolver sessionHashArgumentResolver;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(secureIdResolver);
        resolvers.add(sessionHashArgumentResolver);
    }
}