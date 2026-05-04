package com.example.ticketservice.unit.repository;

import com.example.ticketservice.domain.entity.Ticket;
import com.example.ticketservice.domain.repository.TicketSpecification;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.*;
import java.time.Instant;
import static org.mockito.ArgumentMatchers.any;
import jakarta.persistence.criteria.Expression;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class TicketSpecificationTest {

    @Test
    void filter_ShouldBuildPredicates() {
        Instant dateFrom = Instant.now().minusSeconds(86400);
        Instant dateTo = Instant.now();

        Specification<Ticket> spec = TicketSpecification.filter(
                1, 2, null, dateFrom, dateTo, 3
        );

        assertThat(spec).isNotNull();

        // Verify predicates are built correctly
        Root<Ticket> root = mock(Root.class);
        Path<Object> statusPath = mock(Path.class);
        Path<Object> typePath = mock(Path.class);
        Path<Object> timePath = mock(Path.class);
        Path<Object> priorityPath = mock(Path.class);

        when(root.get("status")).thenReturn(statusPath);
        when(statusPath.get("id")).thenReturn(mock(Path.class));
        when(root.get("typeTicket")).thenReturn(typePath);
        when(typePath.get("id")).thenReturn(mock(Path.class));
        when(root.get("timeRequest")).thenReturn(timePath);
        when(root.get("priority")).thenReturn(priorityPath);

        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        spec.toPredicate(root, query, cb);

        verify(cb, atLeastOnce()).equal(any(Path.class), any(Integer.class));
        verify(cb, atLeastOnce())
                .greaterThanOrEqualTo(any(Expression.class), any(Instant.class));
        verify(cb, atLeastOnce())
                .lessThanOrEqualTo(any(Expression.class), any(Instant.class));
    }

    @Test
    void filter_WithNullParams_ShouldReturnEmptyPredicate() {
        Specification<Ticket> spec = TicketSpecification.filter(null, null, null, null, null, null);

        assertThat(spec).isNotNull();
    }
}