package com.project.demo.rest.game;

import com.project.demo.logic.entity.game.Game;
import com.project.demo.logic.entity.game.GameRepository;
import com.project.demo.logic.entity.game.StarsAssignment;
import com.project.demo.logic.entity.game.StarsAssignmentRepository;
import com.project.demo.logic.entity.user.User;
import com.project.demo.logic.entity.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/stars")
public class StarsAssignmentController {

    @Autowired
    private StarsAssignmentRepository starsAssignmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GameRepository gameRepository;

    private int calculateStarsBasedOnTime(Game game, int time) {
        if (time < 10) {
            return 5;
        } else if (time < 20) {
            return 3;
        }
        return 1;
    }

    @PostMapping("/assign")
    public ResponseEntity<String> assignStars(
            @RequestParam Long gameId,
            @RequestParam Long studentId,
            @RequestParam int time) {

        try {
            Optional<User> studentOptional = userRepository.findById(studentId);
            Optional<Game> gameOptional = gameRepository.findById(gameId);

            if (studentOptional.isPresent() && gameOptional.isPresent()) {
                Game game = gameOptional.get();
                User student = studentOptional.get();

                int newStars = calculateStarsBasedOnTime(game, time);
                final int MAX_STARS = 5;

                Optional<StarsAssignment> existingAssignment = starsAssignmentRepository.findByGameIdAndStudentId(gameId, studentId);
                if (existingAssignment.isPresent()) {
                    StarsAssignment assignment = existingAssignment.get();
                    if (assignment.getStars() < MAX_STARS && newStars > assignment.getStars()) {
                        assignment.setStars(Math.min(newStars, MAX_STARS));
                        starsAssignmentRepository.save(assignment);
                        return ResponseEntity.ok("Estrellas actualizadas exitosamente.");
                    }
                    return ResponseEntity.ok("");
                } else {
                    StarsAssignment assignment = new StarsAssignment();
                    assignment.setGame(game);
                    assignment.setStudent(student);
                    assignment.setStars(Math.min(newStars, MAX_STARS));
                    starsAssignmentRepository.save(assignment);
                    return ResponseEntity.ok("Estrellas asignadas exitosamente.");
                }
            } else {
                return ResponseEntity.badRequest().body("Error: Juego o estudiante no encontrado.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Ha ocurrido un error. Por favor, inténtelo nuevamente.");
        }
    }
}

