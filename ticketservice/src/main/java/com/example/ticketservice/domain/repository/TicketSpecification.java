package com.example.ticketservice.domain.repository;

import com.example.ticketservice.domain.entity.Ticket;
import com.example.ticketservice.domain.entity.TicketAssignment;
import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
public class TicketSpecification {

    // ID статусов (проверьте соответствие в БД)
    public static final int STATUS_CREATED = 1;      // Создана
    public static final int STATUS_ASSIGNED = 2;     // Назначена
    public static final int STATUS_IN_PROGRESS = 3;  // В работе
    public static final int STATUS_RETURNED = 4;     // Возвращена
    public static final int STATUS_CLOSED = 5;       // Закрыта

    // ===== ✅ НОВЫЙ МЕТОД: Для пользователя (ROLE_USER) =====

    /**
     * Спецификация для пользователя (ROLE_USER)
     * Показывает заявки от его учреждений ИЛИ назначенные ему
     */
    public static Specification<Ticket> forUser(
            UUID userId,
            List<Integer> institutionIds,
            List<Integer> statusIds,
            Integer priorityMin,
            Instant dateFrom,
            Instant dateTo) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Базовое условие: не удалено
            predicates.add(cb.isFalse(root.get("isDeleted")));

            // ✅ Пользователь видит заявки от своих учреждений ИЛИ назначенные ему
            if (institutionIds != null && !institutionIds.isEmpty()) {
                Join<Object, Object> assignmentJoin = root.join("ticketAssignments", JoinType.LEFT);
                Predicate institutionPredicate = root.get("idInstitution").in(institutionIds);
                Predicate userAssignmentPredicate = cb.equal(assignmentJoin.get("idUser"), userId);
                predicates.add(cb.or(institutionPredicate, userAssignmentPredicate));
            } else {
                // Если нет учреждений, показываем только назначенные
                Join<Object, Object> assignmentJoin = root.join("ticketAssignments", JoinType.INNER);
                predicates.add(cb.equal(assignmentJoin.get("idUser"), userId));
            }

            // ✅ Фильтр по статусам — используем status.id (не status!)
            if (statusIds != null && !statusIds.isEmpty()) {
                predicates.add(root.get("status").get("id").in(statusIds));
            }

            // Фильтр по приоритету
            if (priorityMin != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("priority"), priorityMin));
            }

            // Фильтр по дате от
            if (dateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("timeRequest"), dateFrom));
            }

            // Фильтр по дате до
            if (dateTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("timeRequest"), dateTo));
            }

            // Сортировка по дате создания (новые сверху)
            query.orderBy(cb.desc(root.get("timeRequest")));
            query.distinct(true);

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    // ===== МЕТОДЫ ДЛЯ МЕНЕДЖЕРА =====

    public static Specification<Ticket> filterByRole(
            String role,
            UUID userId,
            List<Integer> userInstitutionIds,
            List<Integer> userContractorIds
    ) {
        return (root, query, criteriaBuilder) -> {
            // Загружаем связанные сущности
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                query.distinct(true);
                root.fetch("typeTicket", JoinType.LEFT);
                root.fetch("status", JoinType.LEFT);
                root.fetch("institution", JoinType.LEFT);
            }

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.isFalse(root.get("isDeleted")));

            String roleUpper = role != null ? role.toUpperCase() : "ROLE_USER";
            log.debug("Filtering by role: {}, userId: {}, institutions: {}, contractors: {}",
                    roleUpper, userId, userInstitutionIds, userContractorIds);

            switch (roleUpper) {
                case "ROLE_ROOT":
                case "ROLE_ADMIN":
                    log.debug("Admin access - no restrictions");
                    break;

                case "ROLE_MANAGER":
                    if (userInstitutionIds != null && !userInstitutionIds.isEmpty()) {
                        predicates.add(root.get("idInstitution").in(userInstitutionIds));
                        log.debug("Manager access - institutions: {}", userInstitutionIds);
                    } else {
                        log.warn("Manager {} has no institutions assigned!", userId);
                        predicates.add(criteriaBuilder.disjunction());
                    }
                    break;

                case "ROLE_EXECUTOR":
                    if (userId == null) {
                        log.warn("Executor has no userId!");
                        predicates.add(criteriaBuilder.disjunction());
                        break;
                    }

                    if (userContractorIds == null || userContractorIds.isEmpty()) {
                        log.warn("Executor {} has no contractors assigned!", userId);
                        predicates.add(criteriaBuilder.disjunction());
                        break;
                    }

                    // ✅ 1. Заявки со статусом "Назначена" (2), где исполнитель = пользователь (через contractor)
                    Predicate assignedToUserByContractor = criteriaBuilder.and(
                            criteriaBuilder.equal(root.get("status").get("id"), STATUS_ASSIGNED),
                            root.get("idContractor").in(userContractorIds)
                    );

                    // ✅ 2. Свободные заявки со статусом "Назначена" (2) без исполнителя
                    Predicate freeAssigned = criteriaBuilder.and(
                            criteriaBuilder.equal(root.get("status").get("id"), STATUS_ASSIGNED),
                            criteriaBuilder.isNull(root.get("idContractor"))
                    );

                    // ✅ 3. Заявки со статусом "В работе" (3), где пользователь назначен через ticket_assignment
                    Subquery<UUID> subqueryInProgress = query.subquery(UUID.class);
                    Root<TicketAssignment> assignmentRootInProgress = subqueryInProgress.from(TicketAssignment.class);
                    subqueryInProgress.select(assignmentRootInProgress.get("idTicket"));
                    subqueryInProgress.where(
                            criteriaBuilder.equal(assignmentRootInProgress.get("idUser"), userId)
                    );

                    Predicate inProgressAssigned = criteriaBuilder.and(
                            criteriaBuilder.equal(root.get("status").get("id"), STATUS_IN_PROGRESS),
                            root.get("uuid").in(subqueryInProgress)
                    );

                    // ✅ 4. Заявки со статусом "Закрыта" (5), где пользователь назначен через ticket_assignment
                    Subquery<UUID> subqueryClosed = query.subquery(UUID.class);
                    Root<TicketAssignment> assignmentRootClosed = subqueryClosed.from(TicketAssignment.class);
                    subqueryClosed.select(assignmentRootClosed.get("idTicket"));
                    subqueryClosed.where(
                            criteriaBuilder.equal(assignmentRootClosed.get("idUser"), userId)
                    );

                    Predicate closedAssigned = criteriaBuilder.and(
                            criteriaBuilder.equal(root.get("status").get("id"), STATUS_CLOSED),
                            root.get("uuid").in(subqueryClosed)
                    );

                    // ✅ Комбинируем все условия
                    predicates.add(criteriaBuilder.or(
                            criteriaBuilder.and(
                                    criteriaBuilder.equal(root.get("status").get("id"), STATUS_ASSIGNED),
                                    criteriaBuilder.or(assignedToUserByContractor, freeAssigned)
                            ),
                            inProgressAssigned,
                            closedAssigned
                    ));

                    log.debug("Executor access - userId: {}, contractors: {}", userId, userContractorIds);
                    break;

                default:
                    log.warn("Default user {} has no role - seeing no tickets", userId);
                    predicates.add(criteriaBuilder.disjunction());
                    break;
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * ✅ СТАРЫЙ метод — для обратной совместимости (с одиночным statusId)
     */
    public static Specification<Ticket> filterWithParams(
            String role,
            UUID userId,
            List<Integer> userInstitutionIds,
            List<Integer> userContractorIds,
            Integer statusId,
            Integer typeId,
            Integer institutionId,
            Instant dateFrom,
            Instant dateTo,
            Integer priorityMin
    ) {
        List<Integer> statusIds = statusId != null ? List.of(statusId) : null;
        return filterWithParams(
                role, userId, userInstitutionIds, userContractorIds,
                statusIds, typeId, institutionId, dateFrom, dateTo, priorityMin
        );
    }

    /**
     * ✅ НОВЫЙ метод — с поддержкой списка статусов
     */
    public static Specification<Ticket> filterWithParams(
            String role,
            UUID userId,
            List<Integer> userInstitutionIds,
            List<Integer> userContractorIds,
            List<Integer> statusIds,
            Integer typeId,
            Integer institutionId,
            Instant dateFrom,
            Instant dateTo,
            Integer priorityMin
    ) {
        return Specification.where(filterByRole(role, userId, userInstitutionIds, userContractorIds))
                .and((root, query, cb) -> {
                    List<Predicate> predicates = new ArrayList<>();

                    if (statusIds != null && !statusIds.isEmpty()) {
                        predicates.add(root.get("status").get("id").in(statusIds));
                        log.debug("Filtering by statusIds: {}", statusIds);
                    }

                    if (typeId != null) {
                        predicates.add(cb.equal(root.get("typeTicket").get("id"), typeId));
                    }
                    if (institutionId != null) {
                        predicates.add(cb.equal(root.get("idInstitution"), institutionId));
                    }
                    if (dateFrom != null) {
                        predicates.add(cb.greaterThanOrEqualTo(root.get("timeRequest"), dateFrom));
                    }
                    if (dateTo != null) {
                        predicates.add(cb.lessThanOrEqualTo(root.get("timeRequest"), dateTo));
                    }
                    if (priorityMin != null) {
                        predicates.add(cb.greaterThanOrEqualTo(root.get("priority"), priorityMin));
                    }

                    return cb.and(predicates.toArray(new Predicate[0]));
                });
    }

    public static Specification<Ticket> forManager(
            Integer contractorId,
            List<Integer> statusIds,
            Integer priorityMin,
            Instant dateFrom,
            Instant dateTo,
            UUID executorId
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.isFalse(root.get("isDeleted")));
            predicates.add(cb.equal(root.get("idContractor"), contractorId));

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

            if (executorId != null) {
                Subquery<UUID> subquery = query.subquery(UUID.class);
                Root<TicketAssignment> assignmentRoot = subquery.from(TicketAssignment.class);
                subquery.select(assignmentRoot.get("idTicket"));
                subquery.where(cb.equal(assignmentRoot.get("idUser"), executorId));
                predicates.add(root.get("uuid").in(subquery));
            }

            query.orderBy(cb.desc(root.get("timeRequest")));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}