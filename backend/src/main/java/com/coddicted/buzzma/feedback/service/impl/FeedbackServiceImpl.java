package com.coddicted.buzzma.feedback.service.impl;

import com.coddicted.buzzma.feedback.entity.Feedback;
import com.coddicted.buzzma.feedback.persistence.FeedbackRepository;
import com.coddicted.buzzma.feedback.service.FeedbackService;
import com.coddicted.buzzma.shared.common.BaseCrudService;
import com.coddicted.buzzma.shared.exception.NotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FeedbackServiceImpl extends BaseCrudService implements FeedbackService {

  private final FeedbackRepository feedbackRepository;

  public FeedbackServiceImpl(final FeedbackRepository feedbackRepository) {
    this.feedbackRepository = feedbackRepository;
  }

  @Override
  @Transactional
  public Feedback create(final Feedback feedback, final UUID requesterId) {
    final Feedback toSave =
        feedback.toBuilder().createdBy(requesterId).updatedBy(requesterId).build();
    return this.feedbackRepository.save(toSave);
  }

  @Override
  @Transactional(readOnly = true)
  public Feedback getById(final UUID id) {
    return this.feedbackRepository
        .findByIdAndIsDeletedFalse(id)
        .orElseThrow(() -> new NotFoundException("Feedback not found: " + id));
  }

  @Override
  @Transactional(readOnly = true)
  public List<Feedback> listByUserId(final UUID userId) {
    return this.feedbackRepository.findByUserIdAndIsDeletedFalse(userId);
  }
}
