package com.apigestionespacios.apigestionespacios.dtos.usuario;

import com.apigestionespacios.apigestionespacios.entities.enums.Rol;
import com.apigestionespacios.apigestionespacios.exceptions.RolNoValidoException;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioCreateDTO {

    @Schema(description = "Nombre del usuario", example = "Daniel")
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 50, message = "El nombre debe tener máximo 50 caracteres")
    private String nombre;

    @Schema(description = "Apellido del usuario", example = "Diaz")
    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 50, message = "El apellido debe tener máximo 50 caracteres")
    private String apellido;

    @Schema(description = "Email del usuario para login", example = "daniel123@email.com")
    @NotBlank(message = "El email es obligatorio")
    @Size(max = 100, message = "El email debe tener máximo 100 caracteres")
    private String email;

    @Schema(description = "Contraseña para el usuario", example = "1234Abcd!")
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, max = 100, message = "La contraseña debe tener entre 6 y 100 caracteres")
    private String password;

    @Schema(description = "Rol asignado al usuario", example = "PROFESOR")
    @NotNull(message = "El rol es obligatorio")
    private Rol rol;

    public static Rol parseOrThrow(String rolStr) {
        try {
            return Rol.valueOf(rolStr.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new RolNoValidoException("Debe especificar un rol válido: " + rolStr);
        }
    }

}
