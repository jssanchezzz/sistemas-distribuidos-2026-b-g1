# OptiView — Modelado de Arquitectura C4

---

## Nivel 1 — Diagrama de Contexto del Sistema

Muestra a OptiView como un único sistema y sus interacciones con usuarios y sistemas externos.

```mermaid
C4Context
 title OptiView — Contexto del Sistema

 Person(admin, "Administrador", "Gestiona inventario, reportes, configuración y usuarios")
 Person(optometrist, "Optómetra", "Registra pacientes y fórmulas ópticas")
 Person(seller, "Vendedor", "Crea órdenes de trabajo, cotizaciones y pagos")
 Person(patient, "Paciente", "Consulta fórmula, estado de orden y saldo")

 System(optiview, "OptiView", "Plataforma SaaS para la gestión integral de ópticas, con portal separado para pacientes")

 System_Ext(lab, "Laboratorio Óptico", "Sistema/proceso externo que talla y monta los lentes")
 System_Ext(paymentGateway, "Pasarela de Pagos", "Procesa pagos/abonos de pacientes")
 System_Ext(notification, "Proveedor de Notificaciones", "Envía notificaciones por email/SMS/push")

 Rel(admin, optiview, "Gestiona el sistema", "HTTPS")
 Rel(optometrist, optiview, "Registra pacientes y fórmulas", "HTTPS")
 Rel(seller, optiview, "Crea órdenes y registra pagos", "HTTPS")
 Rel(patient, optiview, "Consulta orden, fórmula y saldo", "HTTPS")

 Rel(optiview, lab, "Envía órdenes de trabajo / recibe actualizaciones de estado", "HTTPS/Archivo")
 Rel(optiview, paymentGateway, "Procesa pagos", "HTTPS")
 Rel(optiview, notification, "Envía recordatorios de control y de estado", "HTTPS")
```

---

## Nivel 2 — Diagrama de Contenedores

Muestra las unidades desplegables principales: 4 frontends Angular, un API Gateway/BFF, 4 servicios backend (3 Java + 1 Go) y la base de datos PostgreSQL compartida.

```mermaid
C4Container
 title OptiView — Diagrama de Contenedores (aplica igual en dev, qa y staging)

 Person(admin, "Administrador / Optómetra / Vendedor")
 Person(patient, "Paciente")

 System_Boundary(optiview, "OptiView") {
 Container(shell, "optiview-shell", "Angular", "App host/shell: autenticación, layout con sidebar, Dashboard, Facturación, Reportes, Configuración")
 Container(clinical, "optiview-clinical-app", "Angular (micro-frontend)", "Módulo de pacientes: listado/detalle, fórmula, historial visual")
 Container(operations, "optiview-operations-app", "Angular (micro-frontend)", "Módulo de operaciones: órdenes de trabajo e inventario")
 Container(portal, "optiview-patient-portal", "Angular", "App independiente mobile-first para pacientes")

 Container(gateway, "API Gateway / BFF", "Gateway Node/Java", "Ruteo, autenticación JWT, agregación de llamadas entre servicios")

 Container(patientSvc, "patient-service", "Java / Spring Boot\n(Arquitectura Hexagonal)", "Pacientes, fórmulas ópticas, historial visual")
 Container(orderSvc, "order-service", "Java / Spring Boot\n(Arquitectura Hexagonal)", "Órdenes de trabajo y ciclo de vida de estados")
 Container(inventorySvc, "inventory-service", "Java / Spring Boot\n(Arquitectura Hexagonal)", "Monturas, lentes, tratamientos, stock, proveedores")
 Container(billingSvc, "billing-service", "Go\n(Arquitectura Hexagonal)", "Facturas, pagos, saldos, alertas")

 ContainerDb(db, "PostgreSQL", "Base de datos relacional", "1 tabla por servicio: patients, work_orders, inventory_items, billing_records. Compartida entre dev/qa/staging")
 }

 Rel(admin, shell, "Usa", "HTTPS")
 Rel(admin, clinical, "Usa (compuesto dentro del shell)", "HTTPS")
 Rel(admin, operations, "Usa (compuesto dentro del shell)", "HTTPS")
 Rel(patient, portal, "Usa", "HTTPS")

 Rel(shell, gateway, "Llama", "REST/JSON")
 Rel(clinical, gateway, "Llama", "REST/JSON")
 Rel(operations, gateway, "Llama", "REST/JSON")
 Rel(portal, gateway, "Llama", "REST/JSON")

 Rel(gateway, patientSvc, "Rutea peticiones", "REST/JSON")
 Rel(gateway, orderSvc, "Rutea peticiones", "REST/JSON")
 Rel(gateway, inventorySvc, "Rutea peticiones", "REST/JSON")
 Rel(gateway, billingSvc, "Rutea peticiones", "REST/JSON")

 Rel(patientSvc, db, "Lee/escribe", "JDBC — tabla patients")
 Rel(orderSvc, db, "Lee/escribe", "JDBC — tabla work_orders")
 Rel(inventorySvc, db, "Lee/escribe", "JDBC — tabla inventory_items")
 Rel(billingSvc, db, "Lee/escribe", "pgx/database-sql — tabla billing_records")

 Rel(orderSvc, inventorySvc, "Verifica/descuenta stock", "REST/JSON")
 Rel(orderSvc, patientSvc, "Lee la fórmula vigente", "REST/JSON")
 Rel(billingSvc, orderSvc, "Lee el total de la orden", "REST/JSON")
```

**Notas:**
- `optiview-shell`, `optiview-clinical-app` y `optiview-operations-app` se componen como micro-frontends dentro de una única experiencia autenticada (ej. vía Module Federation), en línea con la navegación de sidebar descrita en el PRD (sección 5.1).
- `optiview-patient-portal` se despliega y versiona de forma independiente, mobile-first, sin sidebar (PRD sección 5.2).
- Las llamadas entre servicios (`order-service → inventory-service`, `order-service → patient-service`, `billing-service → order-service`) ocurren vía REST, nunca por acceso directo a base de datos — cada servicio es propietario exclusivo de su tabla.

---

## Nivel 3 — Diagrama de Componentes (ejemplo: `order-service`)

Hace zoom sobre `order-service` para mostrar su **arquitectura hexagonal** interna. El mismo patrón (núcleo de dominio, puertos de entrada/salida, adaptadores de entrada/salida) aplica a `patient-service` e `inventory-service` (Java) y, de forma idiomática, a `billing-service` (Go).

```mermaid
C4Component
 title order-service — Diagrama de Componentes (Arquitectura Hexagonal)

 Container_Boundary(orderSvc, "order-service (Java / Spring Boot)") {

 Component(restController, "WorkOrderRestController", "Adaptador de Entrada (REST)", "Expone los endpoints /api/v1/work-orders")

 Component(createUseCase, "CreateWorkOrderUseCase", "Puerto de Entrada / Servicio de Aplicación", "Orquesta la creación de una nueva orden de trabajo")
 Component(advanceUseCase, "AdvanceWorkOrderStatusUseCase", "Puerto de Entrada / Servicio de Aplicación", "Avanza la orden al siguiente estado del ciclo de vida")
 Component(queryUseCase, "QueryWorkOrdersUseCase", "Puerto de Entrada / Servicio de Aplicación", "Lista y consulta órdenes de trabajo")

 Component(domain, "Modelo de Dominio WorkOrder", "Núcleo de Dominio", "Entidades/value objects WorkOrder, OrderLineItem, OrderStatus y reglas de negocio")

 Component(repoPort, "WorkOrderRepositoryPort", "Puerto de Salida", "Interfaz para persistir/recuperar órdenes de trabajo")
 Component(inventoryPort, "InventoryClientPort", "Puerto de Salida", "Interfaz para verificar/descontar stock")
 Component(patientPort, "PatientClientPort", "Puerto de Salida", "Interfaz para leer la fórmula vigente del paciente")
 Component(notificationPort, "NotificationPort", "Puerto de Salida", "Interfaz para notificar al paciente ante cambio de estado")

 Component(repoAdapter, "PostgresWorkOrderRepository", "Adaptador de Salida", "Implementa WorkOrderRepositoryPort contra la tabla work_orders")
 Component(inventoryAdapter, "InventoryServiceHttpClient", "Adaptador de Salida", "Implementa InventoryClientPort mediante llamadas REST a inventory-service")
 Component(patientAdapter, "PatientServiceHttpClient", "Adaptador de Salida", "Implementa PatientClientPort mediante llamadas REST a patient-service")
 Component(notificationAdapter, "NotificationHttpClient", "Adaptador de Salida", "Implementa NotificationPort vía el proveedor de notificaciones")
 }

 ContainerDb(db, "PostgreSQL", "tabla: work_orders")
 Container(inventorySvc, "inventory-service", "Java / Spring Boot")
 Container(patientSvc, "patient-service", "Java / Spring Boot")
 System_Ext(notification, "Proveedor de Notificaciones")

 Rel(restController, createUseCase, "Invoca")
 Rel(restController, advanceUseCase, "Invoca")
 Rel(restController, queryUseCase, "Invoca")

 Rel(createUseCase, domain, "Usa")
 Rel(advanceUseCase, domain, "Usa")
 Rel(queryUseCase, domain, "Usa")

 Rel(createUseCase, repoPort, "Usa")
 Rel(createUseCase, inventoryPort, "Usa")
 Rel(createUseCase, patientPort, "Usa")
 Rel(advanceUseCase, repoPort, "Usa")
 Rel(advanceUseCase, notificationPort, "Usa")
 Rel(queryUseCase, repoPort, "Usa")

 Rel(repoAdapter, repoPort, "Implementa")
 Rel(inventoryAdapter, inventoryPort, "Implementa")
 Rel(patientAdapter, patientPort, "Implementa")
 Rel(notificationAdapter, notificationPort, "Implementa")

 Rel(repoAdapter, db, "Lee/escribe", "JDBC")
 Rel(inventoryAdapter, inventorySvc, "Llama", "REST/JSON")
 Rel(patientAdapter, patientSvc, "Llama", "REST/JSON")
 Rel(notificationAdapter, notification, "Llama", "HTTPS")
```

**Cómo leer el hexágono:**
- El **núcleo de dominio** (`Modelo de Dominio WorkOrder`) no tiene dependencias hacia Spring, HTTP o JDBC — es lógica de negocio pura.
-**Lado de entrada** (izquierda): controlador REST → casos de uso (puertos de entrada) → dominio.
-**Lado de salida** (derecha): los casos de uso dependen de **interfaces de puertos de salida** (`WorkOrderRepositoryPort`, `InventoryClientPort`, etc.), nunca de adaptadores concretos. Los adaptadores concretos (Postgres/REST) se inyectan en tiempo de ejecución (inversión de dependencias).
- Por esto la tecnología de base de datos, el transporte hacia inventory-service, o el proveedor de notificaciones pueden cambiar sin tocar `CreateWorkOrderUseCase` ni el modelo de dominio.

---

## Nivel 4 — Diagrama de Despliegue (Ambientes)

Muestra cómo los 4 servicios backend + 4 frontends se replican en los tres ambientes, y cómo todos comparten la misma base de datos PostgreSQL.

```mermaid
C4Deployment
 title OptiView — Diagrama de Despliegue (dev / qa / staging)

 Deployment_Node(devEnv, "Ambiente dev", "Namespace/Cluster: dev") {
 Container(devFrontends, "4 apps Angular (dev)", "shell, clinical-app, operations-app, patient-portal")
 Container(devGateway, "API Gateway (dev)", "Gateway/BFF")
 Container(devServices, "4 servicios backend (dev)", "patient-service, order-service, inventory-service (Java) + billing-service (Go)")
 }

 Deployment_Node(qaEnv, "Ambiente qa", "Namespace/Cluster: qa") {
 Container(qaFrontends, "4 apps Angular (qa)", "shell, clinical-app, operations-app, patient-portal")
 Container(qaGateway, "API Gateway (qa)", "Gateway/BFF")
 Container(qaServices, "4 servicios backend (qa)", "patient-service, order-service, inventory-service (Java) + billing-service (Go)")
 }

 Deployment_Node(stagingEnv, "Ambiente staging", "Namespace/Cluster: staging") {
 Container(stagingFrontends, "4 apps Angular (staging)", "shell, clinical-app, operations-app, patient-portal")
 Container(stagingGateway, "API Gateway (staging)", "Gateway/BFF")
 Container(stagingServices, "4 servicios backend (staging)", "patient-service, order-service, inventory-service (Java) + billing-service (Go)")
 }

 Deployment_Node(dbNode, "Infraestructura de Base de Datos Compartida", "Instancia/cluster único de PostgreSQL") {
 ContainerDb(sharedDb, "PostgreSQL", "Tablas: patients, work_orders, inventory_items, billing_records\n(compartidas por dev, qa y staging — sin aislamiento por ambiente)")
 }

 Rel(devFrontends, devGateway, "REST/JSON")
 Rel(devGateway, devServices, "REST/JSON")
 Rel(devServices, sharedDb, "JDBC / pgx")

 Rel(qaFrontends, qaGateway, "REST/JSON")
 Rel(qaGateway, qaServices, "REST/JSON")
 Rel(qaServices, sharedDb, "JDBC / pgx")

 Rel(stagingFrontends, stagingGateway, "REST/JSON")
 Rel(stagingGateway, stagingServices, "REST/JSON")
 Rel(stagingServices, sharedDb, "JDBC / pgx")
```

**Datos clave del despliegue:**
- Cada ambiente (`dev`, `qa`, `staging`) tiene su **propio despliegue independiente** de los 4 servicios backend y los 4 frontends Angular — versiones independientes, configuración independiente, logs independientes.
- Los tres ambientes apuntan a la **misma base de datos PostgreSQL y a las mismas 4 tablas** (`patients`, `work_orders`, `inventory_items`, `billing_records`). No hay esquema-por-ambiente ni base de datos-por-ambiente.
- Implicación práctica: una prueba destructiva en `dev` (ej. eliminar un paciente o una orden) es visible de inmediato desde `qa` y `staging`, ya que todos consultan las mismas filas. Cualquier estrategia de datos de prueba (seeding, limpieza) debe tener en cuenta este estado compartido.

---

## Anexo — Matriz de propiedad de tablas

| Tabla | Servicio propietario | ¿Leída/escrita por otros servicios? |
|---|---|---|
| `patients` | patient-service | No — se accede solo vía la API REST de patient-service |
| `work_orders` | order-service | No — se accede solo vía la API REST de order-service |
| `inventory_items` | inventory-service | No — se accede solo vía la API REST de inventory-service |
| `billing_records` | billing-service | No — se accede solo vía la API REST de billing-service |

Aunque todas las tablas residen en la misma base de datos PostgreSQL y se comparten entre ambientes, **ningún servicio lee o escribe directamente la tabla de otro servicio** — las necesidades de datos entre servicios siempre se resuelven mediante llamadas REST al servicio propietario, preservando los límites de contexto delimitado (bounded context) que exige la arquitectura hexagonal.
