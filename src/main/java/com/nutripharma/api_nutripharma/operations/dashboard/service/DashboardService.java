package com.nutripharma.api_nutripharma.operations.dashboard.service;

import com.nutripharma.api_nutripharma.operations.consultas.domain.Consulta;
import com.nutripharma.api_nutripharma.operations.consultas.domain.EstadoConsulta;
import com.nutripharma.api_nutripharma.operations.consultas.repository.ConsultaRepository;
import com.nutripharma.api_nutripharma.operations.dashboard.controller.DashboardDTO.ResumenMensualNutricionista;
import com.nutripharma.api_nutripharma.organization.nutricionistas.domain.Nutricionista;
import com.nutripharma.api_nutripharma.organization.nutricionistas.repository.NutricionistaRepository;
import com.nutripharma.api_nutripharma.sales.pedidos.domain.Pedido;
import com.nutripharma.api_nutripharma.sales.pedidos.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ConsultaRepository consultaRepository;
    private final PedidoRepository pedidoRepository;
    private final NutricionistaRepository nutricionistaRepository;

    // Regla de negocio: Porcentaje de comisión para el bonus (Ej. 5%)
    private static final BigDecimal PORCENTAJE_BONUS = new BigDecimal("0.05");

    @Transactional(readOnly = true)
    public ResumenMensualNutricionista calcularResumenNutricionista(Long nutricionistaId, int anio, int mes) {

        Nutricionista nutricionista = nutricionistaRepository.findById(nutricionistaId)
                .orElseThrow(() -> new IllegalArgumentException("Nutricionista no encontrado"));

        // 1. Delimitar las fechas (Ej. del 01/03/2026 al 31/03/2026)
        YearMonth yearMonth = YearMonth.of(anio, mes);
        LocalDate fechaInicio = yearMonth.atDay(1);
        LocalDate fechaFin = yearMonth.atEndOfMonth();

        // 2. Traer los datos de Base de Datos
        // OJO: Solo contamos las consultas CONFIRMADAS. Los borradores no suman al sueldo.
        List<Consulta> consultasMes = consultaRepository.findByNutricionistaIdAndEstadoAndFechaBetween(
                nutricionistaId, EstadoConsulta.CONFIRMADA, fechaInicio, fechaFin);

        List<Pedido> pedidosMes = pedidoRepository.findByNutricionistaIdAndFechaPedidoBetween(
                nutricionistaId, fechaInicio, fechaFin);

        // 3. Cálculos de HORAS
        long totalMinutosTrabajados = consultasMes.stream()
                .mapToLong(c -> Duration.between(c.getHoraInicio(), c.getHoraFin()).toMinutes())
                .sum();

        double horasTrabajadas = totalMinutosTrabajados / 60.0;
        // Redondeamos a 2 decimales
        horasTrabajadas = Math.round(horasTrabajadas * 100.0) / 100.0;

        double balanceHoras = horasTrabajadas - nutricionista.getHorasContratoMensual();
        balanceHoras = Math.round(balanceHoras * 100.0) / 100.0;

        // 4. Cálculos CLÍNICOS
        int totalNuevas = consultasMes.stream().mapToInt(Consulta::getNuevas).sum();
        int totalRevisiones = consultasMes.stream().mapToInt(Consulta::getRevisiones).sum();
        int totalPromocionales = consultasMes.stream().mapToInt(Consulta::getPromociones).sum();
        int totalPersonal = consultasMes.stream().mapToInt(Consulta::getPersonalFarmacia).sum();

        // 5. Cálculos FINANCIEROS (Ventas y Bonus)
        BigDecimal volumenVentas = pedidosMes.stream()
                .map(this::calcularTotalPedido)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Bonus = Volumen de Ventas * 0.05
        BigDecimal bonusEstimado = volumenVentas.multiply(PORCENTAJE_BONUS).setScale(2, RoundingMode.HALF_UP);

        // 6. Empaquetar y enviar
        return new ResumenMensualNutricionista(
                yearMonth.getMonth().name(), // Nombre del mes en texto
                anio,
                horasTrabajadas,
                nutricionista.getHorasContratoMensual(),
                balanceHoras,
                totalNuevas,
                totalRevisiones,
                totalPromocionales,
                totalPersonal,
                volumenVentas,
                bonusEstimado
        );
    }

    private BigDecimal calcularTotalPedido(Pedido pedido) {
        return pedido.getLineas().stream()
                .map(linea -> linea.getPrecioAplicado().multiply(new BigDecimal(linea.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}