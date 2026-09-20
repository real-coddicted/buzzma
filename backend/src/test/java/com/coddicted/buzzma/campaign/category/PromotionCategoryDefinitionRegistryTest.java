package com.coddicted.buzzma.campaign.category;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.coddicted.buzzma.campaign.entity.PromotionCategory;
import java.util.List;
import org.junit.jupiter.api.Test;

class PromotionCategoryDefinitionRegistryTest {

  @Test
  void testGetReturnsDefinitionMatchingCategory() {
    final PromotionCategoryDefinitionRegistry registry =
        new PromotionCategoryDefinitionRegistry(
            List.of(
                new EcommercePromotionCategoryDefinition(),
                new QuickCommercePromotionCategoryDefinition(),
                new AppPromotionCategoryDefinition()));

    assertEquals(PromotionCategory.ECOMMERCE, registry.get(PromotionCategory.ECOMMERCE).category());
    assertEquals(
        PromotionCategory.APP_PROMOTION, registry.get(PromotionCategory.APP_PROMOTION).category());
  }

  @Test
  void testGetForUnregisteredCategoryThrows() {
    final PromotionCategoryDefinitionRegistry registry =
        new PromotionCategoryDefinitionRegistry(
            List.of(new EcommercePromotionCategoryDefinition()));

    final IllegalStateException ex =
        assertThrows(
            IllegalStateException.class, () -> registry.get(PromotionCategory.APP_PROMOTION));
    assertEquals(
        "No PromotionCategoryDefinition registered for category: APP_PROMOTION", ex.getMessage());
  }
}
