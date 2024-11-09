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
    public User createUser(@RequestBody User newUser) {
        Optional<Role> optionalRole = roleRepository.findByName(RoleEnum.USER);

        if (optionalRole.isEmpty()) {
            return null;
        }

        var user = new User();
        user.setName(newUser.getName());
        user.setLastname(newUser.getLastname());
        user.setEmail(newUser.getEmail());
        user.setPassword(passwordEncoder.encode(newUser.getPassword()));
        user.setRole(optionalRole.get());
        user.setActive(newUser.getActive());
        user.setAvatarId(newUser.getAvatarId());


        return userRepository.save(user);
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> updateUser(@PathVariable Long userId, @RequestBody Map<String, Object> updatedUserData, HttpServletRequest request) {
        Optional<User> foundUser = userRepository.findById(userId);

        if (foundUser.isPresent()) {
            User user = foundUser.get();

            // Actualizar los atributos básicos
            user.setName((String) updatedUserData.get("name"));
            user.setLastname((String) updatedUserData.get("lastname"));
            user.setEmail((String) updatedUserData.get("email"));

            // Actualizar la contraseña solo si se proporciona una nueva
            if (updatedUserData.get("password") != null && !((String) updatedUserData.get("password")).isEmpty()) {
                user.setPassword(passwordEncoder.encode((String) updatedUserData.get("password")));
            }

            user.setActive((Integer) updatedUserData.get("active"));
            user.setAvatarId((Integer) updatedUserData.get("avatarId"));

            // Asignación dinámica del rol
            if (updatedUserData.get("role") != null) {
                String roleName = (String) updatedUserData.get("role");
                Optional<Role> role = roleRepository.findByName(RoleEnum.valueOf(roleName.toUpperCase()));
                if (role.isPresent()) {
                    user.setRole(role.get());
                } else {
                    return new GlobalResponseHandler().handleResponse("Invalid role provided", HttpStatus.BAD_REQUEST, request);
                }
            }

            userRepository.save(user);
            return new GlobalResponseHandler().handleResponse("User updated successfully", user, HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("User id " + userId + " not found", HttpStatus.NOT_FOUND, request);
        }
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateAuthenticatedUser(@RequestBody Map<String, Object> updatedUserData, HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        // Actualizar los atributos básicos
        user.setName((String) updatedUserData.get("name"));
        user.setLastname((String) updatedUserData.get("lastname"));
        user.setEmail((String) updatedUserData.get("email"));

        // Actualizar la contraseña solo si se proporciona una nueva
        if (updatedUserData.get("password") != null && !((String) updatedUserData.get("password")).isEmpty()) {
            user.setPassword(passwordEncoder.encode((String) updatedUserData.get("password")));
        }

        user.setActive((Integer) updatedUserData.get("active"));
        user.setAvatarId((Integer) updatedUserData.get("avatarId"));

        userRepository.save(user);
        return new GlobalResponseHandler().handleResponse("User profile updated successfully", user, HttpStatus.OK, request);
    }

    @PatchMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updatePartialAuthenticatedUser(@RequestBody Map<String, Object> updates, HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        updates.forEach((key, value) -> {
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
                    if (value != null) {
                        user.setActive(Integer.parseInt(value.toString()));
                    }
                    break;
                case "avatarId":
                    if (value != null) {
                        user.setAvatarId(Integer.parseInt(value.toString()));
                    }
                    break;
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
    public ResponseEntity<User> authenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(user);
    }


}