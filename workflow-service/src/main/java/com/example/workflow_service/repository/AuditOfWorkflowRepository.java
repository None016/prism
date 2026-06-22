package com.example.workflow_service.repository;


import com.example.workflow_service.entity.AuditOfWorkflow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AuditOfWorkflowRepository extends JpaRepository<AuditOfWorkflow, UUID> {

    List<AuditOfWorkflow> findByIdTicketOrderByTimeCreateDesc(UUID ticketId);

    List<AuditOfWorkflow> findByIdUser(UUID userId);
}