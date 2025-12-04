package com.apigestionespacios.apigestionespacios.dtos.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioUpdateDTO {
/* 
    @Schema(description = "Email actual para confirmar update", example = "daniel123@email.com")
    //@NotBlank(message = "Debe ingresar su email actual")
    private String currentEmail;

    @Schema(description = "Contraseña de usuario actual para confirmar update", example = "1234Abcd!")
    //@NotBlank(message = "Debe ingresar su contraseña actual")
    private String currentPassword;
*/
    @Schema(description = "Nombre del usuario", example = "Daniel")
    @Size(max = 50, min = 2, message = "El nombre debe tener máximo 50 caracteres")
    private String nombre;

    @Schema(description = "Apellido del usuario", example = "Diaz")
    @Size(max = 50, min = 2, message = "El apellido debe tener máximo 50 caracteres")
    private String apellido;

    @Schema(description = "Email del usuario para login", example = "daniel123@email.com")
    @Size(max = 100, message = "El email debe tener máximo 100 caracteres")
    @Email
    private String email;

    @Schema(description = "Contraseña para el usuario", example = "1234Abcd!")
    @Size(min = 6, max = 100, message = "La contraseña debe tener entre 6 y 100 caracteres")
    private String password;  // puede ser opcional para update
}
