package com.example.ticketservice.domain.repository;

import com.example.ticketservice.domain.entity.Ticket;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс для построения динамических SQL-запросов (фильтров).
 */
public class TicketSpecification {

    /**
     * Метод, который собирает все условия фильтрации в один запрос.
     * Если параметр null - условие не добавляется.
     */
    public static Specification<Ticket> filter(
            Integer statusId,
            Integer typeId,
            String executorId,
            Instant dateFrom,
            Instant dateTo,
            Integer priorityMin
    ) {
        return (root, query, criteriaBuilder) -> {
            // Добавляем DISTINCT для устранения дубликатов при JOIN FETCH
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                query.distinct(true);
            }

            List<Predicate> predicates = new ArrayList<>();

            // 1. Фильтр по статусу (id_status)
            if (statusId != null) {
                predicates.add(criteriaBuilder.equal(root.get("status").get("id"), statusId));
            }

            // 2. Фильтр по типу заявки (id_type)
            if (typeId != null) {
                predicates.add(criteriaBuilder.equal(root.get("typeTicket").get("id"), typeId));
            }

            // 3. Фильтр по дате создания (time_request)
            if (dateFrom != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("timeRequest"), dateFrom));
            }
            if (dateTo != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("timeRequest"), dateTo));
            }

            // 4. Фильтр по приоритету
            if (priorityMin != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("priority"), priorityMin));
            }

            // Возвращаем комбинацию всех условий через AND
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}