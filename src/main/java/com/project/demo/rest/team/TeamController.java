package com.project.demo.rest.team;

import com.project.demo.logic.entity.rol.Role;
import com.project.demo.logic.entity.team.Team;
import com.project.demo.logic.entity.user.User;
import com.project.demo.logic.entity.team.TeamRepository;
import com.project.demo.logic.entity.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/create")
    public ResponseEntity<String> createTeam(@RequestBody Team team, Authentication authentication) {
        User user = (User) authentication.getPrincipal();

        // Verificar si el usuario tiene el rol de "docente"
        if (!user.getRole().getName().equalsIgnoreCase("docente")) {
            return ResponseEntity.status(403).body("No tienes autorización para crear equipos.");
        }

        // Validar que el equipo tenga un docente líder asignado
        if (team.getTeacherLeader() == null) {
            return ResponseEntity.badRequest().body("Error: El equipo debe tener un docente líder asignado.");
        }
        teamRepository.save(team);
        return ResponseEntity.ok("Equipo creado exitosamente.");
    }

    @PutMapping("/{id}/addStudent")
    public ResponseEntity<String> addStudentToTeam(@PathVariable Long id, @RequestBody User student, Authentication authentication) {
        User user = (User) authentication.getPrincipal();

        Optional<Team> teamOptional = teamRepository.findById(id);
        if (teamOptional.isPresent()) {
            Team team = teamOptional.get();

            // Verificar que el usuario que realiza la acción es el docente líder del equipo
            if (!team.getTeacherLeader().getId().equals(user.getId())) {
                return ResponseEntity.status(403).body("Error: Solo el docente líder puede gestionar el equipo.");
            }

            // Asegurarse de que el estudiante existe antes de añadirlo
            Optional<User> studentOptional = userRepository.findById(student.getId());
            if (studentOptional.isPresent()) {
                team.getStudents().add(studentOptional.get());
                teamRepository.save(team);
                return ResponseEntity.ok("Estudiante añadido al equipo.");
            } else {
                return ResponseEntity.badRequest().body("Error: Estudiante no encontrado.");
            }
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}/removeStudent")
    public ResponseEntity<String> removeStudentFromTeam(@PathVariable Long id, @RequestBody User student, Authentication authentication) {
        User user = (User) authentication.getPrincipal();

        Optional<Team> teamOptional = teamRepository.findById(id);
        if (teamOptional.isPresent()) {
            Team team = teamOptional.get();

            // Verificar que el usuario que realiza la acción es el docente líder del equipo
            if (!team.getTeacherLeader().getId().equals(user.getId())) {
                return ResponseEntity.status(403).body("Error: Solo el docente líder puede gestionar el equipo.");
            }

            // Asegurarse de que el estudiante está en el equipo antes de eliminarlo
            if (team.getStudents().removeIf(s -> s.getId().equals(student.getId()))) {
                teamRepository.save(team);
                return ResponseEntity.ok("Estudiante eliminado del equipo.");
            } else {
                return ResponseEntity.badRequest().body("Error: El estudiante no pertenece al equipo.");
            }
        }
        return ResponseEntity.notFound().build();
    }
}
