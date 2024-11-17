package com.project.demo.logic.entity.team;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    List<Team> findByTeacherLeader_Id(Long teacherLeaderId);

    long countByTeacherLeader_Id(Long teacherId);
}
