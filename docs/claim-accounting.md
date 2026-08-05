# Claim Accounting — Design Document

## Purpose

When a `Claim` is approved, it creates a two-hop money-flow obligation:
1. **Agency → Mediator** (`mediator_receivable_paise`): the agency pays the mediator their referral fee.
2. **Mediator → Buyer** (`buyer_receivable_paise`): the mediator pays the buyer their cashback.

There is no direct agency-to-buyer payment. The `ClaimAccounting` table records computed amounts for both hops and tracks payment status per party, powering the **User Payouts** (agency-facing) and **My Payments** (buyer/mediator-facing) pages.

---

## Entities

### `ClaimAccounting` (`claim_accountings`)

One record per approved `Claim`. The `claim_id` column has a UNIQUE constraint.

| Field | Column | Notes |
|---|---|---|
| `claimId` | `claim_id` | FK → `claims.id` (UNIQUE) |
| `campaignId` | `campaign_id` | Denormalized from claim — for filtering without joins |
| `dealId` | `deal_id` | Denormalized from claim |
| `buyerId` | `buyer_id` | = `claim.owner_id` |
| `mediatorId` | `mediator_id` | = `deal.owner_id` (resolved via `claim.deal_id → deals.owner_id`) |
| `agencyId` | `agency_id` | = `campaign.owner_id` |
| `mediatorReceivablePaise` | `mediator_receivable_paise` | Amount agency owes mediator |
| `buyerReceivablePaise` | `buyer_receivable_paise` | Amount mediator owes buyer |
| `mediatorPaymentStatus` | `mediator_payment_status` | `PENDING` / `PAID` |
| `mediatorPaidAt` | `mediator_paid_at` | Timestamp when hop-1 was confirmed |
| `mediatorPaymentId` | `mediator_payment_id` | FK → `payments.id` (batch covering this claim) |
| `buyerPaymentStatus` | `buyer_payment_status` | `PENDING` / `PAID` |
| `buyerPaidAt` | `buyer_paid_at` | Timestamp when hop-2 was confirmed |
| `buyerPaymentId` | `buyer_payment_id` | FK → `payments.id` |

### `Payment` (`payments`)

One record per payment batch submission. A single `Payment` can link to many `ClaimAccounting` rows (many claims paid together).

| Field | Column | Notes |
|---|---|---|
| `payerId` | `payer_id` | Who is paying — agency (hop-1) or mediator (hop-2) |
| `payeeId` | `payee_id` | Who receives — mediator (hop-1) or buyer (hop-2) |
| `amountPaidPaise` | `amount_paid_paise` | Total amount for this batch |
| `paymentMethod` | `payment_method` | `UPI / BANK / NEFT / IMPS / RTGS / CASH / OTHER` |
| `screenshotStorageKey` | `screenshot_storage_key` | Storage key for payment proof |
| `utrRef` | `utr_ref` | UTR / transaction reference |
| `notes` | `notes` | Free-text notes |
| `paidAt` | `paid_at` | When the payment was made (business timestamp) |

### `AccountingPaymentStatus` (enum)

`PENDING` | `PAID`

### `PaymentMethod` (enum)

`UPI` | `BANK` | `NEFT` | `IMPS` | `RTGS` | `CASH` | `OTHER`

---

## Migrations

- `V0048__create_claim_accountings_table.sql` — creates `claim_accountings` with indexes and UNIQUE on `claim_id`
- `V0049__create_payments_table.sql` — creates `payments` and adds FK constraints from `claim_accountings` back to `payments`

---

## Design Decisions

### Mediator ID source
`mediator_id` = `deals.owner_id`, resolved at computation time via `claim.deal_id → deals.owner_id`. No change to the `claims` table was needed.

### Computation trigger
A **batch job** creates/updates `ClaimAccounting` records for approved claims. Not triggered on claim approval to avoid tight coupling.

### Recalculation guard
If `amount_approved_paise` changes on a claim, the corresponding `ClaimAccounting` record is updated — **unless** either `mediator_payment_status` or `buyer_payment_status` is already `PAID`. In that case the batch job must surface an alert and skip the update.

### Inline status vs separate Payment entity
Payment status + timestamps are stored inline on `ClaimAccounting` (cheap to query on every page load). Receipt details (screenshot, UTR ref, method, notes) live in `Payment` (only fetched on demand). This split allows one `Payment` record to cover multiple claims in the same batch.

### Generic payer/payee on Payment
`Payment` uses `payer_id` / `payee_id` instead of role-specific columns, making the table valid for both payment hops with no schema changes if a third hop were ever added.

### `amount_paid_paise` on Payment is the batch total
Per-claim amounts live on `ClaimAccounting` fields. The "Payments Received" UI must aggregate from `ClaimAccounting.mediator_receivable_paise` / `buyer_receivable_paise`, not by dividing the batch amount.

---

## Page Query Patterns

### User Payouts (agency view)
- List: `SELECT mediator_id, COUNT(*), SUM(mediator_receivable_paise), MIN(created_at) FROM claim_accountings WHERE agency_id = ? AND mediator_payment_status = 'PENDING' GROUP BY mediator_id`
- Pay action: creates a `Payment` record, then updates linked `ClaimAccounting` rows: set `mediator_payment_status = PAID`, `mediator_paid_at`, `mediator_payment_id`

### My Payments — Mediator view
- Received tab: `WHERE mediator_id = me AND mediator_payment_status = 'PAID'` grouped by `mediator_payment_id`
- Awaited tab: `WHERE mediator_id = me AND mediator_payment_status = 'PENDING'` grouped by `agency_id`

### My Payments — Buyer view
- Received tab: `WHERE buyer_id = me AND buyer_payment_status = 'PAID'` grouped by `buyer_payment_id`
- Awaited tab: `WHERE buyer_id = me AND buyer_payment_status = 'PENDING'` grouped by `mediator_id` (mediator owes the buyer, not the agency)
