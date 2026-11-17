# Especificación Funcional CakeTrack — Sprint 1

## Metadatos

| Campo | Valor |
|-------|-------|
| **ID** | SPEC-CAKETRACK-001 |
| **Versión** | 1.0 |
| **Estado** | En revisión - Puerta 1 |
| **Autor** | Jhoan Santiago García Estupiñán |
| **Fecha** | 2026-11-16 |
| **Alcance** | Sprint 1 (HU-01 a HU-07) |

---

## 1. Contexto

CakeTrack es un sistema de gestión para pastelería artesanal que permite administrar usuarios, roles, ingredientes, recetas y catálogo de productos. El objetivo del Sprint 1 es establecer la base funcional del sistema: autenticación, control de acceso, gestión de usuarios, ingredientes y catálogo de productos.

### 1.1 Problema

La pastelería opera sin un sistema centralizado para gestionar:
- Acceso de usuarios con roles diferenciados (CLIENTE, CAJERO, PASTELERO, REPARTIDOR, ADMIN).
- Control de inventario de ingredientes con alertas de stock mínimo.
- Catálogo de productos (tortas y postres) con recetas y disponibilidad.

### 1.2 Objetivo (Product Goal)

Implementar un sistema web monolítico cliente-servidor que permita:
1. Autenticar usuarios con correo y contraseña.
2. Gestionar usuarios y asignar roles.
3. Controlar inventario de ingredientes con alertas visuales.
4. Administrar catálogo de productos con recetas asociadas.
5. Filtrar acceso a funcionalidades según el rol del usuario autenticado.

### 1.3 No-Objetivos del Sprint 1

Quedan **explícitamente excluidos** de esta versión:
- Encargos personalizados de tortas o postres.
- Panel de visualización en cocina (KDS - Kitchen Display System).
- Gestión de domicilios o entregas.
- Integración con pasarelas de pago.
- Notificaciones por correo electrónico o SMS.

---

## 2. Actores / Roles del Sistema

| Rol | Descripción |
|-----|-------------|
| **CLIENTE** | Usuario final que consulta el catálogo público de productos. |
| **CAJERO** | Procesa pedidos y gestiona información básica de clientes. |
| **PASTELERO** | Gestiona ingredientes, recetas y marca productos como disponibles. |
| **REPARTIDOR** | Consulta pedidos asignados para entrega (sin funcionalidad activa en Sprint 1). |
| **ADMIN** | Gestiona usuarios, asigna roles y controla activación/desactivación de cuentas. |

---

## 3. Reglas de Negocio

| ID | Regla |
|----|-------|
| **BR-01** | Toda contraseña debe almacenarse cifrada con BCrypt y tener mínimo 8 caracteres. |
| **BR-02** | Un usuario inactivo (`activo = false`) no puede iniciar sesión aunque sus credenciales sean válidas. |
| **BR-03** | El enlace de recuperación de contraseña es de un solo uso y expira exactamente a los 30 minutos. |
| **BR-04** | Todo ingrediente cuya cantidad actual sea menor o igual a su stock mínimo debe marcarse visualmente en estado de alerta de reposición. |
| **BR-05** | Un producto del catálogo solo puede marcarse como disponible si tiene al menos un ingrediente asociado en su receta. |

---

## 4. Requisitos Funcionales (Sintaxis EARS)

### 4.1 Autenticación y Sesión

| ID | Requisito | Tipo EARS |
|----|-----------|-----------|
| **REQ-CAKE-001** | El sistema debe autenticar al usuario mediante correo electrónico y contraseña. | Ubiquitous |
| **REQ-CAKE-002** | Cuando el usuario envía credenciales válidas, el sistema debe crear una sesión activa y redirigir al usuario según su rol. | Event-driven |
| **REQ-CAKE-003** | El sistema debe cerrar la sesión del usuario cuando este solicita el cierre de sesión. | Ubiquitous |

### 4.2 Recuperación de Contraseña

| ID | Requisito | Tipo EARS |
|----|-----------|-----------|
| **REQ-CAKE-004** | Cuando el usuario solicita recuperación de contraseña, el sistema debe generar un token de un solo uso con vigencia de 30 minutos. | Event-driven |
| **REQ-CAKE-005** | Si el token de recuperación ha expirado o ya fue utilizado, el sistema debe rechazar el cambio de contraseña y mostrar mensaje de error. | State-driven |

### 4.3 Gestión de Usuarios

| ID | Requisito | Tipo EARS |
|----|-----------|-----------|
| **REQ-CAKE-006** | El sistema debe permitir al ADMIN crear usuarios y asignarles uno de los 5 roles definidos (CLIENTE, CAJERO, PASTELERO, REPARTIDOR, ADMIN). | Ubiquitous |
| **REQ-CAKE-007** | El sistema debe permitir al ADMIN activar o desactivar cuentas de usuario. | Ubiquitous |

### 4.4 Control de Acceso por Rol

| ID | Requisito | Tipo EARS |
|----|-----------|-----------|
| **REQ-CAKE-008** | El sistema debe filtrar el menú lateral y superior mostrando únicamente las opciones permitidas para el rol del usuario autenticado. | Ubiquitous |
| **REQ-CAKE-009** | Si un usuario intenta acceder por URL a una ruta no autorizada para su rol, el sistema debe responder con código HTTP 403 (Forbidden). | State-driven |

### 4.5 Gestión de Ingredientes y Catálogo

| ID | Requisito | Tipo EARS |
|----|-----------|-----------|
| **REQ-CAKE-010** | El sistema debe permitir registrar y actualizar ingredientes con nombre, unidad de medida, cantidad actual y stock mínimo. | Ubiquitous |
| **REQ-CAKE-011** | El sistema debe permitir crear productos del catálogo (tortas y postres) asociando ingredientes y cantidades de su receta. | Ubiquitous |
| **REQ-CAKE-012** | El sistema debe permitir consultar el catálogo de productos mostrando nombre, precio y estado de disponibilidad. | Ubiquitous |

---

## 5. Requisitos No Funcionales

| ID | Requisito | Métrica |
|----|-----------|---------|
| **NFR-CAKE-001** | **Seguridad**: Todas las contraseñas deben almacenarse cifradas con BCrypt. Ninguna contraseña en texto plano debe persistir en la base de datos. | Cumplimiento binario |
| **NFR-CAKE-002** | **Rendimiento**: El tiempo de respuesta para operaciones de consulta (catálogo, ingredientes, usuarios) debe ser inferior a 500 milisegundos en condiciones normales de carga (hasta 50 usuarios concurrentes). | p95 < 500 ms |
| **NFR-CAKE-003** | **Protección de Datos**: El sistema debe cumplir con la Ley 1581 de 2012 (Protección de Datos Personales de Colombia). Los datos personales de los usuarios no deben exponerse en URLs, logs ni respuestas visibles a otros usuarios. | Cumplimiento legal |

---

## 6. Historias de Usuario y Criterios de Aceptación

### HU-01: Inicio y Cierre de Sesión

**Como** usuario autenticado, **quiero** iniciar y cerrar sesión en el sistema **para** acceder a las funcionalidades permitidas según mi rol.

#### Criterios de Aceptación

**AC-001.1** — Camino feliz: inicio de sesión exitoso
```gherkin
Given un usuario registrado con correo "usuario@caketrack.com" y contraseña "Password123" en estado activo
When el usuario ingresa el correo y contraseña correctos en el formulario de inicio de sesión
Then el sistema debe autenticar al usuario, crear una sesión activa y redirigir al usuario a la página principal según su rol
```

**AC-001.2** — Caso de error: credenciales incorrectas
```gherkin
Given un usuario que ingresa correo "usuario@caketrack.com" y contraseña "PasswordIncorrecto"
When el usuario envía el formulario de inicio de sesión
Then el sistema debe mostrar el mensaje "Credenciales inválidas" y mantener al usuario en la página de inicio de sesión
```

**AC-001.3** — Caso de error: usuario inactivo
```gherkin
Given un usuario registrado con correo "inactivo@caketrack.com" en estado inactivo (activo = false)
When el usuario ingresa credenciales válidas en el formulario de inicio de sesión
Then el sistema debe mostrar el mensaje "Su cuenta está inactiva. Contacte al administrador" y no debe crear una sesión
```

**AC-001.4** — Camino feliz: cierre de sesión
```gherkin
Given un usuario autenticado con sesión activa
When el usuario hace clic en el botón "Cerrar sesión"
Then el sistema debe destruir la sesión activa y redirigir al usuario a la página de inicio de sesión
```

---

### HU-02: Recuperación de Contraseña

**Como** usuario que olvidó su contraseña, **quiero** solicitar un enlace de recuperación **para** restablecer mi contraseña de forma segura.

#### Criterios de Aceptación

**AC-002.1** — Camino feliz: solicitud de recuperación
```gherkin
Given un usuario registrado con correo "usuario@caketrack.com" en estado activo
When el usuario ingresa su correo en el formulario de recuperación de contraseña
Then el sistema debe generar un token de un solo uso con vigencia de 30 minutos y mostrar el mensaje "Si el correo existe, recibirás instrucciones de recuperación" (respuesta genérica para no filtrar cuentas existentes)
```

**AC-002.2** — Caso de error: correo no registrado
```gherkin
Given un usuario que ingresa correo "noregistrado@caketrack.com" (no existe en el sistema)
When el usuario envía el formulario de recuperación de contraseña
Then el sistema debe mostrar el mensaje "Si el correo existe, recibirás instrucciones de recuperación" (misma respuesta genérica que para correos válidos)
```

**AC-002.3** — Caso de error: token expirado
```gherkin
Given un usuario que recibió un token de recuperación hace 31 minutos
When el usuario hace clic en el enlace de recuperación con el token expirado
Then el sistema debe mostrar el mensaje "El enlace de recuperación ha expirado o ya fue utilizado. Solicite uno nuevo" y no debe permitir el cambio de contraseña
```

**AC-002.4** — Caso de error: token ya utilizado
```gherkin
Given un usuario que ya utilizó su token de recuperación para cambiar la contraseña
When el usuario intenta utilizar nuevamente el mismo enlace de recuperación
Then el sistema debe mostrar el mensaje "El enlace de recuperación ha expirado o ya fue utilizado. Solicite uno nuevo" y no debe permitir el cambio de contraseña
```

---

### HU-03: Gestión de Usuarios

**Como** ADMIN, **quiero** crear usuarios, asignarles roles y activar/desactivar cuentas **para** controlar el acceso al sistema.

#### Criterios de Aceptación

**AC-003.1** — Camino feliz: creación de usuario con rol válido
```gherkin
Given un usuario ADMIN autenticado
When el ADMIN completa el formulario de creación de usuario con nombre "María López", correo "maria@caketrack.com", contraseña "Password123" y rol "CAJERO"
Then el sistema debe crear el usuario con estado activo por defecto, cifrar la contraseña con BCrypt y mostrar el mensaje "Usuario creado exitosamente"
```

**AC-003.2** — Caso de error: contraseña con menos de 8 caracteres
```gherkin
Given un usuario ADMIN autenticado
When el ADMIN intenta crear un usuario con contraseña "Pass1" (5 caracteres)
Then el sistema debe mostrar el mensaje "La contraseña debe tener mínimo 8 caracteres" y no debe crear el usuario
```

**AC-003.3** — Camino feliz: desactivar usuario
```gherkin
Given un usuario ADMIN autenticado y un usuario "CAJERO" con correo "cajero@caketrack.com" en estado activo
When el ADMIN cambia el estado del usuario a inactivo (activo = false)
Then el sistema debe actualizar el estado del usuario a inactivo y mostrar el mensaje "Usuario desactivado exitosamente"
```

**AC-003.4** — Camino feliz: activar usuario
```gherkin
Given un usuario ADMIN autenticado y un usuario "CAJERO" con correo "cajero@caketrack.com" en estado inactivo
When el ADMIN cambia el estado del usuario a activo (activo = true)
Then el sistema debe actualizar el estado del usuario a activo y mostrar el mensaje "Usuario activado exitosamente"
```

---

### HU-04: Control de Acceso por Rol

**Como** usuario autenticado, **quiero** ver únicamente las opciones de menú y acceder a las funcionalidades permitidas para mi rol **para** mantener la seguridad del sistema.

#### Criterios de Aceptación

**AC-004.1** — Camino feliz: menú filtrado para CLIENTE
```gherkin
Given un usuario autenticado con rol "CLIENTE"
When el usuario inicia sesión y accede a la página principal
Then el sistema debe mostrar en el menú lateral y superior únicamente las opciones "Inicio" y "Catálogo de productos"
```

**AC-004.2** — Camino feliz: menú filtrado para ADMIN
```gherkin
Given un usuario autenticado con rol "ADMIN"
When el usuario inicia sesión y accede a la página principal
Then el sistema debe mostrar en el menú lateral y superior las opciones "Inicio", "Usuarios", "Ingredientes", "Productos" y "Catálogo"
```

**AC-004.3** — Caso de error: acceso no autorizado por URL
```gherkin
Given un usuario autenticado con rol "CLIENTE"
When el usuario intenta acceder manualmente a la URL "/admin/usuarios" (ruta exclusiva para ADMIN)
Then el sistema debe responder con código HTTP 403 (Forbidden) y mostrar el mensaje "No tiene permisos para acceder a esta página"
```

---

### HU-05: Gestión de Ingredientes

**Como** PASTELERO o ADMIN, **quiero** registrar y actualizar ingredientes con unidad de medida y stock mínimo **para** controlar el inventario de la pastelería.

#### Criterios de Aceptación

**AC-005.1** — Camino feliz: registro de ingrediente
```gherkin
Given un usuario autenticado con rol "PASTELERO"
When el usuario completa el formulario de registro de ingrediente con nombre "Harina de trigo", unidad de medida "kg", cantidad actual "50" y stock mínimo "10"
Then el sistema debe crear el ingrediente en estado normal (cantidad actual > stock mínimo) y mostrar el mensaje "Ingrediente registrado exitosamente"
```

**AC-005.2** — Camino feliz: alerta de stock mínimo
```gherkin
Given un ingrediente "Harina de trigo" con cantidad actual "10" y stock mínimo "10" (cantidad actual <= stock mínimo)
When el usuario con rol "PASTELERO" consulta la lista de ingredientes
Then el sistema debe mostrar el ingrediente con indicador visual de alerta de reposición (color rojo o ícono de advertencia)
```

**AC-005.3** — Camino feliz: actualización de cantidad actual
```gherkin
Given un ingrediente "Harina de trigo" con cantidad actual "5" y stock mínimo "10" (en estado de alerta)
When el usuario con rol "PASTELERO" actualiza la cantidad actual a "20"
Then el sistema debe actualizar la cantidad actual y remover el indicador visual de alerta de reposición
```

---

### HU-06: Gestión de Catálogo de Productos

**Como** PASTELERO o ADMIN, **quiero** crear tortas y postres asociando ingredientes de su receta **para** administrar el catálogo de productos.

#### Criterios de Aceptación

**AC-006.1** — Camino feliz: creación de producto con ingredientes
```gherkin
Given un usuario autenticado con rol "PASTELERO"
When el usuario completa el formulario de creación de producto con nombre "Torta de Chocolate", tipo "TORTA", precio "45000" y asocia los ingredientes "Harina de trigo: 1 kg", "Azúcar: 0.5 kg", "Chocolate: 0.3 kg"
Then el sistema debe crear el producto con estado de disponibilidad "No disponible" (requiere marca manual) y mostrar el mensaje "Producto creado exitosamente"
```

**AC-006.2** — Caso de error: producto sin ingredientes
```gherkin
Given un usuario autenticado con rol "PASTELERO"
When el usuario intenta crear un producto sin asociar ningún ingrediente a la receta
Then el sistema debe mostrar el mensaje "El producto debe tener al menos un ingrediente asociado" y no debe crear el producto
```

**AC-006.3** — Camino feliz: marcar producto como disponible
```gherkin
Given un producto "Torta de Chocolate" con al menos un ingrediente asociado en su receta
When el usuario con rol "PASTELERO" cambia el estado de disponibilidad a "Disponible"
Then el sistema debe actualizar el estado del producto a "Disponible" y mostrarlo como disponible en el catálogo
```

---

### HU-07: Consulta de Catálogo de Productos

**Como** CLIENTE, CAJERO, PASTELERO o ADMIN, **quiero** consultar el catálogo de tortas y postres con precios y estado de disponibilidad **para** conocer los productos ofrecidos por la pastelería.

#### Criterios de Aceptación

**AC-007.1** — Camino feliz: consulta de catálogo
```gherkin
Given un usuario autenticado con rol "CLIENTE"
When el usuario accede a la página de catálogo de productos
Then el sistema debe mostrar la lista de productos (tortas y postres) con nombre, tipo, precio y estado de disponibilidad ("Disponible" o "No disponible")
```

**AC-007.2** — Camino feliz: filtro por tipo de producto
```gherkin
Given un usuario autenticado con rol "CLIENTE" en la página de catálogo de productos
When el usuario selecciona el filtro "Tipo: TORTA"
Then el sistema debe mostrar únicamente los productos de tipo "TORTA" con su nombre, precio y estado de disponibilidad
```

**AC-007.3** — Caso de error: catálogo vacío
```gherkin
Given un usuario autenticado con rol "CLIENTE" y ningún producto registrado en el catálogo
When el usuario accede a la página de catálogo de productos
Then el sistema debe mostrar el mensaje "No hay productos disponibles en este momento"
```

---

## 7. Resumen de Cobertura

| Historia de Usuario | Criterios de Aceptación |
|---------------------|-------------------------|
| HU-01: Inicio y Cierre de Sesión | AC-001.1, AC-001.2, AC-001.3, AC-001.4 |
| HU-02: Recuperación de Contraseña | AC-002.1, AC-002.2, AC-002.3, AC-002.4 |
| HU-03: Gestión de Usuarios | AC-003.1, AC-003.2, AC-003.3, AC-003.4 |
| HU-04: Control de Acceso por Rol | AC-004.1, AC-004.2, AC-004.3 |
| HU-05: Gestión de Ingredientes | AC-005.1, AC-005.2, AC-005.3 |
| HU-06: Gestión de Catálogo | AC-006.1, AC-006.2, AC-006.3 |
| HU-07: Consulta de Catálogo | AC-007.1, AC-007.2, AC-007.3 |
| **Total** | **24 criterios de aceptación** |

---

## 8. Trazabilidad de Reglas de Negocio

| Regla de Negocio | Requisitos Asociados | Criterios de Aceptación |
|------------------|---------------------|-------------------------|
| BR-01: Contraseñas BCrypt mínimo 8 caracteres | REQ-CAKE-006, NFR-CAKE-001 | AC-003.1, AC-003.2 |
| BR-02: Usuario inactivo no puede iniciar sesión | REQ-CAKE-002 | AC-001.3 |
| BR-03: Token recuperación un solo uso, 30 minutos | REQ-CAKE-004, REQ-CAKE-005 | AC-002.1, AC-002.3, AC-002.4 |
| BR-04: Alerta visual stock mínimo | REQ-CAKE-010 | AC-005.2, AC-005.3 |
| BR-05: Producto disponible requiere ingredientes | REQ-CAKE-011 | AC-006.1, AC-006.2, AC-006.3 |

---

## 9. Glosario

| Término | Definición |
|---------|------------|
| **BCrypt** | Algoritmo de cifrado de contraseñas basado en Blowfish. |
| **Token** | Cadena de caracteres única generada para validación temporal. |
| **Stock mínimo** | Cantidad mínima de un ingrediente antes de activar alerta de reposición. |
| **Receta** | Conjunto de ingredientes y cantidades necesarias para producir un producto. |
| **Sesión activa** | Estado de autenticación del usuario mientras mantiene conexión con el sistema. |

---

**Fin del documento — SPEC-CAKETRACK-001 v1.0**
