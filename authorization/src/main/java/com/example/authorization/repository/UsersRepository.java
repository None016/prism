// authorization/repository/UsersRepository.java
package com.example.authorization.repository;

import com.example.authorization.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UsersRepository extends JpaRepository<Users, UUID> {

    Optional<Users> findByLogin(String login);

    Optional<Users> findByEmail(String email);

    boolean existsByLogin(String login);

    boolean existsByEmail(String email);

    // Загружаем пользователя вместе с контрагентами
    @Query("SELECT u FROM Users u LEFT JOIN FETCH u.contractors WHERE u.login = :login")
    Optional<Users> findByLoginWithContractors(@Param("login") String login);

    @Query("SELECT u FROM Users u LEFT JOIN FETCH u.contractors WHERE u.uuid = :uuid")
    Optional<Users> findByUuidWithContractors(@Param("uuid") UUID uuid);
}