# Tareas del Sprint 1 — CakeTrack

| Campo | Valor |
|-------|-------|
| **Sprint** | Sprint 1 (HU-01 a HU-07) |
| **Referencia** | spec.md (SPEC-CAKETRACK-001), plan.md (PLAN-CAKETRACK-S1) |
| **Fecha** | 2026-11-17 |
| **Enfoque** | Pruebas antes que implementación |

---

## Flujo de Ejecución

```
T001 → T002 → T003 [prueba] → T004 → T005 [prueba] → T006 → T007 [prueba] → T008 → T009 [prueba] → T010
```

---

## Tareas

### Infraestructura y Datos Base

- [x] **T001** `[Sprint Setup]` Configurar `application.properties` (MySQL, Thymeleaf, mensajes i18n) y crear `DataInitializer.java` en `com.caketrack.config` que poble al iniciar la aplicación 1 usuario de prueba por cada uno de los 5 roles (CLIENTE, CAJERO, PASTELERO, REPARTIDOR, ADMIN) con contraseñas BCrypt.
  - **Archivo**: `src/main/resources/application.properties`, `src/main/resources/messages.properties`, `src/main/java/com/caketrack/config/DataInitializer.java`
  - **Dependencias**: Ninguna (primera tarea)
  - **Criterios de aceptación**: `AC-003.1` (contraseñas BCrypt), verificación de 5 usuarios con roles correctos en base de datos al iniciar la aplicación.
  - **Criterio de terminado**: `./mvnw test` compila sin errores; `DataInitializer` crea 5 usuarios verificables al arrancar.

---

### Modelo de Datos y Repositorios

- [x] **T002** `[HU-01 a HU-07]` Crear entidades JPA (`Usuario`, `Ingrediente`, `Producto`, `RecetaItem`) en `com.caketrack.model` y el enum `Rol` con los 5 perfiles (más `TipoProducto` con `TORTA` y `POSTRE`, requerido por `productos.tipo`). Crear interfaces `JpaRepository` correspondientes en `com.caketrack.repository`. Incluir métodos de dominio en entidades: `Usuario.isActivo()`, `Usuario.tokenExpirado(LocalDateTime)`, `Ingrediente.isBajoStockMinimo()`, `Producto.marcarDisponible()` (BR-05) e `Producto.isDisponibleReal()`.
  - **Archivos**: `src/main/java/com/caketrack/model/Rol.java`, `src/main/java/com/caketrack/model/Usuario.java`, `src/main/java/com/caketrack/model/Ingrediente.java`, `src/main/java/com/caketrack/model/Producto.java`, `src/main/java/com/caketrack/model/RecetaItem.java`, `src/main/java/com/caketrack/repository/UsuarioRepository.java`, `src/main/java/com/caketrack/repository/IngredienteRepository.java`, `src/main/java/com/caketrack/repository/ProductoRepository.java`, `src/main/java/com/caketrack/repository/RecetaItemRepository.java`
  - **Dependencias**: T001
  - **Criterios de aceptación**: `BR-01` (contraseña BCrypt en `Usuario`), `BR-02` (`esActivo()`), `BR-03` (`tokenExpirado()`), `BR-04` (`esAlertaReposicion()`), `BR-05` (`puedeMarcarDisponible()` requiere ≥1 `RecetaItem`).
  - **Criterio de terminado**: `./mvnw test-compile` compila; tablas creadas por `ddl-auto=update` al arrancar.

---

### Seguridad y Autenticación

- [x] **T003** `[HU-01, HU-02, HU-04]` **[PRUEBA]** Escribir `AuthSecurityTest.java` con pruebas de: inicio de sesión exitoso con credenciales válidas (`AC-001.1`), rechazo de credenciales incorrectas (`AC-001.2`), bloqueo de usuario inactivo con mensaje correspondiente (`AC-001.3`), cierre de sesión y destrucción de sesión (`AC-001.4`), generación de token de recuperación con respuesta genérica sin filtrar existencia del correo (`AC-002.1`, `AC-002.2`), rechazo de token expirado o ya utilizado (`AC-002.3`, `AC-002.4`), menú filtrado por rol con `sec:authorize` (`AC-004.1`, `AC-004.2`) y bloqueo HTTP 403 al acceder a ruta no autorizada (`AC-004.3`).
  - **Archivo**: `src/test/java/com/caketrack/AuthSecurityTest.java`
  - **Dependencias**: T001, T002
  - **Criterios de aceptación**: `AC-001.1`, `AC-001.2`, `AC-001.3`, `AC-001.4`, `AC-002.1`, `AC-002.2`, `AC-002.3`, `AC-002.4`, `AC-004.1`, `AC-004.2`, `AC-004.3` (pruebas en rojo — fallan porque no existe implementación aún).
  - **Criterio de terminado**: Todas las pruebas compilan con `./mvnw test-compile` y están en estado **RED** (fallan por ausencia de implementación).

- [x] **T004** `[HU-01, HU-02, HU-04]` Implementar `SecurityConfig.java` en `com.caketrack.config` (cadena de filtros, `PasswordEncoder` BCrypt, reglas de autorización por rol, login/logout). Implementar `AuthController.java` y `HomeController.java` en `com.caketrack.controller`. Crear vistas: `templates/layout.html` (fragmento con menú dinámico usando `sec:authorize`), `templates/login.html`, `templates/recuperar.html`, `templates/restablecer.html` y `templates/inicio.html`.
  - **Archivos**: `src/main/java/com/caketrack/config/SecurityConfig.java`, `src/main/java/com/caketrack/controller/AuthController.java`, `src/main/java/com/caketrack/controller/HomeController.java`, `src/main/resources/templates/layout.html`, `src/main/resources/templates/login.html`, `src/main/resources/templates/recuperar.html`, `src/main/resources/templates/restablecer.html`, `src/main/resources/templates/inicio.html`
  - **Dependencias**: T003 (las pruebas ya existen en estado RED)
  - **Criterios de aceptación**: `AC-001.1`, `AC-001.2`, `AC-001.3`, `AC-001.4`, `AC-002.1`, `AC-002.2`, `AC-002.3`, `AC-002.4`, `AC-004.1`, `AC-004.2`, `AC-004.3` (pruebas pasan a estado **GREEN**).
  - **Criterio de terminado**: `./mvnw test` — todas las pruebas de `AuthSecurityTest` pasan. Requisito `REQ-CAKE-001` a `REQ-CAKE-005`, `REQ-CAKE-008`, `REQ-CAKE-009` cubiertos.

---

### Gestión de Usuarios

- [x] **T005** `[HU-03]` **[PRUEBA]** Escribir `AdminUsuarioTest.java` con pruebas de: creación de usuario con rol válido y contraseña BCrypt (`AC-003.1`), rechazo de contraseña con menos de 8 caracteres (`AC-003.2`), desactivación de usuario activo (`AC-003.3`) y activación de usuario inactivo (`AC-003.4`).
  - **Archivo**: `src/test/java/com/caketrack/AdminUsuarioTest.java`
  - **Dependencias**: T004 (la infraestructura de seguridad ya permite autenticar ADMIN)
  - **Criterios de aceptación**: `AC-003.1`, `AC-003.2`, `AC-003.3`, `AC-003.4` (pruebas en estado **RED**).
  - **Criterio de terminado**: `./mvnw test-compile` compila; pruebas en estado **RED**.

- [x] **T006** `[HU-03]` Implementar `AdminUsuarioController.java` en `com.caketrack.controller` con rutas `/admin/usuarios`, `/admin/usuarios/crear` y `/admin/usuarios/{id}/toggle-activo`. Crear vista `templates/admin/usuarios.html` con formulario de creación (nombre, correo, contraseña, rol) y tabla de usuarios con botón de toggle activo/inactivo.
  - **Archivos**: `src/main/java/com/caketrack/controller/AdminUsuarioController.java`, `src/main/resources/templates/admin/usuarios.html`
  - **Dependencias**: T005
  - **Criterios de aceptación**: `AC-003.1`, `AC-003.2`, `AC-003.3`, `AC-003.4` (pruebas pasan a **GREEN**). Requisito `REQ-CAKE-006`, `REQ-CAKE-007` cubiertos.
  - **Criterio de terminado**: `./mvnw test` — todas las pruebas de `AdminUsuarioTest` pasan.

---

### Inventario y Recetas

- [x] **T007** `[HU-05, HU-06]` **[PRUEBA]** Escribir `InventarioRecetaTest.java` con pruebas de: registro de ingrediente con cantidad actual y stock mínimo (`AC-005.1`), cálculo y visualización de alerta de reposición cuando `cantidad_actual <= stock_minimo` (`AC-005.2`), actualización de cantidad actual que remueve la alerta (`AC-005.3`), creación de producto con ingredientes de receta (`AC-006.1`), rechazo de producto sin ingredientes asociados (`AC-006.2`) y marcado de producto como disponible cuando tiene al menos un ingrediente en receta (`AC-006.3`).
  - **Archivo**: `src/test/java/com/caketrack/InventarioRecetaTest.java`
  - **Dependencias**: T004
  - **Criterios de aceptación**: `AC-005.1`, `AC-005.2`, `AC-005.3`, `AC-006.1`, `AC-006.2`, `AC-006.3` (pruebas en estado **RED**).
  - **Criterio de terminado**: `./mvnw test-compile` compila; pruebas en estado **RED**.

- [x] **T008** `[HU-05, HU-06]` Implementar `InventarioController.java` en `com.caketrack.controller` con rutas para CRUD de ingredientes y gestión de productos con receta. Crear vistas `templates/admin/ingredientes.html` (lista con indicador visual de alerta BR-04, formulario de registro/edición) y `templates/admin/productos.html` (lista con toggle de disponibilidad, formulario de creación con selector de ingredientes).
  - **Archivos**: `src/main/java/com/caketrack/controller/InventarioController.java`, `src/main/resources/templates/admin/ingredientes.html`, `src/main/resources/templates/admin/productos.html`
  - **Dependencias**: T007
  - **Criterios de aceptación**: `AC-005.1`, `AC-005.2`, `AC-005.3`, `AC-006.1`, `AC-006.2`, `AC-006.3` (pruebas pasan a **GREEN**). Requisito `REQ-CAKE-010`, `REQ-CAKE-011` cubiertos.
  - **Criterio de terminado**: `./mvnw test` — todas las pruebas de `InventarioRecetaTest` pasan.

---

### Catálogo Público

- [x] **T009** `[HU-07]` **[PRUEBA]** Escribir `CatalogoTest.java` con pruebas de: consulta de catálogo mostrando nombre, tipo, precio y disponibilidad (`AC-007.1`), filtro por tipo de producto TORTA o POSTRE (`AC-007.2`) y mensaje cuando el catálogo está vacío (`AC-007.3`).
  - **Archivo**: `src/test/java/com/caketrack/CatalogoTest.java`
  - **Dependencias**: T008 (se requieren productos creados para consultar)
  - **Criterios de aceptación**: `AC-007.1`, `AC-007.2`, `AC-007.3` (pruebas en estado **RED**).
  - **Criterio de terminado**: `./mvnw test-compile` compila; pruebas en estado **RED**.

- [x] **T010** `[HU-07]` Implementar `CatalogoController.java` en `com.caketrack.controller` con ruta `/catalogo` y parámetro opcional `?tipo=TORTA|POSTRE`. Crear vista `templates/catalogo.html` con tabla de productos (nombre, tipo, precio, disponibilidad) y selector de filtro por tipo.
  - **Archivos**: `src/main/java/com/caketrack/controller/CatalogoController.java`, `src/main/resources/templates/catalogo.html`
  - **Dependencias**: T009
  - **Criterios de aceptación**: `AC-007.1`, `AC-007.2`, `AC-007.3` (pruebas pasan a **GREEN**). Requisito `REQ-CAKE-012` cubierto.
  - **Criterio de terminado**: `./mvnw test` — todas las pruebas de `CatalogoTest` pasan. Sprint 1 completo.

---

## Resumen de Cobertura

| Tarea | HU | Tipo | Criterios AC | Requisitos |
|-------|----|----|--------------|-----------|
| T001 | Setup | Configuración | — | — |
| T002 | HU-01 a HU-07 | Modelo de datos | BR-01 a BR-05 | — |
| T003 | HU-01, HU-02, HU-04 | Prueba (RED) | AC-001.1-4, AC-002.1-4, AC-004.1-3 | REQ-CAKE-001 a 005, 008, 009 |
| T004 | HU-01, HU-02, HU-04 | Implementación (GREEN) | AC-001.1-4, AC-002.1-4, AC-004.1-3 | REQ-CAKE-001 a 005, 008, 009 |
| T005 | HU-03 | Prueba (RED) | AC-003.1-4 | REQ-CAKE-006, 007 |
| T006 | HU-03 | Implementación (GREEN) | AC-003.1-4 | REQ-CAKE-006, 007 |
| T007 | HU-05, HU-06 | Prueba (RED) | AC-005.1-3, AC-006.1-3 | REQ-CAKE-010, 011 |
| T008 | HU-05, HU-06 | Implementación (GREEN) | AC-005.1-3, AC-006.1-3 | REQ-CAKE-010, 011 |
| T009 | HU-07 | Prueba (RED) | AC-007.1-3 | REQ-CAKE-012 |
| T010 | HU-07 | Implementación (GREEN) | AC-007.1-3 | REQ-CAKE-012 |

---

**Total: 10 tareas (4 de prueba + 5 de implementación + 1 de configuración)**
**Total criterios de aceptación cubiertos: 24/24**
**Total requisitos funcionales cubiertos: REQ-CAKE-001 a REQ-CAKE-012 (12/12)**

---

# Tareas del Sprint 2 (v2.0) — CakeTrack

| Campo | Valor |
|-------|-------|
| **Sprint** | Sprint 2 v2.0 (HU-08 a HU-14) |
| **Referencia** | spec.md sección 10 (SPEC-CAKETRACK-001-S2 v2.0), plan.md sección 7 (PLAN-CAKETRACK-S2 v2.0) |
| **Estado** | Pendiente de aprobación (Puerta 3). Ninguna tarea iniciada. |
| **Enfoque** | Pruebas antes que implementación |

## Flujo de Ejecución Sprint 2

```
T011 → T012 [prueba] → T013 → T014 [prueba] → T015 → T017 [prueba] → T018 → T016
```

`T016` va al final porque ejecuta la suite completa (Sprint 1 + Sprint 2) y la navegación depende de las rutas creadas en T013, T015 y T018.

---

## Tareas Sprint 2

### Modelo de Datos Pedidos

- [x] **T011** `[HU-08, HU-09, HU-10, HU-11, HU-12, HU-14]` Crear el enum `EstadoPedido` (PENDIENTE_ABONO, EN_COLA, EN_PREPARACION, HORNEANDO, LISTO_PARA_ENTREGA, ENTREGADO, CANCELADO), las entidades `Pedido` y `DetallePedido` en `com.caketrack.model` con los métodos de dominio `getSaldoPendiente()` (BR-10) y `cumpleAbonoMinimo()` (BR-07), y los repositorios `PedidoRepository` y `DetallePedidoRepository` en `com.caketrack.repository`. Asignar `codigoPedido` tras insertar el registro (`CT-AAAA-` + `id` con cuatro dígitos, plan 7.10).
  - **Archivos**: `src/main/java/com/caketrack/model/EstadoPedido.java`, `src/main/java/com/caketrack/model/Pedido.java`, `src/main/java/com/caketrack/model/DetallePedido.java`, `src/main/java/com/caketrack/repository/PedidoRepository.java`, `src/main/java/com/caketrack/repository/DetallePedidoRepository.java`
  - **Dependencias**: Sprint 1 completo (`Usuario`, `Producto`)
  - **Criterios de aceptación**: Soporte de datos para `AC-008.1`, `AC-009.1`, `AC-010.1` y `AC-014.2`. Requisitos `REQ-CAKE-013` a `REQ-CAKE-016`, `REQ-CAKE-021`.
  - **Criterio de terminado**: `./mvnw test-compile` compila; tablas `pedidos` y `detalle_pedidos` creadas por `ddl-auto=update`; `getSaldoPendiente()` y `cumpleAbonoMinimo()` devuelven valores correctos en pruebas unitarias básicas.

### Pedidos y Abono

- [x] **T012** `[HU-08, HU-09, HU-10]` **[PRUEBA]** Escribir `PedidoAbonoTest.java` con `@SpringBootTest` y `@AutoConfigureMockMvc` que verifique: creación de pedido personalizado válido con anticipación de 24 h o más (`AC-008.1`, BR-06); rechazo de pedido personalizado con menos de 24 h de anticipación (`AC-008.2`, BR-06); creación de pedido de catálogo con subtotal correcto (`AC-008.3`); fijación del total por CAJERO en pedido personalizado sin detalles, con rechazo de total cero o negativo (`AC-008.4`, `AC-008.5`); cálculo de total como suma de subtotales (`AC-009.1`); registro de abono con referencia (`AC-009.2`); bloqueo de paso a `EN_COLA` con abono menor al 50 % (`AC-010.2`, BR-07); aprobación con abono de exactamente el 50 % (`AC-010.3`) y con abono mayor al 50 % (`AC-010.1`).
  - **Archivo**: `src/test/java/com/caketrack/PedidoAbonoTest.java`
  - **Dependencias**: T011
  - **Criterios de aceptación**: `AC-008.1`, `AC-008.2`, `AC-008.3`, `AC-008.4`, `AC-008.5`, `AC-009.1`, `AC-009.2`, `AC-010.1`, `AC-010.2`, `AC-010.3` (pruebas en estado **RED**).
  - **Criterio de terminado**: `./mvnw test-compile` compila; pruebas en estado **RED**.

- [x] **T013** `[HU-08, HU-09, HU-10]` Implementar `PedidoController.java` (`/pedidos/nuevo` GET/POST) y `CajaController.java` (`/caja/validacion-abonos`, `/caja/pedidos/{id}/abono`, `/caja/pedidos/{id}/validar-abono`) en `com.caketrack.controller`. Crear las vistas `templates/pedidos/nuevo.html` y `templates/caja/validacion-abonos.html` siguiendo `/specs/design.md` y `/css/caketrack.css`. Las vistas deben heredar `fragments/layout.html`.
  - **Archivos**: `src/main/java/com/caketrack/controller/PedidoController.java`, `src/main/java/com/caketrack/controller/CajaController.java`, `src/main/resources/templates/pedidos/nuevo.html`, `src/main/resources/templates/caja/validacion-abonos.html`
  - **Dependencias**: T012
  - **Criterios de aceptación**: `AC-008.1`, `AC-008.2`, `AC-008.3`, `AC-008.4`, `AC-008.5`, `AC-009.1`, `AC-009.2`, `AC-010.1`, `AC-010.2`, `AC-010.3` (pruebas pasan a **GREEN**). Requisitos `REQ-CAKE-013`, `REQ-CAKE-014`, `REQ-CAKE-015`, `REQ-CAKE-016` cubiertos.
  - **Criterio de terminado**: `./mvnw test` — todas las pruebas de `PedidoAbonoTest` pasan. Sin atributos `th:*`, `sec:authorize` ni `name` de formularios alterados.

### Cocina KDS e Inventario

- [ ] **T014** `[HU-11, HU-12]` **[PRUEBA]** Escribir `CocinaKdsInventarioTest.java` con `@SpringBootTest` y `@AutoConfigureMockMvc` que verifique: el tablero KDS lista los pedidos en `EN_COLA` ordenados por `fechaEntrega` ascendente (`AC-011.1`); el cambio a `EN_PREPARACION` descuenta exactamente la receta multiplicada por la cantidad pedida (`AC-012.1`, BR-08); el cambio con stock insuficiente se bloquea, no descuenta nada y muestra el ingrediente faltante con la cantidad que falta (`AC-012.2`, BR-09).
  - **Archivo**: `src/test/java/com/caketrack/CocinaKdsInventarioTest.java`
  - **Dependencias**: T013
  - **Criterios de aceptación**: `AC-011.1`, `AC-012.1`, `AC-012.2` (pruebas en estado **RED**).
  - **Criterio de terminado**: `./mvnw test-compile` compila; pruebas en estado **RED**.

- [ ] **T015** `[HU-11, HU-12]` Implementar `CocinaKdsController.java` (`/cocina/kds` y `/cocina/pedidos/{id}/estado`) en `com.caketrack.controller`. El cambio a `EN_PREPARACION` debe ejecutarse en un método `@Transactional` del controlador, con bloqueo pesimista sobre los ingredientes (`IngredienteRepository.findByIdForUpdate`). Agregar a `Ingrediente` los métodos `tieneStockPara()` y `descontar()`. Crear la vista `templates/cocina/kds.html` como tablero de tarjetas KDS con cambio de estado y alertas de stock.
  - **Archivos**: `src/main/java/com/caketrack/controller/CocinaKdsController.java`, `src/main/java/com/caketrack/model/Ingrediente.java` (modificación aditiva), `src/main/java/com/caketrack/repository/IngredienteRepository.java` (método nuevo), `src/main/resources/templates/cocina/kds.html`
  - **Dependencias**: T014
  - **Criterios de aceptación**: `AC-011.1`, `AC-012.1`, `AC-012.2` (pruebas pasan a **GREEN**). Requisitos `REQ-CAKE-017`, `REQ-CAKE-018`, `REQ-CAKE-019` cubiertos.
  - **Criterio de terminado**: `./mvnw test` — todas las pruebas de `CocinaKdsInventarioTest` pasan. No se crea ninguna clase `*Service`.

### Seguimiento y Liquidación

- [ ] **T017** `[HU-13, HU-14]` **[PRUEBA]** Escribir `SeguimientoPedidoTest.java` con `@SpringBootTest` y `@AutoConfigureMockMvc` que verifique: un CLIENTE solo ve sus propios pedidos con estado, fecha de entrega y saldo pendiente (`AC-013.1`, REQ-CAKE-020); la liquidación del saldo restante actualiza `montoAbonado` a `total` (`AC-014.1`, REQ-CAKE-021); el intento de marcar `ENTREGADO` con saldo pendiente se rechaza y el pedido permanece en `LISTO_PARA_ENTREGA` (`AC-014.2`, BR-10).
  - **Archivo**: `src/test/java/com/caketrack/SeguimientoPedidoTest.java`
  - **Dependencias**: T015
  - **Criterios de aceptación**: `AC-013.1`, `AC-014.1`, `AC-014.2` (pruebas en estado **RED**).
  - **Criterio de terminado**: `./mvnw test-compile` compila; pruebas en estado **RED**.

- [ ] **T018** `[HU-13, HU-14]` Implementar en `PedidoController` la vista `/mis-pedidos` (`templates/pedidos/mis-pedidos.html`) con filtro por cliente autenticado. Implementar en `CajaController` la liquidación (`/caja/pedidos/{id}/liquidar`) y la entrega (`/caja/pedidos/{id}/entregar`), usando `Pedido.puedeEntregarse()` (BR-10). Actualizar la vista `caja/validacion-abonos.html` con las acciones de liquidación.
  - **Archivos**: `src/main/java/com/caketrack/controller/PedidoController.java` (modificación aditiva), `src/main/java/com/caketrack/controller/CajaController.java` (modificación aditiva), `src/main/java/com/caketrack/model/Pedido.java` (método `puedeEntregarse()`), `src/main/resources/templates/pedidos/mis-pedidos.html`, `src/main/resources/templates/caja/validacion-abonos.html`
  - **Dependencias**: T017
  - **Criterios de aceptación**: `AC-013.1`, `AC-014.1`, `AC-014.2` (pruebas pasan a **GREEN**). Requisitos `REQ-CAKE-020` y `REQ-CAKE-021` cubiertos.
  - **Criterio de terminado**: `./mvnw test` — todas las pruebas de `SeguimientoPedidoTest` pasan.

- [ ] **T016** `[Integración]` Actualizar `templates/fragments/layout.html` con la navegación del Sprint 2 usando `sec:authorize`: "Mis pedidos" para CLIENTE; "Pedidos" para CLIENTE, CAJERO y ADMIN; "Caja" para CAJERO y ADMIN; "Cocina" para PASTELERO y ADMIN. Ejecutar la suite completa `./mvnw test` y verificar que `AuthSecurityTest`, `AdminUsuarioTest`, `InventarioRecetaTest`, `CatalogoTest`, `PedidoAbonoTest`, `CocinaKdsInventarioTest` y `SeguimientoPedidoTest` terminen en `BUILD SUCCESS`.
  - **Archivos**: `src/main/resources/templates/fragments/layout.html`
  - **Dependencias**: T013, T015, T018
  - **Criterios de aceptación**: `AC-004.1` y `AC-004.2` extendidos (menú filtrado por rol con las opciones del Sprint 2). Requisito `REQ-CAKE-008` extendido. Sin regresiones en el Sprint 1.
  - **Criterio de terminado**: `./mvnw test` finaliza con `BUILD SUCCESS` y 0 fallos en las siete clases de prueba.

---

## Resumen de Cobertura Sprint 2

| Tarea | HU | Tipo | Criterios AC | Requisitos | Reglas |
|-------|----|------|--------------|------------|--------|
| T011 | HU-08 a HU-14 | Modelo | — | — | BR-07, BR-10 |
| T012 | HU-08, HU-09, HU-10 | Prueba (RED) | AC-008.1-5, AC-009.1-2, AC-010.1-3 | REQ-CAKE-013 a 016 | BR-06, BR-07 |
| T013 | HU-08, HU-09, HU-10 | Implementación (GREEN) | AC-008.1-5, AC-009.1-2, AC-010.1-3 | REQ-CAKE-013 a 016 | BR-06, BR-07 |
| T014 | HU-11, HU-12 | Prueba (RED) | AC-011.1, AC-012.1-2 | REQ-CAKE-017 a 019 | BR-08, BR-09 |
| T015 | HU-11, HU-12 | Implementación (GREEN) | AC-011.1, AC-012.1-2 | REQ-CAKE-017 a 019 | BR-08, BR-09 |
| T017 | HU-13, HU-14 | Prueba (RED) | AC-013.1, AC-014.1-2 | REQ-CAKE-020, 021 | BR-10 |
| T018 | HU-13, HU-14 | Implementación (GREEN) | AC-013.1, AC-014.1-2 | REQ-CAKE-020, 021 | BR-10 |
| T016 | Integración | Navegación + suite | AC-004.1-2 (extendidos) | REQ-CAKE-008 (extendido) | — |

**Total tareas Sprint 2: 8 (4 de prueba + 3 de implementación + 1 de integración)**
**Total criterios de aceptación Sprint 2 cubiertos: 16/16**
**Total requisitos funcionales Sprint 2 cubiertos: REQ-CAKE-013 a REQ-CAKE-021 (9/9)**
**Total reglas de negocio Sprint 2 cubiertas: BR-06 a BR-10 (5/5)**
