package com.project.demo.logic.entity.user_achievement;

import com.project.demo.logic.entity.achievement.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {
    // Método para obtener logros de un usuario con paginación
    Page<UserAchievement> findByUserId(Long userId, Pageable pageable);

    @Query("SELECT ua FROM UserAchievement ua WHERE ua.user.id = :userId AND ua.achievement.id = :achievementId")
    Optional<UserAchievement> findByUserIdAndAchievementId(@Param("userId") Long userId, @Param("achievementId") Long achievementId);

    @Query("SELECT ua.achievement FROM UserAchievement ua WHERE ua.achievement.id = :id")
    Optional<Achievement> findAchievementById(@Param("id") Long id);

}
