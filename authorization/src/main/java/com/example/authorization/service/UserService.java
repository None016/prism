package com.example.authorization.service;

import com.example.authorization.model.Users;
import com.example.authorization.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UsersRepository usersRepository;

    public List<Users> findAll() {
        log.info("Finding all users");
        return usersRepository.findAll();
    }

    public Users findById(UUID id) {
        log.info("Finding user by id: {}", id);
        return usersRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    public Users findByLogin(String login) {
        log.info("Finding user by login: {}", login);
        return usersRepository.findByLogin(login)
                .orElseThrow(() -> new RuntimeException("User not found with login: " + login));
    }

    @Transactional
    public void delete(UUID id) {
        log.info("Deleting user by id: {}", id);
        if (!usersRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }
        usersRepository.deleteById(id);
        log.info("User deleted successfully: {}", id);
    }

    @Transactional
    public void updateRole(UUID userId, String roleName) {
        log.info("Updating role for user: {}", userId);
        Users user = findById(userId);
        // Здесь можно добавить логику обновления роли
        usersRepository.save(user);
        log.info("Role updated for user: {}", userId);
    }
}
