package com.example.workflow_service.repository;


import com.example.workflow_service.entity.TicketAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TicketAssignmentRepository extends JpaRepository<TicketAssignment, UUID> {

    Optional<TicketAssignment> findByIdTicket(UUID ticketId);

    void deleteByIdTicket(UUID ticketId);
}