package com.project.demo.logic.entity.achievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AchievementRepository extends JpaRepository<Achievement, Long> {
    @Query("SELECT a FROM Achievement a JOIN UserAchievement ua ON a.id = ua.achievement.id WHERE ua.user.id = :userId")
    List<Achievement> findAchievementsByUserId(@Param("userId") Long userId);
}

