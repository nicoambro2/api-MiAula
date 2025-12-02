package com.apigestionespacios.apigestionespacios.service.reservaService;

import com.apigestionespacios.apigestionespacios.dtos.cronograma.CronogramaEspaciosDTO;
import com.apigestionespacios.apigestionespacios.dtos.reserva.ReservaCreateDTO;
import com.apigestionespacios.apigestionespacios.dtos.reserva.ReservaResponseDTO;
import com.apigestionespacios.apigestionespacios.dtos.reserva.ReservaUpdateDTO;
import com.apigestionespacios.apigestionespacios.entities.*;
import com.apigestionespacios.apigestionespacios.entities.enums.DiaSemana;
import com.apigestionespacios.apigestionespacios.exceptions.EntityValidationException;
import com.apigestionespacios.apigestionespacios.exceptions.ReservaSolapadaException;
import com.apigestionespacios.apigestionespacios.exceptions.ResourceNotFoundException;
import com.apigestionespacios.apigestionespacios.repository.ReservaRepository;
import com.apigestionespacios.apigestionespacios.service.ComisionService;
import com.apigestionespacios.apigestionespacios.service.EspacioService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.LinkedHashMap;


@Service
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final ComisionService comisionService;
    private final EspacioService espacioService;
    private final ReservaMapper reservaMapper;
    private final ReservaValidator reservaValidator;

    @Autowired
    public ReservaService(ReservaRepository reservaRepository, ComisionService comisionService, EspacioService espacioService, ReservaMapper reservaMapper, ReservaValidator reservaValidator) {
        this.reservaRepository = reservaRepository;
        this.comisionService = comisionService;
        this.espacioService = espacioService;
        this.reservaMapper = reservaMapper;
        this.reservaValidator = reservaValidator;
    }

    /**
     * Convierte un DTO de creación de reserva a una entidad Reserva.
     *
     * @param dto DTO de creación de reserva.
     * @return Entidad Reserva.
     */
    public Reserva ReservaCreateDTOtoReserva(ReservaCreateDTO dto) {
        Espacio espacio = espacioService.obtenerPorId(dto.getEspacioId());
        Comision comision = comisionService.obtenerComisionPorId(dto.getComisionId());

        return reservaMapper.toEntidad(dto, espacio, comision);
    }

    /**
     * Convierte una entidad Reserva a un DTO de respuesta.
     *
     * @param reserva Entidad Reserva a convertir.
     * @return DTO de respuesta de reserva.
     */
    public ReservaResponseDTO reservaToReservaResponseDTO(Reserva reserva) {
        return reservaMapper.toDTO(reserva);
    }

    /**
     * Listar todas las reservas en formato DTO.
     *
     * @return Lista de reservas en formato DTO.
     */
    public List<ReservaResponseDTO> listarReservasDTO() {
        return listaReservasAReservasResponseDTO(reservaRepository.findAll());
    }

    /**
     * Convierte una lista de reservas a una lista de ReservaResponseDTO.
     *
     * @param reservas Lista de reservas a convertir.
     * @return Lista de ReservaResponseDTO.
     */
    public List<ReservaResponseDTO> listaReservasAReservasResponseDTO(List<Reserva> reservas) {
        return reservaMapper.toDTOList(reservas);
    }

    /**
     * Crear una nueva reserva a partir de un DTO.
     *
     * @param dto DTO de reserva a crear.
     * @return Reserva creada.
     * @throws ReservaSolapadaException si la reserva se solapa con otra existente.
     */
    public Reserva crearReservaDesdeDTO(ReservaCreateDTO dto) {
        Espacio espacio = espacioService.obtenerPorId(dto.getEspacioId());
        Comision comision = comisionService.obtenerComisionPorId(dto.getComisionId());

        reservaValidator.validarNuevaReserva( dto, espacio, comision);

        Reserva reserva = ReservaCreateDTOtoReserva(dto);
        return reservaRepository.save(reserva);
    }

    /**
     * Actualizar una reserva existente a partir de un DTO.
     *
     * @param dto DTO de reserva con los datos actualizados.
     * @return Reserva actualizada.
     * @throws ResourceNotFoundException si no se encuentra la reserva.
     * @throws EntityValidationException si el nuevo espacio no tiene la capacidad suficiente.
     * @throws ReservaSolapadaException si la reserva se solapa con otra existente.
     */
    public Reserva actualizarReservaDesdeDTO(ReservaUpdateDTO dto) {
        Reserva reservaExistente = obtenerReserva(dto.getId());

        reservaExistente.setFechaInicio(dto.getFechaInicio());
        reservaExistente.setFechaFin(dto.getFechaFin());
        reservaExistente.setDia(DiaSemana.desdeDayOfWeek(dto.getFechaInicio().getDayOfWeek()));
        reservaExistente.setHoraInicio(dto.getHoraInicio());
        reservaExistente.setHoraFin(dto.getHoraFin());

        // Actualiza espacio si cambió
        if (!reservaExistente.getEspacio().getId().equals(dto.getEspacioId())) {

            if (reservaExistente.getComision().getAsignatura().getRequiereLaboratorio() && !(reservaExistente.getEspacio() instanceof Laboratorio)) {
                throw new EntityValidationException("El espacio solicitado debe ser un laboratorio para esta asignatura.");
            }

            // Verifica que el nuevo espacio tenga la capacidad suficiente
            if(espacioService.obtenerPorId(dto.getEspacioId()).getCapacidad() < reservaExistente.getComision().getCantidadAlumnos()) {
                throw new EntityValidationException("La cantidad de alumnos no puede ser mayor a la capacidad del espacio.");
            }

            Espacio espacio = espacioService.obtenerPorId(dto.getEspacioId());
            reservaExistente.setEspacio(espacio);
        }

        // Valida solapamientos antes de guardar
        reservaValidator.existeSolapamiento(reservaExistente);

        return reservaRepository.save(reservaExistente);
    }

    /**
     * Eliminar una reserva por su ID.
     *
     * @param id ID de la reserva a eliminar.
     * @throws ResourceNotFoundException si no se encuentra la reserva.
     */
    public void finalizarReserva(Long id) {
        Reserva reserva = obtenerReserva(id);

        if (reserva.getFechaInicio().isAfter(LocalDate.now())) {
            reserva.setFechaInicio(LocalDate.now());
            reserva.setHoraInicio(LocalTime.now());// Si la reserva está en el futuro, la marca como iniciada hoy
        }
        reserva.setFechaFin(LocalDate.now()); // Marca la reserva como finalizada
        reserva.setHoraFin(LocalTime.now()); // Marca la hora de finalización como ahora

        reservaRepository.save(reserva);
    }

    /**
     * Generar una reserva a partir de una solicitud.
     *
     * @param solicitud Solicitud que contiene los datos para la reserva.
     */
    public void generarDesdeSolicitud(Solicitud solicitud) {
        ReservaCreateDTO dto = ReservaCreateDTO.builder()
                .fechaInicio(solicitud.getFechaInicio())
                .fechaFin(solicitud.getFechaFin())
                .horaInicio(solicitud.getHoraInicio())
                .horaFin(solicitud.getHoraFin())
                .espacioId(solicitud.getNuevoEspacio().getId())
                .comisionId(solicitud.getComision().getId())
                .build();

        this.crearReservaDesdeDTO(dto);
    }

    /**
     * Modificar una reserva existente a partir de una solicitud.
     *
     * @param solicitud Solicitud que contiene los datos para actualizar la reserva.
     */
    public void modificarDesdeSolicitud(Solicitud solicitud) {
        ReservaUpdateDTO dto = ReservaUpdateDTO.builder()
                .id(solicitud.getReservaOriginal().getId())
                .fechaInicio(solicitud.getFechaInicio())
                .fechaFin(solicitud.getFechaFin())
                .horaInicio(solicitud.getHoraInicio())
                .horaFin(solicitud.getHoraFin())
                .espacioId(solicitud.getNuevoEspacio().getId())
                .build();

        this.actualizarReservaDesdeDTO(dto);
    }

    /**
     * Obtener todas las reservas ordenadas por fecha y hora.
     *
     * @return Lista de reservas ordenadas.
     */
    public List<ReservaResponseDTO> obtenerTodasOrdenadas() {
        return listaReservasAReservasResponseDTO(
                reservaRepository.findAll(Sort.by("fechaInicio").ascending().and(Sort.by("horaInicio").ascending())).stream()
                        .filter(r -> !r.getHoraInicio().equals(r.getHoraFin()))
                        .toList());
    }



    /**
     * Obtiene un cronograma de reservas para una fecha específica, agrupado por espacio.
     *
     * @param fecha Fecha para la cual se desea obtener el cronograma.
     * @return Lista de CronogramaEspaciosDTO con las reservas agrupadas por espacio.
     */
    public List<CronogramaEspaciosDTO> obtenerCronogramaDTOParaFecha(LocalDate fecha) {
        DiaSemana diaEnum = DiaSemana.desdeDayOfWeek(fecha.getDayOfWeek()); // Tu enum DiaSemana tiene LUNES, MARTES, etc.

        List<ReservaResponseDTO> reservasResponse = listaReservasAReservasResponseDTO(
                reservaRepository.findReservasActivasParaFecha(fecha, diaEnum).stream()
                        .filter(r -> !r.getHoraInicio().equals(r.getHoraFin())) // <- este filtro es el que pediste
                        .collect(Collectors.toList())
        );

        return reservasResponse.stream()
                .collect(Collectors.groupingBy(
                        ReservaResponseDTO::getNombreEspacio,
                        LinkedHashMap::new,
                        Collectors.toList()
                ))
                .values().stream()
                .map(reservaResponseDTOS -> {
                    ReservaResponseDTO primeraReserva = reservaResponseDTOS.getFirst();
                    return CronogramaEspaciosDTO.builder()
                            .nombreEspacio(primeraReserva.getNombreEspacio())
                            .reservas(reservaResponseDTOS)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Obtener una reserva por su ID.
     *
     * @param id ID de la reserva.
     * @return Reserva encontrada.
     * @throws ResourceNotFoundException si no se encuentra la reserva.
     */
    public Reserva obtenerReserva(Long id) {
        return reservaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva con ID " + id + " no encontrada."));
    }

    /**
     * Obtener resevaResponseDTO por ID.
     * @param id ID de la reserva.
     * @return ReservaResponseDTO correspondiente a la reserva encontrada.
     */
    public ReservaResponseDTO obtenerReservaDTO(Long id) {
        Reserva reserva = obtenerReserva(id);
        return reservaMapper.toDTO(reserva);
    }


    /**
     * Obtener todas las reservas paginadas.
     *
     * @param pageable Información de paginación.
     * @return Página de reservas.
     */
    public Page<ReservaResponseDTO> obtenerReservasPaginadasDTO(Pageable pageable) {
        return reservaRepository.findAll(pageable)
                .map(this::reservaToReservaResponseDTO);
    }

    /**
        * Obtener reservas por Profesor asignado a la comision.
        * @param usuarioId ID del usuario (profesor) cuyas reservas se desean consultar.
        * @return Lista de reservas asociadas al profesor.
        */
    public List<ReservaResponseDTO> obtenerReservasPorProfesor(Long usuarioId) {
        return listaReservasAReservasResponseDTO(reservaRepository.findReservasByProfesorId(usuarioId));
    }

    /**
     * Obtener reservas vigentes por Profesor asignado a la comision.
     * @param usuarioId ID del usuario (profesor) cuyas reservas vigentes se desean consultar.
     * @return Lista de reservas vigentes asociadas al profesor.
     */
    public List<ReservaResponseDTO> obtenerReservasVigentesPorProfesor(Long usuarioId) {
        return listaReservasAReservasResponseDTO(reservaRepository.findReservasActualesByProfesorId(usuarioId));
    }

    /**
     * Obtener reservas vigentes por Comision.
     * @param comisionId ID de la comisión cuyas reservas vigentes se desean consultar.
     * @return Lista de reservas vigentes asociadas a la comisión.
     */
    public List<ReservaResponseDTO> obtenerReservasVigentesPorComision(Long comisionId) {
        return listaReservasAReservasResponseDTO(reservaRepository.findReservasActivasPorComisionOrdenadas(comisionId));
    }
}
