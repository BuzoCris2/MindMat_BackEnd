package com.project.demo.logic.entity.mathleship;
import jakarta.persistence.*;

@Table(name = "mathleshipPowerUp")
@Entity
public class PowerUp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String effect;

    public PowerUp() {}
    public PowerUp(String name, String effect) {
        this.name = name;
        this.effect = effect;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getEffect() {
        return effect;
    }
    public void setEffect(String effect) {
        this.effect = effect;
    }
}
