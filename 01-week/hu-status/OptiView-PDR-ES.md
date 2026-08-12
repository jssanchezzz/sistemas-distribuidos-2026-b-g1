# PRD — OptiView
## Sistema de Gestión Integral para Ópticas

---

## 1. Resumen ejecutivo

**OptiView** es una aplicación web SaaS de tipo dashboard administrativo, pensada para la gestión integral de ópticas (tiendas de lentes/gafas), acompañada de un **portal separado para pacientes**. El diseño debe ser limpio, profesional, moderno y funcional, inspirado en la estética de herramientas como **Linear, Notion o Stripe Dashboard**: mucho whitespace, tipografía clara, colores restringidos y cero ruido visual.

El sistema se construye sobre una **arquitectura hexagonal (puertos y adaptadores)**, dividida en servicios backend y frontends Angular desplegables de forma independiente, ejecutándose en tres ambientes (dev, qa, staging) respaldados por una única base de datos PostgreSQL.

## 2. Contexto del negocio

Una óptica recibe pacientes que necesitan gafas. El optómetra realiza un examen visual y genera una **fórmula óptica** (receta) con valores para cada ojo: esfera (SPH), cilindro (CYL), eje (AXIS), adición (ADD) y distancia pupilar (DP).

Con esa fórmula, el paciente elige una **montura** (armazón) y un tipo de **lente** (monofocal, bifocal o progresivo) con tratamientos opcionales (antirreflejo, fotocromático, blue filter). Todo esto se consolida en una **orden de trabajo (OT)** que se envía a un laboratorio óptico para tallar y montar los lentes. Cuando están listas, el paciente recoge sus gafas y paga (puede abonar parcialmente).

## 3. Usuarios y roles

| Rol | Descripción |
|---|---|
| **Administrador** | Gestiona todo: inventario, reportes, configuración, usuarios del sistema. |
| **Optómetra** | Registra pacientes, crea fórmulas ópticas, consulta historial visual. |
| **Vendedor** | Crea órdenes de trabajo (selecciona montura + lente + fórmula), genera cotizaciones, registra pagos. |
| **Paciente** (portal externo) | Consulta su fórmula, ve el estado de su orden, revisa saldo pendiente, recibe recordatorios de control. |

## 4. Dirección visual y sistema de diseño

### 4.1 Paleta de color
-**Color primario**: teal / verde azulado (asociado a salud visual, confianza, claridad).
-**Acentos semánticos**:
- Verde → éxito / completado
- Ámbar → advertencias / en proceso
- Rojo → errores / stock bajo
- Azul → información
-**Fondos**: gris muy claro para el canvas, blanco para cards y paneles.

### 4.2 Tipografía
- Sans-serif moderna (Inter, SF Pro o similar).
- Dos pesos únicamente: **regular** (cuerpo) y **medium** (títulos y datos destacados).
- No usar bold agresivo.

### 4.3 Bordes y forma
- Bordes hairline (1px gris claro).
- Esquinas redondeadas suaves: 8px en controles, 12px en cards.
- Sin sombras decorativas, sin gradientes, sin efectos glow — todo flat y limpio.

### 4.4 Iconografía
- Estilo outline consistente (Tabler Icons o Phosphor Icons).

## 5. Estructura de navegación

### 5.1 Layout principal (Administrador / Optómetra / Vendedor)
-**Sidebar izquierdo fijo**:
- Logo "OptiView" + ícono de ojo.
- Sección "Principal": Dashboard, Pacientes, Órdenes de trabajo, Inventario.
- Sección "Sistema": Facturación, Reportes, Configuración.
- Footer del sidebar: nombre de la óptica + ciudad (ej. "Óptica Central — Neiva, Huila").
-**Área de contenido** a la derecha del sidebar, con header contextual por pantalla.

### 5.2 Portal del paciente (diseño separado)
- Layout mobile-first, sin sidebar.
- Navegación tipo bottom tab bar o stacked cards.
- Tono más cálido y amigable, mínima complejidad.

## 6. Requisitos funcionales — pantallas

### 6.1 Dashboard
El usuario llega aquí al iniciar sesión. Debe responder en 3 segundos a la pregunta: *"¿cómo va el día?"*

- Barra superior: saludo ("Buenos días, [nombre]") + fecha actual + botón primario "Nueva orden".
-**4 metric cards** en fila horizontal:
- Órdenes hoy (número + variación vs. ayer en %).
- Facturación del día (monto en COP + variación %).
- Pacientes nuevos (esta semana).
- Órdenes pendientes de entrega (con indicador de cuántas están listas hoy).
-**Panel izquierdo (60% ancho)** — "Órdenes recientes": tabla/lista con avatar + nombre del paciente, detalle breve (montura + lente), badge de estado, monto total. Cada fila es clickeable.
-**Panel derecho (40% ancho)** — "Alertas": lista de notificaciones con íconos de color según severidad:
- Stock bajo (rojo): productos por debajo del mínimo.
- Controles vencidos (ámbar): pacientes sin revisión en +12 meses.
- Cartera pendiente (azul): facturas con saldo > 30 días.
- Laboratorio (ámbar): órdenes con muchos días sin cambio de estado.

### 6.2 Pacientes — listado
- Barra superior: título "Pacientes" + buscador (por nombre o documento) + botón "Nuevo paciente".
- Lista de pacientes con: avatar (iniciales), nombre completo, número de documento, EPS/aseguradora, fecha de última visita.
- Cada fila clickeable → va al detalle. Hover sutil para indicar interactividad.

### 6.3 Pacientes — detalle (ficha del paciente)
- Breadcrumb/back button: "← Volver a pacientes".
- Header: avatar grande + nombre + documento + edad + EPS. Botones: "Editar" y "Nueva orden".
-**Tab bar con 3 pestañas**:

**Pestaña "Información"**
- Card izquierda "Datos personales": teléfono, email, EPS, próximo control (resaltado en ámbar si está cerca).
- Card derecha "Resumen": total de visitas, última fórmula, órdenes activas, saldo pendiente.

**Pestaña "Fórmula"**
- Card con la fórmula vigente. Tabla con columnas: [vacío], SPH, CYL, Eje, ADD, DP. Filas: OD (ojo derecho) y OI (ojo izquierdo). Headers en gris, valores en tipografía monoespaciada o con peso medio.
- Indicador del optómetra que la registró y fecha.
- Banner informativo abajo: "Tipo recomendado: Progresivos — Presbicia detectada" (color teal, sutil).

**Pestaña "Historial"**
- Timeline vertical con puntos. Cada entrada: fecha, valores resumidos de OD/OI (solo esfera), tipo de lente, badge si es la fórmula actual. Permite ver la evolución visual del paciente en el tiempo.

### 6.4 Órdenes de trabajo — listado
- Barra superior: título "Órdenes de trabajo" + botón "Nueva orden".
- Filtros horizontales tipo chips/pills: Todas, En laboratorio, Listas, Pendientes pago. El chip activo tiene fondo teal.
- Lista de órdenes con: avatar + nombre paciente, código de OT (ej. OT-2851 en gris), montura + lente, badge de estado (coloreado según el estado), monto total + indicador de saldo si hay pendiente. Clickeable → detalle.

### 6.5 Órdenes de trabajo — detalle
Esta es la **pantalla más importante del sistema**. Representa el flujo completo de una orden.

- Back button + header con código de OT, nombre del paciente, fecha de creación. Botones: "Imprimir" y "Avanzar estado" (botón primario teal).
-**Card de progreso** (ancho completo):
- Barra de progreso horizontal con porcentaje.
- Debajo, **5 steps en fila** como pills redondeados: Cotización → Aprobada → En laboratorio → Lista → Entregada.
- Estados visuales de cada step:
- Completado: fondo verde claro + ícono check + texto verde oscuro.
- Actual: fondo azul claro + borde azul + texto azul oscuro (resaltado).
- Pendiente: fondo gris claro + texto gris claro.
-**Dos columnas debajo**:
- Izquierda "Detalle del pedido": filas label-value con Montura (nombre + referencia), Lente (tipo + material), Tratamientos, Laboratorio asignado, Altura de montaje, Entrega estimada (resaltada en teal).
- Derecha "Facturación": desglose (montura $X, lentes $Y, tratamientos $Z), separador, total, monto abonado (en verde), saldo pendiente (en rojo si hay). Botón "Registrar abono" al fondo.

### 6.6 Inventario
- Barra superior: título "Inventario" + toggle grid/lista + botón "Agregar montura".
-**4 metric cards**: total monturas, valor del inventario, items con stock bajo (rojo), proveedores activos.
- Buscador por marca, modelo o referencia.
- Vista grid: cards con ícono de gafas, nombre de la referencia, marca, color, precio, indicador de stock (verde si OK, rojo con ícono de alerta si bajo).
- Vista lista: filas compactas con la misma información en horizontal.

### 6.7 Portal del paciente (app mobile-first separada)
Diseño centrado en el paciente, no técnico. Simple, amigable, solo lectura (excepto pagos).

- Header: avatar + "Hola, [nombre]" + subtítulo "Tu salud visual al día".
- Card 1 — Orden activa: nombre del producto (ej. "Gafas progresivas — Ray-Ban"), badge de estado, barra de progreso con porcentaje, fecha estimada de entrega.
- Card 2 — Tu fórmula: tabla compacta con SPH, CYL, Eje, ADD, DP para OD y OI. Solo lectura.
- 2 cards pequeños en grid de 2 columnas:
- Próximo control: ícono calendario + fecha + "Revisión anual recomendada".
- Saldo pendiente: monto + botón "Abonar".

## 7. Flujos de interacción clave

### 7.1 Crear orden de trabajo
Vendedor va a "Nueva orden" → busca/selecciona paciente → selecciona fórmula vigente → busca/selecciona montura del inventario → selecciona tipo de lente y tratamientos → revisa resumen → confirma. Se descuenta stock automáticamente.

### 7.2 Avanzar estado de orden
Usuario abre OT → presiona "Avanzar estado" → confirma en modal → el estado avanza al siguiente step → se notifica al paciente.

### 7.3 Registrar paciente + fórmula
Optómetra va a "Nuevo paciente" → llena datos personales → registra fórmula (formulario con campos para OD y OI: esfera, cilindro, eje, adición, DP) → guarda.

## 8. Principios de UX a respetar

-**Information density justa**: ni vacía ni abrumadora. Cada pantalla responde una pregunta principal.
-**Progressive disclosure**: el listado muestra lo mínimo, el detalle muestra todo.
-**Status visibility**: el usuario siempre sabe en qué estado está cada orden sin tener que abrir el detalle.
-**Consistency**: mismos patrones de cards, tablas, badges y botones en todas las pantallas.
-**Acciones claras**: siempre hay un botón primario visible que indica la acción principal de cada pantalla.
-**Feedback inmediato**: badges de color para estados, indicadores de stock, alertas visibles.
-**Mobile portal**: el portal del paciente debe funcionar perfecto en pantallas de 375px de ancho.

## 9. Restricciones de diseño — Qué NO hacer

- No usar sombras pesadas (box-shadow) — todo flat con bordes hairline.
- No usar gradientes ni fondos oscuros.
- No usar iconos filled/solid — solo outline.
- No usar más de 2 pesos de tipografía (regular + medium).
- No sobrecargar con colores — la mayoría de la UI es gris/blanco, los colores son solo para estados y acciones.
- No poner texto placeholder genérico como "Lorem ipsum" — usar datos realistas de una óptica colombiana (nombres, montos en pesos COP, marcas reales de monturas).

## 10. Arquitectura técnica

### 10.1 Estilo arquitectónico — Arquitectura hexagonal

Cada servicio backend sigue una **arquitectura hexagonal (puertos y adaptadores)**:

-**Núcleo de dominio**: entidades, value objects y servicios de dominio, independientes de cualquier framework (Paciente, Fórmula, OrdenDeTrabajo, ItemDeInventario, Factura, etc.).
-**Capa de aplicación**: casos de uso que orquestan el dominio, expuestos a través de **puertos de entrada** (interfaces de casos de uso).
-**Puertos de salida**: interfaces de las que depende la capa de dominio/aplicación pero que no implementa (repositorios, envío de notificaciones, publicación de eventos).
-**Adaptadores de entrada**: controladores REST que exponen los casos de uso vía HTTP/JSON hacia los frontends Angular y el API Gateway/BFF.
-**Adaptadores de salida**: implementaciones de repositorios sobre PostgreSQL, clientes HTTP hacia sistemas externos (ej. el laboratorio óptico), adaptadores de notificaciones.

Esto mantiene las reglas de negocio independientes de frameworks, protocolos de transporte y tecnología de persistencia, y permite que cada servicio cambie de adaptador (ej. driver de base de datos, tecnología de mensajería) sin tocar el dominio.

### 10.2 Servicios backend (4)

| # | Servicio | Lenguaje / Framework | Contexto delimitado (bounded context) | Tabla principal |
|---|---|---|---|---|
| 1 | **patient-service** | Java (Spring Boot) | Pacientes, fórmulas ópticas, historial visual | `patients` |
| 2 | **order-service** | Java (Spring Boot) | Órdenes de trabajo, ciclo de vida/estados, ítems de la orden | `work_orders` |
| 3 | **inventory-service** | Java (Spring Boot) | Monturas, lentes, tratamientos, stock, proveedores | `inventory_items` |
| 4 | **billing-service** | Go | Facturas, pagos, saldos, alertas/notificaciones | `billing_records` |

Cada servicio es dueño de su contexto delimitado de punta a punta (dominio, casos de uso, puertos, adaptadores) y expone una API REST versionada (ej. `/api/v1/...`). El servicio en Go sigue la misma organización hexagonal (paquete de dominio, paquete de aplicación/casos de uso, paquete `ports` con interfaces, paquetes `adapters/http` y `adapters/postgres`) como equivalente idiomático a los servicios Java.

### 10.3 Aplicaciones frontend (4, Angular)

| # | Aplicación | Propósito | Consumidores principales |
|---|---|---|---|
| 1 | **optiview-shell** | App host/shell: autenticación, layout con sidebar, pantallas de Dashboard, Facturación, Reportes y Configuración. Orquesta la navegación y compone los demás frontends. | Administrador, Optómetra, Vendedor |
| 2 | **optiview-clinical-app** | Módulo de pacientes: listado/detalle de pacientes, fórmula óptica, historial visual. | Optómetra, Vendedor |
| 3 | **optiview-operations-app** | Módulo de operaciones: órdenes de trabajo (listado/detalle, pipeline de estados) e inventario. | Administrador, Vendedor |
| 4 | **optiview-patient-portal** | App mobile-first, independiente, orientada al paciente: orden activa, fórmula (solo lectura), saldo y pagos. | Paciente |

`optiview-shell`, `optiview-clinical-app` y `optiview-operations-app` comparten el mismo sistema de diseño y se componen como micro-frontends (ej. vía Module Federation) dentro de una única experiencia autenticada. `optiview-patient-portal` se despliega y versiona de forma independiente, con su propia dirección visual más cálida, como se define en la sección 5.2.

### 10.4 Comunicación servicios ↔ frontends

- Todos los frontends se comunican con los servicios backend a través de una capa de **API Gateway / Backend-for-Frontend (BFF)**, sobre REST/JSON y HTTPS.
- El gateway es responsable del ruteo, la autenticación/autorización (JWT) y de agregar llamadas cuando una pantalla necesita datos de más de un servicio (ej. el Dashboard necesita datos de `order-service`, `patient-service` y `billing-service`).
- Cada servicio backend permanece desplegable de forma independiente y no comparte código con los demás — la consistencia entre servicios se maneja a nivel de API/integración, no mediante librerías compartidas ni un modelo de dominio común.

### 10.5 Ambientes

Se provisionan tres ambientes de punta a punta:

| Ambiente | Propósito |
|---|---|
| **dev** | Desarrollo activo y pruebas de integración. |
| **qa** | Validación funcional/manual de QA antes de promover. |
| **staging** | Validación preproductiva, el espejo más cercano al comportamiento de producción. |

Cada ambiente ejecuta su propio despliegue de los 4 servicios backend y los 4 frontends Angular (URLs/namespaces independientes, configuración independiente y logs de aplicación independientes). Esto permite que cada ambiente se despliegue, versione y revierta de forma independiente.

### 10.6 Base de datos

- Una única instancia/cluster de **PostgreSQL** alberga todos los datos persistentes del sistema.
- Existe **exactamente una tabla por servicio backend** (4 tablas en total: `patients`, `work_orders`, `inventory_items`, `billing_records`), cada una propiedad exclusiva de su servicio correspondiente (sin acceso cruzado entre servicios — toda lectura/escritura pasa por los adaptadores de salida de ese servicio).
-**Los tres ambientes (dev, qa, staging) se conectan y comparten la misma base de datos física y las mismas tablas** — no hay aislamiento de datos por ambiente.

>**Nota arquitectónica:** compartir un único set de datos entre dev, qa y staging es una decisión deliberada de este proyecto (no la mejor práctica general de aislar los datos por ambiente). Simplifica la infraestructura para el alcance actual del proyecto, pero implica que los datos creados o modificados en `dev` son visibles inmediatamente en `qa` y `staging`, y que pruebas destructivas en cualquiera de los ambientes afectan a los demás. Este trade-off debería revaluarse si el proyecto crece más allá de su alcance actual.

## 11. Fuera de alcance (implícito)

Los siguientes puntos no se detallan en este documento y deberían definirse en una fase posterior de especificación técnica:
- Contratos de API detallados (esquemas de request/response a nivel de endpoint).
- Proveedor de autenticación/autorización y estrategia de emisión de tokens.
- Diseño de pipeline CI/CD e infraestructura como código.
- Protocolo de integración con el laboratorio óptico externo y con pasarelas de pago.
- Stack de observabilidad (logging, métricas, tracing).

Ver los documentos de modelado C4 complementarios (`OptiView-C4-Model-EN.md` / `OptiView-C4-Model-ES.md`) para la representación visual de la arquitectura (niveles de Contexto, Contenedores y Componentes).
