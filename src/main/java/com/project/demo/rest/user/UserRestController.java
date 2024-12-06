package com.project.demo.rest.user;

import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
import com.project.demo.logic.entity.rol.Role;
import com.project.demo.logic.entity.rol.RoleEnum;
import com.project.demo.logic.entity.rol.RoleRepository;
import com.project.demo.logic.entity.user.User;
import com.project.demo.logic.entity.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;


import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserRestController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        Pageable pageable = PageRequest.of(page-1, size);
        Page<User> ordersPage = userRepository.findAll(pageable);
        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(ordersPage.getTotalPages());
        meta.setTotalElements(ordersPage.getTotalElements());
        meta.setPageNumber(ordersPage.getNumber() + 1);
        meta.setPageSize(ordersPage.getSize());

        return new GlobalResponseHandler().handleResponse("Order retrieved successfully",
                ordersPage.getContent(), HttpStatus.OK, meta);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'USER')")
    public User createUser(@RequestBody Map<String, Object> requestData) {
        // Extraer y validar los datos del JSON
        String roleName = (String) requestData.get("role");
        if (roleName == null || roleName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El campo 'role' es obligatorio");
        }

        RoleEnum roleEnum;
        try {
            roleEnum = RoleEnum.valueOf(roleName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rol inválido: " + roleName);
        }

        Optional<Role> optionalRole = roleRepository.findByName(roleEnum);
        if (optionalRole.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El rol especificado no existe");
        }

        // Crear el usuario basado en los datos del mapa
        var user = new User();
        user.setName((String) requestData.get("name"));
        user.setLastname((String) requestData.get("lastname"));
        user.setEmail((String) requestData.get("email"));
        user.setPassword(passwordEncoder.encode((String) requestData.get("password")));
        user.setRole(optionalRole.get());

        // Convertir valores numéricos explícitamente
        user.setActive(Integer.valueOf(requestData.get("active").toString()));
        user.setAvatarId(Integer.valueOf(requestData.get("avatarId").toString()));

        // Guardar y devolver el usuario creado
        return userRepository.save(user);
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> updateUser(@PathVariable Long userId, @RequestBody Map<String, Object> requestData, HttpServletRequest request) {
        Optional<User> optionalUser = userRepository.findById(userId);

        if (optionalUser.isPresent()) {
            User existingUser = optionalUser.get();

            // Actualizar campos según los valores del JSON
            if (requestData.containsKey("name")) {
                existingUser.setName((String) requestData.get("name"));
            }
            if (requestData.containsKey("lastname")) {
                existingUser.setLastname((String) requestData.get("lastname"));
            }
            if (requestData.containsKey("email")) {
                existingUser.setEmail((String) requestData.get("email"));
            }
            if (requestData.containsKey("password")) {
                existingUser.setPassword(passwordEncoder.encode((String) requestData.get("password")));
            }
            if (requestData.containsKey("active")) {
                existingUser.setActive(Integer.valueOf(requestData.get("active").toString()));
            }
            if (requestData.containsKey("avatarId")) {
                existingUser.setAvatarId(Integer.valueOf(requestData.get("avatarId").toString()));
            }
            if (requestData.containsKey("role")) {
                String roleName = (String) requestData.get("role");
                try {
                    RoleEnum roleEnum = RoleEnum.valueOf(roleName.toUpperCase());
                    Optional<Role> optionalRole = roleRepository.findByName(roleEnum);
                    if (optionalRole.isPresent()) {
                        existingUser.setRole(optionalRole.get());
                    } else {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El rol especificado no existe");
                    }
                } catch (IllegalArgumentException e) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rol inválido: " + roleName);
                }
            }

            // Guardar usuario actualizado
            userRepository.save(existingUser);

            return new GlobalResponseHandler().handleResponse(
                    "User updated successfully",
                    existingUser,
                    HttpStatus.OK,
                    request
            );
        } else {
            return new GlobalResponseHandler().handleResponse(
                    "User id " + userId + " not found",
                    HttpStatus.NOT_FOUND,
                    request
            );
        }
    }


    @PatchMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updatePartialAuthenticatedUser(@RequestBody Map<String, Object> updates, HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        updates.forEach((key, value) -> {
            try {
                switch (key) {
                    case "name":
                        user.setName((String) value);
                        break;
                    case "lastname":
                        user.setLastname((String) value);
                        break;
                    case "email":
                        user.setEmail((String) value);
                        break;
                    case "password":
                        if (value != null && !((String) value).isEmpty()) {
                            user.setPassword(passwordEncoder.encode((String) value));
                        }
                        break;
                    case "active":
                        user.setActive(Integer.parseInt(value.toString())); // Convierte a Integer si es necesario
                        break;
                    case "avatarId":
                        user.setAvatarId(Integer.parseInt(value.toString())); // Convierte a Integer si es necesario
                        break;
                    // Añadir otros campos si es necesario
                }
            } catch (ClassCastException | NumberFormatException e) {
                // Maneja errores de conversión aquí
                System.out.println("Error de conversión en el campo: " + key + " con valor: " + value);
            }
        });

        userRepository.save(user);
        return new GlobalResponseHandler().handleResponse("User profile updated successfully", user, HttpStatus.OK, request);
    }



    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId, HttpServletRequest request) {
        Optional<User> foundOrder = userRepository.findById(userId);
        if(foundOrder.isPresent()) {
            userRepository.deleteById(userId);
            return new GlobalResponseHandler().handleResponse("User deleted successfully",
                    foundOrder.get(), HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Order id " + userId + " not found"  ,
                    HttpStatus.NOT_FOUND, request);
        }
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public User authenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }

    @PutMapping("/{userId}/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> updateActiveStatus(
            @PathVariable Long userId,
            @RequestBody Map<String, Integer> requestBody,
            HttpServletRequest request) {
        // Buscar al usuario por ID
        Optional<User> optionalUser = userRepository.findById(userId);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            // Obtener el nuevo valor de "active" del request
            Integer newActiveStatus = requestBody.get("active");
            if (newActiveStatus == null) {
                return new GlobalResponseHandler().handleResponse(
                        "Field 'active' is required", HttpStatus.BAD_REQUEST, request);
            }

            // Actualizar el valor de active
            user.setActive(newActiveStatus);
            userRepository.save(user);

            return new GlobalResponseHandler().handleResponse(
                    "User active status updated successfully", user, HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse(
                    "User id " + userId + " not found", HttpStatus.NOT_FOUND, request);
        }
    }

    @GetMapping("/current")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "name", user.getName(),
                "role", user.getRole().getName()
        ));
    }

    @GetMapping("/teachers")
    public ResponseEntity<?> getAllTeachers() {
        List<User> teachers = userRepository.findAll().stream()
                .filter(user -> user.getRole().getName() == RoleEnum.ADMIN) // Comparar con RoleEnum.ADMIN
                .collect(Collectors.toList());
        return ResponseEntity.ok(teachers);
    }

}