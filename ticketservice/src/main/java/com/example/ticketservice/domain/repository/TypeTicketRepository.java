package com.example.ticketservice.domain.repository;

import com.example.ticketservice.domain.entity.TypeTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Репозиторий для работы с таблицей type_tickets.
 *
 * Spring Data JPA автоматически создаст реализацию этого интерфейса.
 * Мы получаем "бесплатные" методы: findById, save, findAll, deleteById и т.д.
 */
@Repository
public interface TypeTicketRepository extends JpaRepository<TypeTicket, Integer> {

    // === Базовые методы (уже реализованы Spring): ===
    // Optional<TypeTicket> findById(Integer id);
    // List<TypeTicket> findAll();
    // <S extends TypeTicket> S save(S entity);
    // void deleteById(Integer id);
    // boolean existsById(Integer id);

    // === Можно добавить кастомные методы, если нужно: ===
    // Например: найти тип по названию
    // Optional<TypeTicket> findByNameType(String nameType);
}