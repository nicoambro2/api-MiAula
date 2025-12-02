package com.apigestionespacios.apigestionespacios.dtos.usuario;

import com.apigestionespacios.apigestionespacios.entities.enums.Rol;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioResponseDTO {

    @Schema(description = "ID único del usuario", example = "1")
    private Long id;

    @Schema(description = "Nombre del usuario", example = "Daniel")
    private String nombre;

    @Schema(description = "Apellido del usuario", example = "Diaz")
    private String apellido;

    @Schema(description = "Nombre de usuario para login", example = "daniel123@email.com")
    private String email;

    @Schema(description = "Rol asignado al usuario", example = "PROFESOR")
    private Rol rol;
}