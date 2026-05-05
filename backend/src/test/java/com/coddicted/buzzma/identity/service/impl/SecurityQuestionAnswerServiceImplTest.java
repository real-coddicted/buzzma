package com.coddicted.buzzma.identity.service.impl;

import static com.coddicted.buzzma.identity.service.impl.Fixtures.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.coddicted.buzzma.identity.entity.SecurityAnswer;
import com.coddicted.buzzma.identity.entity.SecurityQuestion;
import com.coddicted.buzzma.identity.entity.SecurityQuestionWrapper;
import com.coddicted.buzzma.identity.persistence.SecurityAnswerRepository;
import com.coddicted.buzzma.identity.persistence.SecurityQuestionRepository;
import com.coddicted.buzzma.shared.common.PasswordService;
import com.coddicted.buzzma.shared.exception.NotFoundException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SecurityQuestionAnswerServiceImplTest {

  @Mock private SecurityQuestionRepository mockQuestionRepository;
  @Mock private SecurityAnswerRepository mockAnswerRepository;
  @Mock private PasswordService mockPasswordService;
  private SecurityQuestionAnswerServiceImpl service;

  @BeforeEach
  void setUp() {
    this.service =
        new SecurityQuestionAnswerServiceImpl(
            this.mockQuestionRepository, this.mockAnswerRepository, this.mockPasswordService);
  }

  @Test
  void testListSecurityQuestions() {
    final List<SecurityQuestion> questions = List.of(SECURITY_QUESTION_2);
    doReturn(questions).when(this.mockQuestionRepository).findAllByIsDeletedFalse();

    assertEquals(questions, this.service.listSecurityQuestions());
  }

  @Test
  void testCreateSecurityQuestion() {
    doReturn(EXPECTED_SECURITY_QUESTION_1)
        .when(this.mockQuestionRepository)
        .save(SECURITY_QUESTION_1);

    assertEquals(
        EXPECTED_SECURITY_QUESTION_1, this.service.createSecurityQuestion(SECURITY_QUESTION_1));
    verify(this.mockQuestionRepository).save(SECURITY_QUESTION_1);
  }

  @Test
  void testUpdateSecurityQuestion() {
    doReturn(EXPECTED_SECURITY_QUESTION_2)
        .when(this.mockQuestionRepository)
        .save(SECURITY_QUESTION_2);

    assertEquals(
        EXPECTED_SECURITY_QUESTION_2, this.service.updateSecurityQuestion(SECURITY_QUESTION_2));
    verify(this.mockQuestionRepository).save(SECURITY_QUESTION_2);
  }

  @Test
  void testDeleteSecurityQuestionWhenFound() {
    doReturn(Optional.of(SECURITY_QUESTION_2))
        .when(this.mockQuestionRepository)
        .findById(QUESTION_ID);

    this.service.deleteSecurityQuestion(QUESTION_ID, REQUESTER_ID);

    final ArgumentCaptor<SecurityQuestion> captor = ArgumentCaptor.forClass(SecurityQuestion.class);
    verify(this.mockQuestionRepository).save(captor.capture());
    final SecurityQuestion saved = captor.getValue();
    assertEquals(QUESTION_ID, saved.getId());
    assertTrue(saved.getIsDeleted());
    assertEquals(REQUESTER_ID, saved.getUpdatedBy());
  }

  @Test
  void testDeleteSecurityQuestionWhenNotFound() {
    doReturn(Optional.empty()).when(this.mockQuestionRepository).findById(QUESTION_ID);

    final NotFoundException ex =
        assertThrows(
            NotFoundException.class,
            () -> this.service.deleteSecurityQuestion(QUESTION_ID, REQUESTER_ID));
    assertEquals("SecurityQuestion not found: " + QUESTION_ID, ex.getMessage());
  }

  @Test
  void testCreateSecurityAnswer() {
    doReturn(HASHED_ANSWER).when(this.mockPasswordService).hashPassword(PLAIN_ANSWER);

    this.service.createSecurityAnswer(SECURITY_ANSWER_1);

    final ArgumentCaptor<SecurityAnswer> captor = ArgumentCaptor.forClass(SecurityAnswer.class);
    verify(this.mockAnswerRepository).save(captor.capture());
    final SecurityAnswer saved = captor.getValue();
    assertEquals(SECURITY_ANSWER_1.getUserId(), saved.getUserId());
    assertEquals(SECURITY_ANSWER_1.getQuestionId(), saved.getQuestionId());
    assertEquals(HASHED_ANSWER, saved.getAnswerHash());
  }

  @Test
  @SuppressWarnings("unchecked")
  void testUpdateSecurityAnswers() {
    doReturn(HASHED_ANSWER).when(this.mockPasswordService).hashPassword(PLAIN_ANSWER);

    this.service.updateSecurityAnswers(List.of(SECURITY_ANSWER_1));

    final ArgumentCaptor<List<SecurityAnswer>> captor =
        ArgumentCaptor.forClass((Class<List<SecurityAnswer>>) (Class<?>) List.class);
    verify(this.mockAnswerRepository).saveAll(captor.capture());
    final List<SecurityAnswer> saved = captor.getValue();
    assertEquals(1, saved.size());
    assertEquals(HASHED_ANSWER, saved.getFirst().getAnswerHash());
    assertEquals(SECURITY_ANSWER_1.getUserId(), saved.getFirst().getUserId());
    assertEquals(SECURITY_ANSWER_1.getQuestionId(), saved.getFirst().getQuestionId());
  }

  @Test
  void testGetSecurityQuestionsByUserId() {
    final SecurityQuestionWrapper wrapper = mock(SecurityQuestionWrapper.class);
    final List<SecurityQuestionWrapper> questions = List.of(wrapper);
    doReturn(questions).when(this.mockAnswerRepository).findSecurityQuestionByUserId(USER_ID);

    assertEquals(questions, this.service.getSecurityQuestionsByUserId(USER_ID));
  }

  @Test
  void testVerifySecurityAnswerWhenCorrect() {
    doReturn(Optional.of(EXPECTED_SECURITY_ANSWER_1))
        .when(this.mockAnswerRepository)
        .findByUserIdAndQuestionId(USER_ID, QUESTION_ID);
    doReturn(true).when(this.mockPasswordService).verifyPassword(PLAIN_ANSWER, STORED_ANSWER_HASH);

    assertTrue(this.service.verifySecurityAnswer(SECURITY_ANSWER_1));
  }

  @Test
  void testVerifySecurityAnswerWhenIncorrect() {
    doReturn(Optional.of(EXPECTED_SECURITY_ANSWER_1))
        .when(this.mockAnswerRepository)
        .findByUserIdAndQuestionId(USER_ID, QUESTION_ID);
    doReturn(false).when(this.mockPasswordService).verifyPassword(PLAIN_ANSWER, STORED_ANSWER_HASH);

    assertFalse(this.service.verifySecurityAnswer(SECURITY_ANSWER_1));
  }

  @Test
  void testVerifySecurityAnswerWhenNotFound() {
    doReturn(Optional.empty())
        .when(this.mockAnswerRepository)
        .findByUserIdAndQuestionId(USER_ID, QUESTION_ID);

    final NotFoundException ex =
        assertThrows(
            NotFoundException.class, () -> this.service.verifySecurityAnswer(SECURITY_ANSWER_1));
    assertEquals("Security answer not found", ex.getMessage());
  }
}
