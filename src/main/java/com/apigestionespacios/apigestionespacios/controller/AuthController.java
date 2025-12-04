package com.apigestionespacios.apigestionespacios.controller;

import com.apigestionespacios.apigestionespacios.security.jwt.dto.LoginRequest;
import com.apigestionespacios.apigestionespacios.security.jwt.dto.LoginResponse;
import com.apigestionespacios.apigestionespacios.dtos.password.PasswordValidacionDto;
import com.apigestionespacios.apigestionespacios.entities.Usuario;
import com.apigestionespacios.apigestionespacios.exceptions.UsuarioInactivoException;
import com.apigestionespacios.apigestionespacios.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            // primero verifica que el usuario existe
            UserDetails user;
            try {
                user = userDetailsService.loadUserByUsername(request.getUsername());
                if (user instanceof Usuario) {
                    if (!((Usuario) user).getActivo()) {
                        throw new UsuarioInactivoException("Este usuario ha sido eliminado del sistema");
                    }
                }
            } catch (UsernameNotFoundException e) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "No existe un usuario registrado con este Email");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            // autentica al usuario con nombre y contraseña
            validarPassword(request.getUsername(), request.getPassword());

            /* 
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()));
            */
            // Generamos el token JWT
            String token = jwtService.generateToken(user);

            // Devolvemos el token en la respuesta
            return ResponseEntity.ok(new LoginResponse(token));

        } catch (BadCredentialsException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Contraseña incorrecta");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Error en el proceso de autenticación: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    public void validarPassword(String email, String password) throws BadCredentialsException {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        email,
                        password));
    }

    @PostMapping("/validarPassword")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR')")
    public ResponseEntity<?> validarPasswordEndpoint(@RequestBody PasswordValidacionDto request, Authentication auth) {
        try {
            validarPassword(auth.getName(), request.getPassword());
            return ResponseEntity.ok().build();
            
        } catch (BadCredentialsException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Contraseña incorrecta");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }
}
