package com.project.demo.rest.game;

import com.project.demo.logic.entity.game.Game;
import com.project.demo.logic.entity.game.GameRepository;
import com.project.demo.rest.game.GameRestController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class GameRestControllerTest {

    private MockMvc mockMvc;

    @Mock
    private GameRepository gameRepository;

    @InjectMocks
    private GameRestController gameRestController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(gameRestController).build();
    }

    @Test
    public void testGetAllGames() throws Exception {
        // Arrange: Creamos una lista de juegos mockeada
        Game game1 = new Game(1L, "Game 1", "Description 1");
        Game game2 = new Game(2L, "Game 2", "Description 2");
        List<Game> games = Arrays.asList(game1, game2);

        // Mock el comportamiento del repository
        when(gameRepository.findAll()).thenReturn(games);

        // Act & Assert: Realizamos la solicitud GET y verificamos la respuesta
        mockMvc.perform(get("/games"))
                .andExpect(status().isOk())  // Verifica que el status sea 200 OK
                .andExpect(jsonPath("$[0].name").value("Game 1"))  // Verifica el nombre del primer juego
                .andExpect(jsonPath("$[1].name").value("Game 2")); // Verifica el nombre del segundo juego

        // Verifica que el repositorio fue llamado una vez
        verify(gameRepository, times(1)).findAll();
    }

    @Test
    public void testAddGame() throws Exception {
        // Arrange: Creamos un nuevo juego
        Game newGame = new Game(3L, "New Game", "New Game Description");

        // Mock el comportamiento del repository
        when(gameRepository.save(any(Game.class))).thenReturn(newGame);

        // Act & Assert: Realizamos la solicitud POST y verificamos la respuesta
        mockMvc.perform(post("/games")
                        .contentType("application/json")
                        .content("{ \"name\": \"New Game\", \"description\": \"New Game Description\" }"))
                .andExpect(status().isOk())  // Verifica que el status sea 200 OK
                .andExpect(jsonPath("$.name").value("New Game"))  // Verifica el nombre del juego agregado
                .andExpect(jsonPath("$.description").value("New Game Description"));  // Verifica la descripción

        // Verifica que el repositorio fue llamado una vez con el nuevo juego
        verify(gameRepository, times(1)).save(any(Game.class));
    }

}
