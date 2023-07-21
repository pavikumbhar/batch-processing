package com.pavikumbhar.batchprocessing.classifier;

import org.springframework.batch.item.ItemWriter;
import org.springframework.classify.Classifier;

import com.pavikumbhar.batchprocessing.model.Product;

/**
 *
 * @author pavikumbhar
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