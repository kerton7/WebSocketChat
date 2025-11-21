package com.alituran.config;

import com.alituran.jwt.JwtAuthenticationFilter;
import com.alituran.model.User;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    public static final String REGISTER ="/auth/register";

    public static final String AUTHENTICATE ="/auth/authenticate";

    public static final String OAUTH2LOGIN="/auth/oauth2/**";

    public static final String WEBSOCKET = "/ws/**";

    private final AuthenticationProvider authenticationProvider;

    private final JwtAuthenticationFilter  jwtAuthenticationFilter;



    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf->csrf.disable()).authorizeHttpRequests(
                        request->request.
                                requestMatchers(REGISTER,AUTHENTICATE,OAUTH2LOGIN,WEBSOCKET,"/index.html","/chat.html","/auth/verify/**"
                                ,"/verify/**","/admin.html")
                                .permitAll().requestMatchers("/auth/admin/**").hasRole("ADMIN")
                                .anyRequest().authenticated()).authenticationProvider(authenticationProvider).
                sessionManagement(session->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)).
                addFilterBefore(jwtAuthenticationFilter,UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }


}
