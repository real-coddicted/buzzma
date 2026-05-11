package com.coddicted.buzzma.feedback.service.impl;

import static com.coddicted.buzzma.feedback.service.impl.Fixtures.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.feedback.entity.Feedback;
import com.coddicted.buzzma.feedback.persistence.FeedbackRepository;
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
class FeedbackServiceImplTest {

  @Mock private FeedbackRepository mockFeedbackRepository;
  private FeedbackServiceImpl feedbackService;

  @BeforeEach
  void setUp() {
    this.feedbackService = new FeedbackServiceImpl(this.mockFeedbackRepository);
  }

  @Test
  void testCreate() {
    this.feedbackService.create(FEEDBACK_3, REQUESTER_ID);

    final ArgumentCaptor<Feedback> captor = ArgumentCaptor.forClass(Feedback.class);
    verify(this.mockFeedbackRepository).save(captor.capture());
    final Feedback saved = captor.getValue();
    assertEquals(REQUESTER_ID, saved.getCreatedBy());
    assertEquals(REQUESTER_ID, saved.getUpdatedBy());
    assertEquals(FEEDBACK_3.getRating(), saved.getRating());
    assertEquals(FEEDBACK_3.getCategory(), saved.getCategory());
    assertEquals(FEEDBACK_3.getFeedback(), saved.getFeedback());
  }

  @Test
  void testGetByIdWhenFound() {
    when(this.mockFeedbackRepository.findByIdAndIsDeletedFalse(FEEDBACK_ID))
        .thenReturn(Optional.of(FEEDBACK_1));

    assertEquals(FEEDBACK_1, this.feedbackService.getById(FEEDBACK_ID));
  }

  @Test
  void testGetByIdWhenNotFound() {
    when(this.mockFeedbackRepository.findByIdAndIsDeletedFalse(FEEDBACK_ID))
        .thenReturn(Optional.empty());

    final NotFoundException ex =
        assertThrows(NotFoundException.class, () -> this.feedbackService.getById(FEEDBACK_ID));
    assertEquals("Feedback not found: " + FEEDBACK_ID, ex.getMessage());
  }

  @Test
  void testListByUserId() {
    when(this.mockFeedbackRepository.findByUserIdAndIsDeletedFalse(USER_ID))
        .thenReturn(List.of(FEEDBACK_1, FEEDBACK_2));

    final List<Feedback> result = this.feedbackService.listByUserId(USER_ID);

    assertEquals(2, result.size());
    assertEquals(FEEDBACK_1, result.get(0));
    assertEquals(FEEDBACK_2, result.get(1));
  }

  @Test
  void testListByUserIdWhenEmpty() {
    when(this.mockFeedbackRepository.findByUserIdAndIsDeletedFalse(USER_ID)).thenReturn(List.of());

    assertTrue(this.feedbackService.listByUserId(USER_ID).isEmpty());
  }
}
