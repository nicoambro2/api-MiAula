package com.apigestionespacios.apigestionespacios.service;

import com.apigestionespacios.apigestionespacios.dtos.usuario.UsuarioCreateDTO;
import com.apigestionespacios.apigestionespacios.dtos.usuario.UsuarioResponseDTO;
import com.apigestionespacios.apigestionespacios.dtos.usuario.UsuarioUpdateDTO;
import com.apigestionespacios.apigestionespacios.entities.Usuario;
import com.apigestionespacios.apigestionespacios.entities.enums.Rol;
import com.apigestionespacios.apigestionespacios.exceptions.ResourceConflictException;
import com.apigestionespacios.apigestionespacios.exceptions.ResourceNotFoundException;
import com.apigestionespacios.apigestionespacios.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.apigestionespacios.apigestionespacios.dtos.usuario.UsuarioCreateDTO.parseOrThrow;

@Service
public class UsuarioService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;

    }

    private Usuario usuarioCreateDTOtoUsuario(UsuarioCreateDTO dto) {
        return Usuario.builder()
                .nombre(dto.getNombre())
                .apellido(dto.getApellido())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .rol(parseOrThrow(dto.getRol().toString()))
                .build();
    }

    private UsuarioResponseDTO usuarioToUsuarioResponseDTO(Usuario usuario) {
        return UsuarioResponseDTO.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .apellido(usuario.getApellido())
                .email(usuario.getUsername())
                .rol(usuario.getRol())
                .build();
    }

    /**
     * Obtiene todos los usuarios y los convierte a DTOs de respuesta.
     *
     * @return lista de UsuarioResponseDTO con todos los usuarios.
     */
    public List<UsuarioResponseDTO> obtenerTodosDTO() {
        List<Usuario> usuarios = usuarioRepository.findAll();
        return usuarios.stream()
                .map(this::usuarioToUsuarioResponseDTO)
                .toList();
    }

    public Usuario obtenerPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));
    }

    /**
     * Obtiene un usuario por su ID y lo convierte a DTO de respuesta.
     *
     * @param id el ID del usuario a buscar.
     * @return UsuarioResponseDTO correspondiente al usuario.
     * @throws ResourceNotFoundException si no existe el usuario con ese ID.
     */
    public UsuarioResponseDTO obtenerPorIdDTO(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));
        return usuarioToUsuarioResponseDTO(usuario);
    }

    public UsuarioResponseDTO obtenerPorUsername(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + email));
        return this.usuarioToUsuarioResponseDTO(usuario);
    }

    /**
     * Obtiene un usuario por su username y lo convierte a DTO de respuesta.
     *
     * @param email el username del usuario a buscar.
     * @return UsuarioResponseDTO correspondiente al usuario.
     * @throws ResourceNotFoundException si no existe el usuario con ese username.
     */
    public UsuarioResponseDTO obtenerPorUsernameDTO(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con username: " + email));
        return usuarioToUsuarioResponseDTO(usuario);
    }

    /**
     * Guarda un nuevo usuario a partir de un DTO de creación.
     *
     * @param dto DTO con los datos para crear un usuario.
     * @return UsuarioResponseDTO con los datos del usuario guardado.
     * @throws ResourceConflictException si ya existe un usuario con el mismo
     *                                   username.
     */
    public Usuario crearUsuario(UsuarioCreateDTO dto) {
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new ResourceConflictException("Ya existe un usuario registrado con este Email");
        }

        Usuario usuario = usuarioCreateDTOtoUsuario(dto);

        return usuarioRepository.save(usuario);
    }

    /**
     * Actualiza un usuario existente a partir de un DTO de actualización.
     *
     * @param id  ID del usuario a actualizar.
     * @param dto DTO con los datos para actualizar el usuario.
     * @return UsuarioResponseDTO con los datos del usuario actualizado.
     * @throws ResourceNotFoundException si no existe el usuario con ese ID.
     * @throws ResourceConflictException si el nuevo username ya está en uso por
     *                                   otro usuario.
     */
    public UsuarioResponseDTO actualizarUsuario(Long id, UsuarioUpdateDTO dto) {
        Usuario existente = obtenerPorId(id);

        // Actualizar solo campos no nulos
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            if (usuarioRepository.existsByEmail(dto.getEmail())) {
                throw new ResourceConflictException("Ya existe un usuario registrado con este email");
            } else {
                existente.setEmail(dto.getEmail());
            }
        }

        if (dto.getNombre() != null && !dto.getNombre().isBlank()) {
            existente.setNombre(dto.getNombre());
        }
        if (dto.getApellido() != null && !dto.getApellido().isBlank()) {
            existente.setApellido(dto.getApellido());
        }
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            existente.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        Usuario guardado = usuarioRepository.save(existente);
        return usuarioToUsuarioResponseDTO(guardado);
    }

    public void eliminar(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuario no encontrado");
        }

        if (obtenerPorId(id).getRol() == Rol.ADMIN) {
            throw new ResourceConflictException("No se puede eliminar un usuario con rol ADMIN");
        }
        usuarioRepository.deleteById(id);
    }

    /**
     * Obtiene una lista de usuarios filtrados por rol y los convierte a DTOs de
     * respuesta.
     *
     * @param rol el rol por el cual filtrar los usuarios.
     * @return lista de UsuarioResponseDTO con usuarios que tengan el rol
     *         especificado.
     */
    public List<UsuarioResponseDTO> obtenerPorRolDTO(Rol rol) {
        List<Usuario> usuarios = usuarioRepository.findByRol(rol);
        return usuarios.stream()
                .map(this::usuarioToUsuarioResponseDTO)
                .toList();
    }

    public UsuarioResponseDTO modificarRolUsuario(Long id, String nuevoRol) {
        Usuario usuario = obtenerPorId(id);

        usuario.setRol(parseOrThrow(nuevoRol));
        Usuario actualizado = usuarioRepository.save(usuario);

        return usuarioToUsuarioResponseDTO(actualizado);
    }

    @Override
    public Usuario loadUserByUsername(String email) throws UsernameNotFoundException {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));
    }

}
