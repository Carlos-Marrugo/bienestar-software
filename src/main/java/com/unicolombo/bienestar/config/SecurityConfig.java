package com.unicolombo.bienestar.config;

import com.unicolombo.bienestar.filters.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/actividades").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/actividades/instructor/*").hasAnyRole("INSTRUCTOR", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/actividades").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/actividades/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/actividades/*").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/inscripciones").hasRole("ESTUDIANTE")
                        .requestMatchers(HttpMethod.GET, "/api/inscripciones/estudiante").hasRole("ESTUDIANTE")

                        .requestMatchers(HttpMethod.GET, "/api/auditoria/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.PUT, "/api/admin/instructores/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/admin/mi-perfil").hasRole("INSTRUCTOR")
                        .requestMatchers(HttpMethod.PUT, "/api/admin/mi-perfil").hasRole("INSTRUCTOR")
                        .requestMatchers(HttpMethod.GET, "/api/instructor/mis-actividades/*/estudiantes").hasRole("INSTRUCTOR")
                        .requestMatchers(HttpMethod.GET, "/api/admin/mis-actividades/*/estudiantes").hasRole("INSTRUCTOR")
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173",
                "https://bienestar-front.onrender.com"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}