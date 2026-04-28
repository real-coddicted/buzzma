package com.coddicted.buzzma.notifications.web;

import com.coddicted.buzzma.identity.entity.UsersEntity;
import com.coddicted.buzzma.identity.persistence.UsersRepository;
import com.coddicted.buzzma.orders.persistence.OrdersEntity;
import com.coddicted.buzzma.orders.persistence.OrdersRepository;
import com.coddicted.buzzma.shared.enums.OrderWorkflowStatus;
import com.coddicted.buzzma.shared.enums.PayoutStatus;
import com.coddicted.buzzma.shared.enums.TicketStatus;
import com.coddicted.buzzma.shared.security.CurrentUserId;
import com.coddicted.buzzma.support.persistence.TicketsEntity;
import com.coddicted.buzzma.support.persistence.TicketsRepository;
import com.coddicted.buzzma.wallet.persistence.PayoutsEntity;
import com.coddicted.buzzma.wallet.persistence.PayoutsRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class NotificationsController {

  private final UsersRepository usersRepository;
  private final OrdersRepository ordersRepository;
  private final TicketsRepository ticketsRepository;
  private final PayoutsRepository payoutsRepository;

  public NotificationsController(
      UsersRepository usersRepository,
      OrdersRepository ordersRepository,
      TicketsRepository ticketsRepository,
      PayoutsRepository payoutsRepository) {
    this.usersRepository = usersRepository;
    this.ordersRepository = ordersRepository;
    this.ticketsRepository = ticketsRepository;
    this.payoutsRepository = payoutsRepository;
  }

  @GetMapping
  @PreAuthorize("isAuthenticated()")
  public List<Map<String, Object>> list(@CurrentUserId UUID actorId) {
    UsersEntity user = usersRepository.findById(actorId).orElse(null);
    if (user == null) {
      return List.of();
    }

    List<String> roles = user.getRoles() != null ? Arrays.asList(user.getRoles()) : List.of();
    boolean isShopper = roles.contains("shopper");
    boolean isMediator = roles.contains("mediator");

    List<Map<String, Object>> notifications = new ArrayList<>();
    String nowIso = Instant.now().toString();

    // ── Shopper notifications ────────────────────────────────────────────────
    if (isShopper) {
      Instant sevenDaysAgo = Instant.now().minus(7, ChronoUnit.DAYS);
      List<OrdersEntity> orders =
          ordersRepository.findAllByUserIdAndIsDeletedFalseAndUpdatedAtAfter(actorId, sevenDaysAgo);

      for (OrdersEntity o : orders) {
        String shortId = safeOrderShortId(o);
        String wf = o.getWorkflowStatus() != null ? o.getWorkflowStatus().name() : "";
        String pay = o.getPaymentStatus() != null ? o.getPaymentStatus().name() : "";
        String aff = o.getAffiliateStatus() != null ? o.getAffiliateStatus().name() : "";
        String ts = o.getUpdatedAt() != null ? o.getUpdatedAt().toString() : nowIso;
        boolean hasPurchaseProof =
            o.getScreenshotOrder() != null || o.getScreenshotPayment() != null;
        String oid = o.getId().toString();

        if (!hasPurchaseProof
            && (wf.equals("ORDERED") || wf.equals("REDIRECTED") || wf.equals("CREATED"))) {
          notifications.add(
              notification(
                  "order:" + oid + ":need-proof",
                  "alert",
                  "Upload purchase proof",
                  "Upload your purchase screenshot for order #"
                      + shortId
                      + " to start verification.",
                  ts,
                  null,
                  null));
          continue;
        }

        if (o.getRejectionReason() != null && !o.getRejectionReason().isBlank()) {
          notifications.add(
              notification(
                  "order:" + oid + ":rejected:" + ts,
                  "alert",
                  "Proof rejected",
                  o.getRejectionReason(),
                  ts,
                  "Fix now",
                  "/orders"));
          continue;
        }

        // Parse missingProofRequests jsonb for requested steps
        List<String> requestedMissing = parseMissingProofTypes(o.getMissingProofRequests());
        if (!requestedMissing.isEmpty()) {
          String label = requestedMissing.size() >= 2 ? "review & rating" : requestedMissing.get(0);
          notifications.add(
              notification(
                  "order:" + oid + ":requested:" + String.join(",", requestedMissing) + ":" + ts,
                  "alert",
                  "Action requested by mediator",
                  "Please submit your " + label + " proof for order #" + shortId + ".",
                  ts,
                  "Upload now",
                  "/orders"));
          continue;
        }

        // Purchase verified but review/rating missing
        boolean orderVerified =
            o.getVerification() != null
                && o.getVerification().contains("\"order\"")
                && o.getVerification().contains("verifiedAt");
        if (orderVerified) {
          List<String> missingSteps = new ArrayList<>();
          if (o.getScreenshotReview() == null
              && (o.getReviewLink() == null || o.getReviewLink().isBlank())) {
            missingSteps.add("review");
          }
          if (o.getScreenshotRating() == null) {
            missingSteps.add("rating");
          }
          if (!missingSteps.isEmpty()) {
            String label = missingSteps.size() >= 2 ? "review & rating" : missingSteps.get(0);
            notifications.add(
                notification(
                    "order:" + oid + ":missing:" + String.join(",", missingSteps) + ":" + ts,
                    "alert",
                    "Action required to unlock cashback",
                    "Please submit your " + label + " proof for order #" + shortId + ".",
                    ts,
                    null,
                    null));
            continue;
          }
        }

        if (wf.equals("UNDER_REVIEW") || wf.equals("PROOF_SUBMITTED")) {
          notifications.add(
              notification(
                  "order:" + oid + ":under-review:" + ts,
                  "info",
                  "Verification in progress",
                  "Your order #" + shortId + " is under review.",
                  ts,
                  null,
                  null));
          continue;
        }

        if (pay.equals("Paid") || wf.equals("COMPLETED") || aff.equals("Approved_Settled")) {
          notifications.add(
              notification(
                  "order:" + oid + ":paid:" + ts,
                  "success",
                  "Cashback sent",
                  "Cashback for order #" + shortId + " has been processed.",
                  ts,
                  null,
                  null));
          continue;
        }

        if (wf.equals("REJECTED")
            || wf.equals("FAILED")
            || aff.equals("Rejected")
            || aff.equals("Frozen_Disputed")
            || aff.equals("Cap_Exceeded")) {
          notifications.add(
              notification(
                  "order:" + oid + ":issue:" + ts,
                  "alert",
                  "Order needs attention",
                  "There is an issue with order #"
                      + shortId
                      + " ("
                      + (wf.isBlank() ? aff : wf)
                      + ").",
                  ts,
                  null,
                  null));
        }
      }

      // Pending mediator approval
      if (Boolean.FALSE.equals(user.getIsVerifiedByMediator())) {
        notifications.add(
            0,
            notification(
                "shopper:" + actorId + ":pending-approval",
                "info",
                "Approval pending",
                "Your mediator approval is pending. You will be notified once approved.",
                nowIso,
                null,
                null));
      }
    }

    // ── Mediator notifications ───────────────────────────────────────────────
    if (isMediator) {
      String mediatorCode = user.getMediatorCode();
      if (mediatorCode != null && !mediatorCode.isBlank()) {
        long pendingUsers =
            usersRepository.countByParentCodeAndIsVerifiedByMediatorFalseAndIsDeletedFalse(
                mediatorCode);
        long pendingOrders =
            ordersRepository.countByManagerNameAndWorkflowStatusAndIsDeletedFalse(
                mediatorCode, OrderWorkflowStatus.UNDER_REVIEW);

        if (pendingUsers > 0) {
          notifications.add(
              notification(
                  "mediator:" + mediatorCode + ":pending-users:" + pendingUsers,
                  "alert",
                  "Buyer approvals pending",
                  pendingUsers + " buyers are waiting for approval.",
                  nowIso,
                  null,
                  null));
        }
        if (pendingOrders > 0) {
          notifications.add(
              notification(
                  "mediator:" + mediatorCode + ":pending-orders:" + pendingOrders,
                  "alert",
                  "Order verification pending",
                  pendingOrders + " orders need verification.",
                  nowIso,
                  null,
                  null));
        }
      }

      // Recent payouts
      List<PayoutsEntity> payouts =
          payoutsRepository
              .findAllByBeneficiaryUserIdAndIsDeletedFalse(actorId, PageRequest.of(0, 10))
              .getContent();
      for (PayoutsEntity p : payouts) {
        String createdAt =
            p.getProcessedAt() != null
                ? p.getProcessedAt().toString()
                : (p.getCreatedAt() != null ? p.getCreatedAt().toString() : nowIso);
        double amount = (p.getAmountPaise() != null ? p.getAmountPaise() : 0) / 100.0;
        String type = PayoutStatus.paid.equals(p.getStatus()) ? "success" : "info";
        String statusStr = p.getStatus() != null ? p.getStatus().name() : "requested";
        notifications.add(
            notification(
                "payout:" + p.getId(),
                type,
                "Payout recorded",
                "₹" + amount + " payout has been recorded (" + statusStr + ").",
                createdAt,
                null,
                null));
      }
    }

    // ── Ticket notifications (all roles) ────────────────────────────────────
    try {
      List<TicketsEntity> tickets =
          ticketsRepository.findTop20ByUserIdAndIsDeletedFalseOrderByUpdatedAtDesc(actorId);
      for (TicketsEntity t : tickets) {
        String tid = t.getId().toString();
        String status = t.getStatus() != null ? t.getStatus().name() : "";
        String issueType = t.getIssueType() != null ? t.getIssueType() : "Issue";
        String ts =
            t.getUpdatedAt() != null
                ? t.getUpdatedAt().toString()
                : (t.getCreatedAt() != null ? t.getCreatedAt().toString() : nowIso);

        if (TicketStatus.Resolved.name().equals(status)) {
          notifications.add(
              notification(
                  "ticket:" + tid + ":resolved:" + ts,
                  "success",
                  "Ticket resolved",
                  "Your \"" + issueType + "\" ticket has been resolved.",
                  ts,
                  null,
                  null));
        } else if (TicketStatus.Rejected.name().equals(status)) {
          notifications.add(
              notification(
                  "ticket:" + tid + ":rejected:" + ts,
                  "alert",
                  "Ticket rejected",
                  "Your \""
                      + issueType
                      + "\" ticket was rejected. You can create a new ticket if the issue persists.",
                  ts,
                  null,
                  null));
        } else if (TicketStatus.Open.name().equals(status)) {
          long createdMs = t.getCreatedAt() != null ? t.getCreatedAt().toEpochMilli() : 0;
          if (createdMs > 0 && System.currentTimeMillis() - createdMs < 86_400_000L) {
            notifications.add(
                notification(
                    "ticket:" + tid + ":open:" + ts,
                    "info",
                    "Ticket submitted",
                    "Your \"" + issueType + "\" ticket is being reviewed.",
                    ts,
                    null,
                    null));
          }
        }
      }
    } catch (Exception ignored) {
      // don't break notifications if ticket query fails
    }

    // Sort newest-first, cap at 50
    notifications.sort(
        (a, b) -> {
          String ta = String.valueOf(a.getOrDefault("createdAt", ""));
          String tb = String.valueOf(b.getOrDefault("createdAt", ""));
          return tb.compareTo(ta);
        });

    return notifications.subList(0, Math.min(50, notifications.size()));
  }

  private Map<String, Object> notification(
      String id,
      String type,
      String title,
      String message,
      String createdAt,
      String actionLabel,
      String actionHref) {
    Map<String, Object> n = new LinkedHashMap<>();
    n.put("id", id);
    n.put("type", type);
    n.put("title", title);
    n.put("message", message);
    n.put("createdAt", createdAt);
    if (actionLabel != null) {
      Map<String, Object> action = new LinkedHashMap<>();
      action.put("label", actionLabel);
      if (actionHref != null) {
        action.put("href", actionHref);
      }
      n.put("action", action);
    }
    return n;
  }

  private String safeOrderShortId(OrdersEntity o) {
    String external = o.getExternalOrderId() != null ? o.getExternalOrderId().trim() : "";
    if (!external.isEmpty()) {
      return external.length() > 20 ? external.substring(external.length() - 20) : external;
    }
    String id = o.getId().toString();
    return id.length() > 6 ? id.substring(id.length() - 6) : id;
  }

  @SuppressWarnings("unchecked")
  private List<String> parseMissingProofTypes(String json) {
    if (json == null || json.isBlank() || json.equals("[]")) {
      return List.of();
    }
    List<String> result = new ArrayList<>();
    String[] candidates = {"review", "rating"};
    for (String c : candidates) {
      if (json.contains("\"" + c + "\"")) {
        result.add(c);
      }
    }
    return result;
  }
}
