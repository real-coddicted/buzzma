package com.coddicted.buzzma.identity.service;

import com.coddicted.buzzma.identity.entity.SecurityAnswer;
import com.coddicted.buzzma.identity.entity.SecurityQuestion;
import com.coddicted.buzzma.identity.entity.SecurityQuestionWrapper;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public interface SecurityQuestionAnswerService {

  List<SecurityQuestion> listSecurityQuestions();

  SecurityQuestion createSecurityQuestion(SecurityQuestion securityQuestion);

  SecurityQuestion updateSecurityQuestion(SecurityQuestion securityQuestion);

  SecurityQuestion deleteSecurityQuestion(UUID id, UUID requesterId);

  SecurityAnswer createSecurityAnswer(SecurityAnswer securityAnswer);

  List<SecurityAnswer> updateSecurityAnswers(List<SecurityAnswer> securityAnswers);

  List<SecurityQuestionWrapper> getSecurityQuestionsByUserId(UUID userId);

  boolean verifySecurityAnswer(SecurityAnswer securityAnswer);
}
