package com.marceloaleixo.treinoflow.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final AssinaturaAcessoInterceptor assinaturaAcessoInterceptor;

    public WebMvcConfig(AssinaturaAcessoInterceptor assinaturaAcessoInterceptor) {
        this.assinaturaAcessoInterceptor = assinaturaAcessoInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(assinaturaAcessoInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/login", "/cadastro", "/error", "/css/**", "/js/**", "/images/**", "/a/**", "/portal/**");
    }
}
