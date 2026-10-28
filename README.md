# Epicentro Gourmet — Hito 2 de Orientación a Objetos II

Aplicación del Grupo 5 para administrar festivales gastronómicos, unidades de venta, personal, menús, pedidos y reportes. Usa Java 21, Spring Boot, Spring Security, Thymeleaf, JPA/Hibernate y MySQL 8.

## Datos del grupo

**Grupo N.º 5** · Licenciatura en Sistemas · Orientación a Objetos II · UNLa · 2026

| Integrante | Legajo | DNI | Usuario de GitHub |
|---|---|---|---|
| Ivan Kersevan | UNLA-73970 | 41854724 | [Ivankersevan](https://github.com/Ivankersevan) |
| Candela Fernández | UNLA-71844 | 43405646 | [candef582](https://github.com/candef582) |
| Nicolas Ariel Basalo | UNLA-74520 | 46358143 | [nicobasa](https://github.com/nicobasa) |
| Lucas Emanuel Radino Nieva | UNLA-75327 | 44935519 | [LucasRadino](https://github.com/LucasRadino) |

- Repositorio de entrega: **[epicentro-gourmet-oo2-tp-grupo-5](https://github.com/nicobasa/epicentro-gourmet-oo2-tp-grupo-5)**
- Rama de entrega: `main`
- Diagrama de clases: [docs/diagrama-clases.md](docs/diagrama-clases.md)

## Casos de uso implementados y responsables

Cada integrante es responsable de dos casos de uso y de dos validaciones de carga.

| Caso de uso | Responsable | Validaciones de carga a cargo | Pull Request |
|---|---|---|---|
| **CU01** — Registrar una unidad de venta con su Responsable | Ivan Kersevan | **VAL01** DNI del Responsable · **VAL02** código único de unidad | [PR #3](https://github.com/nicobasa/epicentro-gourmet-oo2-tp-grupo-5/pull/3) |
| **CU02** — Consultar el ranking de recaudación de un festival | Ivan Kersevan | | [PR #6](https://github.com/nicobasa/epicentro-gourmet-oo2-tp-grupo-5/pull/6) |
| **CU03** — Incorporar un empleado a una unidad de venta | Nicolas Ariel Basalo | **VAL03** mayoría de edad · **VAL04** correo válido y no repetido | [PR #1](https://github.com/nicobasa/epicentro-gourmet-oo2-tp-grupo-5/pull/1) |
| **CU04** — Reactivar un empleado dado de baja lógicamente | Nicolas Ariel Basalo | | [PR #2](https://github.com/nicobasa/epicentro-gourmet-oo2-tp-grupo-5/pull/2) |
| **CU05** — Modificar un plato del menú de una unidad | Candela Fernández | **VAL05** precio y costo del plato · **VAL06** cantidades del pedido | [PR #5](https://github.com/nicobasa/epicentro-gourmet-oo2-tp-grupo-5/pull/5) |
| **CU06** — Registrar un pedido de venta durante un festival | Candela Fernández | | [PR #8](https://github.com/nicobasa/epicentro-gourmet-oo2-tp-grupo-5/pull/8) |
| **CU07** — Realizar el cierre diario de caja | Lucas Emanuel Radino Nieva | **VAL07** un solo cierre por jornada | [PR #9](https://github.com/nicobasa/epicentro-gourmet-oo2-tp-grupo-5/pull/9) |
| **CU08** — Consultar la rentabilidad neta de una unidad entre dos fechas | Lucas Emanuel Radino Nieva | **VAL08** período de rentabilidad | [PR #10](https://github.com/nicobasa/epicentro-gourmet-oo2-tp-grupo-5/pull/10) |

### Requisitos del grupo

| Requisito | Implementación |
|---|---|
| Envío de e-mail | `EmailService`: al dar de alta un empleado se envía su usuario y una contraseña temporal. |
| DTO de reporte | `ReporteMayoresCanonDTO`, `ReporteVentaDTO`, `ResumenUnidadDTO` y `PlatoDTO` (no persisten). |
| Generación de PDF | `ReciboSueldoService` genera con PDFBox el recibo de sueldo descargable desde `/recibos/{festivalId}.pdf`. |

## Cómo correr la aplicación

Requisitos: JDK 21 y MySQL 8 activo.

1. **Configurar MySQL:** en `src/main/resources/application.properties` poner el usuario y la contraseña de MySQL (`spring.datasource.username` y `spring.datasource.password`). *Opcional:* también se pueden definir como variables de entorno `SPRING_DATASOURCE_USERNAME` y `SPRING_DATASOURCE_PASSWORD`.
2. **Crear la base de datos** en MySQL Workbench:
   ```sql
   CREATE DATABASE epicentro_gourmet_spring CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
3. **Cargar los datos de prueba** (festivales, unidades, empleados, platos y pedidos):
   ```powershell
   .\mvnw.cmd "-Dtest=TestCargarBD" test
   ```
4. **Iniciar el servidor:**
   ```powershell
   .\mvnw.cmd spring-boot:run
   ```
   y abrir <http://localhost:8080>.

Las tablas se crean solas al arrancar (`ddl-auto=update`). Para ejecutar las pruebas automáticas (usan H2 en memoria, no tocan MySQL): `.\mvnw.cmd test`.

El script `sql/datos-demostracion.sql` contiene los mismos datos de prueba como `INSERT`s, como alternativa al paso 3 (cuentas `*.demo`, contraseña `DemoHito2!`).

## Cuentas de prueba

| Perfil | Usuario | Contraseña |
|---|---|---|
| Administrador | `admin` | `admin123` |
| Responsable (Hamburguesas del Sur) | `responsable@epicentro.com` | `resp123` |
| Cajero | `cajero@epicentro.com` | `emp123` |
| Cocinero | `cocinero@epicentro.com` | `emp123` |
