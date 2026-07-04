package com.example.ticketservice.domain.repository;

import com.example.ticketservice.domain.entity.TicketAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TicketAssignmentRepository extends JpaRepository<TicketAssignment, UUID> {

    List<TicketAssignment> findByTicketUuid(UUID ticketId);

    @Query("SELECT ta FROM TicketAssignment ta WHERE ta.idTicket = :ticketId AND ta.idUser = :userId")
    Optional<TicketAssignment> findByTicketUuidAndUserUuid(
            @Param("ticketId") UUID ticketId,
            @Param("userId") UUID userId);

    @Query("SELECT COUNT(ta) > 0 FROM TicketAssignment ta WHERE ta.idTicket = :ticketId AND ta.idUser = :userId")
    boolean existsByTicketUuidAndUserUuid(@Param("ticketId") UUID ticketId, @Param("userId") UUID userId);

    @Query("SELECT COUNT(ta) FROM TicketAssignment ta WHERE ta.idUser = :userId")
    long countByUserId(@Param("userId") UUID userId);

    // ✅ ИСПРАВЛЕННЫЙ запрос: используем IN (2, 3) вместо AND
    @Query("""
        SELECT COUNT(ta) 
        FROM TicketAssignment ta 
        WHERE ta.idUser = :userId
        AND ta.idTicket IN (
            SELECT t.uuid FROM Ticket t 
            WHERE t.status.id IN (2, 3)
            AND t.isDeleted = false
        )
    """)
    long countActiveTicketsByUserId(@Param("userId") UUID userId);

    @Query("SELECT ta FROM TicketAssignment ta " +
            "JOIN Ticket t ON ta.idTicket = t.uuid " +
            "WHERE t.idContractor = :contractorId")
    List<TicketAssignment> findAllByContractorId(@Param("contractorId") Integer contractorId);

    @Modifying
    @Query("DELETE FROM TicketAssignment ta WHERE ta.idTicket = :ticketId AND ta.idUser = :userId")
    void deleteByTicketUuidAndUserUuid(@Param("ticketId") UUID ticketId, @Param("userId") UUID userId);
}