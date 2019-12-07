package com.pravinkumbhar.batchprocessing.writer;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pravinkumbhar.batchprocessing.model.Product;

public interface ProductRepository extends JpaRepository<Product, String> {
}
