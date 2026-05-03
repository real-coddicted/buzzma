package com.coddicted.buzzma.identity.mapper;

import com.coddicted.buzzma.identity.api.auth.SecurityQuestionWrapper;
import com.coddicted.buzzma.identity.api.auth.UserRegistrationRequestDto;
import com.coddicted.buzzma.identity.api.auth.UserSignInRequestDto;
import com.coddicted.buzzma.identity.api.auth.UserSummary;
import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import com.coddicted.buzzma.identity.entity.Invite;
import com.coddicted.buzzma.identity.entity.SecurityAnswer;
import com.coddicted.buzzma.identity.entity.UserBankingDetail;
import com.coddicted.buzzma.identity.entity.UserCredential;
import java.util.List;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AuthMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "role", ignore = true)
  @Mapping(target = "status", ignore = true)
  @Mapping(target = "email", ignore = true)
  @Mapping(target = "username", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "updatedBy", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "isDeleted", ignore = true)
  BuzzmaUser toUser(UserSignInRequestDto request);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "userId", ignore = true)
  @Mapping(source = "password", target = "passwordHash")
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "updatedBy", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "isDeleted", ignore = true)
  UserCredential toCredential(UserSignInRequestDto request);

  @Mapping(source = "userRole", target = "role")
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "username", ignore = true)
  @Mapping(target = "status", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "updatedBy", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "isDeleted", ignore = true)
  BuzzmaUser toUser(UserRegistrationRequestDto request);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "userId", ignore = true)
  @Mapping(source = "password", target = "passwordHash")
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "updatedBy", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "isDeleted", ignore = true)
  UserCredential toCredential(UserRegistrationRequestDto request);

  @Mapping(source = "bankAccountNumber", target = "accountNumber")
  @Mapping(source = "bankAccountHolderName", target = "accountHolderName")
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "updatedBy", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "isDeleted", ignore = true)
  UserBankingDetail toBankingDetail(UserRegistrationRequestDto request);

  @Mapping(source = "answer", target = "answerHash")
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "userId", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "updatedBy", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "isDeleted", ignore = true)
  SecurityAnswer toSecurityAnswer(SecurityQuestionWrapper wrapper);

  List<SecurityAnswer> toSecurityAnswers(List<SecurityQuestionWrapper> wrappers);

  @Mapping(source = "inviteCode", target = "code")
  @Mapping(source = "userRole", target = "role")
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "ownerId", ignore = true)
  @Mapping(target = "status", ignore = true)
  @Mapping(target = "validTo", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "updatedBy", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "isDeleted", ignore = true)
  Invite toInvite(UserRegistrationRequestDto request);

  @Mapping(source = "createdBy", target = "createdBy", qualifiedByName = "uuidToString")
  @Mapping(source = "updatedBy", target = "updatedBy", qualifiedByName = "uuidToString")
  @Mapping(target = "avatar", ignore = true)
  UserSummary toUserSummary(BuzzmaUser user);

  @Named("uuidToString")
  default String uuidToString(final UUID uuid) {
    if (uuid == null) {
      return null;
    }
    return uuid.toString();
  }
}
