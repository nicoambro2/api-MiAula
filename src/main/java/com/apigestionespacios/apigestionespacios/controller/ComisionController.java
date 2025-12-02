package com.apigestionespacios.apigestionespacios.controller;

import com.apigestionespacios.apigestionespacios.dtos.comision.ComisionCreateDTO;
import com.apigestionespacios.apigestionespacios.dtos.comision.ComisionResponseDTO;
import com.apigestionespacios.apigestionespacios.dtos.comision.ComisionUpdateDTO;
import com.apigestionespacios.apigestionespacios.dtos.cronograma.CronogramaComisionDTO;
import com.apigestionespacios.apigestionespacios.dtos.usuario.UsuarioResponseDTO;
import com.apigestionespacios.apigestionespacios.entities.Comision;
import com.apigestionespacios.apigestionespacios.entities.Usuario;
import com.apigestionespacios.apigestionespacios.service.ComisionService;
import com.apigestionespacios.apigestionespacios.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comisiones")
@Tag(name = "Comisiones", description = "Operaciones relacionadas con las comisiones.")
public class ComisionController {

    private final ComisionService comisionService;
    private final UsuarioService usuarioService;

    @Autowired
    public ComisionController(ComisionService comisionService, UsuarioService usuarioService) {
        this.comisionService = comisionService;
        this.usuarioService = usuarioService;
    }

    /**
     * Obtiene todas las comisiones.
     * Solo accesible por administradores.
     *
     * @return Lista de todas las comisiones
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Obtener comisiones.",
            description = "Permite a administradores obtener un listado de todas las comisiones.")
    public ResponseEntity<List<ComisionResponseDTO>> obtenerComisiones() {
        return new ResponseEntity<>(comisionService.obtenerTodasComisionesDTO(), HttpStatus.OK);
    }

    /**
     * Obtiene una comisión por su ID.
     * Si la comisión no existe, lanza una ResourceNotFoundException.
     * Solo accesible por administradores.
     *
     * @param id ID de la comisión a buscar
     * @return Comisión encontrada
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Obtener comision por id.",
            description = "Permite a administradores obtener una comisión a través de su id.")
    public ResponseEntity<CronogramaComisionDTO> obtenerComisonPorId(
            @Parameter(description = "ID de la comisión a buscar", example = "1")
            @PathVariable Long id) {
        return new ResponseEntity<>(comisionService.obtenerComisionDTOPorId(id), HttpStatus.OK);
    }

    /**
     * Crea una nueva comisión.
     * Valida que el DTO de creación sea válido.
     * Solo accesible por administradores y profesores.
     *
     * @param comisionCreateDTO DTO de creación de comisión
     * @return Comisión creada
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR')")
    @Operation(
            summary = "Crear nueva comisión.",
            description = "Permite a administradores y profesores crear una nueva comisión.")
    public ResponseEntity<Comision> crearComision(
            @Parameter(description = "DTO de creación de comisión", required = true)
            @Valid @RequestBody ComisionCreateDTO comisionCreateDTO) {
        return new ResponseEntity<>(comisionService.crearComisionDesdeDTO(comisionCreateDTO), HttpStatus.CREATED);
    }

    /**
     * Actualiza una comisión existente.
     * Valida que el ID de la comisión y el DTO de actualización sean válidos.
     * Solo accesible por administradores y profesores.
     *
     * @param comisionUpdateDTO DTO de actualización de comisión
     * @return Comisión actualizada
     */
    @PutMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR')")
    @Operation(
            summary = "Actualizar comision.",
            description = "Permite a administradores y profesores actualizar una comisión.")
    public ResponseEntity<Comision> actualizarComision(
            @Parameter(description = "DTO de actualización de comisión", required = true)
            @Valid @RequestBody ComisionUpdateDTO comisionUpdateDTO) {
        return new ResponseEntity<>(comisionService.actualizarComision(comisionUpdateDTO), HttpStatus.OK);
    }

     /**
     * Elimina una comisión por su ID.
     * Si la comisión no existe, lanza una ResourceNotFoundException
     * Solo accesible por administradores.
     *
     * @param id ID de la comisión a eliminar
     * @return Respuesta vacía con estado NO_CONTENT
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Actualizar comision.",
            description = "Permite a administradores y profesores actualizar una comisión.")
    public ResponseEntity<Void> eliminarComision(
            @Parameter(description = "ID de la comisión a eliminar", example = "1")
            @PathVariable Long id) {
        comisionService.eliminarComision(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * Obtiene las comisiones asociadas a una asignatura específica.
     * Solo accesible por administradores.
     *
     * @param asignaturaId ID de la asignatura
     * @return Lista de comisiones asociadas a la asignatura
     */
    @GetMapping("/por-asignatura/{asignaturaId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Obtener comisiones asociadas a asignatura.",
            description = "Permite a administradores obtener un listado de las comisiones de una asignatura.")
    public ResponseEntity<List<ComisionResponseDTO>> obtenerComisionesPorAsignatura(
            @Parameter(description = "ID de la asignatura", example = "1")
            @PathVariable Long asignaturaId) {
        List<ComisionResponseDTO> comisiones = comisionService.obtenerComisionesPorAsignatura(asignaturaId);

        if (comisiones.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(comisiones);
    }

    /**
     * Obtiene las comisiones asociadas al profesor autenticado.
     * Solo accesible por profesores.
     *
     * @param authentication Autenticación del usuario
     * @return Lista de comisiones asociadas al profesor autenticado
     */
    @GetMapping("/mis-comisiones")
    @PreAuthorize("hasRole('PROFESOR')")
    @Operation(
            summary = "Obtener comisiones asociadas al profesor autenticado.",
            description = "Permite al profesor logueado obtener un listado de las comisiones a su cargo.")
    public ResponseEntity<List<ComisionResponseDTO>> obtenerMisComisiones(Authentication authentication) {
        String username = authentication.getName();
        UsuarioResponseDTO profesor = usuarioService.obtenerPorUsername(username); // extraemos el ID del profesor autenticado

        List<ComisionResponseDTO> comisiones = comisionService.obtenerComisionesPorProfesor(profesor.getId());

        if (comisiones.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(comisiones);
    }

}
