package com.alexandrov.springsecurityjwtrefreshtoken.util;

import com.alexandrov.springsecurityjwtrefreshtoken.model.entity.Product;
import com.alexandrov.springsecurityjwtrefreshtoken.repositories.ProductRepository;
import jakarta.persistence.Column;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class DbInitializer {
    private final ProductRepository productRepository;

    @Autowired
    public DbInitializer(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @EventListener(ApplicationStartedEvent.class)
    public void fillDbWithProducts() {
        Optional<Product> product1 = productRepository.findByName("Iphone 15");
        if (product1.isEmpty()) {
            productRepository.save(Product
                    .builder()
                    .name("Iphone 15")
                    .price(799.0)
                    .build());
        }

        Optional<Product> product2 = productRepository.findByName("Iphone 14");
        if (product2.isEmpty()) {
            productRepository.save(Product
                    .builder()
                    .name("Iphone 14")
                    .price(699.0)
                    .build());
        }
    }
}
