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
        newScore.getUser().setPassword(null);
        newScore.setStars(scoreRepository.insertScore(newScore.getGame().getId(), newScore.getObtainedAt(), newScore.getRightAnswers(), newScore.getTimeTaken(),newScore.getUser().getId(), newScore.getWrongAnswers()));
        return newScore;
    }
}

