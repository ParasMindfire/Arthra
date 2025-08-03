package com.paras.Arthra.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.paras.Arthra.util.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;


@Component
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter{
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
                final String authHeader = request.getHeader("Authorization");
                String email=null;
                String jwt=null;

                if(authHeader!=null && authHeader.startsWith("Bearer ")){
                    jwt=authHeader.substring(7);
                    email=jwtUtil.extractUsername(jwt);
                }

                if(email!=null && SecurityContextHolder.getContext().getAuthentication()==null){
                    UserDetails userDetails = this.userDetailsService.loadUserByUsername(email);
                    if(jwtUtil.validateToken(jwt, userDetails)){
                        UsernamePasswordAuthenticationToken authToken=new UsernamePasswordAuthenticationToken(userDetails,null,userDetails.getAuthorities());

                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }

                filterChain.doFilter(request, response);
    }


    /*
        Step-by-step Spring Security JWT-based Authorization Flow:

        1. Application Startup:
        - Spring Boot configures the SecurityFilterChain exactly once at startup.
        - Your security configuration declares which endpoints are public ("/register", "/login", etc.) and which require authentication (`anyRequest().authenticated()`).
        - CSRF is disabled (not needed for stateless APIs/JWT usage).
        - CORS (cross-origin resource sharing) is enabled.
        - Session management is set to STATELESS—no sessions are created or stored by Spring.

        2. How Filters Work:
        - You add your custom JwtRequestFilter BEFORE the UsernamePasswordAuthenticationFilter in the filter chain.
        - This means on each HTTP request, your JwtRequestFilter:
            a) Runs BEFORE Spring Security checks if the user is authenticated for protected endpoints.
            b) Can populate the SecurityContext for further processing down the chain.

        3. On Every Incoming HTTP Request:
        - JwtRequestFilter.doFilterInternal() is triggered.
        - It checks the "Authorization" HTTP header:
            a) If present AND starts with "Bearer ", extracts the JWT token substring.
            b) Calls jwtUtil.extractUsername(jwt) to extract the user's email from the JWT.

        4. Checking Existing Authentication:
        - If the extracted email IS NOT null AND SecurityContextHolder.getContext().getAuthentication() == null
            (i.e., this request is not already authenticated by another filter):
            a) Uses your UserDetailsService to load the user object for the extracted email (principal).
            b) Calls jwtUtil.validateToken(jwt, userDetails) to verify the JWT is valid for this user (checks signature, expiration, etc.).

        5. Populating SecurityContext:
        - If the token is valid:
            a) Creates a UsernamePasswordAuthenticationToken with the userDetails as principal, null for credentials (not needed), user roles as authorities.
            b) Populates web authentication details (IP address, etc.) for audit/compliance.
            c) Sets this Authentication object in SecurityContextHolder.getContext().setAuthentication(authToken).
            d) Now, the rest of Spring Security (downstream filters, controller methods, security annotations) sees the request as authenticated and authorized as per roles.

        6. Authorization Enforcement:
        - Now, as the request proceeds:
            a) If the path is PUBLIC ("/login", "/register", etc.), it is always allowed, no authentication needed.
            b) If the path is PROTECTED (any other endpoint), Spring Security checks SecurityContext for authentication. Your filter already set the Authentication object, so:
                • User identity is known (from JWT).
                • Roles/authorities are available for @PreAuthorize, @Secured, etc. to enforce method/endpoint-level access control. 
                • You can access the authorized user via SecurityContextHolder.getContext().getAuthentication() in your services/controllers.

        7. Final Step:
        - Request processing continues with filterChain.doFilter(request, response).
        - If the JWT is invalid/expired/not present, and endpoint is protected, access is denied.

        // Key Concepts:
        // - Your filter is responsible for extracting and validating the JWT,
        //   loading the user, and putting authentication into SecurityContext.
        // - Authorization is handled automatically by Spring Security based
        //   on roles/authorities present in SecurityContext after your filter runs.
        // - No session state is kept on the server: the JWT acts as a portable security token.

        // This process ensures every request to a protected endpoint is authenticated by stateless JWT, 
        // and user's authorization is enforced based on roles in their token and user account.
    */


    
}
