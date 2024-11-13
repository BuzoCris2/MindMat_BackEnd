package com.project.demo.logic.entity.game;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StarsAssignmentRepository extends JpaRepository<StarsAssignment, Long> {
    Optional<StarsAssignment> findByGameIdAndStudentId(Long gameId, Long studentId);
}

