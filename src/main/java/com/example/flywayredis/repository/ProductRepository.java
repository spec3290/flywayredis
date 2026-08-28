package com.example.flywayredis.repository;

import com.example.flywayredis.entity.Product;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
