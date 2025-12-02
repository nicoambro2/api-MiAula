package com.apigestionespacios.apigestionespacios.service;

import com.apigestionespacios.apigestionespacios.dtos.solicitud.SolicitudCreateDTO;
import com.apigestionespacios.apigestionespacios.dtos.solicitud.SolicitudResponseDTO;
import com.apigestionespacios.apigestionespacios.entities.*;
import com.apigestionespacios.apigestionespacios.entities.enums.DiaSemana;
import com.apigestionespacios.apigestionespacios.entities.enums.EstadoSolicitud;
import com.apigestionespacios.apigestionespacios.exceptions.EntityValidationException;
import com.apigestionespacios.apigestionespacios.exceptions.ResourceNotFoundException;
import com.apigestionespacios.apigestionespacios.repository.SolicitudRepository;
import com.apigestionespacios.apigestionespacios.service.reservaService.ReservaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SolicitudService {

    private final SolicitudRepository solicitudRepository;
    private final ReservaService reservaService;
    private final UsuarioService usuarioService;
    private final EspacioService espacioService;
    private final ComisionService comisionService;

    @Autowired
    public SolicitudService(SolicitudRepository solicitudRepository, ReservaService reservaService, UsuarioService usuarioService, EspacioService espacioService, ComisionService comisionService) {
        this.solicitudRepository = solicitudRepository;
        this.reservaService = reservaService;
        this.usuarioService = usuarioService;
        this.espacioService = espacioService;
        this.comisionService = comisionService;
    }

    /**
     * Convierte un DTO de creación de solicitud a una entidad Solicitud.
     *
     * @param dto DTO de creación de solicitud.
     * @return Entidad Solicitud.
     */
    public Solicitud solicitudDTOtoSolicitud(SolicitudCreateDTO dto) {
        Usuario usuario = usuarioService.obtenerPorId(dto.getUsuarioId());
        Espacio nuevoEspacio = espacioService.obtenerPorId(dto.getNuevoEspacioId());
        Comision comision = comisionService.obtenerComisionPorId(dto.getComisionId());
        DiaSemana dia = DiaSemana.desdeDayOfWeek(dto.getFechaInicio().getDayOfWeek());
        Reserva reservaOriginal = null;

        if (dto.getReservaOriginalId() != null) {
            reservaOriginal = reservaService.obtenerReserva(dto.getReservaOriginalId());
        }

        return Solicitud.builder()
                .usuario(usuario)
                .nuevoEspacio(nuevoEspacio)
                .reservaOriginal(reservaOriginal)
                .estado(EstadoSolicitud.PENDIENTE)
                .fechaInicio(dto.getFechaInicio())
                .fechaFin(dto.getFechaFin())
                .diaSemana(dia)
                .horaInicio(dto.getHoraInicio())
                .horaFin(dto.getHoraFin())
                .comentarioProfesor(dto.getComentarioProfesor())
                .fechaHoraSolicitud(LocalDateTime.now())
                .comision(comision)
                .build();
    }

    /**
     * Convierte una entidad Solicitud a un DTO de respuesta SolicitudResponseDTO.
     *
     * @param solicitud la entidad Solicitud a convertir.
     * @return el DTO SolicitudResponseDTO con los datos mapeados de la entidad.
     */
    public SolicitudResponseDTO solicitudToSolicitudResponseDTO(Solicitud solicitud) {
        SolicitudResponseDTO.SolicitudResponseDTOBuilder builder = SolicitudResponseDTO.builder()
                .id(solicitud.getId())
                .nombreUsuario(solicitud.getUsuario().getNombre())
                .nombreNuevoEspacio(solicitud.getNuevoEspacio().getNombre())
                .estado(solicitud.getEstado())
                .fechaInicio(solicitud.getFechaInicio())
                .fechaFin(solicitud.getFechaFin())
                .diaSemana(solicitud.getDiaSemana())
                .horaInicio(solicitud.getHoraInicio())
                .horaFin(solicitud.getHoraFin())
                .comentarioEstado(solicitud.getComentarioEstado())
                .comentarioProfesor(solicitud.getComentarioProfesor())
                .fechaHoraSolicitud(solicitud.getFechaHoraSolicitud())
                .nombreComision(solicitud.getComision().getNombre())
                .nombreAsignatura(solicitud.getComision().getAsignatura().getNombre())
                .nombreDocente(solicitud.getComision().getProfesor().getNombre() + " " + solicitud.getComision().getProfesor().getApellido());

        if (solicitud.getReservaOriginal() != null) {
            builder.reservaOriginalId(solicitud.getReservaOriginal().getId());
        } else {
            builder.reservaOriginalId(null);
        }

        return builder.build();
    }

    /**
     * Listar todas las solicitudes en formato DTO.
     *
     * @return Lista de solicitudes en formato DTO.
     */
    public List<SolicitudResponseDTO> listarSolicitudesDTO() {
        return listaSolicitudesASolicitudesResponseDTO(solicitudRepository.findAll());
    }

    /**
     * Convierte una lista de solicitudes a una lista de SolicitudResponseDTO.
     *
     * @param solicitudes Lista de solicitudes a convertir.
     * @return Lista de SolicitudResponseDTO.
     */
    public List<SolicitudResponseDTO> listaSolicitudesASolicitudesResponseDTO(List<Solicitud> solicitudes) {
        return solicitudes.stream()
                .map(this::solicitudToSolicitudResponseDTO)
                .toList();
    }

    /**
     * Guarda una nueva solicitud a partir de un DTO de creación
     *
     * @param dto el DTO con los datos para crear la solicitud.
     * @return DTO de respuesta con los datos de la solicitud guardada.
     */
    public Solicitud guardarDTO(SolicitudCreateDTO dto) {
        Espacio espacioSolicitado = espacioService.obtenerPorId(dto.getNuevoEspacioId());
        Comision comision = comisionService.obtenerComisionPorId(dto.getComisionId());

        if(espacioSolicitado.getCapacidad() < comision.getCantidadAlumnos()) {
            throw new EntityValidationException("La cantidad de alumnos no puede ser mayor a la capacidad del espacio solicitado.");
        }

        if (dto.getFechaFin().isBefore(dto.getFechaInicio())) {
            throw new EntityValidationException("La fecha de fin no puede ser anterior a la de inicio.");
        }

        if (dto.getHoraFin().isBefore(dto.getHoraInicio())) {
            throw new EntityValidationException("La hora de fin no puede ser anterior a la de inicio.");
        }

        Solicitud solicitud = solicitudDTOtoSolicitud(dto);

        return solicitudRepository.save(solicitud);
    }

    /**
     * Registra una nueva solicitud de reserva a partir de los datos recibidos.
     * La reserva original debe ser nula y el nuevo espacio no debe ser null.
     *
     * @param dto DTO con los datos de la solicitud a crear
     * @return DTO con los datos de la solicitud creada
     * @throws IllegalArgumentException si no se especifica un nuevo espacio o si la reserva original no es nula
     */
    public Solicitud solicitarNuevaReservaDTO(SolicitudCreateDTO dto) {
        Espacio espacioSolicitado = espacioService.obtenerPorId(dto.getNuevoEspacioId());
        Comision comision = comisionService.obtenerComisionPorId(dto.getComisionId());

        if (comision.getAsignatura().getRequiereLaboratorio() && !(espacioSolicitado instanceof Laboratorio)) {
            throw new EntityValidationException("El espacio solicitado debe ser un laboratorio para esta asignatura.");
        }


        if(espacioSolicitado.getCapacidad() < comision.getCantidadAlumnos()) {
            throw new EntityValidationException("La cantidad de alumnos no puede ser mayor a la capacidad del espacio solicitado.");
        }

        if (dto.getFechaFin().isBefore(dto.getFechaInicio())) {
            throw new EntityValidationException("La fecha de fin no puede ser anterior a la de inicio.");
        }

        if (dto.getHoraFin().isBefore(dto.getHoraInicio())) {
            throw new EntityValidationException("La hora de fin no puede ser anterior a la de inicio.");
        }

        if (dto.getReservaOriginalId() != null) {
            throw new IllegalArgumentException("Para nuevas reservas, la reserva original debe ser nula.");
        }

        Solicitud solicitud = solicitudDTOtoSolicitud(dto);

        return solicitudRepository.save(solicitud);
    }

    /**
     * Registra una solicitud de modificación de una reserva existente.
     * Se utiliza el ID de la reserva original y los nuevos datos proporcionados.
     *
     * @param dto DTO con los datos de la solicitud a crear
     * @return DTO con los datos de la solicitud creada
     * @throws IllegalArgumentException si no se encuentra la reserva original o si no se especifica el ID de la reserva original
     */
    public Solicitud solicitarModificacionPorIdReservaDTO(SolicitudCreateDTO dto) {
        Reserva reservaOriginal = reservaService.obtenerReserva(dto.getReservaOriginalId());
        Espacio espacioSolicitado = espacioService.obtenerPorId(dto.getNuevoEspacioId());
        Comision comision = comisionService.obtenerComisionPorId(dto.getComisionId());

        if (dto.getReservaOriginalId() == null) {
            throw new IllegalArgumentException("Para modificaciones, debe especificar el ID de la reserva original.");
        }

        if (reservaOriginal == null) {
            throw new IllegalArgumentException("Reserva original no encontrada con ID: " + dto.getReservaOriginalId());
        }

        if (comision.getAsignatura().getRequiereLaboratorio() && !(espacioSolicitado instanceof Laboratorio)) {
            throw new EntityValidationException("El espacio solicitado debe ser un laboratorio para esta asignatura.");
        }


        if(espacioSolicitado.getCapacidad() < comision.getCantidadAlumnos()) {
            throw new EntityValidationException("La cantidad de alumnos no puede ser mayor a la capacidad del espacio solicitado.");
        }

        if (dto.getFechaFin().isBefore(dto.getFechaInicio())) {
            throw new EntityValidationException("La fecha de fin no puede ser anterior a la de inicio.");
        }

        if (dto.getHoraFin().isBefore(dto.getHoraInicio())) {
            throw new EntityValidationException("La hora de fin no puede ser anterior a la de inicio.");
        }

        Solicitud solicitud = solicitudDTOtoSolicitud(dto);

        return solicitudRepository.save(solicitud);
    }

    public void eliminar(Long id) {
        if (!solicitudRepository.existsById(id)) {
            throw new ResourceNotFoundException("Solicitud no encontrada con ID: " + id);
        }
        solicitudRepository.deleteById(id);
    }

    public Solicitud aprobar(Long id) {
        Solicitud solicitud = obtenerPorId(id);
        Reserva reservaOriginal = solicitud.getReservaOriginal();

        if (!solicitud.getEstado().equals(EstadoSolicitud.PENDIENTE)) {
            throw new EntityValidationException("Solo se pueden aprobar solicitudes pendientes.");
        }

        solicitud.setEstado(EstadoSolicitud.APROBADA);

        if (reservaOriginal != null) {
            // Si la solicitud es una modificación, se actualiza la reserva original
            reservaService.modificarDesdeSolicitud(solicitud);
        } else {
            // Si es una nueva solicitud, se genera una nueva reserva
            reservaService.generarDesdeSolicitud(solicitud);
        }

        return solicitudRepository.save(solicitud);
    }

    public Solicitud rechazar(Long id, String comentario) {
        Solicitud solicitud = obtenerPorId(id);

        if (!solicitud.getEstado().equals(EstadoSolicitud.PENDIENTE)) {
            throw new EntityValidationException("Solo se pueden rechazar solicitudes pendientes.");
        }

        solicitud.setEstado(EstadoSolicitud.RECHAZADA);
        solicitud.setComentarioEstado(comentario);

        return solicitudRepository.save(solicitud);
    }

    /**
     * Obtiene todas las solicitudes de forma paginada y las convierte en DTOs de respuesta.
     *
     * @param pageable objeto para configurar la paginación.
     * @return página de SolicitudResponseDTO.
     */
    public Page<SolicitudResponseDTO> obtenerTodasPaginadasDTO(Pageable pageable) {
        return solicitudRepository.findAll(pageable)
                .map(this::solicitudToSolicitudResponseDTO);
    }


    public Solicitud obtenerPorId(Long id) {
        return solicitudRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrado con id: " + id));
    }

    /**
     * Busca una solicitud por ID y la convierte en un DTO de respuesta.
     *
     * @param id el ID de la solicitud a buscar.
     * @return DTO SolicitudResponseDTO correspondiente a la entidad encontrada.
     */
    public SolicitudResponseDTO obtenerPorIdDTO(Long id) {
        Solicitud solicitud = obtenerPorId(id);
        return solicitudToSolicitudResponseDTO(solicitud);
    }

    /**
     * Obtiene todas las solicitudes de un usuario específico y las convierte en DTOs de respuesta.
     *
     * @param usuarioId el ID del usuario.
     * @return lista de SolicitudResponseDTO correspondientes al usuario.
     */
    public List<SolicitudResponseDTO> obtenerPorUsuarioDTO(Long usuarioId) {
        List<Solicitud> solicitudes = solicitudRepository.findByUsuarioId(usuarioId);
        return listaSolicitudesASolicitudesResponseDTO(solicitudes);
    }

    /**
     * Obtiene el historial paginado de solicitudes de un usuario específico y las convierte en DTOs de respuesta.
     *
     * @param usuarioId el ID del usuario.
     * @param pageable objeto para configurar la paginación.
     * @return página de SolicitudResponseDTO del historial del usuario.
     */
    public Page<SolicitudResponseDTO> obtenerHistorialPorUsuarioDTO(Long usuarioId, Pageable pageable) {
        return solicitudRepository.findByUsuarioId(usuarioId, pageable)
                .map(this::solicitudToSolicitudResponseDTO);
    }

    /**
     * Obtiene todas las solicitudes con un estado específico y las convierte en DTOs de respuesta.
     *
     * @param estado el estado de las solicitudes a buscar.
     * @return lista de SolicitudResponseDTO con ese estado.
     */
    public List<SolicitudResponseDTO> obtenerPorEstadoDTO(EstadoSolicitud estado) {
        return solicitudRepository.findByEstado(estado).stream()
                .map(this::solicitudToSolicitudResponseDTO)
                .toList();
    }

    /**
     * Obtiene todas las solicitudes pendientes de forma paginada y las convierte en DTOs de respuesta.
     *
     * @param pageable objeto para configurar la paginación.
     * @return página de SolicitudResponseDTO con solicitudes pendientes.
     */
    public Page<SolicitudResponseDTO> obtenerSolicitudesPendientesDTO(Pageable pageable) {
        return solicitudRepository.findByEstado(EstadoSolicitud.PENDIENTE, pageable)
                .map(this::solicitudToSolicitudResponseDTO);
    }

    /**
     * Obtiene todas las solicitudes de un usuario con un estado específico y las convierte en DTOs de respuesta.
     *
     * @param estado el estado de las solicitudes.
     * @param usuarioId el ID del usuario.
     * @return lista de SolicitudResponseDTO filtradas por estado y usuario.
     */
    public List<SolicitudResponseDTO> obtenerPorEstadoYUsuarioDTO(EstadoSolicitud estado, Long usuarioId) {
        return solicitudRepository.findByEstadoAndUsuarioId(estado, usuarioId).stream()
                .map(this::solicitudToSolicitudResponseDTO)
                .toList();
    }

    /**
     * Obtiene todas las solicitudes ordenadas por fecha y hora descendente, y las convierte en DTOs de respuesta.
     *
     * @return lista de SolicitudResponseDTO ordenadas por fecha y hora de solicitud descendente.
     */
    public List<SolicitudResponseDTO> obtenerTodasOrdenadasPorFechaHoraSolicitudDescDTO() {
        return solicitudRepository.findAllByOrderByFechaHoraSolicitudDesc().stream()
                .map(this::solicitudToSolicitudResponseDTO)
                .toList();
    }

    public SolicitudResponseDTO cancelarSolicitud(Long idSolicitud, Long idUsuarioSolicitante) {
        Solicitud solicitud = obtenerPorId(idSolicitud);

        if (!solicitud.getUsuario().getId().equals(idUsuarioSolicitante)) {
            throw new EntityValidationException("No tiene permiso para cancelar esta solicitud.");
        }

        if (!"PENDIENTE".equalsIgnoreCase(solicitud.getEstado().toString())) {
            throw new EntityValidationException("Solo se pueden cancelar solicitudes en estado PENDIENTE.");
        }

        solicitud.setEstado(EstadoSolicitud.CANCELADA);
        solicitud.setComentarioEstado("Cancelada por el solicitante.");
        solicitudRepository.save(solicitud);

        return solicitudToSolicitudResponseDTO(solicitud);
    }



}
