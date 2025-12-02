package com.apigestionespacios.apigestionespacios.service;

import com.apigestionespacios.apigestionespacios.dtos.comision.ComisionCreateDTO;
import com.apigestionespacios.apigestionespacios.dtos.comision.ComisionResponseDTO;
import com.apigestionespacios.apigestionespacios.dtos.comision.ComisionUpdateDTO;
import com.apigestionespacios.apigestionespacios.dtos.cronograma.CronogramaComisionDTO;
import com.apigestionespacios.apigestionespacios.entities.Carrera;
import com.apigestionespacios.apigestionespacios.entities.Comision;
import com.apigestionespacios.apigestionespacios.entities.Usuario;
import com.apigestionespacios.apigestionespacios.exceptions.EntityValidationException;
import com.apigestionespacios.apigestionespacios.exceptions.ResourceNotFoundException;
import com.apigestionespacios.apigestionespacios.repository.ComisionRepository;
import com.apigestionespacios.apigestionespacios.service.reservaService.ReservaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ComisionService {

    private final ComisionRepository comisionRepository;
    private final UsuarioService usuarioService;
    private final CarreraService carreraService;
    private final AsignaturaService asignaturaService;
    private final ReservaService reservaService;

    @Autowired
    public ComisionService(ComisionRepository comisionRepository, UsuarioService usuarioService,
                           CarreraService carreraService, AsignaturaService asignaturaService,
                           @Lazy ReservaService reservaService) {
        this.comisionRepository = comisionRepository;
        this.usuarioService = usuarioService;
        this.carreraService = carreraService;
        this.asignaturaService = asignaturaService;
        this.reservaService = reservaService;
    }

    public Comision comisionCreateDTOtoComision(ComisionCreateDTO dto) {
        return Comision.builder()
                .nombre(dto.getNombre())
                .cantidadAlumnos(dto.getCantidadAlumnos())
                .asignatura(asignaturaService.obtenerPorId(dto.getAsignaturaId()))
                .carrera(carreraService.obtenerPorId(dto.getCarreraId()))
                .profesor(usuarioService.obtenerPorId(dto.getProfesorId()))
                .build();
    }

    public ComisionResponseDTO comisionToComisionResponseDTO(Comision comision) {
        return ComisionResponseDTO.builder()
                .id(comision.getId())
                .nombre(comision.getNombre())
                .asignaturaNombre(comision.getAsignatura().getNombre())
                .carreraNombre(comision.getCarrera().getNombre())
                .profesorNombre(comision.getProfesor().getNombre())
                .cantidadAlumnos(comision.getCantidadAlumnos())
                .build();
    }

    public CronogramaComisionDTO comisionToCronogramaComisionDTO(Comision comision) {
        return CronogramaComisionDTO.builder()
                .id(comision.getId())
                .nombre(comision.getNombre())
                .asignaturaNombre(comision.getAsignatura().getNombre())
                .carreraNombre(comision.getCarrera().getNombre())
                .profesorNombre(comision.getProfesor().getNombre())
                .cantidadAlumnos(comision.getCantidadAlumnos())
                .reservasVigentes(reservaService.obtenerReservasVigentesPorComision(comision.getId()))
                .build();
    }

    /**
     * Obtiene todas las comisiones en formato DTO.
     *
     * @return Lista de DTOs de comisiones
     */
    public List<ComisionResponseDTO> obtenerTodasComisionesDTO() {
        return comisionRepository.findAll().stream()
                .map(this::comisionToComisionResponseDTO)
                .toList();
    }

    /**
     * Obtiene una comisión por su ID.
     * Si la comisión no existe, lanza una ResourceNotFoundException.
     *
     * @param id ID de la comisión a buscar
     * @return Comisión encontrada
     */
    public Comision obtenerComisionPorId(Long id) {
        return comisionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comisión no encontrada con ID: " + id));
    }

    /**
     * Obtiene una comisión por su ID y la convierte a DTO.
     * Si la comisión no existe, lanza una ResourceNotFoundException.
     *
     * @param id ID de la comisión a buscar
     * @return DTO de la comisión encontrada
     */
    public CronogramaComisionDTO obtenerComisionDTOPorId(Long id) {
        Comision comision = obtenerComisionPorId(id);
        return comisionToCronogramaComisionDTO(comision);
    }

    /**
     * Crea una nueva comisión a partir de un DTO.
     * Valida que el profesor, carrera y asignatura sean correctos.
     * Valida que la cantidad de alumnos sea mayor a cero y no exceda 200.
     *
     * @param dto DTO con los datos de la comisión a crear
     * @return Comisión creada
     */
    public Comision crearComisionDesdeDTO(ComisionCreateDTO dto) {
        Comision comision = comisionCreateDTOtoComision(dto);
        validarProfesor(comision.getProfesor().getId());
        validarCarrera(comision.getCarrera().getId());
        validarAsignatura(comision.getCarrera().getId(), comision.getAsignatura().getId());
        validarNombreComision(comision);
        return comisionRepository.save(comision);
    }

    /**
     * Actualiza una comisión existente a partir de un DTO.
     * Valida que el nombre, profesor y carrera sean correctos.
     *
     * @param dto DTO con los nuevos datos de la comisión
     * @return Comisión actualizada
     */
    public Comision actualizarComision(ComisionUpdateDTO dto) {
        Comision existente = obtenerComisionPorId(dto.getId());
        existente.setCantidadAlumnos(dto.getCantidadAlumnos());
        existente.setNombre(dto.getNombre());
        existente.setProfesor(usuarioService.obtenerPorId(dto.getProfesorId()));
        existente.setCarrera(carreraService.obtenerPorId(dto.getCarreraId()));

        validarProfesor(dto.getProfesorId());
        validarCarrera(dto.getCarreraId());
        validarNombreComision(existente);

        return comisionRepository.save(existente);
    }

    /**
     * Elimina una comisión por su ID.
     * Si la comisión no existe, lanza una ResourceNotFoundException.
     *
     * @param id ID de la comisión a eliminar
     */
    public void eliminarComision(Long id) {
        if (!comisionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Comisión no encontrada");
        }
        comisionRepository.deleteById(id);
    }

    /**
     * Valida que el usuario tenga el rol de profesor.
     * Si no es así, lanza una EntityValidationException.
     *
     * @param idUsuarioProfesor ID del usuario a validar
     */
    private void validarProfesor(Long idUsuarioProfesor) {
        Usuario usuario = usuarioService.obtenerPorId(idUsuarioProfesor);

        if (!"PROFESOR".equalsIgnoreCase(usuario.getRol().name())) {
            throw new EntityValidationException("El usuario con ID " + idUsuarioProfesor + " no tiene el rol de profesor");
        }
    }

    /**
     * Valida que no exista otra comisión con el mismo nombre para la misma asignatura.
     * Si existe, lanza una IllegalArgumentException.
     *
     * @param comision Comisión a validar
     */
    private void validarNombreComision(Comision comision){
        if (comisionRepository.existsByNombreAndAsignaturaId(
                comision.getNombre(), comision.getAsignatura().getId())) {
            throw new IllegalArgumentException("Ya existe una comisión con ese nombre para la asignatura indicada.");
        }
    }

    /**
     * Valida que la carrera exista.
     * Si no existe, lanza una excepción ResourceNotFoundException.
     *
     * @param idCarrera ID de la carrera a validar
     */
    private void validarCarrera(Long idCarrera) {
        carreraService.obtenerPorId(idCarrera); // Lanza excepción si no existe
    }

    /**
     * Valida que la asignatura pertenezca a la carrera.
     * Si no pertenece, lanza una excepción EntityValidationException.
     *
     * @param idCarrera ID de la carrera
     * @param idAsignatura ID de la asignatura a validar
     */
    private void validarAsignatura(Long idCarrera, Long idAsignatura) {
        Carrera carrera = carreraService.obtenerPorId(idCarrera);
        boolean asignaturaPertenece = carrera.getAsignaturas().stream()
                .anyMatch(asig -> asig.getId().equals(idAsignatura));

        if (!asignaturaPertenece) {
            throw new EntityValidationException("La asignatura con ID " + idAsignatura + " no pertenece a la carrera con ID " + idCarrera);
        }
    }

    /**
     * Obtiene todas las comisiones asociadas a una asignatura específica.
     *
     * @param asignaturaId ID de la asignatura
     * @return Lista de comisiones asociadas a la asignatura
     */
    public List<ComisionResponseDTO> obtenerComisionesPorAsignatura(Long asignaturaId) {
        return comisionRepository.findByAsignaturaId(asignaturaId).stream()
                .map(this::comisionToComisionResponseDTO)
                .toList();
    }

    /**
     * Obtiene todas las comisiones asociadas a un profesor específico.
     *
     * @param profesorId ID del profesor
     * @return Lista de comisiones asociadas al profesor
     */
    public List<ComisionResponseDTO> obtenerComisionesPorProfesor(Long profesorId) {
        return comisionRepository.findByProfesorId(profesorId).stream()
                .map(this::comisionToComisionResponseDTO)
                .toList();
    }



}
