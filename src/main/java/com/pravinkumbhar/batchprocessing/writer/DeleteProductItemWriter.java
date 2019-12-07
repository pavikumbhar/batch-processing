package com.pravinkumbhar.batchprocessing.writer;

import java.util.List;

import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.pravinkumbhar.batchprocessing.model.Product;

@Transactional
public class DeleteProductItemWriter implements ItemWriter<Product> {
    
    @Autowired
    private ProductRepository repository;
    
    @Override
    public void write(List<? extends Product> items) throws Exception {
        for (Product item : items) {
            repository.deleteById(item.getId());
        }
        
        repository.flush();
    }
}