package com.epicentrogourmet.test;

import com.epicentrogourmet.test.entity.*;
import com.epicentrogourmet.test.service.ReporteService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReporteServiceTest {
    private final ReporteService service = new ReporteService(null, null, null, null, null, null);

    @Test
    void calculaCanonFoodTruckYPuestoConBigDecimal() {
        Festival festival = festival(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 15));
        FoodTruck truck = new FoodTruck(); truck.setSuperficie(20.0); truck.setRequiereElectricidad(true);
        PuestoDesarmable puesto = new PuestoDesarmable(); puesto.setSuperficie(30.0); puesto.setTiempoMontaje(120);
        assertEquals(new BigDecimal("12000.00"), service.canon(truck, festival));
        assertEquals(new BigDecimal("13800.00"), service.canon(puesto, festival));
    }

    @Test
    void calculaTramosDeSueldoConDiasInclusivosYPlusConfigurados() {
        Festival quince = festival(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 15));
        Cocinero cocinero = new Cocinero();
        cocinero.setCategoria(CategoriaCocina.COCINERO);
        cocinero.setFechaIngreso(LocalDate.of(2020, 1, 1));
        assertEquals(new BigDecimal("125000.00"), service.sueldo(cocinero, quince));

        Festival dieciseis = festival(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 16));
        Cajero cajero = new Cajero();
        cajero.setTurno("Manana");
        cajero.setFechaIngreso(LocalDate.of(2023, 1, 1));
        assertEquals(new BigDecimal("115000.00"), service.sueldo(cajero, dieciseis));

        Festival treintaYUno = festival(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));
        Empleado base = new Empleado(); base.setFechaIngreso(LocalDate.of(2020, 1, 1));
        assertEquals(new BigDecimal("150000.00"), service.sueldo(base, treintaYUno));
    }

    @Test
    void noLiquidaSiIngresoFuePosteriorAlFestival() {
        Festival festival = festival(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 15));
        Empleado empleado = new Empleado(); empleado.setFechaIngreso(LocalDate.of(2026, 2, 1));
        assertEquals(new BigDecimal("0.00"), service.sueldo(empleado, festival));
    }

    @Test
    void rechazaCostosIncompletos() {
        Festival festival = new Festival();
        FoodTruck truck = new FoodTruck(); truck.setSuperficie(10.0);
        assertThrows(IllegalArgumentException.class, () -> service.canon(truck, festival));
    }

    @Test
    void valoresPredeterminadosConservanLosImportesYNoCompartenLaEntidad() {
        Costo primero = Costo.valoresPredeterminados();
        Costo segundo = Costo.valoresPredeterminados();
        assertEquals(new BigDecimal("500.00"), segundo.getCostoPorSuperficie());
        assertEquals(new BigDecimal("10.00"), segundo.getCostoPorMontaje());
        assertEquals(new BigDecimal("2000.00"), segundo.getPlusElectricidad());
        assertEquals(new BigDecimal("100000.00"), segundo.getSueldoBase());
        assertEquals(new BigDecimal("5000.00"), segundo.getMontoAnioAntiguedad());
        assertEquals(new BigDecimal("150000.00"), segundo.getPlusCocinero());
        assertEquals(new BigDecimal("100000.00"), segundo.getPlusCocineroAyudante());
        assertEquals(new BigDecimal("50000.00"), segundo.getPlusLavaplatos());
        primero.setSueldoBase(new BigDecimal("123456.00"));
        assertEquals(new BigDecimal("100000.00"), segundo.getSueldoBase());
        assertEquals(new BigDecimal("100000.00"), Costo.SUELDO_BASE_PREDETERMINADO);
    }

    private static Festival festival(LocalDate inicio, LocalDate fin) {
        Costo costo = new Costo();
        costo.setCostoPorSuperficie(new BigDecimal("500"));
        costo.setCostoPorMontaje(new BigDecimal("10"));
        costo.setPlusElectricidad(new BigDecimal("2000"));
        costo.setSueldoBase(new BigDecimal("100000"));
        costo.setMontoAnioAntiguedad(new BigDecimal("5000"));
        costo.setPlusCocinero(new BigDecimal("150000"));
        costo.setPlusCocineroAyudante(new BigDecimal("100000"));
        costo.setPlusLavaplatos(new BigDecimal("50000"));
        Festival festival = new Festival(); festival.setFechaInicio(inicio); festival.setFechaFin(fin); festival.setCosto(costo);
        return festival;
    }
}
