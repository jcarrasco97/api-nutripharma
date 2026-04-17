package com.nutripharma.api_nutripharma.operations.dashboard.service;

import com.nutripharma.api_nutripharma.operations.consultas.domain.Consulta;
import com.nutripharma.api_nutripharma.operations.consultas.domain.EstadoConsulta;
import com.nutripharma.api_nutripharma.operations.consultas.repository.ConsultaRepository;
import com.nutripharma.api_nutripharma.operations.dashboard.controller.DashboardDTO.ResumenMensualNutricionista;
import com.nutripharma.api_nutripharma.organization.nutricionistas.domain.Nutricionista;
import com.nutripharma.api_nutripharma.organization.nutricionistas.repository.NutricionistaRepository;
import com.nutripharma.api_nutripharma.sales.pedidos.domain.Pedido;
import com.nutripharma.api_nutripharma.sales.pedidos.domain.RepartoPedido;
import com.nutripharma.api_nutripharma.sales.pedidos.repository.PedidoRepository;
import com.nutripharma.api_nutripharma.operations.dashboard.controller.DashboardDTO;
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
                List<Consulta> consultasMes = consultaRepository.findByNutricionistaIdAndEstadoAndFechaBetween(
                                nutricionistaId, EstadoConsulta.VALIDADA, fechaInicio, fechaFin);

                // --- CORRECCIÓN: Usamos el nuevo método del repositorio multicapa ---
                List<Pedido> pedidosMes = pedidoRepository.findByRepartosNutricionistaIdAndFechaPedidoBetween(
                                nutricionistaId, fechaInicio, fechaFin);

                // 3. Cálculos de HORAS
                long totalMinutosTrabajados = consultasMes.stream()
                                .mapToLong(c -> Duration.between(c.getHoraInicio(), c.getHoraFin()).toMinutes())
                                .sum();

                double horasTrabajadas = totalMinutosTrabajados / 60.0;
                horasTrabajadas = Math.round(horasTrabajadas * 100.0) / 100.0;

                double balanceHoras = horasTrabajadas - nutricionista.getHorasContratoMensual();
                balanceHoras = Math.round(balanceHoras * 100.0) / 100.0;

                // 4. Cálculos CLÍNICOS
                int totalNuevas = consultasMes.stream().mapToInt(Consulta::getNuevas).sum();
                int totalRevisiones = consultasMes.stream().mapToInt(Consulta::getRevisiones).sum();
                int totalPromocionales = consultasMes.stream().mapToInt(Consulta::getPromociones).sum();
                int totalPersonal = consultasMes.stream().mapToInt(Consulta::getPersonalFarmacia).sum();

                // 5. Cálculos FINANCIEROS (Ventas Multicapa y Bonus)
                BigDecimal volumenVentas = BigDecimal.ZERO;

                for (Pedido p : pedidosMes) {
                        BigDecimal totalPedido = calcularTotalPedido(p);

                        // Buscamos el porcentaje específico asignado a esta nutricionista en este
                        // pedido
                        BigDecimal porcentajeAsignado = p.getRepartos().stream()
                                        .filter(r -> r.getNutricionista().getId().equals(nutricionistaId))
                                        .findFirst()
                                        .map(RepartoPedido::getPorcentaje)
                                        .orElse(new BigDecimal("100.00")); // Si no hay reparto explícito, asume el 100%

                        // Calculamos su parte proporcional: (TotalPedido * Porcentaje) / 100
                        BigDecimal parteProporcional = totalPedido.multiply(porcentajeAsignado)
                                        .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

                        volumenVentas = volumenVentas.add(parteProporcional);
                }

                // Bonus = Volumen de Ventas Proporcional * 0.05
                BigDecimal bonusEstimado = volumenVentas.multiply(PORCENTAJE_BONUS).setScale(2, RoundingMode.HALF_UP);

                // 6. Empaquetar y enviar
                return new ResumenMensualNutricionista(
                                yearMonth.getMonth().name(),
                                anio,
                                horasTrabajadas,
                                nutricionista.getHorasContratoMensual(),
                                balanceHoras,
                                totalNuevas,
                                totalRevisiones,
                                totalPromocionales,
                                totalPersonal,
                                volumenVentas,
                                bonusEstimado);
        }

        // --- Auditoria de Nutricionista (Admin) ---
        @Transactional(readOnly = true)
        public DashboardDTO.AuditoriaNutriDTO obtenerAuditoriaNutricionista(Long nutricionistaId, int anio, int mes) {
                Nutricionista nutricionista = nutricionistaRepository.findById(nutricionistaId)
                                .orElseThrow(() -> new IllegalArgumentException("Nutricionista no encontrado"));

                YearMonth yearMonth = YearMonth.of(anio, mes);
                LocalDate fechaInicio = yearMonth.atDay(1);
                LocalDate fechaFin = yearMonth.atEndOfMonth();

                List<Consulta> consultasMes = consultaRepository.findByNutricionistaIdAndEstadoAndFechaBetween(
                                nutricionistaId, EstadoConsulta.VALIDADA, fechaInicio, fechaFin);

                List<Pedido> pedidosMes = pedidoRepository.findByRepartosNutricionistaIdAndFechaPedidoBetween(
                                nutricionistaId, fechaInicio, fechaFin);

                int totalNuevas = consultasMes.stream().mapToInt(Consulta::getNuevas).sum();
                int totalRevisiones = consultasMes.stream().mapToInt(Consulta::getRevisiones).sum();
                BigDecimal facturacionConsultas = new BigDecimal((totalNuevas * 25) + (totalRevisiones * 20));

                BigDecimal facturacionProductos = BigDecimal.ZERO;

                for (Pedido p : pedidosMes) {
                        BigDecimal totalPedido = calcularTotalPedido(p);
                        BigDecimal porcentajeAsignado = p.getRepartos().stream()
                                        .filter(r -> r.getNutricionista().getId().equals(nutricionistaId))
                                        .findFirst()
                                        .map(RepartoPedido::getPorcentaje)
                                        .orElse(new BigDecimal("100.00"));

                        BigDecimal parteProporcional = totalPedido.multiply(porcentajeAsignado)
                                        .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

                        facturacionProductos = facturacionProductos.add(parteProporcional);
                }

                int totalKilometros = consultasMes.stream().mapToInt(c -> {
                        if (c.getFarmacia() == null)
                                return 0;
                        return nutricionista.getAsignaciones().stream()
                                        .filter(a -> a.getFarmacia() != null &&
                                                        (a.getFarmacia().getId().equals(c.getFarmacia().getId()) ||
                                                                        (a.getFarmacia().getNombre() != null && c
                                                                                        .getFarmacia()
                                                                                        .getNombre() != null &&
                                                                                        a.getFarmacia().getNombre()
                                                                                                        .equalsIgnoreCase(
                                                                                                                        c.getFarmacia().getNombre()))))
                                        .findFirst()
                                        .map(a -> a.getKilometros() != null ? a.getKilometros() : 0)
                                        .orElse(0);
                }).sum();

                return new DashboardDTO.AuditoriaNutriDTO(
                                totalKilometros,
                                consultasMes.size(),
                                pedidosMes.size(),
                                facturacionConsultas,
                                facturacionProductos);
        }

        // --- Meses Disponibles para Auditoría (Admin) ---
        @Transactional(readOnly = true)
        public List<String> obtenerMesesActividadNutricionista(Long nutricionistaId) {
                if (!nutricionistaRepository.existsById(nutricionistaId)) {
                        throw new IllegalArgumentException("Nutricionista no encontrado");
                }

                java.util.Set<String> mesesUnicos = new java.util.HashSet<>();

                // 1. Extraer meses de consultas validadas
                consultaRepository.findByNutricionistaId(nutricionistaId).stream()
                                .filter(c -> c.getEstado() == EstadoConsulta.VALIDADA)
                                .forEach(c -> {
                                        String mesAnio = String.format("%04d-%02d", c.getFecha().getYear(),
                                                        c.getFecha().getMonthValue());
                                        mesesUnicos.add(mesAnio);
                                });

                // 2. Extraer meses de pedidos enviados/liquidados (según el frontend)
                pedidoRepository.findByRepartosNutricionistaId(nutricionistaId).stream()
                                .filter(p -> p.getEstado() != com.nutripharma.api_nutripharma.sales.pedidos.domain.EstadoPedido.CANCELADO)
                                .forEach(p -> {
                                        String mesAnio = String.format("%04d-%02d", p.getFechaPedido().getYear(),
                                                        p.getFechaPedido().getMonthValue());
                                        mesesUnicos.add(mesAnio);
                                });

                return mesesUnicos.stream()
                                .sorted(java.util.Comparator.reverseOrder())
                                .toList();
        }

        // --- Gráfica de Facturación Anual (Admin) ---
        @Transactional(readOnly = true)
        public List<DashboardDTO.FacturacionMensualDTO> obtenerFacturacionGlobalAnual(int anio, Long farmaciaId,
                        Long nutricionistaId) {
                LocalDate inicioAnio = LocalDate.of(anio, 1, 1);
                LocalDate finAnio = LocalDate.of(anio, 12, 31);

                List<Pedido> pedidosTotales = pedidoRepository.findByFechaPedidoBetween(inicioAnio, finAnio);
                List<Consulta> consultasTotales = consultaRepository.findByFechaBetween(inicioAnio, finAnio);

                // --- APLICAR FILTROS EN MEMORIA ---

                // 1. Filtrado de Pedidos
                List<Pedido> pedidosAnuales = pedidosTotales.stream()
                                .filter(p -> p.getEstado() != com.nutripharma.api_nutripharma.sales.pedidos.domain.EstadoPedido.CANCELADO)
                                .filter(p -> farmaciaId == null || (p.getFarmacia() != null
                                                && p.getFarmacia().getId().equals(farmaciaId)))
                                .filter(p -> nutricionistaId == null || p.getRepartos().stream()
                                                .anyMatch(r -> r.getNutricionista() != null && r.getNutricionista()
                                                                .getId().equals(nutricionistaId)))
                                .toList();

                // 2. Filtrado de Consultas
                List<Consulta> consultasAnuales = consultasTotales.stream()
                                .filter(c -> c.getEstado() == EstadoConsulta.VALIDADA)
                                .filter(c -> farmaciaId == null || (c.getFarmacia() != null
                                                && c.getFarmacia().getId().equals(farmaciaId)))
                                .filter(c -> nutricionistaId == null || (c.getNutricionista() != null
                                                && c.getNutricionista().getId().equals(nutricionistaId)))
                                .toList();

                List<DashboardDTO.FacturacionMensualDTO> facturacionMeses = new java.util.ArrayList<>();
                String[] nombresMeses = { "Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov",
                                "Dic" };

                for (int i = 1; i <= 12; i++) {
                        final int mesActual = i;

                        BigDecimal ingresosPedidos = pedidosAnuales.stream()
                                        .filter(p -> p.getFechaPedido().getMonthValue() == mesActual)
                                        .map(p -> {
                                                BigDecimal total = calcularTotalPedido(p);
                                                if (nutricionistaId == null)
                                                        return total;

                                                // Si filtramos por nutricionista, sacar el proporcional de su reparto
                                                BigDecimal porcentaje = p.getRepartos().stream()
                                                                .filter(r -> r.getNutricionista().getId()
                                                                                .equals(nutricionistaId))
                                                                .map(RepartoPedido::getPorcentaje)
                                                                .findFirst()
                                                                .orElse(BigDecimal.ZERO);

                                                return total.multiply(porcentaje).divide(new BigDecimal("100"), 2,
                                                                RoundingMode.HALF_UP);
                                        })
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                        BigDecimal ingresosConsultas = consultasAnuales.stream()
                                        .filter(c -> c.getFecha().getMonthValue() == mesActual)
                                        .map(c -> new BigDecimal((c.getNuevas() * 25) + (c.getRevisiones() * 20)))
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                        facturacionMeses.add(new DashboardDTO.FacturacionMensualDTO(
                                        nombresMeses[i - 1],
                                        mesActual,
                                        ingresosConsultas,
                                        ingresosPedidos,
                                        ingresosConsultas.add(ingresosPedidos)));
                }
                return facturacionMeses;
        }

        // --- Eventos para el Calendario (Admin) ---
        @Transactional(readOnly = true)
        public List<DashboardDTO.EventoCalendarioDTO> obtenerEventosCalendario(int anio, int mes) {
                YearMonth yearMonth = YearMonth.of(anio, mes);
                LocalDate inicioMes = yearMonth.atDay(1);
                LocalDate finMes = yearMonth.atEndOfMonth();

                List<DashboardDTO.EventoCalendarioDTO> eventos = new java.util.ArrayList<>();

                pedidoRepository.findByFechaPedidoBetween(inicioMes, finMes).forEach(p -> {
                        // 🛡️ ESCUDO ANTI-NULOS (Por si la farmacia se dio de baja)
                        String nombreDestino = p.getFarmacia() != null ? p.getFarmacia().getNombre()
                                        : "[Farmacia Borrada]";

                        eventos.add(new DashboardDTO.EventoCalendarioDTO(
                                        "PED-" + p.getId(),
                                        "PEDIDO",
                                        "Pedido: " + nombreDestino,
                                        p.getFechaPedido(),
                                        p.getEstado().name(),
                                        calcularTotalPedido(p).setScale(2, RoundingMode.HALF_UP) + "€"));
                });

                consultaRepository.findByFechaBetween(inicioMes, finMes).forEach(c -> {
                        // 🛡️ ESCUDO ANTI-NULOS
                        String nombreNutri = c.getNutricionista() != null
                                        ? c.getNutricionista().getNombre() + " " + c.getNutricionista().getApellidos()
                                        : "[Nutri Borrado]";
                        String nombreFarmacia = c.getFarmacia() != null ? c.getFarmacia().getNombre()
                                        : "[Farmacia Borrada]";

                        eventos.add(new DashboardDTO.EventoCalendarioDTO(
                                        "CON-" + c.getId(),
                                        "CONSULTA",
                                        "Consulta: " + nombreNutri,
                                        c.getFecha(),
                                        c.getEstado().name(),
                                        nombreFarmacia));
                });

                return eventos;
        }

        private BigDecimal calcularTotalPedido(Pedido pedido) {
                return pedido.getLineas().stream()
                                .map(linea -> linea.getPrecioAplicado().multiply(new BigDecimal(linea.getCantidad())))
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
}