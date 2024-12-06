package com.project.demo.logic.entity.user_listener;

import com.project.demo.logic.entity.user.User;
import com.project.demo.logic.entity.user_achievement.UserAchievement;
import com.project.demo.logic.entity.user_achievement.UserAchievementRepository;
import jakarta.persistence.PostPersist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class UserListener {

    @Autowired
    private UserAchievementRepository userAchievementRepository;

    /*@PostPersist
    public void onPostPersist(User user) {
        UserAchievement userAchievement = new UserAchievement();
        userAchievement.setUser(user);
        userAchievement.setAchievedAt(new Date());
        userAchievementRepository.save(userAchievement);
    }*/
}
