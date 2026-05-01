package com.coddicted.buzzma.identity.service.impl;

import com.coddicted.buzzma.identity.entity.SecurityAnswer;
import com.coddicted.buzzma.identity.entity.SecurityQuestion;
import com.coddicted.buzzma.identity.persistence.SecurityAnswerRepository;
import com.coddicted.buzzma.identity.persistence.SecurityQuestionRepository;
import com.coddicted.buzzma.identity.service.SecurityQuestionAnswerService;
import com.coddicted.buzzma.shared.common.BaseCrudService;
import com.coddicted.buzzma.shared.common.PasswordService;
import com.coddicted.buzzma.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class SecurityQuestionAnswerServiceImpl extends BaseCrudService
        implements SecurityQuestionAnswerService {

    private final SecurityQuestionRepository questionRepository;
    private final SecurityAnswerRepository answerRepository;
    private final PasswordService passwordService;

    public SecurityQuestionAnswerServiceImpl(
            final SecurityQuestionRepository questionRepository,
            final SecurityAnswerRepository answerRepository,
            final PasswordService passwordService) {
        this.questionRepository = questionRepository;
        this.answerRepository = answerRepository;
        this.passwordService = passwordService;
    }

    @Override
    public List<SecurityQuestion> listSecurityQuestions() {
        return questionRepository.findAllByIsDeletedFalse();
    }

    @Override
    @Transactional
    public SecurityQuestion createSecurityQuestion(final SecurityQuestion securityQuestion) {
        return questionRepository.save(securityQuestion);
    }

    @Override
    @Transactional
    public SecurityQuestion updateSecurityQuestion(final SecurityQuestion securityQuestion) {
        return questionRepository.save(securityQuestion);
    }

    @Override
    @Transactional
    public SecurityQuestion deleteSecurityQuestion(final UUID id, final UUID requesterId) {
        final SecurityQuestion question = mustFind(questionRepository, id, "SecurityQuestion");
        return questionRepository.save(question.toBuilder()
                .isDeleted(true)
                .updatedBy(requesterId)
                .build());
    }

    @Override
    @Transactional
    public SecurityAnswer createSecurityAnswer(final SecurityAnswer securityAnswer) {
        return answerRepository.save(securityAnswer.toBuilder()
                .answerHash(passwordService.hashPassword(securityAnswer.getAnswerHash()))
                .build());
    }

    @Override
    @Transactional
    public List<SecurityAnswer> updateSecurityAnswers(final List<SecurityAnswer> securityAnswers) {
        final List<SecurityAnswer> hashed = securityAnswers.stream()
                .map(a -> a.toBuilder()
                        .answerHash(passwordService.hashPassword(a.getAnswerHash()))
                        .build())
                .toList();
        return answerRepository.saveAll(hashed);
    }

    @Override
    public boolean verifySecurityAnswer(final SecurityAnswer securityAnswer) {
        final SecurityAnswer stored = answerRepository
                .findByUserIdAndQuestionId(securityAnswer.getUserId(), securityAnswer.getQuestionId())
                .orElseThrow(() -> new NotFoundException("Security answer not found"));
        return passwordService.verifyPassword(securityAnswer.getAnswerHash(), stored.getAnswerHash());
    }
}
