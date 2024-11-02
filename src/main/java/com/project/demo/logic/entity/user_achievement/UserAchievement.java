package com.project.demo.logic.entity.user_achievement;
import com.project.demo.logic.entity.user.User;
import com.project.demo.logic.entity.achievement.Achievement;
import jakarta.persistence.*;
import java.util.Date;
@Entity
@Table(name = "user_achievement")
public class UserAchievement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "achievement_id")
    private Achievement achievement;

    @Column(name = "achieved_at")
    private Date achievedAt;
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Achievement getAchievement() {
        return achievement;
    }

    public void setAchievement(Achievement achievement) {
        this.achievement = achievement;
    }

    public Date getAchievedAt() {
        return achievedAt;
    }

    public void setAchievedAt(Date achievedAt) {
        this.achievedAt = achievedAt;
    }
}
