package com.apigestionespacios.apigestionespacios.controller;

import com.apigestionespacios.apigestionespacios.dtos.asignatura.AsignaturaResponseDTO;
import com.apigestionespacios.apigestionespacios.dtos.carrera.CarreraCreateDTO;
import com.apigestionespacios.apigestionespacios.dtos.carrera.CarreraResponseDTO;
import com.apigestionespacios.apigestionespacios.dtos.carrera.CarreraUpdateDTO;
import com.apigestionespacios.apigestionespacios.entities.Carrera;
import com.apigestionespacios.apigestionespacios.service.CarreraService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/carreras")
@Tag(name = "Carreras", description = "Operaciones relacionadas con las carreras.")
public class CarreraController {
    private final CarreraService carreraService;

    @Autowired
    public CarreraController(CarreraService carreraService) {
        this.carreraService = carreraService;
    }

    /**
     * Endpoint para listar carreras.
     * Permite filtrar por ID o nombre.
     * Los administradores y profesores pueden acceder a esta información.
     *
     * @param id     ID de la carrera (opcional).
     * @param nombre Nombre de la carrera (opcional).
     * @return Lista de carreras filtradas o todas las carreras si no se especifica filtro.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR')")
    @Operation(
            summary = "Filtrar carreras por ID o nombre.",
            description = "Permite a administradores y profesores filtrar entre las distintas carreras. En caso de no proporcionar parámetros devolverá el listado total.")
    public ResponseEntity<List<CarreraResponseDTO>> obtenerCarreras(
            @Parameter(description = "ID de la carrera a buscar", example = "1", required = false)
            @RequestParam(required = false) Long id,
            @Parameter(description = "Nombre de la carrera a buscar", example = "Ingeniería Informática", required = false)
            @RequestParam(required = false) String nombre
    )
    {
        if (id != null && nombre != null) {
            return new ResponseEntity<>(List.of(carreraService.obtenerDTOPorId(id)), HttpStatus.OK);
        } else if (id != null) {
            return new ResponseEntity<>(List.of(carreraService.obtenerDTOPorId(id)), HttpStatus.OK);
        } else if (nombre != null) {
            return new ResponseEntity<>(List.of(carreraService.obtenerPorNombre(nombre)), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(carreraService.obtenerTodas(), HttpStatus.OK);
        }
    }

    /**
     * Endpoint para obtener las asignaturas de una carrera específica.
     * Permite a administradores y profesores acceder a las asignaturas de una carrera.
     *
     * @param carreraId ID de la carrera.
     * @return Lista de asignaturas asociadas a la carrera.
     */
    @GetMapping("/{carreraId}/asignaturas")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESOR')")
    @Operation(
            summary = "Obtener asignaturas de una carrera.",
            description = "Permite a administradores y profesores obtener un listado de las asignaturas de una carrera específica.")
    public ResponseEntity<List<AsignaturaResponseDTO>> obtenerAsignaturasDeCarrera(
            @Parameter(description = "ID de la carrera a buscar", example = "1")
            @PathVariable Long carreraId) {
        return new ResponseEntity<>(carreraService.obtenerAsignaturasDeCarrera(carreraId), HttpStatus.OK);
    }

    /**
     * Endpoint para crear una nueva carrera.
     * Solo accesible por administradores.
     *
     * @param carrera Objeto DTO con los datos de la carrera a crear.
     * @return Carrera creada con su ID asignado.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Crear nueva carrera.",
            description = "Permite a administradores crear una nueva carrera.")
    public ResponseEntity<Carrera> crearCarrera(
            @Parameter(description = "DTO de creación de carrera", required = true)
            @Valid @RequestBody CarreraCreateDTO carrera) {
        return new  ResponseEntity<>(carreraService.crearCarrera(carrera), HttpStatus.CREATED);
    }

    /**
     * Endpoint para asignar asignaturas a una carrera.
     * Solo accesible por administradores.
     *
     * @param carreraId ID de la carrera a la que se le asignarán las asignaturas.
     * @param asignaturaIds Lista de IDs de las asignaturas a asignar.
     * @return Respuesta vacía con código 204 No Content si la operación es exitosa.
     */
    @PostMapping("/{carreraId}/agregar-asignaturas")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Asignar asignaturas a una carrera.",
            description = "Permite a administradores asignar asignaturas a una carrera.")
    public ResponseEntity<CarreraResponseDTO> asignarAsignaturaACarrera(
            @Parameter(description = "ID de la carrera a la que se le asignarán las asignaturas", example = "1")
            @PathVariable Long carreraId,
            @Parameter(description = "Lista de IDs de las asignaturas a asignar", example = "[1, 2, 3]", required = true)
            @RequestBody List<Long> asignaturaIds
    ) {
        carreraService.asignarAsignaturaACarrera(carreraId, asignaturaIds);
        return ResponseEntity.ok(carreraService.obtenerDTOPorId(carreraId));
    }

    /**
     * Endpoint para actualizar una carrera existente.
     * Solo accesible por administradores.
     *
     * @param carrera Objeto DTO con los nuevos datos de la carrera.
     * @return Carrera actualizada.
     */
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Actualizar carrera.",
            description = "Permite a administradores actualizar una carrera existente.")
    public ResponseEntity<Carrera> actualizarCarrera(
            @Parameter(description = "Datos de actualización de carrera", required = true)
            @Valid @RequestBody CarreraUpdateDTO carrera) {
        return new ResponseEntity<>(carreraService.actualizarCarrera(carrera), HttpStatus.OK);
    }

    /**
     * Endpoint para eliminar una carrera por su ID.
     * Solo accesible por administradores.
     *
     * @param id ID de la carrera a eliminar.
     * @return Respuesta vacía con código 204 No Content si la operación es exitosa.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Eliminar carrera por id.",
            description = "Permite a administradores eliminar una carrera.")
    public  ResponseEntity<Map<String, String>> eliminarCarrera(
            @Parameter(description = "ID de la carrera a eliminar", example = "1")
            @PathVariable Long id) {
        carreraService.eliminarCarrera(id);

        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Carrera eliminada correctamente");

        return new ResponseEntity<>(respuesta, HttpStatus.OK);
    }

    /**
     * Endpoint para eliminar asignaturas de una carrera.
     * Solo accesible por administradores.
     *
     * @param id ID de la carrera de la que se eliminarán las asignaturas.
     * @param asignaturaIds Lista de IDs de las asignaturas a eliminar de la carrera.
     * @return Respuesta vacía con código 204 No Content si la operación es exitosa.
     */
    @PostMapping("/{id}/remover-asignaturas")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Eliminar asignaturas de una carrera.",
            description = "Permite a administradores eliminar asignaturas de una carrera existente.")
    public ResponseEntity<CarreraResponseDTO> removerAsignaturaDeCarrera(
            @Parameter(description = "ID de la carrera de la que se eliminarán las asignaturas", example = "1")
            @PathVariable Long id,
            @Parameter(description = "Lista de IDs de las asignaturas a eliminar de la carrera", example = "[1, 2, 3]", required = true)
            @RequestBody List<Long> asignaturaIds
    ) {
        carreraService.eliminarAsignaturaDeCarrera(id, asignaturaIds);
        return ResponseEntity.ok(carreraService.obtenerDTOPorId(id));
    }

}
