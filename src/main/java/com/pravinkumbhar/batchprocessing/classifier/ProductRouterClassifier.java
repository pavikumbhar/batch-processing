package com.pravinkumbhar.batchprocessing.classifier;

import org.springframework.classify.annotation.Classifier;

import com.pravinkumbhar.batchprocessing.model.Product;

public class ProductRouterClassifier {

    @Classifier
    public String classify(Product classifiable) {
        return classifiable.getOperation();
    }
}