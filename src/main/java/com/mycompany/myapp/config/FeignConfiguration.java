package com.mycompany.myapp.config;

import feign.RequestInterceptor;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.security.oauth2.client.feign.OAuth2FeignRequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;

import javax.inject.Inject;

@Configuration
@EnableFeignClients(basePackages = "com.mycompany.myapp")
public class FeignConfiguration {

    @Inject
    JHipsterProperties jHipsterProperties;

    @Bean
    public RequestInterceptor oauth2FeignRequestInterceptor() {
        return new OAuth2FeignRequestInterceptor(new DefaultOAuth2ClientContext(), jHipsterProperties.getSecurity().getClientAuthorization());
    }

}
