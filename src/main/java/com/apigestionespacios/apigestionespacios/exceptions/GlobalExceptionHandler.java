package com.apigestionespacios.apigestionespacios.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.apigestionespacios.apigestionespacios.exceptions.UsuarioInactivoException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Lanzado cuando las credenciales de logueo son incorrectas
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> manejarBadCredentials(BadCredentialsException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("message", "Contraseña incorrecta");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Map<String, String>> manejarUserNotFound(UsernameNotFoundException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("message", "No existe un usuario registrado con este Email");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    // lanzado cuando no se encuentra un recurso al que se quiere acceder
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> manejarNotFoundException(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    // lanzado cuando la solicitud no cumple con las reglas de negocio o
    // validaciones logicas
    @ExceptionHandler(EntityValidationException.class)
    public ResponseEntity<String> manejarEntityValidationException(EntityValidationException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ex.getMessage());
    }

    // lanzado cuando se intenta crear un recurso que ya existe (ejemplo: crear una
    // asignatura con un código que ya existe)
    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<String> manejarResourceConflictException(ResourceConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(ReservaSolapadaException.class)
    public ResponseEntity<String> manejarReservaSolapada(ReservaSolapadaException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    // Lanzado cuando se intenta acceder a un usuario eliminado
    @ExceptionHandler(UsuarioInactivoException.class)
    public ResponseEntity<Map<String, String>> manejarUserInactive(UsuarioInactivoException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("message", "Este usuario ha sido eliminado. Contáctese con la administración");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    // lanzado para excepciones genericas
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> manejarGenericException(Exception ex) {
        return new ResponseEntity<>("Error inesperado: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
