package com.example.authorization.repository;

import com.example.authorization.model.UserContractor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserContractorRepository extends JpaRepository<UserContractor, Integer> {

    @Query("SELECT uc.idContractor FROM UserContractor uc WHERE uc.idUser = :userId")
    List<Integer> findContractorIdsByUserId(@Param("userId") UUID userId);
}