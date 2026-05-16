package com.blyp.service;

import com.blyp.dto.GuardarTicketRequest;
import com.blyp.model.ItemTicket;
import com.blyp.model.Ticket;
import com.blyp.model.Usuario;
import com.blyp.repository.ItemTicketRepository;
import com.blyp.repository.TicketRepository;
import com.blyp.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
public class GastosService {

    private final TicketRepository ticketRepository;
    private final ItemTicketRepository itemTicketRepository;
    private final UsuarioRepository usuarioRepository;

    public GastosService(TicketRepository ticketRepository,
                         ItemTicketRepository itemTicketRepository,
                         UsuarioRepository usuarioRepository) {
        this.ticketRepository    = ticketRepository;
        this.itemTicketRepository = itemTicketRepository;
        this.usuarioRepository   = usuarioRepository;
    }

    @Transactional
    public Map<String, Object> guardarTicket(String email, GuardarTicketRequest request) {
        Usuario usuario = getUsuario(email);

        Ticket ticket = new Ticket();
        ticket.setUsuario(usuario);
        ticket.setFecha(request.getFecha() != null ? request.getFecha() : LocalDate.now());
        ticket.setTienda(request.getTienda());
        ticketRepository.save(ticket);

        BigDecimal total = BigDecimal.ZERO;
        for (GuardarTicketRequest.Item item : request.getItems()) {
            ItemTicket it = new ItemTicket();
            it.setTicket(ticket);
            it.setNombre(item.getNombre());
            it.setCantidad(item.getCantidad());
            it.setPrecioUnidad(item.getPrecioUnidad());
            it.setCategoria(item.getCategoria() != null ? item.getCategoria() : "Otros");
            itemTicketRepository.save(it);
            total = total.add(item.getPrecioUnidad().multiply(BigDecimal.valueOf(item.getCantidad())));
        }

        return Map.of("id", ticket.getId(), "total", total, "fecha", ticket.getFecha());
    }

    public Map<String, Object> getGastos(String email, String periodo) {
        Usuario usuario = getUsuario(email);

        LocalDate hasta = LocalDate.now();
        LocalDate desde = "año".equals(periodo)
                ? hasta.withDayOfYear(1)
                : hasta.withDayOfMonth(1);

        List<Object[]> rows = itemTicketRepository.gastosPorCategoria(usuario.getId(), desde, hasta);
        BigDecimal total    = itemTicketRepository.totalGastos(usuario.getId(), desde, hasta);

        List<Map<String, Object>> categorias = new ArrayList<>();
        for (int i = 0; i < Math.min(10, rows.size()); i++) {
            Object[] row = rows.get(i);
            categorias.add(Map.of(
                "categoria", row[0] != null ? row[0] : "Otros",
                "total",     row[1]
            ));
        }

        List<Ticket> tickets = ticketRepository.findTop10ByUsuarioIdAndFechaBetweenOrderByFechaDesc(usuario.getId(), desde, hasta);
        List<Map<String, Object>> resumen = tickets.stream().map(t -> {
            BigDecimal tot = itemTicketRepository.calcularTotalTicket(t.getId());
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id",     t.getId());
            m.put("fecha",  t.getFecha());
            m.put("tienda", t.getTienda());
            m.put("total",  tot);
            return m;
        }).toList();

        return Map.of(
            "periodo",    periodo != null ? periodo : "mes",
            "desde",      desde,
            "hasta",      hasta,
            "total",      total,
            "categorias", categorias,
            "tickets",    resumen
        );
    }

    private Usuario getUsuario(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));
    }
}
