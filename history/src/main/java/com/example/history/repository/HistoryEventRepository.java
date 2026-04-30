package com.example.history.repository;

import com.example.history.model.HistoryEvent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface HistoryEventRepository extends MongoRepository<HistoryEvent, String> {

    // Поиск по сущности (сортировка от новых к старым)
    List<HistoryEvent> findByEntityTypeAndEntityIdOrderByTimestampDesc(String entityType, String entityId);

    // Поиск по сущности с пагинацией
    List<HistoryEvent> findByEntityTypeAndEntityId(String entityType, String entityId, Pageable pageable);

    // Поиск по сущности за период
    List<HistoryEvent> findByEntityTypeAndEntityIdAndTimestampBetween(
            String entityType,
            String entityId,
            Instant from,
            Instant to
    );

    // Поиск по пользователю
    List<HistoryEvent> findByUserIdOrderByTimestampDesc(String userId, Pageable pageable);

    // Поиск по типу события
    List<HistoryEvent> findByEventTypeOrderByTimestampDesc(String eventType, Pageable pageable);

    // Кастомный запрос через @Query
    @Query("{ 'entityType': ?0, 'entityId': ?1, 'eventType': ?2 }")
    List<HistoryEvent> findByEntityAndEventType(String entityType, String entityId, String eventType);
}