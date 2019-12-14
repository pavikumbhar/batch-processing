package com.pavikumbhar.batchprocessing.classifier;

import org.springframework.classify.annotation.Classifier;

import com.pavikumbhar.batchprocessing.model.Product;

public class ProductRouterClassifier {

    @Classifier
    public String classify(Product classifiable) {
        return classifiable.getOperation();
    }
}