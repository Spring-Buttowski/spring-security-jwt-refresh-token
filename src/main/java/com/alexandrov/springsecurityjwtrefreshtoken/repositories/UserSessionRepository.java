package com.alexandrov.springsecurityjwtrefreshtoken.repositories;

import com.alexandrov.springsecurityjwtrefreshtoken.model.entity.User;
import com.alexandrov.springsecurityjwtrefreshtoken.model.entity.UserSession;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface UserSessionRepository extends CrudRepository<UserSession, Integer> {
    Optional<UserSession> findUserSessionByUser( User user);

    void deleteByRefreshToken(String refreshToken);
}
