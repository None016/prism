package com.example.ticketservice.domain.repository;

import com.example.ticketservice.domain.entity.StatusTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Репозиторий для работы с таблицей status_tickets.
 *
 * Spring Data JPA автоматически создаст реализацию этого интерфейса.
 */
@Repository
public interface StatusTicketRepository extends JpaRepository<StatusTicket, Integer> {

    // === Базовые методы (уже реализованы Spring): ===
    // Optional<StatusTicket> findById(Integer id);
    // List<StatusTicket> findAll();
    // <S extends StatusTicket> S save(S entity);
    // void deleteById(Integer id);
    // boolean existsById(Integer id);

    // === Можно добавить кастомные методы, если нужно: ===
    // Например: найти статус по названию
    // Optional<StatusTicket> findByNameType(String nameType);
}