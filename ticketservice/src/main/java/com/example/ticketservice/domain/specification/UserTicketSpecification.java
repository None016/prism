package com.example.ticketservice.domain.specification;

import com.example.ticketservice.domain.entity.Ticket;
import com.example.ticketservice.domain.entity.TicketAssignment;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserTicketSpecification {

    /**
     * ✅ ИСПРАВЛЕНО: Фильтрация по contractorIds, а не institutionIds
     */
    public static Specification<Ticket> forUser(
            UUID userId,
            List<Integer> contractorIds,  // ✅ ИЗМЕНЕНО: contractorIds
            List<Integer> statusIds,
            Integer priorityMin,
            Instant dateFrom,
            Instant dateTo) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Базовое условие: не удалено
            predicates.add(cb.equal(root.get("isDeleted"), false));

            // ✅ Пользователь видит заявки от своих ПОДРЯДЧИКОВ ИЛИ назначенные ему
            if (contractorIds != null && !contractorIds.isEmpty()) {
                Join<Object, Object> assignmentJoin = root.join("ticketAssignments", JoinType.LEFT);
                Predicate contractorPredicate = root.get("idContractor").in(contractorIds);
                Predicate userAssignmentPredicate = cb.equal(assignmentJoin.get("idUser"), userId);
                predicates.add(cb.or(contractorPredicate, userAssignmentPredicate));
                query.distinct(true);
            } else {
                // Если нет contractorIds, показываем только назначенные
                Join<Object, Object> assignmentJoin = root.join("ticketAssignments", JoinType.INNER);
                predicates.add(cb.equal(assignmentJoin.get("idUser"), userId));
                query.distinct(true);
            }

            // ✅ ИСПРАВЛЕНО: используем status.id вместо status
            if (statusIds != null && !statusIds.isEmpty()) {
                predicates.add(root.get("status").get("id").in(statusIds));
            }

            if (priorityMin != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("priority"), priorityMin));
            }

            if (dateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("timeRequest"), dateFrom));
            }

            if (dateTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("timeRequest"), dateTo));
            }

            query.orderBy(cb.desc(root.get("timeRequest")));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}