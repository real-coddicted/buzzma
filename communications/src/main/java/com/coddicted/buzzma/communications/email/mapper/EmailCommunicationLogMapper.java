package com.coddicted.buzzma.communications.email.mapper;

import com.coddicted.buzzma.communications.email.dto.EmailStatusResponseDto;
import com.coddicted.buzzma.communications.email.dto.SendEmailResponseDto;
import com.coddicted.buzzma.communications.email.model.EmailCommunicationLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EmailCommunicationLogMapper {

  @Mapping(source = "id", target = "requestId")
  SendEmailResponseDto toSendResponse(EmailCommunicationLog log);

  @Mapping(source = "id", target = "requestId")
  @Mapping(source = "toAddress", target = "to")
  @Mapping(source = "fromAddress", target = "from")
  @Mapping(source = "createdAt", target = "requestedAt")
  EmailStatusResponseDto toStatusResponse(EmailCommunicationLog log);
}
