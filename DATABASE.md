# Database Schema Documentation

## Overview
The payment gateway uses PostgreSQL 15 for persistence with 4 main entities: Merchant, Order, Payment, and supporting enums.

## Tables

### `merchants`
Represents payment gateway users (merchants).

| Column | Type | Constraints | Description |
|--------|------|-----------|-------------|
| `id` | UUID | PK | Unique merchant identifier |
| `api_key` | VARCHAR(255) | UNIQUE, NOT NULL | API authentication key |
| `api_secret` | VARCHAR(255) | NOT NULL | API authentication secret |
| `email` | VARCHAR(255) | UNIQUE, NOT NULL | Merchant email |
| `name` | VARCHAR(255) | NOT NULL | Merchant business name |
| `webhook_url` | VARCHAR(255) | NULL | Webhook endpoint for events |
| `is_active` | BOOLEAN | DEFAULT true | Account status |
| `created_at` | TIMESTAMPTZ | DEFAULT NOW() | Creation timestamp |
| `updated_at` | TIMESTAMPTZ | DEFAULT NOW() | Last update timestamp |

**Indexes:**
- PK on `id`
- UNIQUE on `api_key`
- UNIQUE on `email`

---

### `orders`
Represents payment orders created by merchants.

| Column | Type | Constraints | Description |
|--------|------|-----------|-------------|
| `id` | VARCHAR(32) | PK | Order identifier (format: `order_*`, 16 random chars) |
| `merchant_id` | UUID | FK → merchants(id) | Parent merchant |
| `amount` | BIGINT | NOT NULL, ≥ 100 | Amount in paise (integers) |
| `currency` | VARCHAR(3) | DEFAULT 'INR' | Currency code |
| `receipt` | VARCHAR(255) | NOT NULL | External receipt reference |
| `notes` | TEXT (JSON) | NULL | Custom key-value notes |
| `status` | VARCHAR(50) | DEFAULT 'created' | Order status |
| `created_at` | TIMESTAMPTZ | DEFAULT NOW() | Creation timestamp |
| `updated_at` | TIMESTAMPTZ | DEFAULT NOW() | Last update timestamp |

**Indexes:**
- PK on `id`
- FK on `merchant_id`
- INDEX on `merchant_id` for filtering by merchant
- INDEX on `status` for status queries

---

### `payments`
Represents payment transactions initiated against orders.

| Column | Type | Constraints | Description |
|--------|------|-----------|-------------|
| `id` | VARCHAR(32) | PK | Payment identifier (format: `pay_*`, 16 random chars) |
| `order_id` | VARCHAR(32) | FK → orders(id) | Associated order |
| `merchant_id` | UUID | FK → merchants(id) | Parent merchant |
| `amount` | BIGINT | NOT NULL | Amount in paise (from order) |
| `currency` | VARCHAR(3) | DEFAULT 'INR' | Currency |
| `method` | VARCHAR(10) | NOT NULL | Payment method (UPI, CARD) - *Enum* |
| `status` | VARCHAR(50) | NOT NULL | Payment status (processing, success, failed) |
| `vpa` | VARCHAR(255) | NULL | UPI Virtual Payment Address |
| `card_number` | VARCHAR(50) | NULL | **NOT STORED** — ignored on save |
| `card_last4` | VARCHAR(4) | NULL | Last 4 digits of card (if card) |
| `card_network` | VARCHAR(20) | NULL | Card network (VISA, MASTERCARD, AMEX, RUPAY, UNKNOWN) - *Enum* |
| `cvv` | VARCHAR(10) | NULL | **NOT STORED** — ignored on save |
| `holder_name` | VARCHAR(255) | NULL | Cardholder name |
| `created_at` | TIMESTAMPTZ | DEFAULT NOW() | Creation timestamp |
| `updated_at` | TIMESTAMPTZ | DEFAULT NOW() | Last update timestamp |

**Indexes:**
- PK on `id`
- FK on `order_id`
- FK on `merchant_id`
- INDEX on `order_id` for order lookups
- INDEX on `merchant_id` for merchant filtering
- INDEX on `status` for transaction queries

**Security Notes:**
- Full card numbers and CVV are **never persisted**
- Only last 4 digits and detected network are retained
- No PCI compliance burden on storage

---

## Enums (PostgreSQL Custom Types)

### `payment_method`
```sql
CREATE TYPE payment_method AS ENUM ('upi', 'card');
```
Used in `payments.method` column.

### `card_network`
```sql
CREATE TYPE card_network AS ENUM ('visa', 'mastercard', 'amex', 'rupay', 'unknown');
```
Used in `payments.card_network` column. Auto-detected via Luhn algorithm.

---

## Relationships

```
merchants (1)
  ├─── (1:N) ──→ orders
  └─── (1:N) ──→ payments

orders (1)
  └─── (1:N) ──→ payments
```

- **Merchant ↔ Order**: One merchant creates many orders
- **Merchant ↔ Payment**: One merchant receives many payments (across all orders)
- **Order ↔ Payment**: One order has many payment attempts

---

## Data Flow

1. **Order Creation**: Merchant calls `POST /api/v1/orders` with amount (paise), currency, receipt, notes
   - New row inserted into `orders` table
   - Status: `created`
   - Amount stored as integer (no float precision loss)

2. **Payment Creation**: Merchant calls `POST /api/v1/payments` with order_id, payment method, and method-specific details
   - New row inserted into `payments` table
   - Status: `processing` (not `created`)
   - Sensitive fields (card_number, cvv) are **not persisted**
   - Card network auto-detected via Luhn check
   - VPA validated via regex

3. **Payment Processing** (Async Background Task):
   - Worker sleeps 5-10 seconds
   - Rolls success dice (UPI: 90%, Card: 95%)
   - Updates `payments.status` to `success` or `failed`
   - Updates `payments.updated_at` timestamp

4. **Status Queries**: Clients poll `GET /api/v1/payments/{payment_id}` to fetch final status

---

## Constraints & Validations

| Table | Column | Constraint | Details |
|-------|--------|-----------|---------|
| orders | amount | ≥ 100 | Minimum 100 paise (~₹1.00) |
| orders | merchant_id | NOT NULL | Every order belongs to a merchant |
| payments | amount | NOT NULL | Payment amount required |
| payments | method | IN (upi, card) | Must be valid payment method |
| payments | status | IN (processing, success, failed) | Lifecycle: processing → (success\|failed) |
| merchants | api_key | UNIQUE | No duplicate API keys |
| merchants | email | UNIQUE | One account per email |

---

## Indexes Summary

- **merchants**: PK(id), UNIQUE(api_key), UNIQUE(email)
- **orders**: PK(id), FK(merchant_id), INDEX(merchant_id), INDEX(status)
- **payments**: PK(id), FK(order_id), FK(merchant_id), INDEX(order_id), INDEX(merchant_id), INDEX(status)

**Performance**: Indexes optimized for:
- Merchant authentication (api_key lookup)
- Order/payment listing by merchant
- Status filtering (transactions, pending payments)

---

## Test Data

**Pre-seeded Test Merchant** (from DataSeederConfig):
```sql
INSERT INTO merchants (id, api_key, api_secret, email, name, is_active, created_at, updated_at)
VALUES (
  '550e8400-e29b-41d4-a716-446655440000',
  'key_test_abc123',
  'secret_test_xyz789',
  'test@example.com',
  'Test Merchant',
  true,
  NOW(),
  NOW()
);
```

This merchant is created automatically on application startup and is ready for testing.

