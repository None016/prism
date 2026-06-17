// authorization/repository/ContractorRepository.java
package com.example.authorization.repository;

import com.example.authorization.model.Contractor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContractorRepository extends JpaRepository<Contractor, Integer> {
}