# PRD — OptiView
## Integrated Management System for Optical Stores

---

## 1. Executive Summary

**OptiView** is a SaaS web application built as an administrative dashboard for the integrated management of optical stores (eyewear/glasses shops), paired with a **separate patient portal**. The design must be clean, professional, modern and functional — inspired by the aesthetic of tools like **Linear, Notion or the Stripe Dashboard**: generous whitespace, clear typography, a restrained color palette and zero visual noise.

The system is built on a **hexagonal (ports & adapters) architecture**, split into independently deployable backend services and Angular frontends, running across three environments (dev, qa, staging) backed by a single PostgreSQL database.

## 2. Business Context

An optical store receives patients who need glasses. The optometrist performs a visual exam and produces an **optical formula** (prescription) with values for each eye: sphere (SPH), cylinder (CYL), axis (AXIS), addition (ADD) and pupillary distance (DP).

Using that formula, the patient chooses a **frame** and a **lens** type (single-vision, bifocal or progressive) with optional treatments (anti-reflective coating, photochromic, blue-light filter). All of this is consolidated into a **work order (WO)** that is sent to an optical laboratory to cut and mount the lenses. Once ready, the patient picks up the glasses and pays (partial payments/installments allowed).

## 3. Users and Roles

| Role | Description |
|---|---|
| **Administrator** | Manages everything: inventory, reports, configuration, system users. |
| **Optometrist** | Registers patients, creates optical formulas, reviews visual history. |
| **Seller** | Creates work orders (selects frame + lens + formula), generates quotes, records payments. |
| **Patient** (external portal) | Views their formula, tracks order status, checks pending balance, receives check-up reminders. |

## 4. Visual Direction and Design System

### 4.1 Color Palette
-**Primary color**: teal / blue-green (associated with visual health, trust, clarity).
-**Semantic accents**:
- Green → success / completed
- Amber → warnings / in progress
- Red → errors / low stock
- Blue → informational
-**Backgrounds**: very light gray for the canvas, white for cards and panels.

### 4.2 Typography
- Modern sans-serif (Inter, SF Pro or similar).
- Only two weights: **regular** (body) and **medium** (titles and highlighted data).
- No aggressive bold.

### 4.3 Borders and Shape
- Hairline borders (1px light gray).
- Soft rounded corners: 8px on controls, 12px on cards.
- No decorative shadows, no gradients, no glow effects — everything flat and clean.

### 4.4 Iconography
- Consistent outline style (Tabler Icons or Phosphor Icons).

## 5. Navigation Structure

### 5.1 Main Layout (Administrator / Optometrist / Seller)
-**Fixed left sidebar**:
- "OptiView" logo + eye icon.
- "Main" section: Dashboard, Patients, Work Orders, Inventory.
- "System" section: Billing, Reports, Settings.
- Sidebar footer: optical store name + city (e.g. "Óptica Central — Neiva, Huila").
-**Content area** to the right of the sidebar, with a contextual header per screen.

### 5.2 Patient Portal (separate design)
- Mobile-first layout, no sidebar.
- Bottom tab bar or stacked-cards style navigation.
- Warmer, friendlier tone, minimal complexity.

## 6. Functional Requirements — Screens

### 6.1 Dashboard
The user lands here after login. It must answer, within 3 seconds: *"how's the day going?"*

- Top bar: greeting ("Good morning, [name]") + current date + primary "New order" button.
-**4 metric cards** in a horizontal row:
- Orders today (number + % variation vs. yesterday).
- Today's revenue (amount in COP + % variation).
- New patients (this week).
- Orders pending delivery (with indicator of how many are ready today).
-**Left panel (60% width)** — "Recent orders": table/list with patient avatar + name, short detail (frame + lens), status badge, total amount. Each row is clickable.
-**Right panel (40% width)** — "Alerts": notification list with color icons by severity:
- Low stock (red): products below minimum threshold.
- Overdue check-ups (amber): patients with no review in +12 months.
- Pending receivables (blue): invoices with a balance older than 30 days.
- Lab delays (amber): orders with many days without a status change.

### 6.2 Patients — List
- Top bar: "Patients" title + search box (by name or ID) + "New patient" button.
- Patient list with: avatar (initials), full name, ID number, insurance provider (EPS), last visit date.
- Each row clickable → goes to detail. Subtle hover to indicate interactivity.

### 6.3 Patients — Detail (patient record)
- Breadcrumb/back button: "← Back to patients".
- Header: large avatar + name + ID + age + insurance provider. Buttons: "Edit" and "New order".
-**Tab bar with 3 tabs**:

**"Information" tab**
- Left card "Personal data": phone, email, insurance provider, next check-up (highlighted in amber if approaching).
- Right card "Summary": total visits, latest formula, active orders, pending balance.

**"Formula" tab**
- Card with the current formula. Table with columns: [blank], SPH, CYL, Axis, ADD, DP. Rows: OD (right eye) and OS (left eye). Gray headers, values in monospace or medium-weight typography.
- Indicator of which optometrist recorded it and the date.
- Info banner below: "Recommended type: Progressive — Presbyopia detected" (subtle teal color).

**"History" tab**
- Vertical timeline with dots. Each entry: date, summarized OD/OS values (sphere only), lens type, badge if it's the current formula. Lets the user see the patient's visual evolution over time.

### 6.4 Work Orders — List
- Top bar: "Work Orders" title + "New order" button.
- Horizontal filters as chips/pills: All, In lab, Ready, Pending payment. Active chip has a teal background.
- Order list with: patient avatar + name, WO code (e.g. WO-2851 in gray), frame + lens, status badge (colored by state), total amount + balance indicator if pending. Clickable → detail.

### 6.5 Work Orders — Detail
This is the **most important screen in the system**. It represents the full lifecycle of an order.

- Back button + header with WO code, patient name, creation date. Buttons: "Print" and "Advance status" (primary teal button).
-**Progress card** (full width):
- Horizontal progress bar with percentage.
- Below it, **5 steps in a row** as rounded pills: Quote → Approved → In lab → Ready → Delivered.
- Visual state of each step:
- Completed: light green background + check icon + dark green text.
- Current: light blue background + blue border + dark blue text (highlighted).
- Pending: light gray background + light gray text.
-**Two columns below**:
- Left "Order detail": label-value rows with Frame (name + reference), Lens (type + material), Treatments, Assigned lab, Mounting height, Estimated delivery (highlighted in teal).
- Right "Billing": breakdown (frame $X, lenses $Y, treatments $Z), separator, total, amount paid (in green), pending balance (in red if any). "Register payment" button at the bottom.

### 6.6 Inventory
- Top bar: "Inventory" title + grid/list toggle + "Add frame" button.
-**4 metric cards**: total frames, inventory value, items with low stock (red), active suppliers.
- Search by brand, model or reference.
- Grid view: cards with a glasses icon, reference name, brand, color, price, stock indicator (green if OK, red with alert icon if low).
- List view: compact rows with the same information laid out horizontally.

### 6.7 Patient Portal (separate mobile-first app)
Patient-centered design, non-technical. Simple, friendly, read-only (except for payments).

- Header: avatar + "Hi, [name]" + subtitle "Your visual health, up to date".
- Card 1 — Active order: product name (e.g. "Progressive glasses — Ray-Ban"), status badge, progress bar with percentage, estimated delivery date.
- Card 2 — Your formula: compact table with SPH, CYL, Axis, ADD, DP for OD and OS. Read-only.
- 2 small cards in a 2-column grid:
- Next check-up: calendar icon + date + "Annual review recommended".
- Pending balance: amount + "Pay" button.

## 7. Key Interaction Flows

### 7.1 Create a Work Order
Seller goes to "New order" → searches/selects patient → selects current formula → searches/selects frame from inventory → selects lens type and treatments → reviews summary → confirms. Stock is discounted automatically.

### 7.2 Advance Order Status
User opens a WO → clicks "Advance status" → confirms in a modal → the order moves to the next step → the patient is notified.

### 7.3 Register Patient + Formula
Optometrist goes to "New patient" → fills in personal data → registers the formula (form with fields for OD and OS: sphere, cylinder, axis, addition, DP) → saves.

## 8. UX Principles to Respect

-**Fair information density**: neither empty nor overwhelming. Every screen answers one main question.
-**Progressive disclosure**: the list shows the minimum, the detail shows everything.
-**Status visibility**: the user always knows what state each order is in without opening the detail.
-**Consistency**: the same card, table, badge and button patterns across all screens.
-**Clear actions**: there is always a visible primary button indicating each screen's main action.
-**Immediate feedback**: color badges for statuses, stock indicators, visible alerts.
-**Mobile portal**: the patient portal must work flawlessly on 375px-wide screens.

## 9. Design Constraints — What NOT to Do

- No heavy shadows (box-shadow) — everything flat with hairline borders.
- No gradients or dark backgrounds.
- No filled/solid icons — outline only.
- No more than 2 typography weights (regular + medium).
- Don't overload with color — most of the UI is gray/white; color is reserved for states and actions.
- No generic placeholder text like "Lorem ipsum" — use realistic data from a Colombian optical store (names, amounts in COP, real frame brands).

## 10. Technical Architecture

### 10.1 Architectural Style — Hexagonal Architecture

Every backend service follows **Hexagonal Architecture (Ports & Adapters)**:

-**Domain core**: entities, value objects and domain services, framework-agnostic (Patient, Formula, WorkOrder, InventoryItem, Invoice, etc.).
-**Application layer**: use cases orchestrating the domain, exposed through **inbound ports** (use-case interfaces).
-**Outbound ports**: interfaces the domain/application layer depends on but does not implement (repositories, notification senders, event publishers).
-**Inbound adapters**: REST controllers exposing the use cases over HTTP/JSON to the Angular frontends and the API Gateway/BFF.
-**Outbound adapters**: PostgreSQL repository implementations, HTTP clients for external systems (e.g. the optical laboratory), notification adapters.

This keeps business rules independent of frameworks, transport protocols and persistence technology, and allows each service to swap adapters (e.g. database driver, messaging technology) without touching the domain.

### 10.2 Backend Services (4)

| # | Service | Language / Framework | Bounded Context | Primary Table |
|---|---|---|---|---|
| 1 | **patient-service** | Java (Spring Boot) | Patients, optical formulas, visual history | `patients` |
| 2 | **order-service** | Java (Spring Boot) | Work orders, order lifecycle/status, order line items | `work_orders` |
| 3 | **inventory-service** | Java (Spring Boot) | Frames, lenses, treatments, stock, suppliers | `inventory_items` |
| 4 | **billing-service** | Go | Invoices, payments, balances, alerts/notifications | `billing_records` |

Each service owns its bounded context end-to-end (domain, use cases, ports, adapters) and exposes a versioned REST API (e.g. `/api/v1/...`). The Go service follows the same hexagonal layering (domain package, application/use-case package, `ports` package with interfaces, `adapters/http` and `adapters/postgres` packages) as an idiomatic equivalent to the Java services.

### 10.3 Frontend Applications (4, Angular)

| # | Application | Purpose | Primary consumers |
|---|---|---|---|
| 1 | **optiview-shell** | Host/shell app: authentication, sidebar layout, Dashboard, Billing, Reports and Settings screens. Orchestrates navigation and composes the other frontends. | Administrator, Optometrist, Seller |
| 2 | **optiview-clinical-app** | Patients module: patient list/detail, optical formula, visual history. | Optometrist, Seller |
| 3 | **optiview-operations-app** | Operations module: work orders (list/detail, status pipeline) and inventory. | Administrator, Seller |
| 4 | **optiview-patient-portal** | Mobile-first, standalone patient-facing app: active order, formula (read-only), balance and payments. | Patient |

`optiview-shell`, `optiview-clinical-app` and `optiview-operations-app` share the same design system and are composed as micro-frontends (e.g. via Module Federation) behind a single authenticated experience. `optiview-patient-portal` is deployed and versioned independently, with its own warmer visual theme, as defined in section 5.2.

### 10.4 Service ↔ Frontend Communication

- All frontends talk to backend services through an **API Gateway / Backend-for-Frontend (BFF)** layer, over REST/JSON and HTTPS.
- The gateway is responsible for routing, authentication/authorization (JWT), and aggregating calls when a screen needs data from more than one service (e.g. the Dashboard needs data from `order-service`, `patient-service` and `billing-service`).
- Each backend service remains independently deployable and does not share code with the others — cross-service consistency is handled at the API/integration level, not through shared libraries or a shared domain model.

### 10.5 Environments

Three environments are provisioned end-to-end:

| Environment | Purpose |
|---|---|
| **dev** | Active development and integration testing. |
| **qa** | Functional/manual QA validation before promotion. |
| **staging** | Pre-production validation, closest mirror of production behavior. |

Each environment runs its own deployment of the 4 backend services and the 4 Angular frontends (independent URLs/namespaces, independent configuration and independent application logs). This allows each environment to be deployed, versioned and rolled back independently.

### 10.6 Database

- A single **PostgreSQL** instance/cluster hosts all persistent data for the system.
- There is **exactly one table per backend service** (4 tables total: `patients`, `work_orders`, `inventory_items`, `billing_records`), each owned exclusively by its corresponding service (no cross-service table access — every read/write goes through that service's outbound adapters).
-**All three environments (dev, qa, staging) connect to and share the same physical database and the same tables** — there is no per-environment data isolation.

>**Architectural note:** sharing a single dataset across dev, qa and staging is a deliberate project decision (not the general best practice of isolating data per environment). It simplifies infrastructure for this project's scope, but it means data created or modified in `dev` is immediately visible in `qa` and `staging`, and destructive testing in any one environment affects the others. This trade-off should be re-evaluated if the project grows beyond its current scope.

## 11. Out of Scope (implicit)

The following items are not detailed in this document and should be defined in a later technical specification phase:
- Detailed API contracts (endpoint-level request/response schemas).
- Authentication/authorization provider and token issuance strategy.
- CI/CD pipeline design and infrastructure-as-code.
- Integration protocol with the external optical laboratory and with payment gateways.
- Observability stack (logging, metrics, tracing).

See the companion C4 model documents (`OptiView-C4-Model-EN.md` / `OptiView-C4-Model-ES.md`) for the visual architecture representation (Context, Container and Component levels).
