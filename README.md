# Coupon Vault

Peer-to-peer **coupon marketplace** with automated (mock) validation, JWT auth, Spring Boot REST API, and a Next.js store-centric UI.

## Architecture

| Layer | Stack |
|--------|--------|
| **Backend** | Java 17, Spring Boot 3.4, Spring Security + JWT, Spring Data JPA, PostgreSQL |
| **Frontend** | Next.js 14 (App Router), TypeScript, Tailwind CSS |
| **Validation** | Pluggable `StoreValidationStrategy` — default mock: codes starting with `VALID-` pass |
| **Payments** | `PaymentService` mock — succeeds unless the coupon id contains `FAILPAY` (for demos) |

## Quick start

### 1. Database

**Default — Neon (serverless PostgreSQL)**  
The app is configured for **[Neon](https://neon.tech)** by default. At startup it loads `backend/.env` (if present) so secrets stay out of Git.

1. Copy `backend/.env.example` to `backend/.env`.
2. Set `NEON_DATABASE_URL`, `NEON_DATABASE_USERNAME`, and `NEON_DATABASE_PASSWORD` from your Neon project (**Dashboard → Connection string**). Use a **JDBC** URL shape:

   `jdbc:postgresql://YOUR-HOST.neon.tech:5432/neondb?sslmode=require&channel_binding=require`

3. Run the API from the **`backend`** folder so `.env` is found:

```powershell
cd backend
mvn spring-boot:run
```

API: **http://localhost:8080**

You can also set the same `NEON_*` variables in your OS or IDE instead of using `.env`.

**Security:** If a database password was ever pasted in chat or committed, **rotate it in the Neon console** and update `backend/.env`.

**Option A — H2 only (no cloud DB)**  
Embedded **H2**; data is cleared when the process exits.

```powershell
cd backend
mvn spring-boot:run "-Dspring-boot.run.profiles=dev"
```

**Option B — Local PostgreSQL (Docker)**  
`docker compose up -d`, then set `NEON_DATABASE_*` or override `spring.datasource.*` to point at `localhost` (see `docker-compose.yml`).

### 2. Backend (summary)

| Variable | Purpose |
|----------|---------|
| `NEON_DATABASE_URL` | JDBC URL (Neon pooler host, `sslmode=require`, etc.) |
| `NEON_DATABASE_USERNAME` | Neon role (e.g. `neondb_owner`) |
| `NEON_DATABASE_PASSWORD` | Neon password |
| `JWT_SECRET` | Optional; HS256 secret (default in `application.properties` is long enough for local dev) |

Run from `backend`: `mvn spring-boot:run` (uses Neon + `.env` unless `dev` profile is set).

API base: **http://localhost:8080**

### 3. Frontend

```bash
cd frontend
copy .env.local.example .env.local   # Windows; adjust URL if needed
npm install
npm run dev
```

App: **http://localhost:3000**

Set `NEXT_PUBLIC_API_URL` to your API origin if not `http://localhost:8080`.

## Seed data (dev)

With the default profile (not `test`), on first empty DB the app seeds:

- **alice@example.com** / `password123` — seller  
- **bob@example.com** / `password123` — buyer  
- Store **MegaStore** and sample coupons (`VALID-MEGA20` listed publicly; invalid sample not listed)

## API overview

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/auth/register` | No | Register |
| POST | `/auth/login` | No | Login → JWT |
| GET | `/auth/me` | Optional | Current user |
| GET | `/stores` | No | List stores |
| GET | `/coupons` | No | Marketplace (filters: `store`, `discountType`, `priceMin`, `priceMax`, `minOrderMin`, `minOrderMax`) |
| GET | `/coupons/{id}` | Optional | **Code hidden** unless seller or buyer who purchased |
| POST | `/coupons` | Yes | Create listing → validation runs |
| GET | `/seller/coupons` | Yes | Seller’s coupons + last validation message |
| DELETE | `/seller/coupons/{id}` | Yes | Remove listing (not sold) |
| POST | `/transactions` | Yes | Buy (mock payment) |
| GET | `/buyer/transactions` | Yes | Purchases + revealed codes |
| GET | `/seller/transactions` | Yes | Sales you made (buyer, amount, coupon) |
| POST | `/internal/validate-coupon/{couponId}` | No* | Re-run validation (prototype) |

\* Lock down `/internal/**` in production (API key / network rules).

## Tests

```bash
cd backend
mvn test
```

Uses H2 in-memory (`test` profile), seed disabled.

## Project layout

```
backend/          Spring Boot API + JPA entities
frontend/         Next.js UI
docs/schemas/     Example JSON Schema for create-coupon body
docker-compose.yml
```

## Extending

- **Real store validation:** implement `StoreValidationStrategy` (or a registry keyed by `storeId`) and call external merchant APIs.
- **Real payments:** replace `PaymentService` with a Stripe/Adyen client behind the same interface.
- **Schema-driven forms:** align UI with `docs/schemas/create-coupon.request.json` and add client-side validation (e.g. Zod).

## License

Academic / demo project.
