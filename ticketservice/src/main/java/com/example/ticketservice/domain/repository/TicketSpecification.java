package com.example.ticketservice.domain.repository;

import com.example.ticketservice.domain.entity.Ticket;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
public class TicketSpecification {

    public static Specification<Ticket> filter(
            Integer statusId,
            Integer typeId,
            Integer institutionId,  // НОВЫЙ ПАРАМЕТР
            UUID executorId,        // НОВЫЙ ПАРАМЕТР
            Instant dateFrom,
            Instant dateTo,
            Integer priorityMin
    ) {
        return (root, query, criteriaBuilder) -> {
            // Добавляем DISTINCT и FETCH для устранения N+1 проблемы
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                query.distinct(true);
                // Принудительно загружаем связанные сущности в одном запросе
                root.fetch("typeTicket", JoinType.LEFT);
                root.fetch("status", JoinType.LEFT);
            }

            List<Predicate> predicates = new ArrayList<>();

            if (statusId != null) {
                predicates.add(criteriaBuilder.equal(root.get("status").get("id"), statusId));
            }

            if (typeId != null) {
                predicates.add(criteriaBuilder.equal(root.get("typeTicket").get("id"), typeId));
            }

            // НОВАЯ ФИЛЬТРАЦИЯ ПО УЧРЕЖДЕНИЮ
            if (institutionId != null) {
                predicates.add(criteriaBuilder.equal(root.get("idInstitution"), institutionId));
            }

            // НОВАЯ ФИЛЬТРАЦИЯ ПО ИСПОЛНИТЕЛЮ
            if (executorId != null) {
                // TODO: Когда добавится поле executor в таблицу tickets
                // predicates.add(criteriaBuilder.equal(root.get("executorId"), executorId));

                // ВРЕМЕННО: если поля executor нет, добавим комментарий
                // Пока просто игнорируем или можно добавить заглушку
                log.warn("Filter by executorId is not yet implemented - waiting for executor field in tickets table");
            }

            if (dateFrom != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("timeRequest"), dateFrom));
            }
            if (dateTo != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("timeRequest"), dateTo));
            }

            if (priorityMin != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("priority"), priorityMin));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}