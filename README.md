# CakeTrack

Sistema de gestión para pastelería artesanal.

## Tecnología

- **Java 21**
- **Spring Boot 3.4.x**
- **Thymeleaf + Bootstrap 5**
- **Spring Data JPA + MySQL**
- **Spring Security**

## Estructura del proyecto

```
caketrackproyecto/
├── specs/          # Especificaciones (SDD)
├── docs/adr/       # Decisiones de arquitectura
├── ia/             # Bitácora de IA
└── src/            # Código fuente
    ├── main/java/com/caketrack/
    │   ├── model/
    │   ├── repository/
    │   ├── controller/
    │   └── config/
    └── main/resources/
        ├── application.properties
        ├── templates/
        └── static/
```

## Requisitos

- Java 21+
- MySQL 8.x
- Maven 3.9+

## Compilación

```bash
mvn test-compile
```
