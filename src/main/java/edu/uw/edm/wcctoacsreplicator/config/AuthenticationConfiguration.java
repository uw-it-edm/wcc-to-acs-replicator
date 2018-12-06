package edu.uw.edm.wcctoacsreplicator.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author Maxime Deravet Date: 3/27/18
 */
@Configuration
@EnableWebSecurity
public class AuthenticationConfiguration {


    @Bean
    public WebMvcConfigurer corsConfigurer() {


        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                String[] allowedHeaders = {HttpHeaders.CONTENT_TYPE};

                registry.addMapping("/**")
                        .allowedOrigins("*")
                        .allowedMethods("*")
                        .allowedHeaders(allowedHeaders)
                        .allowCredentials(false).maxAge(3600);
            }
        };
    }

    @Configuration
    @Order(1)
    public static class ConfigurerAdapter extends WebSecurityConfigurerAdapter {

        @Autowired
        public ConfigurerAdapter() {

        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.csrf().disable();

            http
                    .anonymous();

            http.sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        }


    }
}
