package com.coddicted.buzzma.feedback.service;

import com.coddicted.buzzma.feedback.entity.Feedback;
import java.util.List;
import java.util.UUID;

public interface FeedbackService {

  Feedback create(Feedback feedback, UUID requesterId);

  Feedback getById(UUID id);

  List<Feedback> listByUserId(UUID userId);
}
