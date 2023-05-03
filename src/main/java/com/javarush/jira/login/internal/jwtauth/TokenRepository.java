package com.javarush.jira.login.internal.jwtauth;

import com.javarush.jira.common.BaseRepository;
import com.javarush.jira.login.internal.jwtauth.model.Token;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
public interface TokenRepository extends BaseRepository<Token> {

    @Query("SELECT t FROM Token t INNER JOIN User u ON t.user.id = u.id WHERE u.id = :id AND (t.expired = FALSE OR t.revoked = FALSE)")
    List<Token> findAllValidTokenByUser(@Param("id") Long id);

    Optional<Token> findByToken(String jwt);
}
