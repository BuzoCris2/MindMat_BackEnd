package com.project.demo.logic.entity.mathleship;

import com.project.demo.logic.entity.category.Category;
import com.project.demo.logic.entity.game.Game;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Table(name = "mathleship")
@Entity
public class Mathleship extends Game {
    private int remainingTime;

    public Mathleship() {}

    public Mathleship(int remainingTime) {
        this.remainingTime = remainingTime;
    }

    public int getRemainingTime() {
        return remainingTime;
    }

    public void setRemainingTime(int remainingTime) {
        this.remainingTime = remainingTime;
    }
}
