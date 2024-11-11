package com.project.demo.rest.mathleship;

import com.project.demo.logic.entity.mathleship.*;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;


@RestController
@RequestMapping("/api/mathleship")
public class MathleshipRestController {

    @Autowired
    private MathleshipRepository mathleshipRepository;

    @Autowired
    private ShipRepository shipRepository;

    @Autowired
    private GridCellRepository gridCellRepository;



    private final int BOARD_SIZE = 6;
    private final List<Integer> SHIP_SIZES = List.of(4, 3, 2, 1);
    private final Random random = new Random();
    private final char[] COLUMNS = {'A', 'B', 'C', 'D', 'E', 'F'};

    @GetMapping("/initialize")
    public List<Ship> initializeBoard() {
        shipRepository.deleteAll();
        gridCellRepository.deleteAll();

        List<Ship> ships = new ArrayList<>();
        boolean[][] board = new boolean[BOARD_SIZE][BOARD_SIZE];

        for (int size : SHIP_SIZES) {
            Ship ship = new Ship();
            ship.setSize(size);
            ship.setHitCount(0);
            shipRepository.save(ship); // Guarda el ship primero

            boolean placed = false;

            // Intentar colocar el barco hasta que se encuentre una posición válida
            while (!placed) {
                boolean horizontal = random.nextBoolean();
                GridCell startPosition = getRandomStartPosition(size, horizontal);

                try {
                    if (canPlaceShip(board, startPosition, size, horizontal)) {
                        List<GridCell> cells = placeShipOnBoard(board, startPosition, size, horizontal);

                        // Asocia cada celda con el barco y guárdala
                        cells.forEach(cell -> {
                            cell.setShip(ship); // Asigna el barco a la celda
                            gridCellRepository.save(cell); // Guarda cada celda en la base de datos
                            ship.getCellsOccupied().add(cell); // Agrega la celda a la lista de celdas ocupadas del barco
                        });

                        shipRepository.save(ship); // Guarda el barco actualizado con sus celdas ocupadas
                        ships.add(ship);
                        placed = true; // Marca como colocado para salir del bucle
                    }
                } catch (IllegalStateException e) {
                    // Si la posición está ocupada, ignora la excepción y reintenta con una nueva posición
                    System.out.println("Posición ocupada, reintentando...");
                }
            }
        }
        return ships;
    }

    private GridCell getRandomStartPosition(int size, boolean horizontal) {
        int row = random.nextInt(horizontal ? BOARD_SIZE : BOARD_SIZE - size + 1);
        int col = random.nextInt(horizontal ? BOARD_SIZE - size + 1 : BOARD_SIZE);
        return new GridCell(row + 1, COLUMNS[col], 1, 0); // Ajusta para que comience en 1
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
            int row = start.getRow() - 1; // Ajuste para que comience en 0
            int colIndex = getColumnIndex(start.getColumn());
            if (horizontal) {
                colIndex += i;
            } else {
                row += i;
            }

            // Verificación para evitar la superposición
            if (board[row][colIndex]) {
                throw new IllegalStateException("La casilla (" + (row + 1) + ", " + COLUMNS[colIndex] + ") ya está ocupada por otro barco.");
            }

            // Marca la casilla como ocupada
            board[row][colIndex] = true;

            GridCell cell = new GridCell();
            cell.setRow(row + 1); // Ajuste para el frontend
            cell.setColumn(COLUMNS[colIndex]);
            cell.setHasShip(1);
            cell.setIsHit(0);

            cells.add(cell);
        }
        return cells;
    }

    @PostMapping("/attack")
    public ResponseEntity<?> attackCell(@RequestBody AttackRequest attackRequest, HttpServletRequest request) {
        int row = attackRequest.getRow();
        char column = attackRequest.getColumn();

        boolean isHit = checkHit(row, column);
        String message = isHit ? "Hit!" : "Miss!";

        return new ResponseEntity<>(Map.of(
                "message", message,
                "isHit", isHit
        ), HttpStatus.OK);
    }

    private boolean checkHit(int row, char column) {
        System.out.println("Checking hit for row: " + row + " and column: " + column);

        for (Ship ship : shipRepository.findAll()) {
            System.out.println("Checking ship with ID: " + ship.getId());
            for (GridCell cell : ship.getCellsOccupied()) {
                System.out.println("Checking against cell - Row: " + cell.getRow() + ", Column: " + cell.getColumn());
                // Asegúrate de que los valores de row y column están alineados.
                if (cell.getRow() == row && cell.getColumn() == column) {
                    System.out.println("Hit detected at row: " + row + " and column: " + column);
                    return true;
                }
            }
        }

        System.out.println("Miss detected!");
        return false;
    }

    // Clase interna para representar la solicitud de ataque
    private static class AttackRequest {
        private int row;
        private char column;

        public int getRow() { return row; }
        public void setRow(int row) { this.row = row; }

        public char getColumn() { return column; }
        public void setColumn(char column) { this.column = column; }
    }

    @PostMapping("/reset")
    public ResponseEntity<String> resetGame() {
        shipRepository.deleteAll();  // Eliminar todos los barcos
        gridCellRepository.deleteAll();  // Eliminar todas las celdas del tablero

        initializeBoard();  // Inicializar el tablero de nuevo

        return ResponseEntity.ok("Game reset successfully.");
    }

}
