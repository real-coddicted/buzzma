package com.coddicted.buzzma.campaign.step;

import com.coddicted.buzzma.campaign.entity.CampaignStepType;
import com.coddicted.buzzma.claim.entity.ScreenshotType;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Looks up the {@link StepDefinition} for a given {@link CampaignStepType} or {@link
 * ScreenshotType} in O(1).
 */
@Component
public class StepDefinitionRegistry {

  private final Map<CampaignStepType, StepDefinition> definitionsByStepType;
  private final Map<ScreenshotType, StepDefinition> definitionsByScreenshotType;

  public StepDefinitionRegistry(final List<StepDefinition> definitions) {
    this.definitionsByStepType =
        definitions.stream()
            .collect(Collectors.toMap(StepDefinition::stepType, Function.identity()));
    this.definitionsByScreenshotType =
        definitions.stream()
            .filter(definition -> definition.screenshotType().isPresent())
            .collect(
                Collectors.toMap(
                    definition -> definition.screenshotType().orElseThrow(), Function.identity()));
  }

  public StepDefinition get(final CampaignStepType stepType) {
    final StepDefinition definition = this.definitionsByStepType.get(stepType);
    if (definition == null) {
      throw new IllegalStateException("No StepDefinition registered for step type: " + stepType);
    }
    return definition;
  }

  public StepDefinition get(final ScreenshotType screenshotType) {
    final StepDefinition definition = this.definitionsByScreenshotType.get(screenshotType);
    if (definition == null) {
      throw new IllegalStateException(
          "No StepDefinition registered for screenshot type: " + screenshotType);
    }
    return definition;
  }
}
