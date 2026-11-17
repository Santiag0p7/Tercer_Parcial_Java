# Plan Técnico — Sprint 1 CakeTrack

| Campo | Valor |
|-------|-------|
| **ID** | PLAN-CAKETRACK-S1 |
| **Versión** | 1.0 |
| **Fecha** | 2026-11-17 |
| **Referencia** | spec.md (SPEC-CAKETRACK-001 v1.0), ADR-001 |
| **ADR aplicable** | ADR-001: Arquitectura monolítica sin capa service |

---

## 1. Modelo de Datos Relacional (MySQL)

### 1.1 Tabla `usuarios`

| Columna | Tipo | Restricciones | Descripción |
|---------|------|---------------|-------------|
| `id` | BIGINT | PK, AUTO_INCREMENT | Identificador único |
| `nombre` | VARCHAR(100) | NOT NULL | Nombre completo |
| `correo` | VARCHAR(150) | NOT NULL, UNIQUE | Correo electrónico (username) |
| `password` | VARCHAR(255) | NOT NULL | Contraseña cifrada BCrypt |
| `rol` | ENUM('CLIENTE','CAJERO','PASTELERO','REPARTIDOR','ADMIN') | NOT NULL | Rol del usuario |
| `activo` | BOOLEAN | NOT NULL, DEFAULT TRUE | Estado de la cuenta |
| `token_recuperacion` | VARCHAR(255) | NULLABLE | Token de un solo uso (BR-03) |
| `token_expiracion` | DATETIME | NULLABLE | Fecha/hora de expiración del token |
| `token_usado` | BOOLEAN | DEFAULT FALSE | Indicador de token ya utilizado |
| `created_at` | DATETIME | NOT NULL, DEFAULT NOW() | Fecha de creación |
| `updated_at` | DATETIME | NOT NULL, ON UPDATE NOW() | Fecha de última actualización |

### 1.2 Tabla `ingredientes`

| Columna | Tipo | Restricciones | Descripción |
|---------|------|---------------|-------------|
| `id` | BIGINT | PK, AUTO_INCREMENT | Identificador único |
| `nombre` | VARCHAR(100) | NOT NULL, UNIQUE | Nombre del ingrediente |
| `unidad_medida` | VARCHAR(20) | NOT NULL | Unidad (kg, g, L, mL, unidad) |
| `cantidad_actual` | DECIMAL(10,2) | NOT NULL, DEFAULT 0 | Stock actual |
| `stock_minimo` | DECIMAL(10,2) | NOT NULL | Umbral de alerta (BR-04) |
| `created_at` | DATETIME | NOT NULL, DEFAULT NOW() | Fecha de creación |
| `updated_at` | DATETIME | NOT NULL, ON UPDATE NOW() | Fecha de última actualización |

### 1.3 Tabla `productos`

| Columna | Tipo | Restricciones | Descripción |
|---------|------|---------------|-------------|
| `id` | BIGINT | PK, AUTO_INCREMENT | Identificador único |
| `nombre` | VARCHAR(150) | NOT NULL | Nombre del producto |
| `tipo` | ENUM('TORTA','POSTRE') | NOT NULL | Tipo de producto |
| `precio` | DECIMAL(10,2) | NOT NULL | Precio en COP |
| `disponible` | BOOLEAN | NOT NULL, DEFAULT FALSE | Estado de disponibilidad (BR-05) |
| `descripcion` | TEXT | NULLABLE | Descripción opcional |
| `created_at` | DATETIME | NOT NULL, DEFAULT NOW() | Fecha de creación |
| `updated_at` | DATETIME | NOT NULL, ON UPDATE NOW() | Fecha de última actualización |

### 1.4 Tabla `receta_items`

| Columna | Tipo | Restricciones | Descripción |
|---------|------|---------------|-------------|
| `id` | BIGINT | PK, AUTO_INCREMENT | Identificador único |
| `producto_id` | BIGINT | NOT NULL, FK → productos.id | Producto al que pertenece |
| `ingrediente_id` | BIGINT | NOT NULL, FK → ingredientes.id | Ingrediente de la receta |
| `cantidad` | DECIMAL(10,2) | NOT NULL | Cantidad requerida |

### 1.5 Diagrama de Relaciones

```
┌─────────────┐       ┌──────────────────┐       ┌─────────────────┐
│  usuarios   │       │    productos     │       │  ingredientes   │
├─────────────┤       ├──────────────────┤       ├─────────────────┤
│ id          │       │ id               │       │ id              │
│ nombre      │       │ nombre           │◄──┐   │ nombre          │
│ correo      │       │ tipo             │   │   │ unidad_medida   │
│ password    │       │ precio           │   │   │ cantidad_actual │
│ rol (ENUM)  │       │ disponible       │   │   │ stock_minimo    │
│ activo      │       │ descripcion      │   │   └─────────────────┘
│ token_*     │       └──────────────────┘   │            │
└─────────────┘                │              │            │
                               │              │            │
                          ┌────┴──────────────┴────────────┘
                          │   receta_items
                          ├──────────────────┐
                          │ id               │
                          │ producto_id  (FK)│──→ productos.id
                          │ ingrediente_id(FK│──→ ingredientes.id
                          │ cantidad         │
                          └──────────────────┘
```

### 1.6 Enum `Rol`

```
Rol: CLIENTE | CAJERO | PASTELERO | REPARTIDOR | ADMIN
```

Definido como `enum` en `com.caketrack.model.Rol`. Mapea directamente al ENUM de MySQL.

---

## 2. Mapa de Rutas Web (`@Controller`)

### 2.1 AuthController — Autenticación y Recuperación

| Método | Ruta HTTP | Acción | Roles permitidos |
|--------|-----------|--------|------------------|
| GET | `/login` | Mostrar formulario de inicio de sesión | Público (anon) |
| POST | `/login` | Procesar inicio de sesión | Público (anon) |
| GET | `/logout` | Cerrar sesión | Todos autenticados |
| GET | `/recuperar` | Mostrar formulario de recuperación | Público (anon) |
| POST | `/recuperar` | Generar token de recuperación | Público (anon) |
| GET | `/restablecer/{token}` | Mostrar formulario de restablecimiento | Público (anon) |
| POST | `/restablecer/{token}` | Procesar cambio de contraseña | Público (anon) |

### 2.2 HomeController — Página Principal

| Método | Ruta HTTP | Acción | Roles permitidos |
|--------|-----------|--------|------------------|
| GET | `/` | Redirigir según rol | Todos autenticados |
| GET | `/inicio` | Mostrar página principal | Todos autenticados |

### 2.3 AdminUsuarioController — Gestión de Usuarios

| Método | Ruta HTTP | Acción | Roles permitidos |
|--------|-----------|--------|------------------|
| GET | `/admin/usuarios` | Listar todos los usuarios | ADMIN |
| GET | `/admin/usuarios/crear` | Mostrar formulario de creación | ADMIN |
| POST | `/admin/usuarios/crear` | Crear nuevo usuario | ADMIN |
| POST | `/admin/usuarios/{id}/toggle-activo` | Activar/desactivar usuario | ADMIN |

### 2.4 InventarioController — Ingredientes y Productos

| Método | Ruta HTTP | Acción | Roles permitidos |
|--------|-----------|--------|------------------|
| GET | `/admin/ingredientes` | Listar ingredientes con alertas | ADMIN, PASTELERO |
| GET | `/admin/ingredientes/crear` | Mostrar formulario de ingrediente | ADMIN, PASTELERO |
| POST | `/admin/ingredientes/crear` | Registrar ingrediente | ADMIN, PASTELERO |
| GET | `/admin/ingredientes/{id}/editar` | Mostrar formulario de edición | ADMIN, PASTELERO |
| POST | `/admin/ingredientes/{id}/editar` | Actualizar ingrediente | ADMIN, PASTELERO |
| GET | `/admin/productos` | Listar productos del catálogo | ADMIN, PASTELERO |
| GET | `/admin/productos/crear` | Mostrar formulario de producto | ADMIN, PASTELERO |
| POST | `/admin/productos/crear` | Crear producto con receta | ADMIN, PASTELERO |
| POST | `/admin/productos/{id}/toggle-disponible` | Marcar disponible/no disponible | ADMIN, PASTELERO |

### 2.5 CatalogoController — Consulta Pública de Catálogo

| Método | Ruta HTTP | Acción | Roles permitidos |
|--------|-----------|--------|------------------|
| GET | `/catalogo` | Mostrar catálogo con filtro opcional por tipo | Todos los roles |

---

## 3. Matriz de Permisos Spring Security

| Ruta | CLIENTE | CAJERO | PASTELERO | REPARTIDOR | ADMIN | Público |
|------|---------|--------|-----------|------------|-------|---------|
| `/login` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| `/logout` | ✅ | ✅ | ✅ | ✅ | ✅ | — |
| `/recuperar` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| `/restablecer/{token}` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| `/inicio` | ✅ | ✅ | ✅ | ✅ | ✅ | — |
| `/catalogo` | ✅ | ✅ | ✅ | ✅ | ✅ | — |
| `/admin/usuarios/**` | ❌ | ❌ | ❌ | ❌ | ✅ | — |
| `/admin/ingredientes/**` | ❌ | ❌ | ✅ | ❌ | ✅ | — |
| `/admin/productos/**` | ❌ | ❌ | ✅ | ❌ | ✅ | — |

---

## 4. Matriz de Cobertura: Requisito → Componente → Vista

| Requisito | HU | Componente Java | Vista Thymeleaf | Test |
|-----------|----|-----------------|-----------------|------|
| **REQ-CAKE-001** | HU-01 | `AuthController.java` + `Usuario.java` (método dominio) | `login.html` | `AuthSecurityTest.java` |
| **REQ-CAKE-002** | HU-01 | `AuthController.java` + `UsuarioRepository.java` | `login.html` → redirect | `AuthSecurityTest.java` |
| **REQ-CAKE-003** | HU-01 | `SecurityConfig.java` (logout handler) | `layout.html` (botón) | `AuthSecurityTest.java` |
| **REQ-CAKE-004** | HU-02 | `AuthController.java` + `Usuario.java` (generar token) | `recuperar.html` | `AuthSecurityTest.java` |
| **REQ-CAKE-005** | HU-02 | `AuthController.java` + `Usuario.java` (validar token) | `restablecer.html` | `AuthSecurityTest.java` |
| **REQ-CAKE-006** | HU-03 | `AdminUsuarioController.java` + `UsuarioRepository.java` | `admin/usuarios.html` | `AdminUsuarioTest.java` |
| **REQ-CAKE-007** | HU-03 | `AdminUsuarioController.java` + `Usuario.java` (toggle activo) | `admin/usuarios.html` | `AdminUsuarioTest.java` |
| **REQ-CAKE-008** | HU-04 | `layout.html` (fragmento Thymeleaf + `sec:authorize`) | `layout.html` | `AuthSecurityTest.java` |
| **REQ-CAKE-009** | HU-04 | `SecurityConfig.java` (authorization rules) | Página 403 error | `AuthSecurityTest.java` |
| **REQ-CAKE-010** | HU-05 | `InventarioController.java` + `Ingrediente.java` (esAlerta) | `admin/ingredientes.html` | `InventarioRecetaTest.java` |
| **REQ-CAKE-011** | HU-06 | `InventarioController.java` + `Producto.java` + `RecetaItem.java` | `admin/productos.html` | `InventarioRecetaTest.java` |
| **REQ-CAKE-012** | HU-07 | `CatalogoController.java` + `ProductoRepository.java` | `catalogo.html` | `CatalogoTest.java` |

---

## 5. Estructura de Archivos del Sprint 1

```
src/main/java/com/caketrack/
├── CakeTrackApplication.java
├── config/
│   ├── SecurityConfig.java              (T004)
│   └── DataInitializer.java             (T001)
├── model/
│   ├── Rol.java                         (T002)
│   ├── Usuario.java                     (T002)
│   ├── Ingrediente.java                 (T002)
│   ├── Producto.java                    (T002)
│   └── RecetaItem.java                  (T002)
├── repository/
│   ├── UsuarioRepository.java           (T002)
│   ├── IngredienteRepository.java       (T002)
│   ├── ProductoRepository.java          (T002)
│   └── RecetaItemRepository.java        (T002)
└── controller/
    ├── AuthController.java              (T004)
    ├── HomeController.java              (T004)
    ├── AdminUsuarioController.java      (T006)
    ├── InventarioController.java        (T008)
    └── CatalogoController.java          (T010)

src/main/resources/
├── application.properties               (T001)
├── messages.properties                  (T001)
└── templates/
    ├── layout.html                      (T004)
    ├── login.html                       (T004)
    ├── recuperar.html                   (T004)
    ├── restablecer.html                 (T004)
    ├── inicio.html                      (T004)
    ├── catalogo.html                    (T010)
    └── admin/
        ├── usuarios.html                (T006)
        ├── ingredientes.html            (T008)
        └── productos.html               (T008)

src/test/java/com/caketrack/
├── config/
│   └── DataInitializerTest.java         (T001)
├── model/
│   ├── UsuarioTest.java                 (T002)
│   ├── IngredienteTest.java             (T002)
│   └── ProductoTest.java               (T002)
└── controller/
    ├── AuthSecurityTest.java            (T003)
    ├── AdminUsuarioTest.java            (T005)
    ├── InventarioRecetaTest.java        (T007)
    └── CatalogoTest.java                (T009)
```

---

## 6. Dependencias entre Componentes

```
T001 (config + DataInitializer)
  └── T002 (entidades + repositorios)
        ├── T003 [prueba] → T004 (implementación auth)
        │     └── T005 [prueba] → T006 (implementación usuarios)
        │           └── T007 [prueba] → T008 (implementación inventario)
        │                 └── T009 [prueba] → T010 (implementación catálogo)
```

---

**Fin del documento — PLAN-CAKETRACK-S1 v1.0**
