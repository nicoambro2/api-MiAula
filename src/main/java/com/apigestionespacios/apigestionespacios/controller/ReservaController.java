package com.apigestionespacios.apigestionespacios.controller;

import com.apigestionespacios.apigestionespacios.dtos.cronograma.CronogramaEspaciosDTO;
import com.apigestionespacios.apigestionespacios.dtos.reserva.ReservaCreateDTO;
import com.apigestionespacios.apigestionespacios.dtos.reserva.ReservaResponseDTO;
import com.apigestionespacios.apigestionespacios.dtos.reserva.ReservaUpdateDTO;
import com.apigestionespacios.apigestionespacios.entities.Reserva;
import com.apigestionespacios.apigestionespacios.service.reservaService.ReservaService;
import com.apigestionespacios.apigestionespacios.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/reservas")
@Tag(name = "Reservas", description = "Operaciones relacionadas con las reservas.")
public class ReservaController {

    private final ReservaService reservaService;
    private final UsuarioService usuarioService;

    @Autowired
    public ReservaController(ReservaService reservaService, UsuarioService usuarioService) {
        this.reservaService = reservaService;
        this.usuarioService = usuarioService;
    }

    @Operation(
            summary = "Crear reserva",
            description = "Permite a administradores crear una nueva reserva a partir de los datos proporcionados en el DTO.")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Reserva> crearReserva(
            @Parameter(description = "Datos de la reserva a crear", required = true)
            @Valid @RequestBody ReservaCreateDTO dto) {
        Reserva reserva = reservaService.crearReservaDesdeDTO(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(reserva);
    }

    @Operation(
            summary = "Actualizar reserva",
            description = "Permite a administradores actualizar una reserva existente usando los datos del DTO.")
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Reserva> actualizarReserva(
            @Parameter(description = "Datos de la reserva a actualizar", required = true)
            @Valid @RequestBody ReservaUpdateDTO reservaUpdateDTO) {
        Reserva reservaActualizada = reservaService.actualizarReservaDesdeDTO(reservaUpdateDTO);
        return ResponseEntity.ok(reservaActualizada);
    }

    @Operation(
            summary = "Listar reservas",
            description = "Permite a administradores obtener la lista completa de reservas registradas.")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReservaResponseDTO>> obtenerReservas() {
        List<ReservaResponseDTO> reservasDTO = reservaService.listarReservasDTO();
        if (reservasDTO.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(reservasDTO);
    }

    @Operation(
            summary = "Historial paginado de reservas",
            description = "Permite a administradores obtener el historial completo de reservas de forma paginada y ordenada.")
    @GetMapping("/historial-paginado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ReservaResponseDTO>> obtenerReservas(
            @Parameter(description = "Número de página a consultar", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de la página", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Campo por el cual ordenar las reservas", example = "fechaInicio")
            @RequestParam(defaultValue = "fechaInicio") String sortBy,
            @Parameter(description = "Dirección del ordenamiento (asc o desc)", example = "asc")
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending()
        );

        Page<ReservaResponseDTO> reservas = reservaService.obtenerReservasPaginadasDTO(pageable);

        if (reservas.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(reservas);
    }

    @Operation(
            summary = "Obtener reserva por ID",
            description = "Permite a administradores obtener los detalles de una reserva específica por su ID.")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReservaResponseDTO> obtenerReserva(
            @Parameter(description = "ID de la reserva a consultar", required = true, example = "1")
            @PathVariable Long id) {
        return new ResponseEntity<>(reservaService.obtenerReservaDTO(id), HttpStatus.OK);
    }

    @Operation(
            summary = "Eliminar reserva",
            description = "Permite a administradores finalizar una reserva existente por su ID.")
    @PostMapping("/{id}/finalizar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> finalizarReserva(
            @Parameter(description = "ID de la reserva a eliminar", required = true, example = "1")
            @PathVariable Long id) {
        reservaService.finalizarReserva(id);

        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Reserva finalizada correctamente");

        return new ResponseEntity<>(respuesta, HttpStatus.OK);
    }

    @Operation(
            summary = "Listar reservas históricas y vigentes de profesor logueado.",
            description = "Permite a profesores obtener el historial de sus propias reservas.")
    @PreAuthorize("hasRole('PROFESOR')")
    @GetMapping("/mis-reservas")
    public ResponseEntity<List<ReservaResponseDTO>> obtenerMiHistorialReservas(Authentication authentication) {
        String usuarioLogueado = authentication.getName();
        Long profesorId = usuarioService.obtenerPorUsername(usuarioLogueado).getId();

        List<ReservaResponseDTO> reservas = reservaService.obtenerReservasPorProfesor(profesorId);

        if (reservas.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(reservas);
    }

    @Operation(
            summary = "Listar reservas vigentes de profesor logueado",
            description = "Permite a profesores obtener la lista de sus reservas vigentes.")
    @PreAuthorize("hasRole('PROFESOR')")
    @GetMapping("/mis-reservas/vigentes")
    public ResponseEntity<List<ReservaResponseDTO>> obtenerMisReservasVigentes(Authentication authentication) {
        String usuarioLogueado = authentication.getName();
        Long profesorId = usuarioService.obtenerPorUsername(usuarioLogueado).getId();

        List<ReservaResponseDTO> reservas = reservaService.obtenerReservasVigentesPorProfesor(profesorId);

        if (reservas.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(reservas);
    }

    /**
     * Endpoint para obtener el cronograma completo de reservas agrupado por espacio.
     * Acceso público, no requiere autenticación.
     *
     * @return Mapa donde la clave es el nombre del espacio y el valor es una lista de reservas.
     */
    @Operation(
            summary = "Cronograma completo de reservas",
            description = "Obtiene el cronograma completo de reservas agrupado por espacio. Acceso público.")
    @GetMapping("/cronograma")
    public ResponseEntity<Map<String, List<ReservaResponseDTO>>> obtenerCronogramaCompletoAgrupadoPorEspacio() {
        // Trae todas las reservas ordenadas por fechaInicio y horaInicio
        List<ReservaResponseDTO> reservas = reservaService.obtenerTodasOrdenadas();

        // Agrupa por espacio
        Map<String, List<ReservaResponseDTO>> cronograma = reservas.stream()
                .collect(Collectors.groupingBy(
                        ReservaResponseDTO::getNombreEspacio,
                        LinkedHashMap::new, // mantiene el orden de inserción
                        Collectors.toList()
                ));

        return ResponseEntity.ok(cronograma);
    }

    /**
     * Endpoint para obtener el cronograma de espacios para una fecha específica.
     * Acceso público, no requiere autenticación.
     *
     * @param fecha Fecha para la cual se desea obtener el cronograma.
     * @return Lista de cronogramas de espacios para la fecha especificada.
     */
    @Operation(
            summary = "Cronograma de espacios por fecha",
            description = "Obtiene el cronograma de reservas para una fecha específica, agrupado por espacio. Acceso público.")
    @GetMapping("/cronograma-por-dia")
    public ResponseEntity<List<CronogramaEspaciosDTO>> obtenerCronogramaParaFechaAgrupadoPorEspacio(
            @Parameter(description = "Fecha para la cual se desea obtener el cronograma (formato: yyyy-MM-dd)", required = true, example = "2025-06-15")
            @RequestParam("fecha") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {

        List<CronogramaEspaciosDTO> cronograma = reservaService.obtenerCronogramaDTOParaFecha(fecha);
        return cronograma.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(cronograma);
    }

}