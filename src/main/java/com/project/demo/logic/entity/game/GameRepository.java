package com.project.demo.logic.entity.game;

import com.project.demo.logic.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Date;
import java.util.Optional;
public interface GameRepository extends JpaRepository<Game, Long> {

}
