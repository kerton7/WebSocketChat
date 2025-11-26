package com.alituran.jwt;


import com.alituran.model.User;
import com.alituran.repository.AuthRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    private final UserDetailsService userDetailsService;

    private final AuthRepository authRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // OAuth2 callback endpoint'lerini atla
        String requestPath = request.getRequestURI();
        if (requestPath != null && (requestPath.startsWith("/login/oauth2/") || requestPath.startsWith("/oauth2/"))) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = header.substring(7);

        // Token parse edilemezse (geçersiz format) filter'ı atla
        String usernameFromToken;
        try {
            usernameFromToken = jwtService.getUsernameFromToken(token);
        } catch (Exception e) {
            filterChain.doFilter(request, response);
            return;
        }

        String role = jwtService.getRoleFromToken(token);
        if(usernameFromToken==null || SecurityContextHolder.getContext().getAuthentication()!=null) {
            filterChain.doFilter(request,response);
            return;
        }
        User user = authRepository.findByUsername(usernameFromToken).orElse(null);

        if (user == null || user.isBanned() || !token.equals(user.getCurrentToken()) || !jwtService.validateToken(token)) {
            filterChain.doFilter(request, response);
            return;
        }


        UserDetails userDetails = userDetailsService.loadUserByUsername(usernameFromToken);

        if(userDetails==null || !jwtService.validateToken(token)) {
            filterChain.doFilter(request,response);
            return;
        }
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(usernameFromToken, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        filterChain.doFilter(request,response);

    }
}
