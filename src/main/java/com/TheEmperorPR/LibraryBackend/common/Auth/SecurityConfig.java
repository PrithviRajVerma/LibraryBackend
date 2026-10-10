package com.TheEmperorPR.LibraryBackend.common.Auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@EnableWebSecurity
@EnableMethodSecurity
@Configuration
public class SecurityConfig {

    private final JWTAuthorizationFilter jwtFilter;

    public SecurityConfig(JWTAuthorizationFilter jwtFilter) {
        System.out.println("✅ JWTAuthorizationFilter initialized");
        this.jwtFilter = jwtFilter;
    }

    @Bean public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
   public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(req -> req
                .requestMatchers("/auth/**", "/api/v1/auth/**").permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/books/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/books/**").hasAnyRole("STAFF", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/books/**").hasAnyRole("STAFF", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/books/**").hasRole("ADMIN")
                .requestMatchers("/admin/**", "/api/v1/admin/**").hasRole("ADMIN")
                .requestMatchers("/staff/**", "/api/v1/staff/**").hasAnyRole("STAFF","ADMIN")
                .requestMatchers("/student/**", "/api/v1/student/**").hasRole("STUDENT")
                .anyRequest().authenticated()
                )
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
   }

}
