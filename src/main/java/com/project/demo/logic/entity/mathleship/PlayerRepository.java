package com.project.demo.logic.entity.mathleship;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface PlayerRepository extends JpaRepository<Player, Long> { }
