package com.apigestionespacios.apigestionespacios.config;

import com.apigestionespacios.apigestionespacios.security.jwt.JwtAuthFilter;
import com.apigestionespacios.apigestionespacios.service.UsuarioService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;


import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final String[] rutasSinRestriccion = {"http://localhost:8080/swagger-ui.html","/auth/login", "/reservas/cronograma-por-dia", "/reservas/cronograma", "/usuarios"};
    private final String[] rutasSinRestriccionPOST = {"/usuarios"};

    // Bean para codificar y verificar contraseñas con BCrypt

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }



    // Proveedor de autenticación que conecta al servicio de usuarios y al codificador
    @Bean
    public AuthenticationProvider authenticationProvider(UsuarioService userDetailsService) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // AuthenticationManager usando directamente el AuthenticationProvider
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationProvider authenticationProvider) {
        return new ProviderManager(authenticationProvider);
    }

    // Configuración del filtro de seguridad para proteger rutas y validar JWT

    @Bean
    public SecurityFilterChain securityFilterChain2(HttpSecurity http,
                                                    JwtAuthFilter jwtAuthFilter,
                                                    AuthenticationProvider authenticationProvider
                                                    ) throws Exception {
        return http
                //.cors(cors -> cors.configurationSource(corsConfigurationSource()))  //Habilitar CORS si es necesario
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(rutasSinRestriccion).permitAll()
                        .requestMatchers(HttpMethod.POST, rutasSinRestriccionPOST).permitAll()
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /*
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    */

}