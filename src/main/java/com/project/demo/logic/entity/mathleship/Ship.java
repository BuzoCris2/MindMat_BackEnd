package com.project.demo.logic.entity.mathleship;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Table(name = "mathleshipShip")
@Entity
public class Ship {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private int size;
    private int hitCount;

    @OneToMany(mappedBy = "ship", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<GridCell> cellsOccupied = new ArrayList<>();

    public Ship() {}

    public Ship(String name, int size, int hitCount, List<GridCell> cellsOccupied) {
        this.name = name;
        this.size = size;
        this.hitCount = hitCount;
        this.cellsOccupied = cellsOccupied;
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
    public int getSize() {
        return size;
    }
    public void setSize(int size) {
        this.size = size;
    }
    public int getHitCount() {
        return hitCount;
    }
    public void setHitCount(int hitCount) {
        this.hitCount = hitCount;
    }
    public List<GridCell> getCellsOccupied() {
        return cellsOccupied;
    }
    public void setCellsOccupied(List<GridCell> cellsOccupied) {
        this.cellsOccupied = cellsOccupied;
    }
}

