package com.vikim.demo;

import com.vikim.demo.security.RestAccessDeniedHandler;
import com.vikim.demo.security.RestAuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Value("${cors.allowed-origins}")
    String corsAllowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            RestAuthenticationEntryPoint authenticationEntryPoint,
            RestAccessDeniedHandler accessDeniedHandler,
            JwtAuthenticationConverter converter
    ) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session -> session
                        .sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                HttpMethod.POST,
                                "/auth/login/email-password",
                                "/auth/send-verification-code",
                                "/auth/login/email-verification-code",
                                "/user/register",
                                "/oauth/token",
                                // admin 的公开路由
                                "/admin/auth/login"
                        ).permitAll()
                        .requestMatchers(
                                HttpMethod.GET,
                                "/health",
                                "/uploads/**",
                                "/oauth/**",
                                "/note/shared/note/*",
                                "/note/shared/article/*/*"
                        ).permitAll()
                        // admin 的非公开路由，都要求 authentication 的 authorities 中有 TOKEN_TYPE_ADMIN
                        .requestMatchers("/admin/**").hasAuthority("TOKEN_TYPE_ADMIN")
                        .anyRequest().authenticated()
                )

                .oauth2ResourceServer(oauth2 -> oauth2
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(converter))
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration =
                new CorsConfiguration();

        configuration.setAllowedOrigins(
                List.of(corsAllowedOrigins.split(","))
        );

        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "PATCH",
                        "DELETE",
                        "OPTIONS"
                )
        );

        // 允许前端发送的请求头
        configuration.setAllowedHeaders(
                List.of(
                        "Authorization",
                        "Content-Type",
                        "X-Request-Id",
                        "X-Trace-Id"
                )
        );

        // 允许浏览器前端读取响应中的排查 ID，然后就可以交给后端，让后端排查 bug
        configuration.setExposedHeaders(
                List.of(
                        "X-Request-Id",
                        "X-Trace-Id",
                        "Content-Disposition"
                )
        );

        // 允许跨域请求携带 cookie
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<GrantedAuthority> authorities = new ArrayList<>();

            // tokenType
            if ("ADMIN".equals(jwt.getClaimAsString("tokenType"))) {
                authorities.add(new SimpleGrantedAuthority("TOKEN_TYPE_ADMIN"));
            }

            // roles -> ROLE_XXX
            List<String> roles = jwt.getClaimAsStringList("roles");
            if (roles != null) {
                roles.forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role)));
            }

            // permissions
            List<String> permissions = jwt.getClaimAsStringList("permissions");
            if (permissions != null) {
                permissions.forEach(permission -> authorities.add(new SimpleGrantedAuthority(permission)));
            }

            return authorities;
        });

        return converter;
    }
}
