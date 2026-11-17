# Constitución del Proyecto CakeTrack — v1.0

Este documento establece los principios no negociables que rigen el desarrollo del sistema CakeTrack. Toda decisión técnica, arquitectónica o de implementación debe alinearse con estos principios.

## Principio 1: Especificación como Fuente de Verdad

Ningún código de producción se escribe sin contar previamente con:
- Requisitos funcionales redactados en sintaxis **EARS** (Easy Approach to Requirements Syntax).
- Criterios de aceptación binarios en formato **Given/When/Then**.
- Aprobación explícita del equipo antes de iniciar implementación.

## Principio 2: Arquitectura Cliente-Servidor Monolítica

- Todas las vistas se renderizan en el servidor con **Thymeleaf + Bootstrap 5**.
- Los controladores (`@Controller`) devuelven nombres de plantillas, no JSON.
- **Prohibido**: APIs REST independientes, microservicios, frontends separados, SPAs.

## Principio 3: Cero Capa Service

**Estrictamente prohibido** crear paquetes, carpetas o clases denominadas `service` o `*Service`.

Las validaciones, reglas de negocio y transacciones se resuelven directamente entre:
- Controladores (`@Controller`)
- Repositorios (`JpaRepository`)
- Métodos de dominio en entidades (`@Entity`)

## Principio 4: Pruebas Antes que Implementación

Toda regla de negocio o criterio de aceptación debe contar con su prueba automatizada **antes** de dar por terminada la codificación.

- Framework: **JUnit 5 + Spring Boot Test + MockMvc**.
- Cobertura mínima: criterios de aceptación binarios.

## Principio 5: Seguridad y Control de Roles por Defecto

- Todas las rutas protegidas mediante **Spring Security**.
- 5 roles del sistema: `CLIENTE`, `CAJERO`, `PASTELERO`, `REPARTIDOR`, `ADMIN`.
- Cumplimiento estricto de la **Ley 1581 de 2012** (protección de datos personales).

## Principio 6: Simplicidad y Cero Dependencias Innecesarias

No se agregan librerías o frameworks externos al `pom.xml` sin justificación explícita documentada en `/docs/adr/`.

## Principio 7: Trazabilidad y Revisión Humana

- Cada commit en Git referencia un identificador de tarea (`T###`).
- Todo cambio significativo se registra en `/ia/bitacora.md`.
- Revisiones de código obligatorias antes de merge a `main`.

---

**Versión**: 1.0  
**Última actualización**: 2026-11-16
