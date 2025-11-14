# Release 1.0 Context

## System Actors
- **Buyers** – end users purchasing in-game items or services through the marketplace. They fund custodial wallets, browse catalog entries, submit orders, unlock delivered credentials, and raise disputes when needed.
- **Sellers** – shop operators listing products, managing inventory, fulfilling escalations, and responding to disputes.
- **Administrators** – internal staff handling compliance (KYC), cash management, dispute arbitration, and operational reporting.
- **External Services** – identity providers (Google SSO), payment rails (VNPay gateway, VietQR banking network), messaging/email infrastructure, and storage for compliance evidence.

## Data Flows
### Inbound
- VNPay payment notifications with deposit identifiers (`TxnRef`, amount, signature) mapped to buyer wallet top-ups.
- VietQR withdrawal requests uploaded by buyers as transfer proof for manual treasury review.
- Google SSO assertions (OpenID profile, email) to bootstrap account registration/login.
- Seller product data imports (CSV template) to seed shop catalogs.

### Outbound
- Transactional email/OTP payloads for login, order status, and dispute events.
- Escrow/fulfillment state updates surfaced on buyer dashboards and seller consoles.
- Admin exports: compliance audit logs, payout spreadsheets, and dispute resolution summaries.

## Feature Scope Summary
| Domain | Capabilities (Release 1.0) |
| --- | --- |
| Buyer Experience | Wallet balance display and filtering, VNPay deposits, VietQR withdrawal submissions, marketplace search/browse, instant purchase, credential unlock, dispute filing. |
| Seller Console | Shop profile, product CRUD, inventory toggle per variant, order queue visibility, dispute participation, credential stock uploads. |
| Admin Back Office | User KYC review, manual wallet adjustments, payout batch reconciliation, dispute arbitration, system config overrides, reporting extracts. |
| Infrastructure | Google SSO integration, transactional email/OTP delivery, async order processing queue/worker, manual KYC evidence storage, monitoring dashboards. |

## Buyer Journey
1. **Discover & Filter Inventory**
   - Buyers browse catalog listings with filters by game, price range, stock availability, and delivery format. Wallet balance is surfaced alongside listings to indicate purchasing power.
2. **Fund Wallet via VNPay**
   - Wallet screen presents VNPay payment options and generates `TxnRef` per session. Users are redirected to the VNPay gateway; upon completion, the system listens for VNPay IPN callbacks, validates signatures, and posts deposits into the buyer wallet ledger.
3. **Withdraw via VietQR**
   - Buyers request cash-out by submitting target bank details and VietQR confirmation screenshots. Requests enter a manual review queue where admins validate transfer proof before releasing funds.
4. **Place Order**
   - Product detail page issues `Buy Now` POST with idempotency token. `OrderService#placeOrderPending` writes a pending order and publishes an `OrderMessage` into the internal queue.
5. **Asynchronous Fulfillment Pipeline**
   - `InMemoryOrderQueue` dispatches to `AsyncOrderWorker` which locks the order, reserves wallet funds, verifies inventory, and debits the wallet. Successful fulfillment marks the order `Completed`, attaches credentials, and schedules escrow release; failures roll back and mark `Failed` with retry metadata.
6. **Escrow & Dispute Handling**
   - Buyers unlock credentials post-completion. Escrow timers manage auto-release unless a dispute is opened, triggering admin review and seller participation.

## Seller Capabilities
- Maintain shop metadata (branding, contact, policy snippets) and publish/unpublish storefront.
- Create, update, or archive products and variants; upload credential batches; toggle availability per SKU.
- Monitor order queue, respond to fulfillment escalations, and upload replacement credentials when disputes arise.
- Join dispute threads with buyers and admins, providing evidence and resolution proposals.

## Administrator Responsibilities
- Review KYC submissions (identity documents, proof-of-account) and approve or reject users before high-value activity.
- Perform manual wallet adjustments (cash deposits, chargebacks) and reconcile VNPay payouts versus internal ledgers.
- Validate VietQR withdrawal evidence and initiate manual payouts through banking portals.
- Chair dispute resolution, recording outcomes and applying restitution or penalties.
- Produce operational reports (KPI dashboards, ledger exports) for finance and compliance stakeholders.

## External Integrations & Trust Requirements
- **Google SSO** – OpenID authentication; requires secure handling of client secrets and refresh tokens.
- **Transactional Email & OTP** – integration with SMTP/OTP provider to deliver login verifications, order receipts, and dispute notices.
- **VNPay Gateway** – payment initiation plus IPN validation; ensure checksum secrecy and TLS pinning where available.
- **VietQR / Banking** – manual verification of uploaded QR confirmation for withdrawals; evidence stored in secure blob storage.
- **Manual KYC Repository** – encrypted storage for identity documents with access logging.

### Current Constraints
- VNPay withdrawals/payouts are executed manually via treasury team; no automated transfer API yet.
- KYC review and evidence capture are human-driven with secure manual uploads.

### Deferred Roadmap Items
- Automated payout integration (e.g., VNPay disbursement API or partner bank bulk transfer).
- In-app KYC verification workflow with third-party identity provider.
- Multi-channel notification orchestration (push, SMS) beyond email/OTP.
- Queue persistence via external broker (Redis/RabbitMQ) replacing in-memory implementation.

