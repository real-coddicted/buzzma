# Campaign Type Step Configuration

## Overview

Each campaign type maps to an ordered list of steps a user must complete to claim a deal. The step configuration is DB-driven — adding or removing steps for an existing campaign type requires only a SQL change, no code deployment.

**Step sequences:**

| Campaign Type | Steps |
|---|---|
| `CAMPAIGN_TYPE_ORDER` | Order → Return Window → Cashback |
| `CAMPAIGN_TYPE_RATING` | Order → Rating → Return Window → Cashback |
| `CAMPAIGN_TYPE_REVIEW` | Order → Rating → Review → Return Window → Cashback |
| `CAMPAIGN_TYPE_DISCOUNT` | Order → Return Window → Cashback |

---

## Design decisions

**DB-driven config:** Steps are stored in the `campaign_type_step` table, seeded by Flyway migration `V0020__add_campaign_type_step_config.sql`. Adding/removing a step for an existing campaign type = SQL only. Adding a brand-new step type (e.g. a future `VIDEO` step) still requires a new `CampaignStepType` enum value.

**Auth-protected API:** `GET /api/v1/campaigns/step-config` returns `Map<String, List<CampaignStepDto>>`. Caller must supply a valid JWT Bearer token. Obtain one via `POST /api/v1/auth/sign-in` first.

**Frontend caching:** `fetchStepConfig()` in `frontend/src/api/campaignApi.ts` uses a module-level promise so only one network request is made per page session regardless of how many components call it.

**Step type drives UI dispatch:** `ClaimStepForm` switches on `step.type` string (`ORDER`, `RATING`, `REVIEW`, `RETURN_WINDOW`, `CASHBACK`) rather than a numeric array index, decoupling rendering from step count.

---

## Backend files

| File | Role |
|---|---|
| `backend/src/main/java/.../campaign/entity/CampaignStepType.java` | Enum: ORDER, RATING, REVIEW, RETURN_WINDOW, CASHBACK — each with a display label |
| `backend/src/main/java/.../campaign/entity/CampaignTypeStepId.java` | `@Embeddable` composite key (campaignType + stepType) |
| `backend/src/main/java/.../campaign/entity/CampaignTypeStep.java` | `@Entity` for `campaign_type_step` table |
| `backend/src/main/java/.../campaign/persistence/CampaignTypeStepRepository.java` | JPA repository |
| `backend/src/main/java/.../campaign/dto/CampaignStepDto.java` | Response record: `{type, label}` |
| `backend/src/main/java/.../campaign/controller/CampaignController.java` | `GET /api/v1/campaigns/step-config` endpoint |
| `backend/src/main/resources/db/migration/V0020__add_campaign_type_step_config.sql` | Creates and seeds the table |

## Frontend files

| File | Change |
|---|---|
| `frontend/src/constants/claimSteps.ts` | `STEP_TYPE_COLORS` map + `toStepperSteps()` helper (replaced hardcoded `CLAIM_STEPS` array) |
| `frontend/src/api/campaignApi.ts` | `CampaignStepDto` type, `StepConfig` type, `fetchStepConfig()` with module-level cache |
| `frontend/src/components/ui/deal/ClaimStepForm.tsx` | Stores `CampaignStepDto[]`, dispatches on `step.type`; separate `RatingStep` and `ReviewStep` components |
| `frontend/src/components/ui/deal/ClaimedDealListItem.tsx` | Fetches step config, passes `StepperStep[]` to inline `Stepper` |
| `frontend/src/components/ui/deal/ClaimedDealDetail.tsx` | Fetches step config, passes `StepperStep[]` to `StepperHeader` |
| `frontend/src/components/ui/deal/ClaimedDealDrawer.tsx` | Same as above |

---

## How to extend

**Add a step to an existing campaign type:**
```sql
INSERT INTO campaign_type_step (campaign_type, step_type, step_order)
VALUES ('CAMPAIGN_TYPE_RATING', 'VIDEO', 3);
-- Also update step_order for steps that should follow it
UPDATE campaign_type_step SET step_order = 4 WHERE campaign_type = 'CAMPAIGN_TYPE_RATING' AND step_type = 'RETURN_WINDOW';
UPDATE campaign_type_step SET step_order = 5 WHERE campaign_type = 'CAMPAIGN_TYPE_RATING' AND step_type = 'CASHBACK';
```

**Remove a step from a campaign type:**
```sql
DELETE FROM campaign_type_step WHERE campaign_type = 'CAMPAIGN_TYPE_REVIEW' AND step_type = 'RATING';
-- Reorder remaining steps if needed
```

**Add a brand-new step type:**
1. Add value to `CampaignStepType` enum in backend
2. Add entry to `STEP_TYPE_COLORS` in `frontend/src/constants/claimSteps.ts`
3. Add a new step sub-component in `ClaimStepForm.tsx` and wire it into the `stepType` switch
4. Seed the DB via a new Flyway migration

**Add a new campaign type:**
1. Add value to `CampaignType` enum in backend (`campaign/entity/CampaignType.java`)
2. Add the same value to the frontend `CampaignType` union in `frontend/src/types/CampaignTypes.ts`
3. Add rows to `campaign_type_step` via a new Flyway migration
