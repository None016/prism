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

    // ID статусов (проверьте соответствие в БД)
    public static final int STATUS_CREATED = 1;      // Создана
    public static final int STATUS_ASSIGNED = 2;     // Назначена
    public static final int STATUS_IN_PROGRESS = 3;  // В работе
    public static final int STATUS_RETURNED = 4;     // Возвращена
    public static final int STATUS_CLOSED = 5;       // Закрыта

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
                    // Админ - видит всё
                    log.debug("Admin access - no restrictions");
                    break;

                case "ROLE_MANAGER":
                    // Менеджер - видит заявки учреждений, к которым привязан
                    if (userInstitutionIds != null && !userInstitutionIds.isEmpty()) {
                        predicates.add(root.get("idInstitution").in(userInstitutionIds));
                        log.debug("Manager access - institutions: {}", userInstitutionIds);
                    } else {
                        log.warn("Manager {} has no institutions assigned!", userId);
                        predicates.add(criteriaBuilder.disjunction());
                    }
                    break;

                case "ROLE_EXECUTOR":
                    // Исполнитель:
                    // 1. НЕ ВИДИТ заявки со статусом "Создана" (id = 1)
                    // 2. Видит заявки, где он является исполнителем (id_contractor в его списке)
                    // 3. Видит свободные назначенные заявки (статус "Назначена" без исполнителя)

                    if (userContractorIds == null || userContractorIds.isEmpty()) {
                        log.warn("Executor {} has no contractors assigned!", userId);
                        predicates.add(criteriaBuilder.disjunction());
                        break;
                    }

                    // Заявки, где исполнитель = пользователь (через contractor)
                    Predicate assignedToUser = root.get("idContractor").in(userContractorIds);

                    // Свободные заявки со статусом "Назначена" (ждут исполнителя)
                    Predicate freeAssigned = criteriaBuilder.and(
                            criteriaBuilder.equal(root.get("status").get("id"), STATUS_ASSIGNED),
                            criteriaBuilder.isNull(root.get("idContractor"))
                    );

                    // НЕ ПОКАЗЫВАТЬ заявки со статусом "Создана"
                    Predicate notCreated = criteriaBuilder.notEqual(
                            root.get("status").get("id"), STATUS_CREATED
                    );

                    // Комбинируем: (назначены пользователю ИЛИ свободные) И не созданы
                    predicates.add(criteriaBuilder.and(
                            criteriaBuilder.or(assignedToUser, freeAssigned),
                            notCreated
                    ));

                    log.debug("Executor access - user can see tickets assigned to contractors: {}, or free assigned tickets, but NOT CREATED",
                            userContractorIds);
                    break;

                default:
                    // Обычный пользователь - не видит ничего
                    log.warn("Default user {} has no role - seeing no tickets", userId);
                    predicates.add(criteriaBuilder.disjunction());
                    break;
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

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
        return Specification.where(filterByRole(role, userId, userInstitutionIds, userContractorIds))
                .and((root, query, cb) -> {
                    List<Predicate> predicates = new ArrayList<>();

                    if (statusId != null) {
                        predicates.add(cb.equal(root.get("status").get("id"), statusId));
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
}