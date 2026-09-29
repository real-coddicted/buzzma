package com.coddicted.buzzma.extraction.service;

import com.coddicted.buzzma.shared.enums.Platform;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class GeminiExtractionPromptBuilder {

  private static final String PLATFORM_VALUES =
      Arrays.stream(Platform.values()).map(Enum::name).collect(Collectors.joining("|"));

  private static final String PROMPT =
      """
      You are an order-data extractor. Analyze the provided e-commerce order screenshot and \
      return ONLY valid JSON with no markdown fences, no extra text, and no explanation. \
      The JSON must match this exact schema:
      {
        "platform": "<%s|null>",
        "orderId": "<order identifier string or null>",
        "orderDate": "<YYYY-MM-DD or null>",
        "productName": "<product name string or null>",
        "sellerName": "<seller or sold-by name string or null>",
        "amount": <total order amount as a number without currency symbol, or null>,
        "orderedBy": "<customer full name or null>"
      }
      Use null for any field that cannot be clearly determined from the image."""
          .formatted(PLATFORM_VALUES);

  private static final String RATING_PROMPT =
      """
      You are a rating-data extractor. Analyze the provided screenshot of a 5-star rating UI \
      and return ONLY valid JSON with no markdown fences, no extra text, and no explanation. \
      The JSON must match this exact schema:
      {
        "platform": "<%s|null>",
        "productName": "<product name string or null>",
        "accountName": "<the account or user name visible in the screenshot, or null>",
        "rating": <the numeric star rating given by the user as an integer between 1 and 5, or null>
      }
      Use null for any field that cannot be clearly determined from the image."""
          .formatted(PLATFORM_VALUES);

  private static final String REVIEW_PROMPT =
      """
      You are a review-data extractor. Analyze the provided screenshot of a product review and \
      return ONLY valid JSON with no markdown fences, no extra text, and no explanation. \
      The JSON must match this exact schema:
      {
        "platform": "<%s|null>",
        "productName": "<product name string or null>",
        "reviewText": "<full text of the review provided by the customer, or null>",
        "accountName": "<the account or user name of the reviewer, or null>",
        "reviewDate": "<date the review was posted in YYYY-MM-DD format, or null>",
        "reviewUrl": "<the URL visible in the browser's address bar, or null if the address bar is not visible in the screenshot>"
      }
      Use null for any field that cannot be clearly determined from the image."""
          .formatted(PLATFORM_VALUES);

  private static final String RETURN_PROMPT =
      """
      You are a return-window-data extractor. Analyze the provided screenshot showing product \
      return information and return ONLY valid JSON with no markdown fences, no extra text, and \
      no explanation. The JSON must match this exact schema:
      {
        "platform": "<%s|null>",
        "productName": "<product name string or null>",
        "accountName": "<the account or user name visible in the screenshot, or null>",
        "returnWindowClosedText": "<the exact text or label confirming the return window has closed, or null>",
        "returnWindowClosedDate": "<the date when the return window closed in YYYY-MM-DD format, or null>"
      }
      Use null for any field that cannot be clearly determined from the image."""
          .formatted(PLATFORM_VALUES);

  private static final String DELIVERY_PROMPT =
      """
      You are a delivery-proof-data extractor. Analyze the provided screenshot showing an order's \
      delivery status (e.g. a "Delivered" order tracking page) and return ONLY valid JSON with no \
      markdown fences, no extra text, and no explanation. The JSON must match this exact schema:
      {
        "platform": "<%s|null>",
        "productName": "<product name string or null>",
        "orderId": "<order identifier string or null>",
        "deliveryDate": "<the date the order was delivered in YYYY-MM-DD format, or null>",
        "deliveryStatus": "<the delivery status text shown (e.g. 'Delivered'), or null>",
        "orderedBy": "<customer full name or null>"
      }
      Extract only what is clearly visible; use null for any field that cannot be determined \
      from the image."""
          .formatted(PLATFORM_VALUES);

  private static final String SELLER_FEEDBACK_PROMPT =
      """
      You are a seller-feedback-data extractor. Analyze the provided screenshot of a "Leave \
      Seller Feedback" or "Rate your experience" UI and return ONLY valid JSON with no markdown \
      fences, no extra text, and no explanation. The JSON must match this exact schema:
      {
        "platform": "<%s|null>",
        "sellerName": "<the seller or sold-by name string, or null>",
        "productName": "<product name string or null>",
        "orderId": "<order identifier string or null>",
        "rating": <the numeric star rating given to the seller as an integer between 1 and 5, or null>,
        "feedbackText": "<the short feedback label visible in the screenshot (e.g. 'Excellent', 'Good', 'Fair', 'Poor'), or null>",
        "comment": "<the full free-text comment the customer wrote about the seller, if any (e.g. under a 'Comments' heading), or null>",
        "reviewerName": "<the name of the customer/reviewer who left the feedback, or null>"
      }
      Use null for any field that cannot be clearly determined from the image."""
          .formatted(PLATFORM_VALUES);

  private static final String LIKE_PROMPT =
      """
      You are a social-engagement-data extractor. Analyze the provided screenshot of a \
      YouTube video page or Instagram post where the user has liked the content and return \
      ONLY valid JSON with no markdown fences, no extra text, and no explanation. \
      The JSON must match this exact schema:
      {
        "platform": "<%s|null>",
        "accountName": "<the logged-in user's account or channel name visible in the screenshot, or null>",
        "contentTitle": "<the video title or post caption/description, or null>",
        "likeStatus": "<'liked' if the thumbs-up icon (YouTube) or heart icon (Instagram) appears solid/filled/active, 'not_liked' if it appears as an outline/inactive, or null>"
      }
      Use null for any field that cannot be clearly determined from the image."""
          .formatted(PLATFORM_VALUES);

  private static final String VIEW_PROMPT =
      """
      You are a social-engagement-data extractor. Analyze the provided screenshot showing \
      that the user has viewed a YouTube video or Instagram post and return ONLY valid JSON \
      with no markdown fences, no extra text, and no explanation. \
      The JSON must match this exact schema:
      {
        "platform": "<%s|null>",
        "accountName": "<the logged-in user's account or channel name visible in the screenshot, or null>",
        "contentTitle": "<the video title or post caption/description, or null>",
        "contentUrl": "<the URL visible in the browser's address bar, or null if the address bar is not visible>"
      }
      Use null for any field that cannot be clearly determined from the image."""
          .formatted(PLATFORM_VALUES);

  private static final String COMMENT_PROMPT =
      """
      You are a social-engagement-data extractor. Analyze the provided screenshot of a \
      YouTube video or Instagram post where the user has left a comment and return ONLY \
      valid JSON with no markdown fences, no extra text, and no explanation. \
      The JSON must match this exact schema:
      {
        "platform": "<%s|null>",
        "accountName": "<the logged-in user's account or channel name visible in the screenshot, or null>",
        "contentTitle": "<the video title or post caption/description, or null>",
        "commentText": "<the full text of the comment left by the user, or null>"
      }
      Use null for any field that cannot be clearly determined from the image."""
          .formatted(PLATFORM_VALUES);

  private static final String SUBSCRIBE_PROMPT =
      """
      You are a social-engagement-data extractor. Analyze the provided screenshot of a \
      YouTube channel page or video page where the user has subscribed and return ONLY \
      valid JSON with no markdown fences, no extra text, and no explanation. \
      The JSON must match this exact schema:
      {
        "platform": "<%s|null>",
        "accountName": "<the logged-in user's account or channel name visible in the screenshot, or null>",
        "channelName": "<the name of the YouTube channel the user subscribed to, or null>",
        "subscribeStatus": "<'subscribed' if the Subscribe button shows a bell icon or 'Subscribed' text or 'Notifications' dropdown, 'not_subscribed' if the button still shows 'Subscribe' in its default state, or null>"
      }
      Use null for any field that cannot be clearly determined from the image."""
          .formatted(PLATFORM_VALUES);

  private static final String FOLLOW_PROMPT =
      """
      You are a social-engagement-data extractor. Analyze the provided screenshot of an \
      Instagram profile or post where the user has followed the account and return ONLY \
      valid JSON with no markdown fences, no extra text, and no explanation. \
      The JSON must match this exact schema:
      {
        "platform": "<%s|null>",
        "accountName": "<the logged-in user's account or channel name visible in the screenshot, or null>",
        "followedAccount": "<the name or handle of the account being followed, or null>",
        "followStatus": "<'following' if the button shows 'Following' or 'Requested' or a person-check icon, 'not_following' if the button still shows 'Follow' in its default state, or null>"
      }
      Use null for any field that cannot be clearly determined from the image."""
          .formatted(PLATFORM_VALUES);

  private static final String DOWNLOAD_INSTALL_PROMPT =
      """
      You are an app-install-data extractor. Analyze the provided screenshot of an app store \
      listing (Google Play Store or Apple App Store) for the installed app and return ONLY valid \
      JSON with no markdown fences, no extra text, and no explanation. The JSON must match this \
      exact schema:
      {
        "platform": "<%s|null>",
        "productName": "<the app name string or null>",
        "accountName": "<the account or user name visible in the screenshot, or null>",
        "installStatus": "<the exact button or status text confirming the app is installed (e.g. 'Open', 'Installed', 'Uninstall', 'Update'), or null>",
        "appVersion": "<the app's version number as shown in the listing, or null>"
      }
      Use null for any field that cannot be clearly determined from the image."""
          .formatted(PLATFORM_VALUES);

  public String build() {
    return PROMPT;
  }

  public String buildRatingPrompt() {
    return RATING_PROMPT;
  }

  public String buildSellerFeedbackPrompt() {
    return SELLER_FEEDBACK_PROMPT;
  }

  public String buildReviewPrompt() {
    return REVIEW_PROMPT;
  }

  public String buildReturnPrompt() {
    return RETURN_PROMPT;
  }

  public String buildDeliveryPrompt() {
    return DELIVERY_PROMPT;
  }

  public String buildDownloadInstallPrompt() {
    return DOWNLOAD_INSTALL_PROMPT;
  }

  public String buildLikePrompt() {
    return LIKE_PROMPT;
  }

  public String buildViewPrompt() {
    return VIEW_PROMPT;
  }

  public String buildCommentPrompt() {
    return COMMENT_PROMPT;
  }

  public String buildSubscribePrompt() {
    return SUBSCRIBE_PROMPT;
  }

  public String buildFollowPrompt() {
    return FOLLOW_PROMPT;
  }
}
