package com.example.ticketservice.domain.repository;

import com.example.ticketservice.domain.entity.Ticket;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class TicketSpecification {

    public static Specification<Ticket> filter(
            Integer statusId,
            Integer typeId,
            String executorId,
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