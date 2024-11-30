package com.project.demo.rest.score;
import com.project.demo.logic.entity.game.GameRepository;
import com.project.demo.logic.entity.score.Score;
import com.project.demo.logic.entity.score.ScoreRepository;
import com.project.demo.logic.entity.user.User;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Transactional
@RestController
@RequestMapping("/score")
public class ScoreRestController {
    @Autowired
    private ScoreRepository scoreRepository;
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

        int stars = scoreRepository.insertScore(
                newScore.getGame().getId(),
                newScore.getObtainedAt(),
                newScore.getRightAnswers(),
                newScore.getTimeTaken(),
                newScore.getUser().getId(),
                newScore.getWrongAnswers()
        );

        System.out.println("Stars Calculadas: " + stars);

        // Crear respuesta
        Map<String, Object> response = new HashMap<>();
        response.put("stars", stars);
        response.put("message", "Score registrado exitosamente");

        return newScore;
    }
}

