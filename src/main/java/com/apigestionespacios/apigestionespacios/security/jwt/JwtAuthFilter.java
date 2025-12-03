package com.apigestionespacios.apigestionespacios.security.jwt;

import com.apigestionespacios.apigestionespacios.service.UsuarioService;

import io.micrometer.common.lang.NonNull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@AllArgsConstructor
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        try {

            // Obtenemos el header "Authorization" del request
            final String authHeader = request.getHeader("Authorization");

            // Verificamos si el header es nulo o no empieza con "Bearer "
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return; // No hay token, dejamos que el resto de la cadena siga
            }

            // Extraemos el token sin el prefijo "Bearer "
            String token = authHeader.substring(7);

            // Obtenemos el nombre de usuario desde el token
            String username = jwtService.extractUsername(token);

            // Si obtenemos un username y no hay autenticación en contexto, validamos el
            // token
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Cargamos los datos del usuario desde base de datos
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // Verificamos si el token es válido
                if (jwtService.isTokenValid(token, userDetails)) {

                    // Creamos el token de autenticación de Spring
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities());

                    // Cargamos información adicional del request
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));

                    // Establecemos el usuario autenticado en el contexto de Spring
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }

            // Continuamos la cadena de filtros
            filterChain.doFilter(request, response);
        } catch (io.jsonwebtoken.JwtException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token JWT invalido o expirado.");
        }

    }

}
