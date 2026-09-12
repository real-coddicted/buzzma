package com.coddicted.buzzma.claim.template;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.campaign.entity.PromotionCategory;
import com.coddicted.buzzma.shared.exception.BusinessRuleViolationException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ClaimCreationTemplateRegistryTest {

  @Mock private ClaimCreationTemplate mockAppPromotionTemplate;
  @Mock private ClaimCreationTemplate mockEcommerceTemplate;

  @Test
  void testGetReturnsTemplateMatchingCategory() {
    when(this.mockAppPromotionTemplate.category()).thenReturn(PromotionCategory.APP_PROMOTION);
    when(this.mockEcommerceTemplate.category()).thenReturn(PromotionCategory.ECOMMERCE);
    final ClaimCreationTemplateRegistry registry =
        new ClaimCreationTemplateRegistry(
            List.of(this.mockAppPromotionTemplate, this.mockEcommerceTemplate));

    assertEquals(this.mockAppPromotionTemplate, registry.get(PromotionCategory.APP_PROMOTION));
    assertEquals(this.mockEcommerceTemplate, registry.get(PromotionCategory.ECOMMERCE));
  }

  @Test
  void testGetForUnregisteredCategoryThrows() {
    when(this.mockAppPromotionTemplate.category()).thenReturn(PromotionCategory.APP_PROMOTION);
    final ClaimCreationTemplateRegistry registry =
        new ClaimCreationTemplateRegistry(List.of(this.mockAppPromotionTemplate));

    final BusinessRuleViolationException ex =
        assertThrows(
            BusinessRuleViolationException.class,
            () -> registry.get(PromotionCategory.PAGE_PROMOTION));
    assertEquals(
        "No claim creation template registered for category PAGE_PROMOTION", ex.getMessage());
  }
}
