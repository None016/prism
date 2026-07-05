package com.example.ticketservice.domain.repository;

import com.example.ticketservice.domain.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<Users, UUID> {

    Optional<Users> findByUuid(UUID uuid);

    Optional<Users> findByLogin(String login);

    @Query("SELECT u FROM Users u " +
            "JOIN UserContractor uc ON u.uuid = uc.userId " +
            "WHERE uc.contractorId = :contractorId " +
            "AND u.roleId IN (SELECT r.id FROM Role r WHERE r.nameRole = 'ROLE_EXECUTOR')")
    List<Users> findExecutorsByContractorId(@Param("contractorId") Integer contractorId);

    @Query("SELECT u FROM Users u WHERE u.roleId IN (SELECT r.id FROM Role r WHERE r.nameRole = 'ROLE_EXECUTOR')")
    List<Users> findAllExecutors();


    // ✅ НОВЫЙ МЕТОД: Получить institutionIds пользователя
    @Query("SELECT ui.institutionId FROM UserInstitution ui WHERE ui.userId = :userId")
    List<Integer> findInstitutionIdsByUserId(@Param("userId") UUID userId);

    // ✅ НОВЫЙ МЕТОД: Получить contractorIds пользователя
    @Query("SELECT uc.contractorId FROM UserContractor uc WHERE uc.userId = :userId")
    List<Integer> findContractorIdsByUserId(@Param("userId") UUID userId);
}