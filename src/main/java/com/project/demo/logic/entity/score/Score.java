package com.project.demo.logic.entity.score;

import com.project.demo.logic.entity.game.Game;
import com.project.demo.logic.entity.user.User;
import jakarta.persistence.*;

import java.sql.Time;
import java.util.Date;

@Entity
@Table(name = "score")

public class Score {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;
    @Column(name = "obtained_at", nullable = false)
    private Date obtainedAt;
    @Column(name = "right_answers", nullable = true)
    private Long rightAnswers;
    @Column(name = "stars", nullable = false)
    private Long stars;
    @Column(name = "time_taken", nullable = true)
    private Time timeTaken;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(name = "wrong_answers", nullable = true)
    private Long wrongAnswers;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public Date getObtainedAt() {
        return obtainedAt;
    }

    public void setObtainedAt(Date obtainedAt) {
        this.obtainedAt = obtainedAt;
    }

    public Long getRightAnswers() {
        return rightAnswers;
    }

    public void setRightAnswers(Long rightAnswers) {
        this.rightAnswers = rightAnswers;
    }

    public Long getStars() {
        return stars;
    }

    public void setStars(Long stars) {
        this.stars = stars;
    }

    public Time getTimeTaken() {
        return timeTaken;
    }

    public void setTimeTaken(Time timeTaken) {
        this.timeTaken = timeTaken;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Long getWrongAnswers() {
        return wrongAnswers;
    }

    public void setWrongAnswers(Long wrongAnswers) {
        this.wrongAnswers = wrongAnswers;
    }
}
