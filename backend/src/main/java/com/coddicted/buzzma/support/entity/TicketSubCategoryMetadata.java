package com.coddicted.buzzma.support.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketSubCategoryMetadata {

  private boolean isOrderIdRequired;
  private boolean isDealIdRequired;
}
