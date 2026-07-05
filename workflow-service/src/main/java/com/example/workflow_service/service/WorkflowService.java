package com.example.workflow_service.service;

import com.example.workflow_service.client.TicketServiceClient;
import com.example.workflow_service.client.UserServiceClient;
import com.example.workflow_service.dto.response.*;
import com.example.workflow_service.dto.request.*;
import com.example.workflow_service.entity.AuditOfWorkflow;
import com.example.workflow_service.entity.TicketAssignment;
import com.example.workflow_service.exception.BusinessException;
import com.example.workflow_service.repository.AuditOfWorkflowRepository;
import com.example.workflow_service.repository.TicketAssignmentRepository;
import com.example.workflow_service.utils.TicketStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowService {

    private final AuditOfWorkflowRepository auditRepository;
    private final TicketAssignmentRepository assignmentRepository;
    private final TicketServiceClient ticketClient;
    private final UserServiceClient userClient;

    /**
     * Назначить исполнителя на заявку (только для менеджеров)
     */
    @Transactional
    public WorkflowResponse assignTicket(AssignRequest request, String managerUuid) {
        log.info("📋 Назначение заявки: ticket={}, manager={}, executor={}",
                request.getTicketUuid(), managerUuid, request.getExecutorUuid());

        UUID ticketId = UUID.fromString(request.getTicketUuid());

        TicketResponse ticket = ticketClient.getTicket(ticketId);
        int oldStatus = ticket.getStatus();

        if (oldStatus != TicketStatus.CREATED && oldStatus != TicketStatus.RETURNED) {
            throw new BusinessException(
                    String.format("Нельзя назначить исполнителя на заявку со статусом '%s'",
                            TicketStatus.getName(oldStatus))
            );
        }

        if (request.getExecutorUuid() != null) {
            UUID executorId = UUID.fromString(request.getExecutorUuid());

            List<Integer> userContractors = userClient.getUserContractors(executorId);
            if (!userContractors.contains(ticket.getIdContractor())) {
                throw new BusinessException(
                        "Исполнитель не привязан к contractor этой заявки"
                );
            }

            Optional<TicketAssignment> existingAssignment =
                    assignmentRepository.findByIdTicket(ticketId);

            if (existingAssignment.isPresent()) {
                TicketAssignment assignment = existingAssignment.get();
                assignment.setIdUser(executorId);
                assignmentRepository.save(assignment);
            } else {
                // ✅ УБРАЛИ uuid(UUID.randomUUID())
                TicketAssignment assignment = TicketAssignment.builder()
                        .idTicket(ticketId)
                        .idUser(executorId)
                        .build();
                assignmentRepository.save(assignment);
            }

            String fullName = userClient.getUserFullName(executorId);
            String comment = request.getComment() != null
                    ? request.getComment()
                    : String.format("Назначен: %s", fullName);

            AuditOfWorkflow audit = AuditOfWorkflow.builder()
                    .idTicket(ticketId)
                    .idStartStatus(oldStatus)
                    .idEndStatus(TicketStatus.ASSIGNED)
                    .idUser(UUID.fromString(managerUuid))
                    .comment(comment)
                    .timeCreate(LocalDateTime.now())
                    .build();
            auditRepository.save(audit);

            ticketClient.updateTicketStatus(ticketId, TicketStatus.ASSIGNED);

            return WorkflowResponse.builder()
                    .ticketUuid(request.getTicketUuid())
                    .oldStatus(oldStatus)
                    .newStatus(TicketStatus.ASSIGNED)
                    .userUuid(managerUuid)
                    .comment(comment)
                    .timestamp(LocalDateTime.now())
                    .message("Исполнитель успешно назначен")
                    .build();

        } else {
            String comment = request.getComment() != null
                    ? request.getComment()
                    : "Заявка переведена в статус 'Назначена'";

            AuditOfWorkflow audit = AuditOfWorkflow.builder()
                    .idTicket(ticketId)
                    .idStartStatus(oldStatus)
                    .idEndStatus(TicketStatus.ASSIGNED)
                    .idUser(UUID.fromString(managerUuid))
                    .comment(comment)
                    .timeCreate(LocalDateTime.now())
                    .build();
            auditRepository.save(audit);

            ticketClient.updateTicketStatus(ticketId, TicketStatus.ASSIGNED);

            return WorkflowResponse.builder()
                    .ticketUuid(request.getTicketUuid())
                    .oldStatus(oldStatus)
                    .newStatus(TicketStatus.ASSIGNED)
                    .userUuid(managerUuid)
                    .comment(comment)
                    .timestamp(LocalDateTime.now())
                    .message("Статус изменен на 'Назначена'")
                    .build();
        }
    }

    /**
     * Взять заявку в работу (только для исполнителей)
     */
    @Transactional
    public WorkflowResponse takeToWork(TicketActionRequest request, String executorUuid) {
        log.info("🔨 Взятие в работу: ticket={}, executor={}",
                request.getTicketUuid(), executorUuid);

        UUID ticketId = UUID.fromString(request.getTicketUuid());
        UUID userId = UUID.fromString(executorUuid);

        // 1. Получаем информацию о заявке
        TicketResponse ticket = ticketClient.getTicket(ticketId);

        // 2. Проверяем статус
        if (ticket.getStatus() != TicketStatus.ASSIGNED) {
            throw new BusinessException(
                    String.format("Нельзя взять в работу заявку со статусом '%s'",
                            TicketStatus.getName(ticket.getStatus()))
            );
        }

        // 3. Проверяем, что исполнитель привязан к contractor
        List<Integer> userContractors = userClient.getUserContractors(userId);
        if (!userContractors.contains(ticket.getIdContractor())) {
            throw new BusinessException(
                    "Вы не привязаны к contractor этой заявки"
            );
        }

        // 4. Создаем запись в ticket_assignment (если ещё нет)
        Optional<TicketAssignment> existingAssignment =
                assignmentRepository.findByIdTicket(ticketId);

        if (existingAssignment.isEmpty()) {
            // ✅ УБРАЛИ uuid(UUID.randomUUID())
            TicketAssignment assignment = TicketAssignment.builder()
                    .idTicket(ticketId)
                    .idUser(userId)
                    .build();
            assignmentRepository.save(assignment);  // UUID сгенерируется автоматически
        }

        // 5. Создаем запись в audit_of_vocflowe
        String comment = request.getComment() != null
                ? request.getComment()
                : "Взял в работу";

        AuditOfWorkflow audit = AuditOfWorkflow.builder()
                .idTicket(ticketId)
                .idStartStatus(TicketStatus.ASSIGNED)
                .idEndStatus(TicketStatus.IN_PROGRESS)
                .idUser(userId)
                .comment(comment)
                .timeCreate(LocalDateTime.now())
                .build();
        auditRepository.save(audit);

        // 6. Обновляем статус заявки
        ticketClient.updateTicketStatus(ticketId, TicketStatus.IN_PROGRESS);

        log.info("✅ Заявка взята в работу: ticket={}, executor={}", ticketId, userId);

        return WorkflowResponse.builder()
                .ticketUuid(request.getTicketUuid())
                .oldStatus(TicketStatus.ASSIGNED)
                .newStatus(TicketStatus.IN_PROGRESS)
                .userUuid(executorUuid)
                .comment(comment)
                .timestamp(LocalDateTime.now())
                .message("Заявка взята в работу")
                .build();
    }

    /**
     * Закрыть заявку (исполнитель или менеджер)
     */
    @Transactional
    public WorkflowResponse closeTicket(TicketActionRequest request, String userUuid) {
        log.info("✅ Закрытие заявки: ticket={}, user={}",
                request.getTicketUuid(), userUuid);

        UUID ticketId = UUID.fromString(request.getTicketUuid());
        UUID userId = UUID.fromString(userUuid);

        // 1. Получаем информацию о заявке
        TicketResponse ticket = ticketClient.getTicket(ticketId);

        // 2. Проверяем статус
        if (ticket.getStatus() != TicketStatus.IN_PROGRESS &&
                ticket.getStatus() != TicketStatus.ASSIGNED &&
                ticket.getStatus() != TicketStatus.RETURNED) {
            throw new BusinessException(
                    String.format("Нельзя закрыть заявку со статусом '%s'",
                            TicketStatus.getName(ticket.getStatus()))
            );
        }

        // 3. Комментарий обязателен при закрытии
        if (request.getComment() == null || request.getComment().trim().isEmpty()) {
            throw new BusinessException("Комментарий обязателен при закрытии заявки");
        }

        // 4. Создаем запись в audit_of_vocflowe
        AuditOfWorkflow audit = AuditOfWorkflow.builder()
                .idTicket(ticketId)
                .idStartStatus(ticket.getStatus())
                .idEndStatus(TicketStatus.CLOSED)
                .idUser(userId)
                .comment(request.getComment())
                .timeCreate(LocalDateTime.now())
                .build();
        auditRepository.save(audit);

        // 5. Обновляем статус заявки
        ticketClient.updateTicketStatus(ticketId, TicketStatus.CLOSED);

        log.info("✅ Заявка закрыта: ticket={}", ticketId);

        return WorkflowResponse.builder()
                .ticketUuid(request.getTicketUuid())
                .oldStatus(ticket.getStatus())
                .newStatus(TicketStatus.CLOSED)
                .userUuid(userUuid)
                .comment(request.getComment())
                .timestamp(LocalDateTime.now())
                .message("Заявка закрыта")
                .build();
    }

    /**
     * Вернуть заявку на доработку (только исполнитель)
     */
    @Transactional
    public WorkflowResponse returnTicket(TicketActionRequest request, String executorUuid) {
        log.info("🔄 Возврат заявки: ticket={}, executor={}",
                request.getTicketUuid(), executorUuid);

        UUID ticketId = UUID.fromString(request.getTicketUuid());
        UUID userId = UUID.fromString(executorUuid);

        // 1. Получаем информацию о заявке
        TicketResponse ticket = ticketClient.getTicket(ticketId);

        // 2. Проверяем статус
        if (ticket.getStatus() != TicketStatus.IN_PROGRESS) {
            throw new BusinessException(
                    String.format("Нельзя вернуть заявку со статусом '%s'",
                            TicketStatus.getName(ticket.getStatus()))
            );
        }

        // 3. Комментарий обязателен при возврате
        if (request.getComment() == null || request.getComment().trim().isEmpty()) {
            throw new BusinessException("Комментарий обязателен при возврате заявки");
        }

        // 4. Создаем запись в audit_of_vocflowe
        AuditOfWorkflow audit = AuditOfWorkflow.builder()
                .idTicket(ticketId)
                .idStartStatus(TicketStatus.IN_PROGRESS)
                .idEndStatus(TicketStatus.RETURNED)
                .idUser(userId)
                .comment(request.getComment())
                .timeCreate(LocalDateTime.now())
                .build();
        auditRepository.save(audit);

        // 5. Обновляем статус заявки
        ticketClient.updateTicketStatus(ticketId, TicketStatus.RETURNED);

        log.info("✅ Заявка возвращена: ticket={}", ticketId);

        return WorkflowResponse.builder()
                .ticketUuid(request.getTicketUuid())
                .oldStatus(TicketStatus.IN_PROGRESS)
                .newStatus(TicketStatus.RETURNED)
                .userUuid(executorUuid)
                .comment(request.getComment())
                .timestamp(LocalDateTime.now())
                .message("Заявка возвращена на доработку")
                .build();
    }

    /**
     * Вернуть заявку в работу (только менеджер)
     */
    @Transactional
    public WorkflowResponse resumeTicket(TicketActionRequest request, String managerUuid) {
        log.info("▶️ Возобновление заявки: ticket={}, manager={}",
                request.getTicketUuid(), managerUuid);

        UUID ticketId = UUID.fromString(request.getTicketUuid());

        // 1. Получаем информацию о заявке
        TicketResponse ticket = ticketClient.getTicket(ticketId);

        // 2. Проверяем статус
        if (ticket.getStatus() != TicketStatus.RETURNED) {
            throw new BusinessException(
                    String.format("Нельзя возобновить заявку со статусом '%s'",
                            TicketStatus.getName(ticket.getStatus()))
            );
        }

        // 3. Создаем запись в audit_of_vocflowe
        String comment = request.getComment() != null
                ? request.getComment()
                : "Заявка возвращена в работу";

        AuditOfWorkflow audit = AuditOfWorkflow.builder()
                .idTicket(ticketId)
                .idStartStatus(TicketStatus.RETURNED)
                .idEndStatus(TicketStatus.IN_PROGRESS)
                .idUser(UUID.fromString(managerUuid))
                .comment(comment)
                .timeCreate(LocalDateTime.now())
                .build();
        auditRepository.save(audit);

        // 4. Обновляем статус заявки
        ticketClient.updateTicketStatus(ticketId, TicketStatus.IN_PROGRESS);

        log.info("✅ Заявка возобновлена: ticket={}", ticketId);

        return WorkflowResponse.builder()
                .ticketUuid(request.getTicketUuid())
                .oldStatus(TicketStatus.RETURNED)
                .newStatus(TicketStatus.IN_PROGRESS)
                .userUuid(managerUuid)
                .comment(comment)
                .timestamp(LocalDateTime.now())
                .message("Заявка возвращена в работу")
                .build();
    }

    /**
     * Получить историю изменений заявки
     */
    public List<AuditOfWorkflow> getTicketHistory(UUID ticketId) {
        return auditRepository.findByIdTicketOrderByTimeCreateDesc(ticketId);
    }

    /**
     * Получить текущее назначение
     */
    public Optional<TicketAssignment> getCurrentAssignment(UUID ticketId) {
        return assignmentRepository.findByIdTicket(ticketId);
    }
}