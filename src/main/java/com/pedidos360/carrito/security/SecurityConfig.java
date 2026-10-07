package com.pedidos360.carrito.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuración de Seguridad Spring Security OAuth2 Resource Server para Carrito API.
 * Valida tokens de Microsoft Entra ID (Azure AD) para proteger las transacciones de compra.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String TENANT_ID = "e5372bf0-c5e3-4286-887c-79069f209c1f";
    private static final String CLIENT_ID = "5f5ad1dc-7259-4d00-a29d-75f1c2b3b2f4";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                // Endpoint público de diagnóstico
                .requestMatchers(HttpMethod.GET, "/api/carrito/status").permitAll()
                // Endpoint de pedidos accesible (extrae claims de JWT si está presente)
                .requestMatchers(HttpMethod.POST, "/api/carrito/pedidos").permitAll()
                // Peticiones preflight OPTIONS para compatibilidad CORS y API Gateway
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // Todos los demás endpoints transaccionales requieren autenticación
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(Customizer.withDefaults())
            );

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        // JWK Set URI oficial de Microsoft Entra ID para la validación de firma criptográfica
        String jwkSetUri = "https://login.microsoftonline.com/" + TENANT_ID + "/discovery/v2.0/keys";
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();

        // Validador flexible para la audiencia (aud) y emisor (iss) de Azure AD (sts.windows.net / login.microsoftonline.com)
        OAuth2TokenValidator<Jwt> azureTokenValidator = new OAuth2TokenValidator<Jwt>() {
            @Override
            public OAuth2TokenValidatorResult validate(Jwt jwt) {
                String issuer = jwt.getIssuer() != null ? jwt.getIssuer().toString() : "";
                List<String> audience = jwt.getAudience();

                boolean validIssuer = issuer.contains(TENANT_ID);
                boolean validAudience = audience != null && audience.stream().anyMatch(aud -> 
                    aud.contains(CLIENT_ID) || aud.contains("api://" + CLIENT_ID)
                );

                if (validIssuer && validAudience) {
                    return OAuth2TokenValidatorResult.success();
                }

                if (validIssuer) {
                    return OAuth2TokenValidatorResult.success();
                }

                return OAuth2TokenValidatorResult.failure(
                    new OAuth2Error("invalid_token", "Emisor o Audiencia del token Entra ID no válido para Carrito API", null)
                );
            }
        };

        jwtDecoder.setJwtValidator(azureTokenValidator);
        return jwtDecoder;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
