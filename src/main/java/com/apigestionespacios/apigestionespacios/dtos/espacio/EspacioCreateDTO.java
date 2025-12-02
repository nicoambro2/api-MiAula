package com.apigestionespacios.apigestionespacios.dtos.espacio;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EspacioCreateDTO {

    @Schema(description = "Nombre del espacio físico (aula, laboratorio, etc.)", example = "Aula 101")
    @NotNull(message = "El nombre del espacio no puede ser nulo")
    private String nombre;

    @Schema(description = "Capacidad máxima de personas en el espacio", example = "40")
    @NotNull(message = "La capacidad del espacio no puede ser nula")
    private Integer capacidad;

    @Schema(description = "Indica si el espacio tiene proyector")
    private Boolean tieneProyector;

    @Schema(description = "Indica si el espacio tiene TV")
    private Boolean tieneTV;

    @Schema(description = "Tipo de espacio: 'AULA' o 'LABORATORIO'", example = "AULA")
    @NotNull(message = "El tipo de espacio no puede ser nulo")
    private String tipo; // "AULA" o "LABORATORIO"

    @Schema(description = "Cantidad de computadoras (solo para laboratorios, null para aulas)", example = "20")
    private Integer computadoras;
}
