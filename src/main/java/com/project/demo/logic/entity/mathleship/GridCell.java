package com.project.demo.logic.entity.mathleship;
import jakarta.persistence.*;

@Table(name = "mathleshipGridCell")
@Entity
public class GridCell {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int row;
    private char column;
    private int hasShip;
    private int isHit;

    public GridCell() {}

    public GridCell(int row, char column, int hasShip, int isHit) {
        this.row = row;
        this.column = column;
        this.hasShip = hasShip;
        this.isHit = isHit;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public int getRow() {
        return row;
    }
    public void setRow(int row) {
        this.row = row;
    }
    public char getColumn() {
        return column;
    }
    public void setColumn(char column) {
        this.column = column;
    }
    public int getHasShip() {
        return hasShip;
    }
    public void setHasShip(int hasShip) {
        this.hasShip = hasShip;
    }
    public int getIsHit() {
        return isHit;
    }
    public void setIsHit(int isHit) {
        this.isHit = isHit;
    }
}
