package com.example.ticketservice.api.mapper;

import com.example.ticketservice.api.dto.TicketCreateRequest;
import com.example.ticketservice.api.dto.TicketResponse;
import com.example.ticketservice.api.dto.TicketUpdateRequest;
import com.example.ticketservice.domain.entity.Ticket;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.UUID;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface TicketMapper {

    // Create Request → Entity
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "typeTicket", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "timeUpdate", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "timeRequest", ignore = true)
    @Mapping(target = "timeClosing", ignore = true)
    // ditionalFields НЕ игнорируем - будет замаплен автоматически
    Ticket toEntity(TicketCreateRequest request);

    // Entity → Response
    @Mapping(source = "typeTicket.nameType", target = "typeName")
    @Mapping(source = "status.nameType", target = "statusName")
    TicketResponse toResponse(Ticket ticket);

    // Helper для генерации UUID
    default UUID generateUuid() {
        return UUID.randomUUID();
    }
}