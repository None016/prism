package com.example.ticketservice.domain.repository;

import com.example.ticketservice.domain.entity.Contractor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContractorRepository extends JpaRepository<Contractor, Integer> {
}