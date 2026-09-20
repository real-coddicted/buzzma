package com.coddicted.buzzma.campaign.category;

import com.coddicted.buzzma.campaign.entity.PromotionCategory;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Looks up the {@link PromotionCategoryDefinition} for a given {@link PromotionCategory} in O(1).
 */
@Component
public class PromotionCategoryDefinitionRegistry {

  private final Map<PromotionCategory, PromotionCategoryDefinition> definitionsByCategory;

  public PromotionCategoryDefinitionRegistry(final List<PromotionCategoryDefinition> definitions) {
    this.definitionsByCategory =
        definitions.stream()
            .collect(Collectors.toMap(PromotionCategoryDefinition::category, Function.identity()));
  }

  public PromotionCategoryDefinition get(final PromotionCategory category) {
    final PromotionCategoryDefinition definition = this.definitionsByCategory.get(category);
    if (definition == null) {
      throw new IllegalStateException(
          "No PromotionCategoryDefinition registered for category: " + category);
    }
    return definition;
  }
}
