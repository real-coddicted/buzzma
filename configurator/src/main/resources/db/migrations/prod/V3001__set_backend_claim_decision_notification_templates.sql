-- Config-driven claim decision notification templates (approve/reject).
-- First prod/ migration: these four keys have never been written via the
-- Admin API, so there is no live-value drift to check (see prod/README.md).
-- Values seeded identical to the Java caller-provided defaults in
-- ClaimReviewEventPublisher; editing these rows changes buyer-facing text
-- without a redeploy.

INSERT INTO config_entries (namespace, environment, key, value_type, value, description, owner, updated_by)
VALUES
    ('backend', 'prod', 'claim-decision-notification.approved.title',
     'string', '"Hurray! Claim {claimCode} approved!!"',
     'Title of the notification sent to the buyer when their claim is approved. Placeholders: {claimCode}', 'claims-team', 'flyway'),

    ('backend', 'prod', 'claim-decision-notification.approved.message',
     'string', '"Congratulations! Your claim with code {claimCode} has been approved. We will keep you updated once payment is released for it."',
     'Body of the notification sent to the buyer when their claim is approved. Placeholders: {claimCode}', 'claims-team', 'flyway'),

    ('backend', 'prod', 'claim-decision-notification.rejected.title',
     'string', '"Claim {claimCode} rejected!"',
     'Title of the notification sent to the buyer when their claim is rejected. Placeholders: {claimCode}', 'claims-team', 'flyway'),

    ('backend', 'prod', 'claim-decision-notification.rejected.message',
     'string', '"Your claim with code {claimCode} has been rejected with reason: {reviewerComment}. Please visit claims page for more details or contact your mediator."',
     'Body of the notification sent to the buyer when their claim is rejected. Placeholders: {claimCode}, {reviewerComment}', 'claims-team', 'flyway');
