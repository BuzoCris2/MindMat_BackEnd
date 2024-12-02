package com.project.demo.rest.user;

import com.project.demo.logic.entity.achievement.Achievement;
import com.project.demo.logic.entity.achievement.AchievementRepository;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
import com.project.demo.logic.entity.user.User;
import com.project.demo.logic.entity.user.UserRepository;
import com.project.demo.logic.entity.user_achievement.UserAchievement;
import com.project.demo.logic.entity.user_achievement.UserAchievementRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/achievements")
public class UserAchievementRestController {
    @Autowired
    private UserAchievementRepository userAchievementRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AchievementRepository achievementRepository;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Achievement> achievementsPage = achievementRepository.findAll(pageable);

        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(achievementsPage.getTotalPages());
        meta.setTotalElements(achievementsPage.getTotalElements());
        meta.setPageNumber(achievementsPage.getNumber() + 1);
        meta.setPageSize(achievementsPage.getSize());

        return new GlobalResponseHandler().handleResponse(
                "Achievements retrieved successfully",
                achievementsPage.getContent(),
                HttpStatus.OK,
                meta
        );
    }

    @GetMapping("/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getUserAchievementsByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        // Crear objeto Pageable para la paginación
        Pageable pageable = PageRequest.of(page - 1, size);

        // Obtener la página de UserAchievement asociada al usuario
        Page<UserAchievement> userAchievementsPage = userAchievementRepository.findByUserId(userId, pageable);

        if (userAchievementsPage.isEmpty()) {
            return new GlobalResponseHandler().handleResponse(
                    "No achievements found for the given user ID", HttpStatus.NOT_FOUND, request);
        }

        // Configuramos la metadata para la paginación
        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(userAchievementsPage.getTotalPages());
        meta.setTotalElements(userAchievementsPage.getTotalElements());
        meta.setPageNumber(userAchievementsPage.getNumber() + 1);
        meta.setPageSize(userAchievementsPage.getSize());

        // Retornar la respuesta con los datos y metadata
        return new GlobalResponseHandler().handleResponse(
                "User achievements retrieved successfully",
                userAchievementsPage.getContent(),
                HttpStatus.OK,
                meta
        );
    }


    // Crear un nuevo logro para un usuario
    @PostMapping("/{userId}")
    public ResponseEntity<?> addAchievementToUser(
            @PathVariable Long userId,
            @RequestBody UserAchievement newAchievement,
            HttpServletRequest request) {
        Optional<User> user = userRepository.findById(userId);

        if (user.isEmpty()) {
            return new GlobalResponseHandler().handleResponse(
                    "User not found", HttpStatus.NOT_FOUND, request);
        }

        Optional<Achievement> achievement = achievementRepository.findById(newAchievement.getAchievement().getId());

        if (achievement.isEmpty()) {
            return new GlobalResponseHandler().handleResponse(
                    "Achievement not found", HttpStatus.NOT_FOUND, request);
        }

        UserAchievement userAchievement = new UserAchievement();
        userAchievement.setUser(user.get());
        userAchievement.setAchievement(achievement.get());
        userAchievement.setAchievedAt(newAchievement.getAchievedAt());

        userAchievementRepository.save(userAchievement);

        return new GlobalResponseHandler().handleResponse(
                "Achievement added successfully", userAchievement, HttpStatus.CREATED, request);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<?> updateAchievementForUser(
            @PathVariable Long userId,
            @RequestBody UserAchievement updatedAchievement,
            HttpServletRequest request) {
        // Verificar si el usuario existe
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            return new GlobalResponseHandler().handleResponse(
                    "User not found", HttpStatus.NOT_FOUND, request);
        }

        // Verificar si el logro (achievement) existe en la base de datos
        Optional<Achievement> achievement = achievementRepository.findById(updatedAchievement.getAchievement().getId());
        if (achievement.isEmpty()) {
            return new GlobalResponseHandler().handleResponse(
                    "Achievement not found", HttpStatus.NOT_FOUND, request);
        }

        // Verificar si el logro está asociado con este usuario
        Optional<UserAchievement> userAchievement = userAchievementRepository.findByUserIdAndAchievementId(userId, updatedAchievement.getAchievement().getId());
        if (userAchievement.isEmpty()) {
            return new GlobalResponseHandler().handleResponse(
                    "Achievement not found for this user", HttpStatus.NOT_FOUND, request);
        }

        // Actualizar el logro
        UserAchievement existingAchievement = userAchievement.get();
        existingAchievement.setAchievedAt(updatedAchievement.getAchievedAt());

        // Guardar la actualización
        userAchievementRepository.save(existingAchievement);

        return new GlobalResponseHandler().handleResponse(
                "Achievement updated successfully", existingAchievement, HttpStatus.OK, request);
    }


    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteAchievementForUser(
            @PathVariable Long userId,
            @RequestBody UserAchievement achievementToDelete,
            HttpServletRequest request) {
        // Verificar si el usuario existe
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            return new GlobalResponseHandler().handleResponse(
                    "User not found", HttpStatus.NOT_FOUND, request);
        }

        // Verificar si el logro existe en la base de datos
        Optional<Achievement> achievement = achievementRepository.findById(achievementToDelete.getAchievement().getId());
        if (achievement.isEmpty()) {
            return new GlobalResponseHandler().handleResponse(
                    "Achievement not found", HttpStatus.NOT_FOUND, request);
        }

        // Verificar si el logro está asociado con este usuario
        Optional<UserAchievement> userAchievement = userAchievementRepository.findByUserIdAndAchievementId(userId, achievementToDelete.getAchievement().getId());
        if (userAchievement.isEmpty()) {
            return new GlobalResponseHandler().handleResponse(
                    "Achievement not found for this user", HttpStatus.NOT_FOUND, request);
        }

        // Eliminar el logro
        userAchievementRepository.delete(userAchievement.get());

        return new GlobalResponseHandler().handleResponse(
                "Achievement deleted successfully", HttpStatus.NO_CONTENT, request);
    }

}
