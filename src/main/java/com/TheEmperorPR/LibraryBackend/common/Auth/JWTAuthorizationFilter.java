package com.TheEmperorPR.LibraryBackend.common.Auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;


public class JWTAuthorizationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JWTAuthorizationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException , IOException{

        String path = request.getRequestURI();
        System.out.println("JWT Filter triggered on: " + request.getRequestURI());

        if (path.startsWith("/auth")) {
            System.out.println("✅ Skipping JWT check for /auth path.");
            chain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");
        System.out.println("Authorization Header: " + request.getHeader("Authorization"));

        if(header == null || !header.startsWith("Bearer ")){
            System.out.println("⛔ No valid Authorization header. Skipping filter.");
            chain.doFilter(request,response);
            return;
        }

        String token = header.substring(7);
        String username = jwtUtil.extractUsername(token);
        String role = jwtUtil.extractRole(token);

        System.out.println("🔐 Extracted username: " + username);
        System.out.println("🔐 Extracted role: " + role);

        if(username != null && SecurityContextHolder.getContext().getAuthentication() == null){
            List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_"+ role.toUpperCase()));
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(username,null,authorities);

            SecurityContextHolder.getContext().setAuthentication(auth);
            System.out.println("✅ SecurityContext set for: " + username);
        }

        chain.doFilter(request,response);
    }

}
