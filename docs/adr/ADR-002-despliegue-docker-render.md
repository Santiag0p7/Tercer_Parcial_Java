# ADR-002: Despliegue con Docker multi-etapa en Render y base de datos en Clever Cloud

| Campo | Valor |
|-------|-------|
| **ID** | ADR-002 |
| **Estado** | Aceptada |
| **Fecha** | 2026-11-17 |
| **Decisores** | Jhoan Santiago García Estupiñán |
| **Referencia** | Constitution.md Principios 5 y 6; ADR-001 |

---

## Contexto

CakeTrack necesita un despliegue continuo. La aplicación se ejecuta en Render (plan gratuito, 512 MB de RAM) y la base de datos MySQL está en Clever Cloud, que limita las conexiones simultáneas por usuario. Las credenciales no pueden quedar en el repositorio (Principio 5).

## Decisión

1. **Docker multi-etapa**:
   - Etapa `build` con `maven:3.9.9-eclipse-temurin-21` para compilar el JAR ejecutable.
   - Etapa `runtime` con `eclipse-temurin:21-jre-alpine`, solo con el JRE y el JAR.
   - La JVM usa `-XX:MaxRAMPercentage=75.0` para respetar el límite de memoria del contenedor.
2. **Configuración por variables de entorno**:
   - `PORT` lo asigna Render; `server.port=${PORT:8080}`.
   - `DB_URL`, `DB_USER` y `DB_PASS` contienen la conexión a Clever Cloud. En `application.properties` solo existen valores por defecto para desarrollo local (`localhost`, `root`, contraseña vacía).
   - Ningún host, usuario ni contraseña real se escribe en archivos versionados.
3. **Pool de conexiones acotado**: HikariCP con `maximum-pool-size=5` y `minimum-idle=1`.
4. **`.dockerignore`** excluye `target/`, `.git/`, IDE, documentación y `.env`, para reducir el contexto de construcción.

## Alternativas descartadas

| Alternativa | Motivo de descarte |
|-------------|--------------------|
| Buildpacks automáticos de Render sin Dockerfile | Menos control sobre la imagen base y la opción de memoria de la JVM |
| Imagen con Maven y JDK completos en runtime | Imagen más pesada sin beneficio en ejecución |
| Credenciales en `application-prod.properties` versionado | Viola el Principio 5 |

## Consecuencias

- **Positivas**: imagen de runtime pequeña; el mismo artefacto corre igual en local y en Render; credenciales fuera del repositorio.
- **Negativas**:
  - El desarrollo local necesita definir `DB_URL`, `DB_USER` y `DB_PASS` (antes era `DB_PASSWORD`).
  - `DB_URL` debe incluir `sslMode=REQUIRED` para Clever Cloud.
  - Con `maximum-pool-size=5` y varios contextos de prueba abiertos, puede alcanzarse el límite de 5 conexiones de Clever Cloud; en ese caso hay que bajar el pool en las pruebas.

## Cumplimiento

| Principio | Cumplimiento |
|-----------|--------------|
| Principio 5 (Seguridad) | Credenciales solo en variables de entorno |
| Principio 6 (Simplicidad) | Sin dependencias nuevas; un solo Dockerfile |
