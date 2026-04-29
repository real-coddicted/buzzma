package com.coddicted.buzzma.campaign.api;

import com.coddicted.buzzma.campaign.entity.AssigneeType;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class AssigneeDto {
    AssigneeType assigneeType;
    String assigneeCode;
}
