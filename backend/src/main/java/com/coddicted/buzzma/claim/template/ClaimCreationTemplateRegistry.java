package com.coddicted.buzzma.claim.template;

import com.coddicted.buzzma.campaign.entity.PromotionCategory;
import com.coddicted.buzzma.shared.exception.BusinessRuleViolationException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/***
 * Claim creation template registry.
 *
 */
@Component
public class ClaimCreationTemplateRegistry {

  private final Map<PromotionCategory, ClaimCreationTemplate> templatesByCategory;

  public ClaimCreationTemplateRegistry(final List<ClaimCreationTemplate> templates) {
    this.templatesByCategory = new EnumMap<>(PromotionCategory.class);
    for (final ClaimCreationTemplate template : templates) {
      this.templatesByCategory.put(template.category(), template);
    }
  }

  public ClaimCreationTemplate get(final PromotionCategory category) {
    final ClaimCreationTemplate template = this.templatesByCategory.get(category);
    if (template == null) {
      throw new BusinessRuleViolationException(
          "No claim creation template registered for category " + category);
    }
    return template;
  }
}
