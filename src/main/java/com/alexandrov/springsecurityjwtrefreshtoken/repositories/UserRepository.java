package com.alexandrov.springsecurityjwtrefreshtoken.repositories;

import com.alexandrov.springsecurityjwtrefreshtoken.model.entity.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User, Integer> {
    Optional<User> findByEmail(String email);
}
