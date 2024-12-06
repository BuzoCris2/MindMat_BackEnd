package com.project.demo.logic.entity.score;

import com.project.demo.logic.entity.game.Game;
import com.project.demo.logic.entity.user.User;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Time;
import java.util.Date;

@Entity
@Table(name = "score")
@NamedStoredProcedureQuery(
        name = "insertScore",
        procedureName = "insertScore",
        parameters = {
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_game_id", type = Long.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_obtained_at", type = Date.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_right_answers", type = Integer.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_time_taken", type = Time.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_user_id", type = Long.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_wrong_answers", type = Integer.class)
                //@StoredProcedureParameter(mode = ParameterMode.OUT, name = "calculated_stars", type = Integer.class)
        }
)
public class Score {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;
    @CreationTimestamp
    @Column(updatable = false, name = "obtained_at")
    private Date obtainedAt;
    @Column(name = "right_answers", nullable = true)
    private Integer rightAnswers;
    @Column(name = "stars")
    private Integer stars;
    @Column(name = "time_taken")
    private Time timeTaken;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    @Column(name = "wrong_answers")
    private Integer wrongAnswers;

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

    public Integer getRightAnswers() {
        return rightAnswers;
    }

    public void setRightAnswers(Integer rightAnswers) {
        this.rightAnswers = rightAnswers;
    }

    public Integer getStars() {
        return stars;
    }

    public void setStars(Integer stars) {
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

    public Integer getWrongAnswers() {
        return wrongAnswers;
    }

    public void setWrongAnswers(Integer wrongAnswers) {
        this.wrongAnswers = wrongAnswers;
    }
}
