package com.pravinkumbhar.batchprocessing.classifier;

import org.springframework.batch.item.ItemWriter;
import org.springframework.classify.Classifier;

import com.pravinkumbhar.batchprocessing.model.Product;

/**
 *
 * @author Pravin Kumbhar
 */
@SuppressWarnings("serial")
public class ProductClassifier implements Classifier<Product, ItemWriter<? super Product>> {

    private ItemWriter<Product> insertJpaBatchItemWriter;
    private ItemWriter<Product> deleteJpaBatchItemWriter;
    
    public ProductClassifier(ItemWriter<Product> insertJpaBatchItemWriter, ItemWriter<Product> deleteJpaBatchItemWriter) {
        this.insertJpaBatchItemWriter = insertJpaBatchItemWriter;
        this.deleteJpaBatchItemWriter = deleteJpaBatchItemWriter;
    }
    
    @Override
    public ItemWriter<? super Product> classify(Product product) {
        if ("INSERT".equalsIgnoreCase(product.getOperation())) {
            return insertJpaBatchItemWriter;
        } else {
            return deleteJpaBatchItemWriter;
        }
        
    }
}