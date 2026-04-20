package com.carebridge.security;

import com.carebridge.security.TokenSecurity;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class JwtFilter extends OncePerRequestFilter {

    private final TokenSecurity tokenSecurity = new TokenSecurity();
    private final String secret = "A_VERY_LONG_SECRET_KEY_FOR_TESTING_PURPOSES_ONLY_123456";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                if (tokenSecurity.tokenIsValid(token, secret) && tokenSecurity.tokenNotExpired(token)) {
                    Map<String, Object> userMap = tokenSecurity.getUserWithRolesFromToken(token);
                    String username = (String) userMap.get("username");
                    Set<String> roles = (Set<String>) userMap.get("roles");

                    var authorities = roles.stream()
                            .map(r -> new SimpleGrantedAuthority("ROLE_" + r.toUpperCase()))
                            .collect(Collectors.toList());

                    var auth = new UsernamePasswordAuthenticationToken(username, null, authorities);
                    auth.setDetails(userMap); 
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    request.setAttribute("user", userMap);
                }
            } catch (Exception e) {
                // Invalid token, just continue
            }
        }

        filterChain.doFilter(request, response);
    }
}
