package com.apigestionespacios.apigestionespacios.config;

import com.apigestionespacios.apigestionespacios.dtos.usuario.UsuarioCreateDTO;
import com.apigestionespacios.apigestionespacios.entities.enums.Rol;
import com.apigestionespacios.apigestionespacios.repository.UsuarioRepository;
import com.apigestionespacios.apigestionespacios.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import com.apigestionespacios.apigestionespacios.entities.Usuario;

import java.util.Collections;


 /**Clase que se ejecuta al iniciar la aplicación para crear un usuario administrador por defecto.*/
 @RequiredArgsConstructor
 @Component
public class StartupAdminSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;

        @Override
        public void run(String... args) {
            // Verificamos si ya hay un admin
            if (usuarioRepository.count() == 0) {


                Usuario admin = Usuario.builder()
                        .nombre("Admin")
                        .apellido("Principal")
                        .email("admin@gmail.com")
                        .password("admin")
                        .rol(Rol.ADMIN)
                        .comisiones(Collections.emptyList())
                        .build();

                UsuarioCreateDTO usuarioCreateDTO = UsuarioCreateDTO.builder()
                        .nombre(admin.getNombre())
                        .apellido(admin.getApellido())
                        .email(admin.getUsername())
                        .password(admin.getPassword())
                        .rol(admin.getRol())
                        .build();

                usuarioService.crearUsuario(usuarioCreateDTO);
                System.out.println("🛡 Usuario administrador creado: admin/admin");
            }else {
                System.out.println("🛡 Ya existen usuarios en la Database, no se creó uno nuevo.");
            }
        }
    }

