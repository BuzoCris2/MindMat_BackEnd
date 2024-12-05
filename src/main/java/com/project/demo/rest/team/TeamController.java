package com.project.demo.rest.team;

import com.project.demo.logic.entity.rol.Role;
import com.project.demo.logic.entity.rol.RoleEnum;
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
import java.util.stream.Collectors;


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
        User currentUser = (User) authentication.getPrincipal();

        if (currentUser.getRole().getName() != RoleEnum.ADMIN && currentUser.getRole().getName() != RoleEnum.SUPER_ADMIN) {
            return ResponseEntity.status(403).body(Map.of("error", "No tienes permisos para crear equipos."));
        }
        Map<String, String> response = new HashMap<>();

        // Validar teacherLeader
        if (team.getTeacherLeader() == null || team.getTeacherLeader().getId() == null) {
            response.put("error", "El equipo debe tener un docente líder asignado.");
            return ResponseEntity.badRequest().body(response);
        }

        // Buscar el docente líder
        User teacherLeader = userRepository.findById(team.getTeacherLeader().getId())
                .orElse(null);

        if (teacherLeader == null || !teacherLeader.getRole().getName().equalsIgnoreCase("TEACHER")) {
            response.put("error", "El docente líder no existe o no tiene el rol adecuado.");
            return ResponseEntity.badRequest().body(response);
        }

        // Validar estudiantes
        if (team.getStudents() != null && !team.getStudents().isEmpty()) {
            List<Long> studentIds = team.getStudents().stream().map(User::getId).collect(Collectors.toList());
            List<User> validStudents = userRepository.findAllById(studentIds);

            if (validStudents.size() != studentIds.size()) {
                response.put("error", "Uno o más estudiantes no existen en la base de datos.");
                return ResponseEntity.badRequest().body(response);
            }
            team.setStudents(validStudents);
        }

        // Guardar el equipo
        team.setTeacherLeader(teacherLeader);
        teamRepository.save(team);

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
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getTeamsByTeacherLeader(@PathVariable Long teacherId, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();

        // Verifica si el usuario tiene permisos para ver todos los equipos
        if (currentUser.getRole().getName() == RoleEnum.ADMIN || currentUser.getRole().getName() == RoleEnum.SUPER_ADMIN) {
            List<Team> allTeams = teamRepository.findAll();

            List<Map<String, Object>> response = allTeams.stream()
                    .map(team -> {
                        Map<String, Object> teamMap = new HashMap<>();
                        teamMap.put("id", team.getId());
                        teamMap.put("name", team.getName());
                        teamMap.put("description", team.getDescription());
                        return teamMap;
                    })
                    .toList();

            return ResponseEntity.ok(response);
        }

        // Si es un usuario normal, filtra por su ID de líder docente
        List<Team> teams = teamRepository.findByTeacherLeader_Id(teacherId);

        if (teams.isEmpty()) {
            return ResponseEntity.status(404).body("No se encontraron equipos para este docente líder.");
        }

        List<Map<String, Object>> response = teams.stream()
                .map(team -> Map.of(
                        "id", team.getId(),
                        "name", team.getName(),
                        "description", team.getDescription(),
                        "avatarId", team.getAvatarId() != null ? team.getAvatarId() : 0,
                        "teacherLeader", team.getTeacherLeader() != null ? Map.of(
                                "name", team.getTeacherLeader().getName() != null ? team.getTeacherLeader().getName() : "N/A",
                                "lastname", team.getTeacherLeader().getLastname() != null ? team.getTeacherLeader().getLastname() : "N/A"
                        ) : null
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
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAllTeams(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();

        if (currentUser.getRole().getName() == RoleEnum.ADMIN || currentUser.getRole().getName() == RoleEnum.SUPER_ADMIN) {
            List<Team> allTeams = teamRepository.findAll();

            List<Map<String, Object>> response = allTeams.stream()
                    .map(team -> {
                        Map<String, Object> teamMap = new HashMap<>();
                        teamMap.put("id", team.getId());
                        teamMap.put("name", team.getName());
                        teamMap.put("description", team.getDescription());
                        return teamMap;
                    })
                    .toList();

            return ResponseEntity.ok(response);
        }

        // Si es un usuario normal, filtrar por equipos donde es docente líder
        List<Team> userTeams = teamRepository.findByTeacherLeader_Id(currentUser.getId());

        if (userTeams.isEmpty()) {
            return ResponseEntity.status(404).body("No se encontraron equipos.");
        }

        List<Map<String, Object>> response = userTeams.stream()
                .map(team -> {
                    Map<String, Object> teamMap = new HashMap<>();
                    teamMap.put("id", team.getId());
                    teamMap.put("name", team.getName());
                    teamMap.put("description", team.getDescription());
                    return teamMap;
                })
                .toList();

        return ResponseEntity.ok(response);
    }

}