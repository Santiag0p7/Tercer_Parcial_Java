# ADR-001: Arquitectura Monolítica Cliente-Servidor sin Capa Service

| Campo | Valor |
|-------|-------|
| **ID** | ADR-001 |
| **Estado** | Aceptada |
| **Fecha** | 2026-11-17 |
| **Decisores** | Jhoan Santiago García Estupiñán |
| **Referencia** | Constitution.md Principios 2 y 3; spec.md Sprint 1 |

---

## Contexto

CakeTrack es un sistema de gestión para una pastelería artesanal con un equipo de desarrollo pequeño (1-3 desarrolladores), un dominio de negocio acotado (usuarios, roles, ingredientes, recetas, catálogo) y un conjunto fijo de 5 roles con flujos de trabajo internos. El Sprint 1 debe establecer la base funcional con autenticación, control de acceso, gestión de usuarios, inventario de ingredientes y catálogo de productos.

Se requiere decidir la arquitectura general del sistema antes de comenzar la codificación.

## Decisión

Se adopta una **arquitectura cliente-servidor monolítica** basada en Spring MVC + Thymeleaf con renderizado de vistas en el servidor, acoplando directamente los siguientes componentes:

```
┌───────────────────────────────────────────────────────────────┐
│  @Controller  ──→  @Entity (lógica de dominio)               │
│       │                    │                                  │
│       │                    ↓                                  │
│       └──────────→  JpaRepository                            │
│                            │                                  │
│                            ↓                                  │
│                       MySQL DB                                │
└───────────────────────────────────────────────────────────────┘
```

- **Vistas**: Thymeleaf + Bootstrap 5 renderizadas en el servidor.
- **Controladores**: `@Controller` (nunca `@RestController` como patrón principal).
- **Lógica de negocio**: Métodos de dominio en `@Entity` + lógica directa en `@Controller`.
- **Acceso a datos**: `JpaRepository` invocado directamente desde controladores y entidades.
- **Sin capa intermedia**: No existe paquete, carpeta ni clase con nombre `service` o `*Service`.

## Alternativas Descartadas

### Alternativa A: Arquitectura de Microservicios

| Criterio | Evaluación |
|----------|------------|
| Complejidad operacional | Alta: requiere orquestador, API Gateway, registro de servicios, configuración distribuida |
| Necesidad real | El dominio es acotado y no requiere escalado independiente de componentes |
| Costo de desarrollo | 3-5x mayor para un equipo pequeño |
| **Veredicto** | **Descartada**: sobre-ingeniería para el contexto actual |

### Alternativa B: API REST + Frontend Separado (SPA)

| Criterio | Evaluación |
|----------|------------|
| Complejidad | Requiere dos aplicaciones desplegables, CORS, autenticación por tokens JWT, manejo de estado en cliente |
| SEO / Rendimiento inicial | Desventajas respecto al renderizado en servidor |
| Equipo necesario | Exige competencias separadas en frontend y backend |
| **Veredicto** | **Descartada**: Thymeleaf server-side cubre las necesidades del negocio con menor fricción |

### Alternativa C: Arquitectura en Capas con Service Layer (Controller → Service → Repository)

| Criterio | Evaluación |
|----------|------------|
| Complejidad | Introduce una capa intermedia que para el dominio actual solo contiene delegación trivial |
| Anemia de dominio | Fomenta entidades anémicas y lógica dispersa entre capas |
| Mantenibilidad | Más archivos, más indirección, más puntos de falla |
| **Veredicto** | **Descartada**: la lógica de negocio se resuelve entre `@Controller`, `@Entity` y `JpaRepository` |

## Consecuencias

### Positivas
- **Menor cantidad de código**: sin interfaces service, sin DTOs de capa intermedia innecesarios.
- **Lógica de dominio rica**: las entidades `@Entity` encapsulan validaciones y reglas de negocio (BR-01 a BR-05).
- **Simplicidad operativa**: un único artefacto desplegable (JAR de Spring Boot).
- **Trazabilidad directa**: cada requisito funcional se mapea a un `@Controller` y una plantilla Thymeleaf sin capas ocultas.
- **Velocidad de desarrollo**: adecuado para el tamaño del equipo y alcance del Sprint 1.

### Negativas (aceptadas)
- Si el dominio crece significativamente, podría requerir reestructuración posterior (no previsto para CakeTrack).
- Los controladores asumen más responsabilidad de la habitual en arquitectura en capas (compensado con métodos de dominio en entidades).
- No es posible escalar componentes de forma independiente (no es necesario para el volumen esperado).

### Riesgos Mitigados
| Riesgo | Mitigación |
|--------|------------|
| Controladores demasiado grandes | Métodos de dominio en `@Entity` extraen lógica de negocio |
| Duplicación de lógica | Repositorios compartidos + métodos estáticos de utilidades en entidades |
| Falta de testabilidad | Los controladores son testables con `MockMvc`; la lógica de dominio se prueba directamente en entidades |

## Cumplimiento con la Constitución

| Principio | Cumplimiento |
|-----------|--------------|
| Principio 2 (Cliente-Servidor Monolítico) | ✅ Spring MVC + Thymeleaf, sin APIs REST ni frontends separados |
| Principio 3 (Cero Capa Service) | ✅ No existen paquetes ni clases `*Service` |
| Principio 6 (Simplicidad) | ✅ Menor cantidad de componentes, sin dependencias innecesarias |

---

**Esta decisión es vinculante para todo el proyecto CakeTrack.**
