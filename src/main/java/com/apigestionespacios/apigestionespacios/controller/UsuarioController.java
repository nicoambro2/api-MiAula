package com.apigestionespacios.apigestionespacios.controller;

import com.apigestionespacios.apigestionespacios.dtos.usuario.UsuarioCreateDTO;
import com.apigestionespacios.apigestionespacios.dtos.usuario.UsuarioResponseDTO;
import com.apigestionespacios.apigestionespacios.dtos.usuario.UsuarioUpdateDTO;
import com.apigestionespacios.apigestionespacios.entities.Usuario;
import com.apigestionespacios.apigestionespacios.entities.enums.Rol;
import com.apigestionespacios.apigestionespacios.service.UsuarioService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/usuarios")
@Tag(name = "Usuarios", description = "Operaciones relacionadas con los usuarios.")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @Autowired
    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Operation(
            summary = "Listar usuarios",
            description = "Obtiene la lista de todos los usuarios registrados en el sistema.")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UsuarioResponseDTO>> listarUsuarios() {
        List<UsuarioResponseDTO> usuarios = usuarioService.obtenerTodosDTO();
        return new ResponseEntity<>(usuarios, HttpStatus.OK);
    }

    @Operation(
            summary = "Listar usuarios por rol",
            description = "Obtiene la lista de usuarios filtrados por rol.")
    @GetMapping("/rol")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UsuarioResponseDTO>> listarUsuariosPorRol(
            @Parameter(description = "Rol de los usuarios a filtrar", required = true)
            @RequestParam Rol rol) {
        List<UsuarioResponseDTO> usuarios = usuarioService.obtenerPorRolDTO(rol);
        return new ResponseEntity<>(usuarios, HttpStatus.OK);
    }

    @Operation(summary =
            "Obtener usuario por ID", description =
            "Obtiene los detalles de un usuario específico según su ID. Solo accesible para administradores")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponseDTO> obtenerUsuario(
            @Parameter(description = "ID del usuario a obtener", required = true)
            @PathVariable Long id) {
        return new ResponseEntity<>(usuarioService.obtenerPorIdDTO(id), HttpStatus.OK);
    }

    @Operation(
            summary = "Crear usuario",
            description = "Crea un nuevo usuario con los datos proporcionados en el cuerpo de la petición.")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Usuario> crearUsuario(
            @Parameter(description = "Datos del usuario a crear", required = true)
            @Valid @RequestBody UsuarioCreateDTO usuario) {
        return new ResponseEntity<>(usuarioService.crearUsuario(usuario), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Eliminar usuario",
            description = "Elimina un usuario específico según su ID.Solo accesible para administradores.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> eliminarUsuario(
            @Parameter(description = "ID del usuario a eliminar", required = true)
            @PathVariable Long id) {
        usuarioService.eliminar(id);

        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Usuario eliminado correctamente");

        return new ResponseEntity<>(respuesta, HttpStatus.OK);
    }

    @Operation(
            summary = "Obtener usuario por email",
            description = "Obtiene los detalles de un usuario específico según su email. Solo accesible para administradores")
    @GetMapping("/email/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponseDTO> obtenerUsuarioPorEmail(
            @Parameter(description = "Email del usuario a obtener", required = true)
            @PathVariable String email) {
        return new ResponseEntity<>(usuarioService.obtenerPorUsernameDTO(email), HttpStatus.OK);
    }

    @Operation(
            summary = "Obtener usuario logueado",
            description = "Obtiene los detalles del usuario actualmente autenticado.")
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR')")
    public ResponseEntity<UsuarioResponseDTO> obtenerUsuarioLogueado(Authentication authentication) {
        String email = authentication.getName();
        return new ResponseEntity<>(usuarioService.obtenerPorUsername(email), HttpStatus.OK);
    }

    @Operation(
            summary = "Actualizar usuario",
            description = "Actualiza los datos del un usuario logueado.")
    @PatchMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR')")
    public ResponseEntity<UsuarioResponseDTO> actualizarUsuario(
            Authentication authentication,
            @Parameter(description = "Datos del usuario a actualizar", required = true)
            @Valid @RequestBody UsuarioUpdateDTO usuario) {

            Usuario usuarioLogueado = (Usuario) authentication.getPrincipal();
            Long usuarioId = usuarioLogueado.getId();

        return new ResponseEntity<>(usuarioService.actualizarUsuario(usuarioId, usuario), HttpStatus.OK);
    }
/* 
    @Operation(
        summary = "Actualizar informacion personal del usuario",
        description = "Actualiza nombre, apellido y email del usuario")
        @PatchMapping
        @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR')")
        public ResponseEntity<UsuarioResponseDTO> actualizarInfoPersonal(
                Authentication authentication,
                @Parameter(description = )
        )
*/
    @Operation(
            summary = "Actualizar rol de usuario por ID",
            description = "Actualiza el rol de un usuario específico según su ID. Solo accesible para administradores.")
    @PutMapping("/{id}/rol")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponseDTO> actualizarRolUsuario(
            @Parameter(description = "ID del usuario a actualizar", required = true)
            @PathVariable Long id,
            @Parameter(description = "Nuevo rol del usuario", required = true)
            @RequestParam String rol) {
        return new ResponseEntity<>(usuarioService.modificarRolUsuario(id, rol), HttpStatus.OK);
    }
}