// ticketservice/domain/repository/InstitutionRepository.java
package com.example.ticketservice.domain.repository;

import com.example.ticketservice.domain.entity.Institution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InstitutionRepository extends JpaRepository<Institution, Integer> {
}