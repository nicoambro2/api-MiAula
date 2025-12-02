package com.apigestionespacios.apigestionespacios.service.reservaService;

import com.apigestionespacios.apigestionespacios.dtos.reserva.ReservaCreateDTO;
import com.apigestionespacios.apigestionespacios.dtos.reserva.ReservaResponseDTO;
import com.apigestionespacios.apigestionespacios.entities.Comision;
import com.apigestionespacios.apigestionespacios.entities.Espacio;
import com.apigestionespacios.apigestionespacios.entities.Reserva;
import com.apigestionespacios.apigestionespacios.entities.enums.DiaSemana;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReservaMapper {

    public Reserva toEntidad(ReservaCreateDTO dto, Espacio espacio, Comision comision) {
        DiaSemana dia = DiaSemana.desdeDayOfWeek(dto.getFechaInicio().getDayOfWeek());

        return Reserva.builder()
                .fechaInicio(dto.getFechaInicio())
                .fechaFin(dto.getFechaFin())
                .horaInicio(dto.getHoraInicio())
                .horaFin(dto.getHoraFin())
                .dia(dia)
                .espacio(espacio)
                .comision(comision)
                .build();
    }

    public ReservaResponseDTO toDTO(Reserva reserva) {
        return ReservaResponseDTO.builder()
                .id(reserva.getId())
                .fechaInicio(reserva.getFechaInicio())
                .fechaFin(reserva.getFechaFin())
                .dia(reserva.getDia())
                .horaInicio(reserva.getHoraInicio())
                .horaFin(reserva.getHoraFin())
                .nombreEspacio(reserva.getEspacio().getNombre())
                .nombreComision(reserva.getComision().getNombre())
                .nombreAsignatura(reserva.getComision().getAsignatura().getNombre())
                .nombreDocente(reserva.getComision().getProfesor().getNombre() + " " + reserva.getComision().getProfesor().getApellido())
                .build();
    }

    public List<ReservaResponseDTO> toDTOList(List<Reserva> reservas) {
        return reservas.stream()
                .map(this::toDTO)
                .toList();
    }

}
