package com.paras.Arthra.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.paras.Arthra.security.JwtRequestFilter;
import com.paras.Arthra.service.AppUserDetailService;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final AppUserDetailService appUserDetailsService;
    private final JwtRequestFilter jwtRequestFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception{
        httpSecurity.cors(Customizer.withDefaults()) // Enable CORS
                    .csrf(AbstractHttpConfigurer::disable) // Disable CSRF (not needed for JWT)
                    .authorizeHttpRequests(auth->auth.requestMatchers("/status","/health","/activate","/register","/login").permitAll() // Public endpoints
                    .anyRequest().authenticated()) // All other endpoints require authentication
                    .sessionManagement(session->session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // No sessions
                    .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class); 
                    /*Without the .addFilterBefore() call:
                        1)Your JwtRequestFilter exists as a Spring @Component
                        2)But it's NOT part of the Spring Security filter chain
                        3)Spring Security doesn't know about your JWT filter
                        4)Only default Spring Security filters run

                        This line is executed only ONCE during application startup when Spring Security builds the filter chain. It's not called for each request.

                        Using UsernamePasswordAuthenticationFilter.class is a convention, not a requirement. You can use other filter classes as reference points . The key is ensuring your JWT filter runs before the authorization check so it can set authentication in the SecurityContext. As long as that's satisfied, the exact position doesn't matter much functionally!
                    */
        return httpSecurity.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(){
        CorsConfiguration configuration =new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization","Content-Type","Accept"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source=new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); //Maps your CORS configuration to all URLs in your application.

        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(){
        DaoAuthenticationProvider authenticationProvider=new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(appUserDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(authenticationProvider);
    }

}
