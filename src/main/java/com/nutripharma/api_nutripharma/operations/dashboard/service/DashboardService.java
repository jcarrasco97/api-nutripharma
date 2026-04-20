package com.nutripharma.api_nutripharma.operations.dashboard.service;

import com.nutripharma.api_nutripharma.operations.consultas.domain.Consulta;
import com.nutripharma.api_nutripharma.operations.consultas.domain.EstadoConsulta;
import com.nutripharma.api_nutripharma.operations.consultas.repository.ConsultaRepository;
import com.nutripharma.api_nutripharma.operations.dashboard.controller.DashboardDTO;
import com.nutripharma.api_nutripharma.operations.dashboard.controller.DashboardDTO.ResumenMensualNutricionista;
import com.nutripharma.api_nutripharma.organization.nutricionistas.domain.Nutricionista;
import com.nutripharma.api_nutripharma.organization.nutricionistas.repository.NutricionistaRepository;
import com.nutripharma.api_nutripharma.sales.pedidos.domain.Pedido;
import com.nutripharma.api_nutripharma.sales.pedidos.domain.RepartoPedido;
import com.nutripharma.api_nutripharma.sales.pedidos.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ConsultaRepository consultaRepository;
    private final PedidoRepository pedidoRepository;
    private final NutricionistaRepository nutricionistaRepository;

    private static final BigDecimal PORCENTAJE_BONUS = new BigDecimal("0.05");

    // --- CÁLCULOS PARA NUTRICIONISTAS (INDIVIDUAL) ---

    @Transactional(readOnly = true)
    public ResumenMensualNutricionista calcularResumenNutricionista(Long nutricionistaId, int anio, int mes) {
        Nutricionista nutricionista = nutricionistaRepository.findById(nutricionistaId)
                .orElseThrow(() -> new IllegalArgumentException("Nutricionista no encontrado"));

        YearMonth yearMonth = YearMonth.of(anio, mes);
        LocalDate inicio = yearMonth.atDay(1);
        LocalDate fin = yearMonth.atEndOfMonth();

        List<Consulta> consultasMes = consultaRepository.findByNutricionistaIdAndEstadoAndFechaBetween(
                nutricionistaId, EstadoConsulta.VALIDADA, inicio, fin);

        List<Pedido> pedidosMes = pedidoRepository.findByRepartosNutricionistaIdAndFechaPedidoBetween(
                nutricionistaId, inicio, fin);

        long totalMinutos = consultasMes.stream()
                .mapToLong(c -> Duration.between(c.getHoraInicio(), c.getHoraFin()).toMinutes())
                .sum();

        double horasTrabajadas = Math.round((totalMinutos / 60.0) * 100.0) / 100.0;
        double balanceHoras = Math.round((horasTrabajadas - nutricionista.getHorasContratoMensual()) * 100.0) / 100.0;

        BigDecimal volumenVentas = BigDecimal.ZERO;
        for (Pedido p : pedidosMes) {
            BigDecimal totalPedido = calcularTotalPedido(p);
            BigDecimal porcentaje = p.getRepartos().stream()
                    .filter(r -> r.getNutricionista().getId().equals(nutricionistaId))
                    .findFirst()
                    .map(RepartoPedido::getPorcentaje)
                    .orElse(new BigDecimal("100.00"));

            BigDecimal parteProporcional = totalPedido.multiply(porcentaje)
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            volumenVentas = volumenVentas.add(parteProporcional);
        }

        BigDecimal bonusEstimado = volumenVentas.multiply(PORCENTAJE_BONUS).setScale(2, RoundingMode.HALF_UP);

        return new ResumenMensualNutricionista(
                yearMonth.getMonth().name(), anio, horasTrabajadas,
                nutricionista.getHorasContratoMensual(), balanceHoras,
                consultasMes.stream().mapToInt(Consulta::getNuevas).sum(),
                consultasMes.stream().mapToInt(Consulta::getRevisiones).sum(),
                consultasMes.stream().mapToInt(Consulta::getPromociones).sum(),
                consultasMes.stream().mapToInt(Consulta::getPersonalFarmacia).sum(),
                volumenVentas, bonusEstimado);
    }

    // --- INFORMES JERÁRQUICOS POR RANGO (ADMIN) ---

    @Transactional(readOnly = true)
    public DashboardDTO.ReporteJerarquicoDTO<List<DashboardDTO.RendimientoProductoDTO>> obtenerRendimientoProductosRango(
            int anioI, int anioF, Integer mes, Long farmId, Long nutriId) {

        List<DashboardDTO.DesgloseAnualDTO<List<DashboardDTO.RendimientoProductoDTO>>> desgloses = new ArrayList<>();
        Map<String, DashboardDTO.RendimientoProductoDTO> mapaTotales = new HashMap<>();

        for (int a = anioI; a <= anioF; a++) {
            List<DashboardDTO.RendimientoProductoDTO> datosAnio = pedidoRepository.findRendimientoProductos(
                    a, mes, farmId, nutriId, PageRequest.of(0, 100));

            desgloses.add(new DashboardDTO.DesgloseAnualDTO<>(a, datosAnio));

            // Combinar en el mapa de totales del rango
            for (DashboardDTO.RendimientoProductoDTO p : datosAnio) {
                mapaTotales.merge(p.productoNombre(), p, (ex, nu) -> new DashboardDTO.RendimientoProductoDTO(
                        ex.productoNombre(),
                        ex.cantidadVendida() + nu.cantidadVendida(),
                        nu.precioVentaFarmacia(), // Mantenemos precios del año más reciente
                        nu.precioVentaPublico(),
                        ex.ingresosGeneradosPvf().add(nu.ingresosGeneradosPvf()),
                        ex.ingresosPotencialesPvp().add(nu.ingresosPotencialesPvp())
                ));
            }
        }

        List<DashboardDTO.RendimientoProductoDTO> totales = mapaTotales.values().stream()
                .sorted(Comparator.comparing(DashboardDTO.RendimientoProductoDTO::cantidadVendida).reversed())
                .toList();

        return new DashboardDTO.ReporteJerarquicoDTO<>(totales, desgloses);
    }

    @Transactional(readOnly = true)
    public DashboardDTO.ReporteJerarquicoDTO<List<DashboardDTO.FacturacionMensualDTO>> obtenerFacturacionRango(
            int anioI, int anioF, Long farmId, Long nutriId) {

        List<DashboardDTO.DesgloseAnualDTO<List<DashboardDTO.FacturacionMensualDTO>>> desgloses = new ArrayList<>();

        for (int a = anioI; a <= anioF; a++) {
            desgloses.add(new DashboardDTO.DesgloseAnualDTO<>(a, obtenerFacturacionGlobalAnual(a, farmId, nutriId)));
        }

        // Totales consolidados por mes para el rango
        List<DashboardDTO.FacturacionMensualDTO> totalesMensuales = new ArrayList<>();
        String[] nombresMeses = {"Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};

        for (int i = 1; i <= 12; i++) {
            final int mesIdx = i;
            BigDecimal consultas = BigDecimal.ZERO;
            BigDecimal pedidos = BigDecimal.ZERO;

            for (var desgloseAnio : desgloses) {
                var datoMes = desgloseAnio.datos().stream().filter(d -> d.mesNumero() == mesIdx).findFirst();
                if (datoMes.isPresent()) {
                    consultas = consultas.add(datoMes.get().ingresosConsultas());
                    pedidos = pedidos.add(datoMes.get().ingresosPedidos());
                }
            }
            totalesMensuales.add(new DashboardDTO.FacturacionMensualDTO(
                    nombresMeses[i-1], i, consultas, pedidos, consultas.add(pedidos)));
        }

        return new DashboardDTO.ReporteJerarquicoDTO<>(totalesMensuales, desgloses);
    }

    @Transactional(readOnly = true)
    public DashboardDTO.ReporteJerarquicoDTO<List<DashboardDTO.RendimientoClinicoDTO>> obtenerRendimientoClinicoRango(
            int anioI, int anioF, Integer mes, Long nutriId) {

        List<DashboardDTO.DesgloseAnualDTO<List<DashboardDTO.RendimientoClinicoDTO>>> desgloses = new ArrayList<>();

        for (int a = anioI; a <= anioF; a++) {
            desgloses.add(new DashboardDTO.DesgloseAnualDTO<>(a, obtenerRendimientoClinico(a, a, mes, nutriId)));
        }

        // Totales del rango clínico
        return new DashboardDTO.ReporteJerarquicoDTO<>(
                obtenerRendimientoClinico(anioI, anioF, mes, nutriId), desgloses);
    }

    // --- MÉTODOS DE APOYO Y AUDITORÍA ---

    @Transactional(readOnly = true)
    public List<DashboardDTO.RendimientoClinicoDTO> obtenerRendimientoClinico(int anioI, int anioF, Integer mes, Long nutriId) {
        LocalDate inicio = LocalDate.of(anioI, mes != null ? mes : 1, 1);
        LocalDate fin = (mes != null) ? YearMonth.of(anioF, mes).atEndOfMonth() : LocalDate.of(anioF, 12, 31);

        List<Consulta> consultas = consultaRepository.findByFechaBetween(inicio, fin);

        return consultas.stream()
                .filter(c -> c.getEstado() == EstadoConsulta.VALIDADA)
                .filter(c -> nutriId == null || (c.getNutricionista() != null && c.getNutricionista().getId().equals(nutriId)))
                .collect(Collectors.groupingBy(c -> c.getFarmacia().getNombre()))
                .entrySet().stream()
                .map(entry -> {
                    int n = entry.getValue().stream().mapToInt(Consulta::getNuevas).sum();
                    int r = entry.getValue().stream().mapToInt(Consulta::getRevisiones).sum();
                    int pr = entry.getValue().stream().mapToInt(Consulta::getPromociones).sum();
                    int ps = entry.getValue().stream().mapToInt(Consulta::getPersonalFarmacia).sum();
                    BigDecimal euros = new BigDecimal((n * 25) + (r * 20));
                    return new DashboardDTO.RendimientoClinicoDTO(entry.getKey(), n, r, pr, ps, euros);
                })
                .sorted(Comparator.comparing(DashboardDTO.RendimientoClinicoDTO::ingresosGenerados).reversed())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DashboardDTO.FacturacionMensualDTO> obtenerFacturacionGlobalAnual(int anio, Long farmId, Long nutriId) {
        LocalDate inicio = LocalDate.of(anio, 1, 1);
        LocalDate fin = LocalDate.of(anio, 12, 31);

        List<Pedido> pedidos = pedidoRepository.findByFechaPedidoBetween(inicio, fin).stream()
                .filter(p -> p.getEstado() != com.nutripharma.api_nutripharma.sales.pedidos.domain.EstadoPedido.CANCELADO)
                .filter(p -> farmId == null || (p.getFarmacia() != null && p.getFarmacia().getId().equals(farmId)))
                .filter(p -> nutriId == null || p.getRepartos().stream().anyMatch(r -> r.getNutricionista().getId().equals(nutriId)))
                .toList();

        List<Consulta> consultas = consultaRepository.findByFechaBetween(inicio, fin).stream()
                .filter(c -> c.getEstado() == EstadoConsulta.VALIDADA)
                .filter(c -> farmId == null || (c.getFarmacia() != null && c.getFarmacia().getId().equals(farmId)))
                .filter(c -> nutriId == null || (c.getNutricionista() != null && c.getNutricionista().getId().equals(nutriId)))
                .toList();

        List<DashboardDTO.FacturacionMensualDTO> lista = new ArrayList<>();
        String[] meses = {"Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};

        for (int i = 1; i <= 12; i++) {
            final int m = i;
            BigDecimal ingPedidos = pedidos.stream()
                    .filter(p -> p.getFechaPedido().getMonthValue() == m)
                    .map(p -> {
                        BigDecimal total = calcularTotalPedido(p);
                        if (nutriId == null) return total;
                        BigDecimal porc = p.getRepartos().stream()
                                .filter(r -> r.getNutricionista().getId().equals(nutriId))
                                .map(RepartoPedido::getPorcentaje).findFirst().orElse(BigDecimal.ZERO);
                        return total.multiply(porc).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                    }).reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal ingConsultas = consultas.stream()
                    .filter(c -> c.getFecha().getMonthValue() == m)
                    .map(c -> new BigDecimal((c.getNuevas() * 25) + (c.getRevisiones() * 20)))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            lista.add(new DashboardDTO.FacturacionMensualDTO(meses[i-1], i, ingConsultas, ingPedidos, ingConsultas.add(ingPedidos)));
        }
        return lista;
    }

    private BigDecimal calcularTotalPedido(Pedido pedido) {
        return pedido.getLineas().stream()
                .map(l -> l.getPrecioAplicado().multiply(new BigDecimal(l.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // --- OTROS MÉTODOS EXISTENTES ---
    @Transactional(readOnly = true)
    public List<DashboardDTO.EventoCalendarioDTO> obtenerEventosCalendario(int anio, int mes) {
        YearMonth ym = YearMonth.of(anio, mes);
        List<DashboardDTO.EventoCalendarioDTO> eventos = new ArrayList<>();
        pedidoRepository.findByFechaPedidoBetween(ym.atDay(1), ym.atEndOfMonth()).forEach(p -> {
            eventos.add(new DashboardDTO.EventoCalendarioDTO("PED-"+p.getId(), "PEDIDO", "Pedido: " + (p.getFarmacia() != null ? p.getFarmacia().getNombre() : "N/A"), p.getFechaPedido(), p.getEstado().name(), calcularTotalPedido(p).setScale(2, RoundingMode.HALF_UP) + "€"));
        });
        consultaRepository.findByFechaBetween(ym.atDay(1), ym.atEndOfMonth()).forEach(c -> {
            eventos.add(new DashboardDTO.EventoCalendarioDTO("CON-"+c.getId(), "CONSULTA", "Consulta: " + (c.getNutricionista() != null ? c.getNutricionista().getNombre() : "N/A"), c.getFecha(), c.getEstado().name(), c.getFarmacia() != null ? c.getFarmacia().getNombre() : "N/A"));
        });
        return eventos;
    }

    @Transactional(readOnly = true)
    public DashboardDTO.AuditoriaNutriDTO obtenerAuditoriaNutricionista(Long id, int anio, int mes) {
        Nutricionista n = nutricionistaRepository.findById(id).orElseThrow();
        YearMonth ym = YearMonth.of(anio, mes);
        List<Consulta> cons = consultaRepository.findByNutricionistaIdAndEstadoAndFechaBetween(id, EstadoConsulta.VALIDADA, ym.atDay(1), ym.atEndOfMonth());
        List<Pedido> peds = pedidoRepository.findByRepartosNutricionistaIdAndFechaPedidoBetween(id, ym.atDay(1), ym.atEndOfMonth());

        BigDecimal factCons = new BigDecimal((cons.stream().mapToInt(Consulta::getNuevas).sum() * 25) + (cons.stream().mapToInt(Consulta::getRevisiones).sum() * 20));
        BigDecimal factPeds = peds.stream().map(p -> calcularTotalPedido(p)).reduce(BigDecimal.ZERO, BigDecimal::add);

        return new DashboardDTO.AuditoriaNutriDTO(0, cons.size(), peds.size(), factCons, factPeds);
    }

    @Transactional(readOnly = true)
    public List<String> obtenerMesesActividadNutricionista(Long id) {
        Set<String> meses = new HashSet<>();
        consultaRepository.findByNutricionistaId(id).forEach(c -> meses.add(String.format("%04d-%02d", c.getFecha().getYear(), c.getFecha().getMonthValue())));
        pedidoRepository.findByRepartosNutricionistaId(id).forEach(p -> meses.add(String.format("%04d-%02d", p.getFechaPedido().getYear(), p.getFechaPedido().getMonthValue())));
        return meses.stream().sorted(Comparator.reverseOrder()).toList();
    }
}