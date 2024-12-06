package com.project.demo.rest.user;

import com.project.demo.logic.entity.rol.Role;
import com.project.demo.logic.entity.rol.RoleEnum;
import com.project.demo.logic.entity.rol.RoleRepository;
import com.project.demo.logic.entity.user.User;
import com.project.demo.logic.entity.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserRestControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private UserRestController userRestController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAll() {
        Pageable pageable = PageRequest.of(0, 10);
        User user = new User(); // Configura los valores según tu entidad
        Page<User> userPage = new PageImpl<>(List.of(user), pageable, 1);

        when(userRepository.findAll(pageable)).thenReturn(userPage);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost/users"));

        ResponseEntity<?> response = userRestController.getAll(1, 10, request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userRepository, times(1)).findAll(pageable);
    }

    @Test
    void testCreateUser() {
        User newUser = new User();
        newUser.setName("John");
        newUser.setPassword("password");

        Role role = new Role();
        role.setName(RoleEnum.USER);

        when(roleRepository.findByName(RoleEnum.USER)).thenReturn(Optional.of(role));
        when(passwordEncoder.encode(newUser.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        User createdUser = userRestController.createUser(newUser);

        assertNotNull(createdUser);
        assertEquals("John", createdUser.getName());
        verify(roleRepository, times(1)).findByName(RoleEnum.USER);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testUpdateUser_UserExists() {
        Long userId = 1L;
        User existingUser = new User();
        User updatedUser = new User();
        updatedUser.setPassword("newPassword");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.encode(updatedUser.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(updatedUser)).thenReturn(updatedUser);
        when(request.getMethod()).thenReturn("PUT");
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost/users/" + userId));

        ResponseEntity<?> response = userRestController.updateUser(userId, updatedUser, request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(updatedUser);
    }


    @Test
    void testAuthenticatedUser() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        User user = new User();

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user);
        SecurityContextHolder.setContext(securityContext);

        User authenticatedUser = userRestController.authenticatedUser();

        assertNotNull(authenticatedUser);
        assertEquals(user, authenticatedUser);
    }
}
