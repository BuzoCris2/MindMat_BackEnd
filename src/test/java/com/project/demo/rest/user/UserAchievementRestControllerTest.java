package com.project.demo.rest.user;
import com.project.demo.logic.entity.achievement.Achievement;
import com.project.demo.logic.entity.achievement.AchievementRepository;
import com.project.demo.logic.entity.user.User;
import com.project.demo.logic.entity.user.UserRepository;
import com.project.demo.logic.entity.user_achievement.UserAchievement;
import com.project.demo.logic.entity.user_achievement.UserAchievementRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;


import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

public class UserAchievementRestControllerTest {

    @Mock
    private UserAchievementRepository userAchievementRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AchievementRepository achievementRepository;
    @InjectMocks
    private UserAchievementRestController userAchievementRestController;

    private User user;
    private Achievement achievement;
    private UserAchievement userAchievement;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Inicialización de objetos de prueba
        user = new User();
        user.setId(1L);
        user.setName("John");
        user.setLastname("Doe");

        achievement = new Achievement();
        achievement.setId(1L);
        achievement.setName("First Achievement");

        userAchievement = new UserAchievement();
        userAchievement.setUser(user);
        userAchievement.setAchievement(achievement);
    }

    @Test
    public void testGetAllAchievements() {
        Page<Achievement> achievementPage = new PageImpl<>(List.of(achievement));

        when(achievementRepository.findAll(any(Pageable.class))).thenReturn(achievementPage);

        // Mocking HttpServletRequest to return a valid URL
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost/achievements"));

        ResponseEntity<?> response = userAchievementRestController.getAll(1, 10, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    public void testGetUserAchievementsByUserId_UserExists() {
        Page<UserAchievement> userAchievementsPage = new PageImpl<>(List.of(userAchievement));

        when(userAchievementRepository.findByUserId(anyLong(), any(Pageable.class))).thenReturn(userAchievementsPage);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        // Mocking HttpServletRequest to return a valid URL
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost/achievements/1"));

        ResponseEntity<?> response = userAchievementRestController.getUserAchievementsByUserId(1L, 1, 10, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    public void testAddAchievementToUser_UserExistsAndAchievementExists() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(achievementRepository.findById(anyLong())).thenReturn(Optional.of(achievement));

        // Mocking HttpServletRequest to return a valid URL
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost/achievements/1"));

        ResponseEntity<?> response = userAchievementRestController.addAchievementToUser(1L, userAchievement, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(userAchievementRepository, times(1)).save(any(UserAchievement.class));
    }

    @Test
    public void testUpdateAchievementForUser_UserExistsAndAchievementExists() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(achievementRepository.findById(anyLong())).thenReturn(Optional.of(achievement));
        when(userAchievementRepository.findByUserIdAndAchievementId(anyLong(), anyLong())).thenReturn(Optional.of(userAchievement));

        // Mocking HttpServletRequest to return a valid URL
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost/achievements/1"));

        ResponseEntity<?> response = userAchievementRestController.updateAchievementForUser(1L, userAchievement, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userAchievementRepository, times(1)).save(any(UserAchievement.class));
    }

    @Test
    public void testDeleteAchievementForUser_UserExistsAndAchievementExists() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(achievementRepository.findById(anyLong())).thenReturn(Optional.of(achievement));
        when(userAchievementRepository.findByUserIdAndAchievementId(anyLong(), anyLong())).thenReturn(Optional.of(userAchievement));

        // Mocking HttpServletRequest to return a valid URL
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost/achievements/1"));

        ResponseEntity<?> response = userAchievementRestController.deleteAchievementForUser(1L, userAchievement, request);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(userAchievementRepository, times(1)).delete(any(UserAchievement.class));
    }
}
