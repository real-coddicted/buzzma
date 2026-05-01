package com.coddicted.buzzma.identity.service;

import com.coddicted.buzzma.identity.entity.SecurityAnswer;
import com.coddicted.buzzma.identity.entity.SecurityQuestion;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public interface SecurityQuestionAnswerService {

    List<SecurityQuestion> listSecurityQuestions();

    SecurityQuestion createSecurityQuestion(SecurityQuestion securityQuestion);

    SecurityQuestion updateSecurityQuestion(SecurityQuestion securityQuestion);

    SecurityQuestion deleteSecurityQuestion(UUID id, UUID requesterId);

    SecurityAnswer createSecurityAnswer(SecurityAnswer securityAnswer);

    List<SecurityAnswer> updateSecurityAnswers(List<SecurityAnswer> securityAnswers);

    boolean verifySecurityAnswer(SecurityAnswer securityAnswer);
}
