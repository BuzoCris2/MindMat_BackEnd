package com.project.demo.logic.entity.score;

import com.project.demo.logic.entity.score.Score;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.sql.Time;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
@Repository
public interface ScoreRepository extends JpaRepository<Score, Long> {
    @Procedure(name = "insertScore")
     Integer insertScore(
            @Param("p_game_id") Long gameId,
            @Param("p_obtained_at") Date obtainedAt,
            @Param("p_right_answers") Integer rightAnswers,
            @Param("p_time_taken") Time timeTaken,
            @Param("p_user_id") Long userId,
            @Param("p_wrong_answers") Integer wrongAnswers
    );
}
