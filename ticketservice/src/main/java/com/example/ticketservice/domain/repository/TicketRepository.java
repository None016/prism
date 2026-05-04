package com.example.ticketservice.domain.repository;

import com.example.ticketservice.domain.entity.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Репозиторий для работы с таблицей tickets.
 *
 * Наследуем два интерфейса:
 * 1. JpaRepository - дает базовые методы (save, findById, delete, findAll).
 * 2. JpaSpecificationExecutor - нужен для сложной динамической фильтрации (WHERE status = ? AND priority > ?).
 */
@Repository
public interface TicketRepository extends JpaRepository<Ticket, UUID>, JpaSpecificationExecutor<Ticket> {

    /**
     * Переопределяем метод findAll с Specification для устранения проблемы N+1
     * EntityGraph заставляет Hibernate загрузить typeTicket и status в одном запросе
     */
    @Override
    @EntityGraph(attributePaths = {"typeTicket", "status"})
    Page<Ticket> findAll(Specification<Ticket> spec, Pageable pageable);

    /**
     * Также оптимизируем обычный findAll для случаев без фильтрации
     */
    @Override
    @EntityGraph(attributePaths = {"typeTicket", "status"})
    Page<Ticket> findAll(Pageable pageable);
}