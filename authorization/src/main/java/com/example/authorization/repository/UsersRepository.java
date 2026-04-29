package com.example.authorization.repository;

import com.example.authorization.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UsersRepository extends JpaRepository<Users, UUID> {
    Optional<Users> findByLogin(String login);
    Optional<Users> findByEmail(String email);
    boolean existsByLogin(String login);
    boolean existsByEmail(String email);
}
