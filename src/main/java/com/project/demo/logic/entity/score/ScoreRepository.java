package com.project.demo.logic.entity.score;

import com.project.demo.logic.entity.user_achievement.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScoreRepository extends JpaRepository<Score, Long> {

}
