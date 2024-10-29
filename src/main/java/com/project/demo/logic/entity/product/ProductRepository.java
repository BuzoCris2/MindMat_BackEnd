package com.project.demo.logic.entity.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Buscar productos por una parte del nombre, ignorando mayúsculas y minúsculas
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE %?1%")
    List<Product> findProductsWithNameContaining(String character);

    // Buscar productos por nombre exacto
    @Query("SELECT p FROM Product p WHERE p.name = ?1")
    Optional<Product> findByName(String name);


    // Consulta para buscar productos por categoría
    List<Product> findByCategory_Name(String categoryName);
}