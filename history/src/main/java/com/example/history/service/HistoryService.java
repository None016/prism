package com.example.history.service;

import com.example.history.model.HistoryEvent;
import com.example.history.repository.HistoryEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HistoryService {

    private final HistoryEventRepository repository;

    public List<HistoryEvent> getEventsByEntity(String entityType, String entityId, int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "timestamp"));
        return repository.findByEntityTypeAndEntityId(entityType, entityId, pageable);
    }

    public List<HistoryEvent> getEventsByEntityAndPeriod(
            String entityType,
            String entityId,
            Instant from,
            Instant to) {
        return repository.findByEntityTypeAndEntityIdAndTimestampBetween(entityType, entityId, from, to);
    }

    public List<HistoryEvent> getEventsByUser(String userId, int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "timestamp"));
        return repository.findByUserIdOrderByTimestampDesc(userId, pageable);
    }

    public List<HistoryEvent> getEventsByType(String eventType, int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "timestamp"));
        return repository.findByEventTypeOrderByTimestampDesc(eventType, pageable);
    }
}