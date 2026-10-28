# Modelo de dominio

```mermaid
classDiagram
    class Festival {
      +Long id
      +String nombre
      +String temporada
      +LocalDate fechaInicio
      +LocalDate fechaFin
    }
    class Costo {
      +BigDecimal costoPorSuperficie
      +BigDecimal costoPorMontaje
      +BigDecimal plusElectricidad
      +BigDecimal sueldoBase
      +BigDecimal montoAnioAntiguedad
      +BigDecimal plusCocinero
      +BigDecimal plusCocineroAyudante
      +BigDecimal plusLavaplatos
    }
    class UnidadVenta {
      +Long id
      +String nombreComercial
      +String codigo
      +Double superficie
    }
    class FoodTruck {
      +String patente
      +boolean requiereElectricidad
    }
    class PuestoDesarmable {
      +Integer cantidadCarpas
      +Integer tiempoMontaje
    }
    class Empleado {
      +Long id
      +Long dni
      +LocalDate fechaIngreso
      +boolean activo
    }
    class Cajero
    class Cocinero {
      +CategoriaCocina categoria
    }
    class Usuario {
      +String username
      +String password
    }
    class Rol {
      +String nombre
    }
    class AsignacionLaboral {
      +LocalDate fechaInicio
      +LocalDate fechaFin
      +BigDecimal sueldoLiquidado
    }
    class CanonUnidadFestival {
      +BigDecimal importe
    }
    class Plato {
      +String nombre
      +Double precioVenta
      +Double costoProduccion
    }
    class Pedido {
      +LocalDateTime fechaTransaccion
    }
    class DetallePedido {
      +Integer cantidad
      +Double precioUnitario
      +Double costoUnitario
    }
    class CierreCaja {
      +LocalDate fecha
      +LocalDateTime cerradoEn
      +BigDecimal total
    }

    Festival "1" *-- "1" Costo : parámetros
    Festival "many" -- "many" UnidadVenta : participa
    Festival "1" -- "many" CanonUnidadFestival : conserva
    UnidadVenta "1" -- "many" CanonUnidadFestival : canon acordado
    UnidadVenta <|-- FoodTruck
    UnidadVenta <|-- PuestoDesarmable
    UnidadVenta "1" -- "many" Empleado : emplea
    Empleado <|-- Cajero
    Empleado <|-- Cocinero
    Empleado "1" -- "1" Usuario : acceso
    Usuario "many" --> "1" Rol
    Empleado "1" -- "many" AsignacionLaboral
    Festival "1" -- "many" AsignacionLaboral
    UnidadVenta "1" -- "many" AsignacionLaboral
    UnidadVenta "many" -- "many" Plato : ofrece
    Festival "1" -- "many" Pedido
    UnidadVenta "1" -- "many" Pedido
    Pedido "1" *-- "many" DetallePedido
    Plato "1" -- "many" DetallePedido
    UnidadVenta "1" -- "many" CierreCaja
    Cajero "1" -- "many" CierreCaja
```

`AsignacionLaboral` conserva el historial de participación de cada empleado por festival y unidad. El recibo se genera en PDF a partir de esa asignación; no se persiste el archivo.

El canon por unidad y festival conserva el importe de la vinculación. Los detalles de venta y las asignaciones conservan sus propios importes históricos. Al editar un plato compartido o vendido, solo se reemplaza la referencia del menú de la unidad que lo modifica; se conserva el original para las otras unidades y el historial.
