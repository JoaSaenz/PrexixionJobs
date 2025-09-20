package com.joa.prexixion.jobs.scheduler;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.joa.prexixion.jobs.dto.NotificacionDTO;
import com.joa.prexixion.jobs.dto.SunatBuzonRequestDTO;
import com.joa.prexixion.jobs.dto.SunatBuzonResponseDTO;
import com.joa.prexixion.jobs.model.Cliente;
import com.joa.prexixion.jobs.model.Notificacion;
import com.joa.prexixion.jobs.repository.ClienteRepository;
import com.joa.prexixion.jobs.repository.NotificacionRepository;

@Service
public class SunatBuzonScheduler {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private NotificacionRepository notificacionRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    // @Scheduled(cron = "0 0 2 * * *") // todos los días a las 2 AM
    @Scheduled(cron = "0 15 12 * * *") // todos los días a las 11:45 AM
    public void consultarSunat() {
        System.out.println("Ejecutando job de consulta Sunat...");

        List<Cliente> clientes = clienteRepository.obtenerClientesActivos(1, 3);

        for (Cliente cliente : clientes) {
            try {
                SunatBuzonRequestDTO request = new SunatBuzonRequestDTO(
                        cliente.getRuc(),
                        cliente.getSolU(),
                        cliente.getSolC());

                String url = "http://localhost:3000/sunat/consultar";
                SunatBuzonResponseDTO response = restTemplate.postForObject(url, request, SunatBuzonResponseDTO.class);
                if (response != null && response.isSuccess()) {
                    System.out.println("Cliente " + cliente.getRuc() + " → " + response.getMessage());
                    procesarNotificaciones(cliente, response.getNotificaciones());
                }

            } catch (Exception e) {
                System.err.println("Fallo consulta SUNAT para cliente " + cliente.getRuc() + ": " + e.getMessage());
            }
        }
    }

    private void procesarNotificaciones(Cliente cliente, List<NotificacionDTO> notificaciones) {
        if (notificaciones.isEmpty())
            return;

        // Traer los que ya existen
        List<String> ids = notificaciones.stream().map(NotificacionDTO::getId).toList();
        List<String> existentes = notificacionRepository.findExistentes(cliente.getRuc(), ids);

        // Filtrar solo las nuevas
        List<Notificacion> nuevas = notificaciones.stream()
                .filter(n -> !existentes.contains(n.getId()))
                .map(n -> {
                    Notificacion entidad = new Notificacion();
                    entidad.setRuc(cliente.getRuc());
                    entidad.setIdSunat(n.getId());
                    entidad.setTitulo(n.getTitulo());

                    LocalDateTime fechaParseada = LocalDateTime.parse(
                            n.getFecha(),
                            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
                    entidad.setFecha(fechaParseada);

                    return entidad;
                })
                .toList();

        // Guardar en batch
        if (!nuevas.isEmpty()) {
            notificacionRepository.saveAll(nuevas);
            System.out.println(
                    "Se guardaron " + nuevas.size() + " notificaciones nuevas para cliente " + cliente.getRuc());
        }
    }
}
