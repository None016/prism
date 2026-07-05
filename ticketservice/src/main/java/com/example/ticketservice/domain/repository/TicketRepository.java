package com.example.ticketservice.domain.repository;

import com.example.ticketservice.domain.entity.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, UUID>, JpaSpecificationExecutor<Ticket> {

    @Override
    @EntityGraph(attributePaths = {"typeTicket", "status", "institution"})
    Page<Ticket> findAll(Specification<Ticket> spec, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"typeTicket", "status", "institution"})
    Page<Ticket> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"typeTicket", "status", "institution"})
    Optional<Ticket> findByUuid(UUID uuid);

    @Query("SELECT t FROM Ticket t WHERE t.idContractor = :contractorId AND t.isDeleted = false")
    Page<Ticket> findByContractorId(@Param("contractorId") Integer contractorId, Pageable pageable);

    // ✅ Запрос для закрытых заявок
    @Query("""
        SELECT DISTINCT t FROM Ticket t
        WHERE (t.idInstitution IN :institutionIds OR t.uuid IN (
            SELECT ta.idTicket FROM TicketAssignment ta WHERE ta.idUser = :userId
        ))
        AND t.isDeleted = false
        AND t.status.id = 5
        AND (:priorityMin IS NULL OR t.priority >= :priorityMin)
        AND (:dateFrom IS NULL OR t.timeRequest >= :dateFrom)
        AND (:dateTo IS NULL OR t.timeRequest <= :dateTo)
        ORDER BY t.timeClosing DESC
    """)
    Page<Ticket> findUserClosedTickets(
            @Param("userId") UUID userId,
            @Param("institutionIds") List<Integer> institutionIds,
            @Param("priorityMin") Integer priorityMin,
            @Param("dateFrom") Instant dateFrom,
            @Param("dateTo") Instant dateTo,
            Pageable pageable
    );
}