package com.apigestionespacios.apigestionespacios.service.reservaService;

import com.apigestionespacios.apigestionespacios.dtos.reserva.ReservaCreateDTO;
import com.apigestionespacios.apigestionespacios.entities.Comision;
import com.apigestionespacios.apigestionespacios.entities.Espacio;
import com.apigestionespacios.apigestionespacios.entities.Laboratorio;
import com.apigestionespacios.apigestionespacios.entities.Reserva;
import com.apigestionespacios.apigestionespacios.exceptions.EntityValidationException;
import com.apigestionespacios.apigestionespacios.exceptions.ReservaSolapadaException;
import com.apigestionespacios.apigestionespacios.repository.ReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReservaValidator {

    private final ReservaRepository reservaRepository;

    @Autowired
    public ReservaValidator(ReservaRepository reservaRepository) {
        this.reservaRepository = reservaRepository;
    }

    public void validarNuevaReserva(ReservaCreateDTO reserva, Espacio espacio, Comision comision) throws EntityValidationException{
        if (comision.getAsignatura().getRequiereLaboratorio() && !(espacio instanceof Laboratorio)) {
            throw new EntityValidationException("El espacio solicitado debe ser un laboratorio para esta asignatura.");
        }

        if(espacio.getCapacidad() < comision.getCantidadAlumnos()) {
            throw new EntityValidationException("La cantidad de alumnos no puede ser mayor a la capacidad del espacio.");
        }

        if (reserva.getFechaFin().isBefore(reserva.getFechaInicio())) {
            throw new EntityValidationException("La fecha de fin no puede ser anterior a la de inicio.");
        }

        if (reserva.getHoraFin().isBefore(reserva.getHoraInicio())) {
            throw new EntityValidationException("La hora de fin no puede ser anterior a la de inicio.");
        }
    }

    /**
     * Verifica si una nueva reserva se solapa con reservas existentes.
     *
     * @param reserva Reserva a verificar.
     */
    public void existeSolapamiento(Reserva reserva) throws ReservaSolapadaException {
        List<Reserva> reservasExistentes = reservaRepository
                .findByEspacioIdAndDia(reserva.getEspacio().getId(), reserva.getDia());

        for (Reserva existente : reservasExistentes) {
            boolean fechasSeSolapan =
                    !(reserva.getFechaFin().isBefore(existente.getFechaInicio()) ||
                            reserva.getFechaInicio().isAfter(existente.getFechaFin()));

            boolean horasSeSolapan =
                    !(reserva.getHoraFin().isBefore(existente.getHoraInicio()) ||
                            reserva.getHoraInicio().isAfter(existente.getHoraFin()));

            if (fechasSeSolapan && horasSeSolapan) {
                throw new ReservaSolapadaException("El espacio ya está reservado en ese horario");
            }
        }
    }


}
