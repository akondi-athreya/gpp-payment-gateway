# System Architecture

## High-Level Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                        Client Applications                       │
├─────────────────────┬──────────────────┬────────────────────────┤
│   Dashboard UI      │   Checkout Page  │    API Consumers       │
│  (React + Vite)     │  (React + Vite)  │   (Mobile/Web)         │
└──────────┬──────────┴────────┬─────────┴────────────┬───────────┘
           │                   │                      │
           │ HTTP/REST         │ HTTP/REST           │ HTTP/REST
           │                   │                      │
┌──────────▼───────────────────▼──────────────────────▼───────────┐
│                      Spring Boot API (Java 21)                   │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │ Controllers                                              │   │
│  │  • OrderController  /api/v1/orders                      │   │
│  │  • PaymentController /api/v1/payments                   │   │
│  │  • HealthController /health                            │   │
│  │  • TestController /api/v1/test/merchant               │   │
│  └──────────┬───────────────────────────────────────────────┘   │
│             │                                                    │
│  ┌──────────▼───────────────────────────────────────────────┐   │
│  │ Services                                                 │   │
│  │  • OrderService: create/retrieve orders                 │   │
│  │  • PaymentService: process payments (UPI/Card)          │   │
│  │  • ValidationService: validate VPA, card, expiry        │   │
│  │  • WorkerStatusService: async task status              │   │
│  └──────────┬───────────────────────────────────────────────┘   │
│             │                                                    │
│  ┌──────────▼───────────────────────────────────────────────┐   │
│  │ Repositories (JPA)                                       │   │
│  │  • MerchantRepository                                    │   │
│  │  • OrderRepository                                       │   │
│  │  • PaymentRepository                                     │   │
│  └──────────┬───────────────────────────────────────────────┘   │
└─────────────┼────────────────────────────────────────────────────┘
              │
              │ JDBC/Connections
              │
┌─────────────▼──────────┬──────────────────┬──────────────────┐
│   PostgreSQL 15        │    Redis 7       │  Worker Thread   │
│   (Persistence)        │   (Cache/Queue)  │   (Async)        │
│                        │                  │                  │
│  • merchants           │  • Session cache │  • Process async │
│  • orders              │  • Task queue    │  • Handle delays │
│  • payments            │  • Locks         │                  │
└────────────────────────┴──────────────────┴──────────────────┘
```

## Data Flow: Order Creation

```
Client Request
    │
    ▼
┌─────────────────────────────────┐
│ Validate API Key & Secret       │
│ (MerchantRepository.find)       │
└────────┬────────────────────────┘
         │
         ▼
┌─────────────────────────────────┐
│ Create Order Entity             │
│ • Generate order_* ID           │
│ • Set merchant reference        │
│ • Amount in paise               │
│ • Status: "created"             │
└────────┬────────────────────────┘
         │
         ▼
┌─────────────────────────────────┐
│ Save to PostgreSQL              │
│ (OrderRepository.save)          │
└────────┬────────────────────────┘
         │
         ▼
┌─────────────────────────────────┐
│ Return OrderResponse DTO        │
│ (JSON to client)                │
└─────────────────────────────────┘
```

## Data Flow: Payment Processing

```
Client Request (Order ID, Method, Details)
    │
    ▼
┌─────────────────────────────────┐
│ Validate API Key & Secret       │
│ (Auth check)                    │
└────────┬────────────────────────┘
         │
         ▼
┌─────────────────────────────────┐
│ Lookup Order                    │
│ (Verify exists & belongs to     │
│  merchant)                      │
└────────┬────────────────────────┘
         │
         ▼
┌─────────────────────────────────┐
│ Validate Payment Details        │
│ • UPI: Regex validation (VPA)   │
│ • Card: Luhn + Expiry check     │
└────────┬────────────────────────┘
         │
         ▼
┌─────────────────────────────────┐
│ Create Payment (status=         │
│ "processing")                   │
│ Generate pay_* ID               │
└────────┬────────────────────────┘
         │
         ▼
┌─────────────────────────────────┐
│ Save to PostgreSQL              │
└────────┬────────────────────────┘
         │
         ▼ (async in background)
┌─────────────────────────────────┐
│ Enqueue Worker Task             │
│ • Sleep 5-10 seconds            │
│ • Roll success dice:            │
│   - UPI: 90%                    │
│   - Card: 95%                   │
└────────┬────────────────────────┘
         │
         ▼
┌─────────────────────────────────┐
│ Update Payment Status           │
│ • status: "success" or "failed" │
│ • updated_at timestamp          │
└────────┬────────────────────────┘
         │
         ▼
┌─────────────────────────────────┐
│ Return PaymentResponse DTO      │
│ (Initially "processing", client │
│  polls for final status)        │
└─────────────────────────────────┘
```

## Container Orchestration

```
Docker Compose Stack
│
├── postgres:15
│   ├── Port: 5432
│   ├── Volume: postgres_data
│   └── Health: pg_isready
│
├── redis:7
│   ├── Port: 6379
│   └── Health: redis-cli ping
│
├── api (Spring Boot)
│   ├── Build: ./backend/DockerFile
│   ├── Port: 8000
│   ├── Depends: postgres (healthy), redis (healthy)
│   └── Env: DATABASE_URL, REDIS_HOST, etc.
│
├── dashboard (React + Nginx)
│   ├── Build: ./frontend/Dockerfile
│   ├── Port: 3000
│   ├── Depends: api
│   └── Serves: dist/ on :80
│
└── checkout (React + Nginx)
    ├── Build: ./checkout-page/Dockerfile
    ├── Port: 3001
    ├── Depends: api
    └── Serves: dist/ on :80
```

## Authentication Flow

```
Request with Headers
│
├── X-Api-Key: key_test_abc123
├── X-Api-Secret: secret_test_xyz789
│
    ▼
┌────────────────────────────────────┐
│ OrderController / PaymentController │
│ Extract headers                    │
└────────┬─────────────────────────────┘
         │
         ▼
┌────────────────────────────────────┐
│ OrderService.createOrder /         │
│ PaymentService.createPayment       │
│ (Pass key & secret)                │
└────────┬─────────────────────────────┘
         │
         ▼
┌────────────────────────────────────┐
│ MerchantRepository                 │
│ .findByApiKeyAndApiSecret          │
│ (Custom JPQL query)                │
└────────┬─────────────────────────────┘
         │
    ┌────┴────┐
    ▼         ▼
Found      Not Found
  │          │
  ▼          ▼
Proceed   Return 401
          Unauthorized
```

## File Organization

```
payment-gateway/
│
├── backend/
│   ├── src/main/java/com/example/gateway/
│   │   ├── PaymentGatewayApplication.java (entry point)
│   │   │
│   │   ├── config/
│   │   │   ├── DataSeederConfig.java (seed test merchant)
│   │   │   ├── JacksonConfig.java (JSON/ObjectMapper)
│   │   │   └── SecurityConfig.java (CORS, security)
│   │   │
│   │   ├── controllers/
│   │   │   ├── HealthController.java (/health)
│   │   │   ├── OrderController.java (/api/v1/orders)
│   │   │   ├── PaymentController.java (/api/v1/payments)
│   │   │   └── TestController.java (/api/v1/test/merchant)
│   │   │
│   │   ├── services/
│   │   │   ├── OrderService.java
│   │   │   ├── PaymentService.java (async + validation)
│   │   │   ├── ValidationService.java (Luhn, VPA regex)
│   │   │   └── WorkerStatusService.java (task executor)
│   │   │
│   │   ├── repositories/
│   │   │   ├── MerchantRepository.java (custom query)
│   │   │   ├── OrderRepository.java
│   │   │   └── PaymentRepository.java
│   │   │
│   │   ├── models/
│   │   │   ├── Merchant.java (UUID, API key/secret)
│   │   │   ├── Order.java (string ID: order_*)
│   │   │   ├── Payment.java (string ID: pay_*)
│   │   │   ├── PaymentMethod.java (enum: UPI, CARD)
│   │   │   └── CardNetwork.java (enum: VISA, MASTERCARD, AMEX, RUPAY)
│   │   │
│   │   ├── dto/
│   │   │   ├── CreateOrderRequest.java
│   │   │   ├── OrderResponse.java
│   │   │   ├── CreatePaymentRequest.java
│   │   │   ├── PaymentResponse.java
│   │   │   └── ErrorResponse.java
│   │   │
│   │   ├── exceptions/
│   │   │   └── GlobalExceptionHandler.java (format errors)
│   │   │
│   │   └── workers/
│   │       └── WorkerStatusService.java
│   │
│   ├── src/main/resources/
│   │   └── application.properties (Spring config, env vars)
│   │
│   ├── DockerFile (multi-stage: build + runtime)
│   └── pom.xml (Maven dependencies)
│
├── frontend/
│   ├── src/
│   │   ├── App.jsx (React Router)
│   │   ├── pages/
│   │   │   ├── Login.jsx (auth demo)
│   │   │   ├── Dashboard.jsx (stats, transactions)
│   │   │   └── Transactions.jsx (payments list)
│   │   ├── services/
│   │   │   └── api.js (axios, base URL)
│   │   ├── App.css
│   │   └── index.css
│   ├── Dockerfile (Vite build + Nginx)
│   └── package.json
│
├── checkout-page/
│   ├── src/
│   │   ├── pages/
│   │   │   └── Checkout.jsx (order fetch, method selection)
│   │   ├── components/
│   │   │   ├── UPIPaymentForm.jsx
│   │   │   └── CardPaymentForm.jsx
│   │   └── services/
│   │       └── api.js
│   ├── Dockerfile (Vite build + Nginx)
│   └── package.json
│
├── docker-compose.yml (postgres, redis, api, dashboard, checkout)
├── .env.example (template)
└── README.md (this guide)
```

## Key Design Decisions

1. **Amounts in Paise**: All amounts are stored as integers (paise) to avoid floating-point precision issues.
2. **String IDs with Prefix**: Order IDs start with `order_`, payment IDs with `pay_` for clarity and debugging.
3. **Async Processing**: Payments start as `processing` and transition to `success`/`failed` after delay.
4. **Configurable Success Rates**: UPI ~90%, Card ~95% in production; can be overridden for testing.
5. **Redis Dependency**: Required for health checks by default; optional via env flag.
6. **Public API for Checkout**: Unauthenticated endpoints let checkout page fetch orders and create payments without keys.
7. **Validation Service**: Centralized validation (Luhn, VPA regex, expiry) to avoid duplication.

