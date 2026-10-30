package com.coddicted.buzzma.shared.security;

/**
 * Admin "view as" mode: an admin (the actor) sends {@link #HEADER} with another user's id (the
 * subject) and the request is served as that subject, read-only.
 */
public final class ViewAs {

  public static final String HEADER = "X-View-As-User";

  /** Request attributes set for a view-as request, read by request logging for auditing. */
  public static final String ACTOR_ID_ATTRIBUTE = "buzzma.viewAs.actorId";

  public static final String SUBJECT_ID_ATTRIBUTE = "buzzma.viewAs.subjectId";

  private ViewAs() {}
}
