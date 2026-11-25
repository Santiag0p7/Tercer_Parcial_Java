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
├── AuthSecurityTest.java                (T003)
├── AdminUsuarioTest.java                (T005)
├── InventarioRecetaTest.java            (T007)
└── CatalogoTest.java                    (T009)
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

---

## 7. Diseño Técnico — Sprint 2 (v2.0)

| Campo | Valor |
|-------|-------|
| **ID** | PLAN-CAKETRACK-S2 |
| **Versión** | 2.0 |
| **Estado** | Borrador - pendiente de aprobación (Puerta 2) |
| **Referencia** | SPEC-CAKETRACK-001-S2 v2.0 (`spec.md` sección 10), PLAN-CAKETRACK-S1 v1.0 |
| **ADR aplicable** | ADR-001: Arquitectura monolítica sin capa service |

Las secciones 1 a 6 (Sprint 1) no se modifican. Este plan agrega las tablas, entidades, rutas y tareas del Sprint 2.

### 7.1 Enum `EstadoPedido`

```
EstadoPedido: PENDIENTE_ABONO | EN_COLA | EN_PREPARACION | HORNEANDO | LISTO_PARA_ENTREGA | ENTREGADO | CANCELADO
```

Definido en `com.caketrack.model.EstadoPedido`, mapeado con `@Enumerated(EnumType.STRING)`.

### 7.2 Máquina de estados

| Desde | Hacia | Actor | Condición | Regla |
|-------|-------|-------|-----------|-------|
| `PENDIENTE_ABONO` | `EN_COLA` | CAJERO | `cumpleAbonoMinimo()` y `abonoValidado = true` | BR-07 |
| `EN_COLA` | `EN_PREPARACION` | PASTELERO | Stock suficiente para toda la receta. Descuenta inventario en la misma transacción. | BR-08, BR-09 |
| `EN_PREPARACION` | `HORNEANDO` | PASTELERO | — | — |
| `HORNEANDO` | `LISTO_PARA_ENTREGA` | PASTELERO | — | — |
| `LISTO_PARA_ENTREGA` | `ENTREGADO` | CAJERO | `puedeEntregarse()` (saldo = 0) | BR-10 |
| `PENDIENTE_ABONO`, `EN_COLA` | `CANCELADO` | — | Fuera de alcance del Sprint 2 (ver 7.10) | — |

Cualquier transición fuera de esta tabla se rechaza en el controlador con un mensaje de error.

### 7.3 Tablas nuevas

#### Tabla `pedidos`

| Columna | Tipo | Restricciones | Descripción |
|---------|------|---------------|-------------|
| `id` | BIGINT | PK, AUTO_INCREMENT | Identificador único |
| `codigo_pedido` | VARCHAR(20) | UNIQUE, NULLABLE | Código visible `CT-AAAA-NNNN`. Se asigna tras insertar el registro: `NNNN` es el `id` con cuatro dígitos (p. ej. `CT-2026-0042`). Nullable solo porque el `id` no existe antes del insert. |
| `cliente_id` | BIGINT | NOT NULL, FK → usuarios.id | Cliente que realiza el pedido |
| `fecha_creacion` | DATETIME | NOT NULL | Fecha/hora de creación |
| `fecha_entrega` | DATETIME | NOT NULL | Fecha/hora de entrega programada |
| `es_personalizado` | BOOLEAN | NOT NULL, DEFAULT FALSE | Indica encargo personalizado (BR-06) |
| `sabor_bizcocho` | VARCHAR(100) | NULLABLE | Sabor del bizcocho |
| `relleno` | VARCHAR(100) | NULLABLE | Relleno |
| `cobertura` | VARCHAR(100) | NULLABLE | Cobertura |
| `dedicatoria` | VARCHAR(200) | NULLABLE | Texto opcional |
| `porciones` | INT | NULLABLE | Número de porciones |
| `notas_adicionales` | TEXT | NULLABLE | Notas del cliente |
| `total` | DECIMAL(10,2) | NOT NULL, DEFAULT 0 | Total del pedido en COP |
| `monto_abonado` | DECIMAL(10,2) | NOT NULL, DEFAULT 0 | Monto recibido |
| `abono_validado` | BOOLEAN | NOT NULL, DEFAULT FALSE | Validación del CAJERO (BR-07) |
| `referencia_pago` | VARCHAR(100) | NULLABLE | Referencia del abono |
| `estado` | ENUM('PENDIENTE_ABONO','EN_COLA','EN_PREPARACION','HORNEANDO','LISTO_PARA_ENTREGA','ENTREGADO','CANCELADO') | NOT NULL | Estado del pedido (7.2) |
| `created_at` | DATETIME | NOT NULL, DEFAULT NOW() | Fecha de creación del registro |
| `updated_at` | DATETIME | NOT NULL, ON UPDATE NOW() | Fecha de última actualización |

#### Tabla `detalle_pedidos`

| Columna | Tipo | Restricciones | Descripción |
|---------|------|---------------|-------------|
| `id` | BIGINT | PK, AUTO_INCREMENT | Identificador único |
| `pedido_id` | BIGINT | NOT NULL, FK → pedidos.id | Pedido al que pertenece |
| `producto_id` | BIGINT | NOT NULL, FK → productos.id | Producto del catálogo |
| `cantidad` | INT | NOT NULL, CHECK (> 0) | Unidades pedidas |
| `precio_unitario` | DECIMAL(10,2) | NOT NULL | Precio copiado del producto al crear el pedido |
| `subtotal` | DECIMAL(10,2) | NOT NULL | `precio_unitario * cantidad` |

Las tablas se crean con `spring.jpa.hibernate.ddl-auto=update`, igual que en el Sprint 1.

### 7.4 Entidades JPA y métodos de dominio (`com.caketrack.model`)

**`Pedido`** (`@Entity`)
- Campos: `id`, `codigoPedido`, `cliente` (`@ManyToOne Usuario`), `fechaCreacion`, `fechaEntrega`, `esPersonalizado`, `saborBizcocho`, `relleno`, `cobertura`, `dedicatoria`, `porciones`, `notasAdicionales`, `total` (`BigDecimal`), `montoAbonado` (`BigDecimal`), `abonoValidado`, `referenciaPago`, `estado` (`@Enumerated`), `detalles` (`@OneToMany(mappedBy = "pedido", cascade = ALL, orphanRemoval = true) List<DetallePedido>`).
- `BigDecimal getSaldoPendiente()`: devuelve `total - montoAbonado`. (BR-10)
- `boolean cumpleAbonoMinimo()`: `montoAbonado >= total * 0.50`, usando `compareTo`. Es verdadero cuando el monto es exactamente el 50 %. (BR-07)
- `boolean puedeEntregarse()`: `getSaldoPendiente().compareTo(BigDecimal.ZERO) == 0`. (BR-10)
- `boolean cumpleAnticipacion(LocalDateTime ahora)`: `fechaEntrega` es igual o posterior a `ahora + 24 h`. Solo se usa para pedidos personalizados. (BR-06)
- `void recalcularTotal()`: suma los subtotales de `detalles`. En pedidos personalizados sin detalles, el total lo fija el CAJERO (ver 7.10).

**`DetallePedido`** (`@Entity`)
- Campos: `id`, `pedido` (`@ManyToOne`), `producto` (`@ManyToOne`), `cantidad`, `precioUnitario`, `subtotal`.
- `void calcularSubtotal()`: `precioUnitario * cantidad`.

**`Ingrediente`** (método agregado en T015, sin cambiar el Sprint 1)
- `boolean tieneStockPara(BigDecimal cantidadRequerida)`: `cantidadActual >= cantidadRequerida`.
- `void descontar(BigDecimal cantidad)`: resta del `cantidadActual`.

### 7.5 Repositorios (`com.caketrack.repository`)

- **`PedidoRepository extends JpaRepository<Pedido, Long>`**
  - `List<Pedido> findByClienteOrderByFechaEntregaAsc(Usuario cliente)`: para "Mis pedidos" (REQ-CAKE-020).
  - `List<Pedido> findByEstadoInOrderByFechaEntregaAsc(Collection<EstadoPedido> estados)`: para el KDS (REQ-CAKE-017).
  - `List<Pedido> findByEstadoOrderByFechaCreacionAsc(EstadoPedido estado)`: para la lista de validación de abonos.
- **`DetallePedidoRepository extends JpaRepository<DetallePedido, Long>`**
  - `List<DetallePedido> findByPedido(Pedido pedido)`.
- **`IngredienteRepository`** (agregar en T015, sin modificar métodos existentes)
  - `@Lock(LockModeType.PESSIMISTIC_WRITE) @Query(...) Optional<Ingrediente> findByIdForUpdate(Long id)`: evita que dos PASTELEROS descuenten el mismo stock a la vez (BR-08).

### 7.6 Mapa de rutas Sprint 2 (`@Controller`)

**PedidoController**

| Método | Ruta HTTP | Acción | Roles permitidos |
|--------|-----------|--------|------------------|
| GET | `/pedidos/nuevo` | Formulario de pedido (catálogo o personalizado) | CLIENTE, CAJERO, ADMIN |
| POST | `/pedidos/nuevo` | Registrar pedido (valida BR-06 y calcula total) | CLIENTE, CAJERO, ADMIN |
| GET | `/mis-pedidos` | Lista de pedidos propios con estado, entrega y saldo | CLIENTE |

**CajaController**

| Método | Ruta HTTP | Acción | Roles permitidos |
|--------|-----------|--------|------------------|
| GET | `/caja/validacion-abonos` | Lista de pedidos en `PENDIENTE_ABONO` y `LISTO_PARA_ENTREGA` | CAJERO, ADMIN |
| POST | `/caja/pedidos/{id}/abono` | Registrar abono con referencia de pago | CAJERO, ADMIN |
| POST | `/caja/pedidos/{id}/total` | Fijar total de un pedido personalizado sin productos (mayor que cero, AC-008.4 y AC-008.5) | CAJERO, ADMIN |
| POST | `/caja/pedidos/{id}/validar-abono` | Validar abono y pasar a `EN_COLA` si cumple el 50 % (BR-07) | CAJERO, ADMIN |
| POST | `/caja/pedidos/{id}/liquidar` | Registrar pago del saldo restante | CAJERO, ADMIN |
| POST | `/caja/pedidos/{id}/entregar` | Marcar `ENTREGADO` si `puedeEntregarse()` (BR-10) | CAJERO, ADMIN |

**CocinaKdsController**

| Método | Ruta HTTP | Acción | Roles permitidos |
|--------|-----------|--------|------------------|
| GET | `/cocina/kds` | Tablero de pedidos `EN_COLA` y `EN_PREPARACION`, ordenados por `fechaEntrega` (REQ-CAKE-017) | PASTELERO, ADMIN |
| POST | `/cocina/pedidos/{id}/estado` | Cambiar estado según la máquina de 7.2. Si pasa a `EN_PREPARACION`, valida y descuenta stock (BR-08, BR-09) | PASTELERO, ADMIN |

### 7.7 Matriz de permisos Sprint 2 (ampliación de la sección 3)

| Ruta | CLIENTE | CAJERO | PASTELERO | REPARTIDOR | ADMIN | Público |
|------|---------|--------|-----------|------------|-------|---------|
| `/pedidos/**` | ✅ | ✅ | ❌ | ❌ | ✅ | — |
| `/mis-pedidos` | ✅ | ❌ | ❌ | ❌ | ❌ | — |
| `/caja/**` | ❌ | ✅ | ❌ | ❌ | ✅ | — |
| `/cocina/**` | ❌ | ❌ | ✅ | ❌ | ✅ | — |

Reglas de `SecurityConfig` (T015 y T018): `/caja/**` y `/cocina/**` se configuran por rol en la cadena de filtros. Para `/mis-pedidos` se requiere autenticación con rol CLIENTE.

### 7.8 Matriz de cobertura Sprint 2: requisito → componente → vista → prueba

| Requisito | HU | Componente Java | Vista Thymeleaf | Prueba |
|-----------|----|-----------------|-----------------|--------|
| **REQ-CAKE-013** | HU-08 | `PedidoController` + `Pedido.cumpleAnticipacion()` | `pedidos/nuevo.html` | `PedidoAbonoTest` |
| **REQ-CAKE-014** | HU-08 | `PedidoController` + `DetallePedido` | `pedidos/nuevo.html` | `PedidoAbonoTest` |
| **REQ-CAKE-015** | HU-09 | `Pedido.recalcularTotal()` + `DetallePedido.calcularSubtotal()` | `caja/validacion-abonos.html` | `PedidoAbonoTest` |
| **REQ-CAKE-016** | HU-10 | `CajaController` + `Pedido.cumpleAbonoMinimo()` | `caja/validacion-abonos.html` | `PedidoAbonoTest` |
| **REQ-CAKE-017** | HU-11 | `CocinaKdsController` + `PedidoRepository.findByEstadoIn...` | `cocina/kds.html` | `CocinaKdsInventarioTest` |
| **REQ-CAKE-018** | HU-12 | `CocinaKdsController` (`@Transactional`) + `Ingrediente.descontar()` | `cocina/kds.html` | `CocinaKdsInventarioTest` |
| **REQ-CAKE-019** | HU-12 | `CocinaKdsController` + `Ingrediente.tieneStockPara()` | `cocina/kds.html` | `CocinaKdsInventarioTest` |
| **REQ-CAKE-020** | HU-13 | `PedidoController` (`/mis-pedidos`) + `PedidoRepository.findByCliente...` | `pedidos/mis-pedidos.html` | `SeguimientoPedidoTest` |
| **REQ-CAKE-021** | HU-14 | `CajaController` + `Pedido.puedeEntregarse()` | `caja/validacion-abonos.html` | `SeguimientoPedidoTest` |

### 7.9 Estructura de archivos Sprint 2

```
src/main/java/com/caketrack/
├── model/
│   ├── EstadoPedido.java                    (T011)
│   ├── Pedido.java                          (T011)
│   └── DetallePedido.java                   (T011)
├── repository/
│   ├── PedidoRepository.java                (T011)
│   └── DetallePedidoRepository.java         (T011)
└── controller/
    ├── PedidoController.java                (T013)
    ├── CajaController.java                  (T013, T018)
    └── CocinaKdsController.java             (T015)

src/main/resources/templates/
├── fragments/layout.html                    (T016, modificado)
├── pedidos/
│   ├── nuevo.html                           (T013)
│   └── mis-pedidos.html                     (T013, T018)
├── caja/
│   └── validacion-abonos.html               (T013, T018)
└── cocina/
    └── kds.html                             (T015)

src/test/java/com/caketrack/
├── PedidoAbonoTest.java                     (T012)
├── CocinaKdsInventarioTest.java             (T014)
└── SeguimientoPedidoTest.java               (T017)
```

Todas las vistas nuevas siguen `/specs/design.md` y usan `/css/caketrack.css`. Ninguna clase se llama `*Service` ni se crea paquete `service`.

### 7.10 Supuestos y decisiones de diseño

- **Pedidos personalizados sin productos**: un pedido personalizado no tiene `DetallePedido` obligatorios. El total lo ingresa el CAJERO al validar, y `recalcularTotal()` solo aplica cuando hay detalles. Decidido: el CAJERO fija el total, que debe ser mayor que cero. Se valida antes de registrar el abono (AC-008.4, AC-008.5).
- **Cancelación con inventario ya descontado**: no se devuelve stock al cancelar un pedido en `EN_PREPARACION` o posterior. Decidido: la cancelación queda fuera del Sprint 2, porque ninguna historia de usuario la pide. El estado `CANCELADO` existe en el enum sin acción asociada.
- **Concurrencia de stock**: el descuento de BR-08 usa bloqueo pesimista (`findByIdForUpdate`) dentro de una transacción del controlador. Así se evitan descuentos dobles.
- **Código de pedido**: se genera como `"CT-" + año de fechaCreacion + "-" + String.format("%04d", id)` después de insertar el registro, dentro de la misma transacción. Así no hay colisiones aunque entren pedidos simultáneos.
- **Sin capa service**: la transacción vive en el método `@Transactional` de `CocinaKdsController`. La lógica de dominio vive en las entidades.

### 7.11 Dependencias entre tareas Sprint 2

```
T011 (entidades + repositorios)
  └── T012 [prueba] → T013 (pedidos + caja)
        └── T014 [prueba] → T015 (KDS + inventario)
              └── T017 [prueba] → T018 (seguimiento + liquidación)
                    └── T016 (navegación + suite completa Sprint 1 + Sprint 2)
```

---

**Fin del documento — PLAN-CAKETRACK-S2 v2.0 (Sprint 2)**
**Fin del documento — PLAN-CAKETRACK-S1 v1.0 (Sprint 1)**
