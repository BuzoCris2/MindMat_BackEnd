package com.project.demo.logic.entity.user_listener;


import com.project.demo.logic.entity.user_achievement.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserListenerRepository extends JpaRepository<UserAchievement, Long> {

}
