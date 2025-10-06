package com.trainhub.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // jwt Bearer 
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // For REST APIs, ignore CSRF for API routes
            // cross-site request forgery
            // ataque er el que elo envio de un formulario web es interceptado
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/users/register"))

            // desactivar completamente cuando use JWT
            // .csrf(csrf -> csrf.disable())

            // permiter /register y /login sin token
            // proteger el restop con JWTAuthenticationFilter
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/users/register").permitAll() // open access
                .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults()); // BasicAuthenticationFilter

            // EN PRODUCCION USARÉ:
//             .httpBasic(AbstractHttpConfigurer::disable)
// .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user = User.withUsername("admin")
            .password(passwordEncoder().encode("admin123"))
            .roles("USER")
            .build();
        
        return new InMemoryUserDetailsManager(user);
    }
}

