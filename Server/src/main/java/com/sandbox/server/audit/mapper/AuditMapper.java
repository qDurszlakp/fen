package com.sandbox.server.audit.mapper;

import com.sandbox.server.audit.dto.AuditDto;
import com.sandbox.server.audit.entity.Audit;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AuditMapper {

    AuditDto auditToAuditDto(Audit audit);

    List<AuditDto> auditsToAuditDtos(List<Audit> audits);
}
