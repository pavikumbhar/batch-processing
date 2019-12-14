package com.pavikumbhar.batchprocessing.writer;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pavikumbhar.batchprocessing.model.Product;

public interface ProductRepository extends JpaRepository<Product, String> {
}
