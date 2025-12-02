package com.apigestionespacios.apigestionespacios.service;

import com.apigestionespacios.apigestionespacios.dtos.espacio.EspacioCreateDTO;
import com.apigestionespacios.apigestionespacios.dtos.espacio.EspacioResponseDTO;
import com.apigestionespacios.apigestionespacios.dtos.espacio.EspacioUpdateDTO;
import com.apigestionespacios.apigestionespacios.dtos.reserva.ReservaResponseDTO;
import com.apigestionespacios.apigestionespacios.entities.Aula;
import com.apigestionespacios.apigestionespacios.entities.Espacio;
import com.apigestionespacios.apigestionespacios.entities.Laboratorio;
import com.apigestionespacios.apigestionespacios.exceptions.ResourceConflictException;
import com.apigestionespacios.apigestionespacios.exceptions.ResourceNotFoundException;
import com.apigestionespacios.apigestionespacios.repository.EspacioRepository;
import com.apigestionespacios.apigestionespacios.service.reservaService.ReservaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EspacioService {
    private final EspacioRepository espacioRepository;
    private final ReservaService reservaService;

    @Autowired
    public EspacioService(EspacioRepository espacioRepository, @Lazy ReservaService reservaService) {
        this.espacioRepository = espacioRepository;
        this.reservaService = reservaService;
    }

    public static Espacio espacioCreateDtoToEspacio(EspacioCreateDTO dto) {
        if ("LABORATORIO".equalsIgnoreCase(dto.getTipo())) {
            return Laboratorio.builder()
                    .computadoras(dto.getComputadoras()) // Solo para Laboratorio
                    .nombre(dto.getNombre())
                    .capacidad(dto.getCapacidad())
                    .tieneProyector(dto.getTieneProyector())
                    .tieneTV(dto.getTieneTV())
                    .build();
        } else if ("AULA".equalsIgnoreCase(dto.getTipo())) {
            return Aula.builder()
                    .nombre(dto.getNombre())
                    .capacidad(dto.getCapacidad())
                    .tieneProyector(dto.getTieneProyector())
                    .tieneTV(dto.getTieneTV())
                    .build();
        } else {
            throw new IllegalArgumentException("Tipo de espacio no válido: " + dto.getTipo());
        }
    }

    public static EspacioResponseDTO espacioToEspacioResponseDTO(Espacio espacio) {
        String tipo = espacio instanceof Laboratorio ? "LABORATORIO" :
                espacio instanceof Aula ? "AULA" : "DESCONOCIDO";

        Integer computadoras = espacio instanceof Laboratorio laboratorio
                ? laboratorio.getComputadoras()
                : null;

        return EspacioResponseDTO.builder()
                .id(espacio.getId())
                .nombre(espacio.getNombre())
                .capacidad(espacio.getCapacidad())
                .tieneProyector(espacio.getTieneProyector())
                .tieneTV(espacio.getTieneTV())
                .tipo(tipo)
                .computadoras(computadoras)
                .build();
    }



    public List<EspacioResponseDTO> obtenerTodos() {
        return espacioRepository.findAll().stream()
                .map(EspacioService::espacioToEspacioResponseDTO)
                .toList();
    }

    public Espacio obtenerPorId(Long id) {
        return espacioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Espacio no encontrado con ID: " + id));
    }

    public EspacioResponseDTO obtenerDTOPorId(Long id) {
        return espacioRepository.findById(id)
                .map(EspacioService::espacioToEspacioResponseDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Espacio no encontrado con ID: " + id));
    }

    public EspacioResponseDTO obtenerPorNombre(String nombre) {
        return espacioRepository.findByNombre(nombre)
                .map(EspacioService::espacioToEspacioResponseDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Espacio no encontrado con nombre: " + nombre));
    }

    public List<ReservaResponseDTO> obtenerReservasPorEspacio(Long espacioId) {
        return obtenerPorId(espacioId).getReservas().stream()
                .map(reservaService::reservaToReservaResponseDTO)
                .toList();
    }

    public List<EspacioResponseDTO> obtenerPorCapacidadMayorA(Integer capacidad) {
        return espacioRepository.findByCapacidadGreaterThanEqual(capacidad)
                .stream()
                .map(EspacioService::espacioToEspacioResponseDTO)
                .toList();
    }

    public List<EspacioResponseDTO> obtenerConProyector() {
        return espacioRepository.findByTieneProyectorTrue()
                .stream()
                .map(EspacioService::espacioToEspacioResponseDTO)
                .toList();
    }

    public List<EspacioResponseDTO> obtenerConTV() {
        return espacioRepository.findByTieneTVTrue()
                .stream()
                .map(EspacioService::espacioToEspacioResponseDTO)
                .toList();
    }

    public Espacio guardar(EspacioCreateDTO espacioDTO) {
        // Validación de tipo
        String tipo = espacioDTO.getTipo();
        if (tipo == null || (!tipo.equalsIgnoreCase("AULA") && !tipo.equalsIgnoreCase("LABORATORIO"))) {
            throw new IllegalArgumentException("El tipo de espacio debe ser 'AULA' o 'LABORATORIO'");
        }

        // Validación de nombre duplicado
        if (espacioRepository.existsByNombre(espacioDTO.getNombre())) {
            throw new ResourceConflictException("Ya existe un espacio con ese nombre");
        }

        // Validación de capacidad
        if (espacioDTO.getCapacidad() == null || espacioDTO.getCapacidad() <= 0) {
            throw new IllegalArgumentException("La capacidad debe ser mayor a 0");
        }

        Espacio nuevoEspacio = espacioCreateDtoToEspacio(espacioDTO);

        return espacioRepository.save(nuevoEspacio);
    }


    public Espacio actualizar(EspacioUpdateDTO nuevoEspacio) {
        Espacio existente = obtenerPorId(nuevoEspacio.getId());

        if (!existente.getNombre().equals(nuevoEspacio.getNombre()) && espacioRepository.existsByNombre(nuevoEspacio.getNombre())) {
            throw new ResourceConflictException("Ya existe otro espacio con ese nombre");
        }

        if (nuevoEspacio.getCapacidad() == null || nuevoEspacio.getCapacidad() <= 0) {
            throw new IllegalArgumentException("La capacidad debe ser mayor a 0");
        }

        existente.setNombre(nuevoEspacio.getNombre());
        existente.setCapacidad(nuevoEspacio.getCapacidad());
        existente.setTieneProyector(nuevoEspacio.getTieneProyector() != null ? nuevoEspacio.getTieneProyector() : false);
        existente.setTieneTV(nuevoEspacio.getTieneTV() != null ? nuevoEspacio.getTieneTV() : false);

        if (existente instanceof Laboratorio laboratorio) {
            laboratorio.setComputadoras(nuevoEspacio.getComputadoras() != null ? nuevoEspacio.getComputadoras() : 0);
        }

        return espacioRepository.save(existente);
    }

    public void eliminar(Long id) {
        if(!espacioRepository.existsById(id)){
            throw new ResourceNotFoundException("Espacio no encontrado");
        }
        espacioRepository.deleteById(id);
    }

}
