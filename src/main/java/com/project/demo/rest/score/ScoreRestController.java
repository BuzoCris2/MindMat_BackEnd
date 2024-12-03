package com.project.demo.rest.score;
import com.project.demo.logic.entity.game.Game;
import com.project.demo.logic.entity.game.GameRepository;
import com.project.demo.logic.entity.score.Score;
import com.project.demo.logic.entity.score.ScoreRepository;
import com.project.demo.logic.entity.user.User;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.HashSet;


@Transactional
@RestController
@RequestMapping("/score")
public class ScoreRestController {
    @Autowired
    private ScoreRepository scoreRepository;
    @Autowired
    private GameRepository gameRepository;
    @PostMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN', 'SUPER_ADMIN')")
    public Score insertScore(@RequestBody Score newScore, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        newScore.setUser(user);

        System.out.println("=== Datos enviados al procedimiento ===");
        System.out.println("Game ID: " + newScore.getGame().getId());
        System.out.println("Obtained At: " + newScore.getObtainedAt());
        System.out.println("Right Answers: " + newScore.getRightAnswers());
        System.out.println("Time Taken: " + newScore.getTimeTaken());
        System.out.println("User ID: " + newScore.getUser().getId());
        System.out.println("Wrong Answers: " + newScore.getWrongAnswers());

        newScore.getUser().setPassword(null);
        newScore.setStars(scoreRepository.insertScore(newScore.getGame().getId(), newScore.getObtainedAt(), newScore.getRightAnswers(), newScore.getTimeTaken(),newScore.getUser().getId(), newScore.getWrongAnswers()));

        /*int stars = scoreRepository.insertScore(
                newScore.getGame().getId(),
                newScore.getObtainedAt(),
                newScore.getRightAnswers(),
                newScore.getTimeTaken(),
                newScore.getUser().getId(),
                newScore.getWrongAnswers()
        );*/

        //System.out.println("Stars Calculadas: " + stars);

        // Crear respuesta
        //Map<String, Object> response = new HashMap<>();
        //response.put("stars", stars);
        //response.put("message", "Score registrado exitosamente");

        return newScore;
    }

    @GetMapping("/achievements")
    @PreAuthorize("hasAnyRole('USER','ADMIN', 'SUPER_ADMIN')")
    public List<Map<String, Object>> getAchievements(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        List<Object[]> scores = scoreRepository.findMaxStarsByUser(user.getId());
        List<Game> allGames = gameRepository.findAll();

        if (scores.isEmpty()) {
            return new ArrayList<>(); // O devuelve un mensaje personalizado si es necesario
        }

        List<Map<String, Object>> achievements = new ArrayList<>();

        Set<Long> playedGameIds = scores.stream()
                .map(score -> (Long) score[0])
                .collect(Collectors.toSet());

        for (Object[] score : scores) {
            Map<String, Object> achievement = new HashMap<>();
            achievement.put("gameId", score[0]); // ID del juego
            achievement.put("gameName", score[1]); // Nombre del juego
            achievement.put("stars", score[2]); // Máxima cantidad de estrellas
            achievements.add(achievement);
        }

        for (Game game : allGames) {
            if (!playedGameIds.contains(game.getId())) {
                Map<String, Object> achievement = new HashMap<>();
                achievement.put("gameId", game.getId()); // ID del juego
                achievement.put("gameName", game.getName()); // Nombre del juego
                achievement.put("stars", 0); // Sin estrellas
                achievements.add(achievement);
            }
        }

        return achievements;
    }

}

