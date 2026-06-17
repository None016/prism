package com.example.authorization.repository;

import com.example.authorization.model.UserInstitution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserInstitutionRepository extends JpaRepository<UserInstitution, Integer> {

    @Query("SELECT ui.institutionId FROM UserInstitution ui WHERE ui.userId = :userId")
    List<Integer> findInstitutionIdsByUserId(@Param("userId") UUID userId);
}