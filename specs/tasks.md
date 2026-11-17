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

- [ ] **T001** `[Sprint Setup]` Configurar `application.properties` (MySQL, Thymeleaf, mensajes i18n) y crear `DataInitializer.java` en `com.caketrack.config` que poble al iniciar la aplicación 1 usuario de prueba por cada uno de los 5 roles (CLIENTE, CAJERO, PASTELERO, REPARTIDOR, ADMIN) con contraseñas BCrypt.
  - **Archivo**: `src/main/resources/application.properties`, `src/main/resources/messages.properties`, `src/main/java/com/caketrack/config/DataInitializer.java`
  - **Dependencias**: Ninguna (primera tarea)
  - **Criterios de aceptación**: `AC-003.1` (contraseñas BCrypt), verificación de 5 usuarios con roles correctos en base de datos al iniciar la aplicación.
  - **Criterio de terminado**: `./mvnw test` compila sin errores; `DataInitializer` crea 5 usuarios verificables al arrancar.

---

### Modelo de Datos y Repositorios

- [ ] **T002** `[HU-01 a HU-07]` Crear entidades JPA (`Usuario`, `Ingrediente`, `Producto`, `RecetaItem`) en `com.caketrack.model` y el enum `Rol` con los 5 perfiles. Crear interfaces `JpaRepository` correspondientes en `com.caketrack.repository`. Incluir métodos de dominio en entidades: `Usuario.esActivo()`, `Usuario.tokenExpirado()`, `Ingrediente.esAlertaReposicion()`, `Producto.puedeMarcarDisponible()`.
  - **Archivos**: `src/main/java/com/caketrack/model/Rol.java`, `src/main/java/com/caketrack/model/Usuario.java`, `src/main/java/com/caketrack/model/Ingrediente.java`, `src/main/java/com/caketrack/model/Producto.java`, `src/main/java/com/caketrack/model/RecetaItem.java`, `src/main/java/com/caketrack/repository/UsuarioRepository.java`, `src/main/java/com/caketrack/repository/IngredienteRepository.java`, `src/main/java/com/caketrack/repository/ProductoRepository.java`, `src/main/java/com/caketrack/repository/RecetaItemRepository.java`
  - **Dependencias**: T001
  - **Criterios de aceptación**: `BR-01` (contraseña BCrypt en `Usuario`), `BR-02` (`esActivo()`), `BR-03` (`tokenExpirado()`), `BR-04` (`esAlertaReposicion()`), `BR-05` (`puedeMarcarDisponible()` requiere ≥1 `RecetaItem`).
  - **Criterio de terminado**: `./mvnw test-compile` compila; tablas creadas por `ddl-auto=update` al arrancar.

---

### Seguridad y Autenticación

- [ ] **T003** `[HU-01, HU-02, HU-04]` **[PRUEBA]** Escribir `AuthSecurityTest.java` con pruebas de: inicio de sesión exitoso con credenciales válidas (`AC-001.1`), rechazo de credenciales incorrectas (`AC-001.2`), bloqueo de usuario inactivo con mensaje correspondiente (`AC-001.3`), cierre de sesión y destrucción de sesión (`AC-001.4`), generación de token de recuperación con respuesta genérica sin filtrar existencia del correo (`AC-002.1`, `AC-002.2`), rechazo de token expirado o ya utilizado (`AC-002.3`, `AC-002.4`), menú filtrado por rol con `sec:authorize` (`AC-004.1`, `AC-004.2`) y bloqueo HTTP 403 al acceder a ruta no autorizada (`AC-004.3`).
  - **Archivo**: `src/test/java/com/caketrack/controller/AuthSecurityTest.java`
  - **Dependencias**: T001, T002
  - **Criterios de aceptación**: `AC-001.1`, `AC-001.2`, `AC-001.3`, `AC-001.4`, `AC-002.1`, `AC-002.2`, `AC-002.3`, `AC-002.4`, `AC-004.1`, `AC-004.2`, `AC-004.3` (pruebas en rojo — fallan porque no existe implementación aún).
  - **Criterio de terminado**: Todas las pruebas compilan con `./mvnw test-compile` y están en estado **RED** (fallan por ausencia de implementación).

- [ ] **T004** `[HU-01, HU-02, HU-04]` Implementar `SecurityConfig.java` en `com.caketrack.config` (cadena de filtros, `PasswordEncoder` BCrypt, reglas de autorización por rol, login/logout). Implementar `AuthController.java` y `HomeController.java` en `com.caketrack.controller`. Crear vistas: `templates/layout.html` (fragmento con menú dinámico usando `sec:authorize`), `templates/login.html`, `templates/recuperar.html`, `templates/restablecer.html` y `templates/inicio.html`.
  - **Archivos**: `src/main/java/com/caketrack/config/SecurityConfig.java`, `src/main/java/com/caketrack/controller/AuthController.java`, `src/main/java/com/caketrack/controller/HomeController.java`, `src/main/resources/templates/layout.html`, `src/main/resources/templates/login.html`, `src/main/resources/templates/recuperar.html`, `src/main/resources/templates/restablecer.html`, `src/main/resources/templates/inicio.html`
  - **Dependencias**: T003 (las pruebas ya existen en estado RED)
  - **Criterios de aceptación**: `AC-001.1`, `AC-001.2`, `AC-001.3`, `AC-001.4`, `AC-002.1`, `AC-002.2`, `AC-002.3`, `AC-002.4`, `AC-004.1`, `AC-004.2`, `AC-004.3` (pruebas pasan a estado **GREEN**).
  - **Criterio de terminado**: `./mvnw test` — todas las pruebas de `AuthSecurityTest` pasan. Requisito `REQ-CAKE-001` a `REQ-CAKE-005`, `REQ-CAKE-008`, `REQ-CAKE-009` cubiertos.

---

### Gestión de Usuarios

- [ ] **T005** `[HU-03]` **[PRUEBA]** Escribir `AdminUsuarioTest.java` con pruebas de: creación de usuario con rol válido y contraseña BCrypt (`AC-003.1`), rechazo de contraseña con menos de 8 caracteres (`AC-003.2`), desactivación de usuario activo (`AC-003.3`) y activación de usuario inactivo (`AC-003.4`).
  - **Archivo**: `src/test/java/com/caketrack/controller/AdminUsuarioTest.java`
  - **Dependencias**: T004 (la infraestructura de seguridad ya permite autenticar ADMIN)
  - **Criterios de aceptación**: `AC-003.1`, `AC-003.2`, `AC-003.3`, `AC-003.4` (pruebas en estado **RED**).
  - **Criterio de terminado**: `./mvnw test-compile` compila; pruebas en estado **RED**.

- [ ] **T006** `[HU-03]` Implementar `AdminUsuarioController.java` en `com.caketrack.controller` con rutas `/admin/usuarios`, `/admin/usuarios/crear` y `/admin/usuarios/{id}/toggle-activo`. Crear vista `templates/admin/usuarios.html` con formulario de creación (nombre, correo, contraseña, rol) y tabla de usuarios con botón de toggle activo/inactivo.
  - **Archivos**: `src/main/java/com/caketrack/controller/AdminUsuarioController.java`, `src/main/resources/templates/admin/usuarios.html`
  - **Dependencias**: T005
  - **Criterios de aceptación**: `AC-003.1`, `AC-003.2`, `AC-003.3`, `AC-003.4` (pruebas pasan a **GREEN**). Requisito `REQ-CAKE-006`, `REQ-CAKE-007` cubiertos.
  - **Criterio de terminado**: `./mvnw test` — todas las pruebas de `AdminUsuarioTest` pasan.

---

### Inventario y Recetas

- [ ] **T007** `[HU-05, HU-06]` **[PRUEBA]** Escribir `InventarioRecetaTest.java` con pruebas de: registro de ingrediente con cantidad actual y stock mínimo (`AC-005.1`), cálculo y visualización de alerta de reposición cuando `cantidad_actual <= stock_minimo` (`AC-005.2`), actualización de cantidad actual que remueve la alerta (`AC-005.3`), creación de producto con ingredientes de receta (`AC-006.1`), rechazo de producto sin ingredientes asociados (`AC-006.2`) y marcado de producto como disponible cuando tiene al menos un ingrediente en receta (`AC-006.3`).
  - **Archivo**: `src/test/java/com/caketrack/controller/InventarioRecetaTest.java`
  - **Dependencias**: T004
  - **Criterios de aceptación**: `AC-005.1`, `AC-005.2`, `AC-005.3`, `AC-006.1`, `AC-006.2`, `AC-006.3` (pruebas en estado **RED**).
  - **Criterio de terminado**: `./mvnw test-compile` compila; pruebas en estado **RED**.

- [ ] **T008** `[HU-05, HU-06]` Implementar `InventarioController.java` en `com.caketrack.controller` con rutas para CRUD de ingredientes y gestión de productos con receta. Crear vistas `templates/admin/ingredientes.html` (lista con indicador visual de alerta BR-04, formulario de registro/edición) y `templates/admin/productos.html` (lista con toggle de disponibilidad, formulario de creación con selector de ingredientes).
  - **Archivos**: `src/main/java/com/caketrack/controller/InventarioController.java`, `src/main/resources/templates/admin/ingredientes.html`, `src/main/resources/templates/admin/productos.html`
  - **Dependencias**: T007
  - **Criterios de aceptación**: `AC-005.1`, `AC-005.2`, `AC-005.3`, `AC-006.1`, `AC-006.2`, `AC-006.3` (pruebas pasan a **GREEN**). Requisito `REQ-CAKE-010`, `REQ-CAKE-011` cubiertos.
  - **Criterio de terminado**: `./mvnw test` — todas las pruebas de `InventarioRecetaTest` pasan.

---

### Catálogo Público

- [ ] **T009** `[HU-07]` **[PRUEBA]** Escribir `CatalogoTest.java` con pruebas de: consulta de catálogo mostrando nombre, tipo, precio y disponibilidad (`AC-007.1`), filtro por tipo de producto TORTA o POSTRE (`AC-007.2`) y mensaje cuando el catálogo está vacío (`AC-007.3`).
  - **Archivo**: `src/test/java/com/caketrack/controller/CatalogoTest.java`
  - **Dependencias**: T008 (se requieren productos creados para consultar)
  - **Criterios de aceptación**: `AC-007.1`, `AC-007.2`, `AC-007.3` (pruebas en estado **RED**).
  - **Criterio de terminado**: `./mvnw test-compile` compila; pruebas en estado **RED**.

- [ ] **T010** `[HU-07]` Implementar `CatalogoController.java` en `com.caketrack.controller` con ruta `/catalogo` y parámetro opcional `?tipo=TORTA|POSTRE`. Crear vista `templates/catalogo.html` con tabla de productos (nombre, tipo, precio, disponibilidad) y selector de filtro por tipo.
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
