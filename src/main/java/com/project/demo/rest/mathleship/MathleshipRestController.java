package com.project.demo.rest.mathleship;

import com.project.demo.logic.entity.mathleship.Mathleship;
import com.project.demo.logic.entity.mathleship.MathleshipRepository;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
import com.project.demo.logic.entity.mathleship.GridCell;
import com.project.demo.logic.entity.mathleship.Ship;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


@RestController
@RequestMapping("/api/mathleship")
public class MathleshipRestController {

    @Autowired
    private MathleshipRepository mathleshipRepository;


    private final int BOARD_SIZE = 6;
    private final List<Integer> SHIP_SIZES = List.of(4, 3, 2, 1);
    private final Random random = new Random();
    private final char[] COLUMNS = {'A', 'B', 'C', 'D', 'E', 'F'};

    @GetMapping("/initialize")
    public List<Ship> initializeBoard() {
        List<Ship> ships = new ArrayList<>();
        boolean[][] board = new boolean[BOARD_SIZE][BOARD_SIZE];

        for (int size : SHIP_SIZES) {
            Ship ship = new Ship();
            ship.setSize(size);
            ship.setHitCount(0);
            boolean placed = false;

            while (!placed) {
                boolean horizontal = random.nextBoolean();
                GridCell startPosition = getRandomStartPosition(size, horizontal);

                if (canPlaceShip(board, startPosition, size, horizontal)) {
                    List<GridCell> cells = placeShipOnBoard(board, startPosition, size, horizontal);
                    ship.setCellsOccupied(cells);
                    ships.add(ship);
                    placed = true;
                }
            }
        }
        return ships;
    }

    private GridCell getRandomStartPosition(int size, boolean horizontal) {
        int row = random.nextInt(horizontal ? BOARD_SIZE : BOARD_SIZE - size + 1);
        int col = random.nextInt(horizontal ? BOARD_SIZE - size + 1 : BOARD_SIZE);
        return new GridCell(row, COLUMNS[col], 1, 0);
    }

    private boolean canPlaceShip(boolean[][] board, GridCell start, int size, boolean horizontal) {
        for (int i = 0; i < size; i++) {
            int row = start.getRow();
            int colIndex = getColumnIndex(start.getColumn());
            if (horizontal) {
                colIndex += i;
            } else {
                row += i;
            }
            if (row >= BOARD_SIZE || colIndex >= BOARD_SIZE || board[row][colIndex]) {
                return false;
            }
        }
        return true;
    }

    private int getColumnIndex(char column) {
        return column - 'A';
    }

    private List<GridCell> placeShipOnBoard(boolean[][] board, GridCell start, int size, boolean horizontal) {
        List<GridCell> cells = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            int row = start.getRow();
            int colIndex = getColumnIndex(start.getColumn());
            if (horizontal) {
                colIndex += i;
            } else {
                row += i;
            }
            board[row][colIndex] = true;
            cells.add(new GridCell(row, COLUMNS[colIndex], 1, 0));
        }
        return cells;
    }
}
