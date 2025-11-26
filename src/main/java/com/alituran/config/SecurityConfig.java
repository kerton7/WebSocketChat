package com.alituran.config;

import com.alituran.jwt.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    public static final String REGISTER ="/auth/register";

    public static final String AUTHENTICATE ="/auth/authenticate";

    public static final String OAUTH2LOGIN="/auth/oauth2/**";

    public static final String WEBSOCKET = "/ws/**";

    private final AuthenticationProvider authenticationProvider;

    private final JwtAuthenticationFilter  jwtAuthenticationFilter;

    private final OAuth2SuccessHandler oAuth2SuccessHandler;



    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf->csrf.disable())
                .authorizeHttpRequests(
                        request->request.
                                requestMatchers(REGISTER,AUTHENTICATE,OAUTH2LOGIN,WEBSOCKET,"/index.html","/chat.html","/auth/verify/**"
                                ,"/verify/**","/admin.html","/login/oauth2/**","/oauth2/**")
                                .permitAll().requestMatchers("/auth/admin/**").hasRole("ADMIN")
                                .anyRequest().authenticated())
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oAuth2SuccessHandler))
                .authenticationProvider(authenticationProvider)
                .sessionManagement(session->
                        session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .addFilterBefore(jwtAuthenticationFilter,UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }


}
