# Bitácora de IA — CakeTrack

Registro de interacciones, decisiones y contextos relevantes compartidos con asistentes de IA.

## Formato de entrada

```
### YYYY-MM-DD — Tema
- **Contexto**: …
- **Decisión**: …
- **Resultado**: …
```

---

### 2026-11-17 — T001 y T002: configuración base y modelo de datos
- **Contexto**: Implementación de las tareas T001 y T002 de `specs/tasks.md` (Sprint 1).
- **Decisión**: Entidades JPA con métodos de dominio (sin capa service). `PasswordEncoder` declarado temporalmente en `config/PasswordEncoderConfig.java`, a migrar a `SecurityConfig` en T004. `TipoProducto` añadido como enum adicional (requerido por `productos.tipo`). Seed de datos en `DataInitializer` protegido por verificación de tabla vacía (idempotente).
- **Resultado**: `./mvnw test-compile` exitoso con Java 21. Arranque con MySQL pendiente de verificación (servidor MySQL no disponible en el equipo durante la implementación).
