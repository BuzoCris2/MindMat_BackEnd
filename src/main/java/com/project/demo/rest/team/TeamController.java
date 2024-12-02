package com.project.demo.rest.team;

import com.project.demo.logic.entity.rol.Role;
import com.project.demo.logic.entity.team.Team;
import com.project.demo.logic.entity.user.User;
import com.project.demo.logic.entity.team.TeamRepository;
import com.project.demo.logic.entity.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("teams")
public class TeamController {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Map<String, String>> createTeam(@RequestBody Team team, Authentication authentication) {
        User user = (User) authentication.getPrincipal();

        // Validar que el equipo tenga un docente líder asignado
        if (team.getTeacherLeader() == null || team.getTeacherLeader().getId() == null) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "El equipo debe tener un docente líder asignado.");
            return ResponseEntity.badRequest().body(response);
        }

        // Buscar al docente líder en la base de datos usando el ID
        User teacherLeader = userRepository.findById(team.getTeacherLeader().getId())
                .orElse(null);

        if (teacherLeader == null) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "El docente líder no existe.");
            return ResponseEntity.badRequest().body(response);
        }

        // Asignar el docente líder al equipo
        team.setTeacherLeader(teacherLeader);

        // Guardar el equipo en la base de datos
        teamRepository.save(team);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Equipo creado exitosamente.");
        return ResponseEntity.ok(response);
    }


    @PutMapping("/{id}/addStudent")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
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
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
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

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> getTeamDetails(@PathVariable Long id) {
        Optional<Team> teamOptional = teamRepository.findById(id);
        if (teamOptional.isPresent()) {
            Team team = teamOptional.get();

            // Construir la respuesta
            Map<String, Object> response = new HashMap<>();
            response.put("name", team.getName());
            response.put("description", team.getDescription());

            // Devuelve el nombre completo del Teacher Leader
            response.put("teacherLeader", Map.of(
                    "id", team.getTeacherLeader().getId(),
                    "name", team.getTeacherLeader().getName(),
                    "lastname", team.getTeacherLeader().getLastname(),
                    "email", team.getTeacherLeader().getEmail()
            ));

            response.put("members", team.getStudents()
                    .stream()
                    .map(student -> Map.of(
                            "id", student.getId(),
                            "name", student.getName(),
                            "lastname", student.getLastname(),
                            "email", student.getEmail()
                    ))
                    .toList()
            );

            return ResponseEntity.ok(response);
        }

        return ResponseEntity.status(404).body("Equipo no encontrado.");
    }

    @GetMapping("/{id}/students")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> getStudentsByTeam(@PathVariable Long id) {
        Optional<Team> teamOptional = teamRepository.findById(id);
        if (teamOptional.isPresent()) {
            Team team = teamOptional.get();

            // Crear una lista de nombres completos de los estudiantes
            List<String> studentNames = team.getStudents()
                    .stream()
                    .map(student -> student.getName() + " " + student.getLastname())
                    .toList();

            return ResponseEntity.ok(studentNames);
        }

        return ResponseEntity.status(404).body("Equipo no encontrado.");
    }

    @GetMapping("/byTeacher/{teacherId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> getTeamsByTeacherLeader(@PathVariable Long teacherId) {
        // Buscar equipos por ID del líder docente
        List<Team> teams = teamRepository.findByTeacherLeader_Id(teacherId);

        if (teams.isEmpty()) {
            return ResponseEntity.status(404).body("No se encontraron equipos para este docente líder.");
        }

        // Construir la respuesta incluyendo el id del equipo
        List<Map<String, Object>> response = teams.stream()
                .map(team -> Map.of(
                        "avatarId", team.getAvatarId(), // Asegúrate de que no sea nulo
                        "id", team.getId(),
                        "teacherLeader", team.getTeacherLeader() != null ? Map.of(
                                "id", team.getTeacherLeader().getId(),
                                "name", team.getTeacherLeader().getName() != null ? team.getTeacherLeader().getName() : "N/A",
                                "lastname", team.getTeacherLeader().getLastname() != null ? team.getTeacherLeader().getLastname() : "N/A",
                                "email", team.getTeacherLeader().getEmail() != null ? team.getTeacherLeader().getEmail() : "N/A"
                        ) : Map.of(),
                        "members", team.getStudents() != null ? team.getStudents().stream()
                                .map(student -> Map.of(
                                        "id", student.getId(),
                                        "name", student.getName() != null ? student.getName() : "N/A",
                                        "lastname", student.getLastname() != null ? student.getLastname() : "N/A",
                                        "email", student.getEmail() != null ? student.getEmail() : "N/A"
                                ))
                                .toList() : List.of(),
                        "name", team.getName() != null ? team.getName() : "N/A",
                        "description", team.getDescription() != null ? team.getDescription() : "N/A"
                ))
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/countByTeacher/{teacherId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> countTeamsByTeacherLeader(@PathVariable Long teacherId) {
        // Buscar equipos por ID del líder docente
        long teamCount = teamRepository.countByTeacherLeader_Id(teacherId);

        if (teamCount == 0) {
            return ResponseEntity.status(404).body("No se encontraron equipos para este docente líder.");
        }

        // Retornar el número de equipos
        return ResponseEntity.ok(Map.of("teacherId", teacherId, "teamCount", teamCount));
    }


    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> getAllTeams() {
        List<Team> teams = teamRepository.findAll();

        if (teams.isEmpty()) {
            return ResponseEntity.status(404).body("No se encontraron equipos.");
        }

        // Construir la respuesta
        List<Map<String, Object>> response = teams.stream()
                .map(team -> Map.of(
                        "avatarId", team.getAvatarId(),
                        "id", team.getId(),
                        "name", team.getName(),
                        "description", team.getDescription(),
                        "teacherLeader", Map.of(
                                "id", team.getTeacherLeader().getId(),
                                "name", team.getTeacherLeader().getName(),
                                "lastname", team.getTeacherLeader().getLastname(),
                                "email", team.getTeacherLeader().getEmail()
                        ),
                        "members", team.getStudents().stream()
                                .map(student -> Map.of(
                                        "id", student.getId(),
                                        "name", student.getName(),
                                        "lastname", student.getLastname(),
                                        "email", student.getEmail()
                                ))
                                .toList()
                ))
                .toList();

        return ResponseEntity.ok(response);
    }


}