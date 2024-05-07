package com.alexandrov.springsecurityjwtrefreshtoken.repositories;

import com.alexandrov.springsecurityjwtrefreshtoken.model.entity.Product;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends CrudRepository<Product, Integer> {
    Optional<Product> findByName(String name);
}
