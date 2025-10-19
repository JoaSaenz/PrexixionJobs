package com.joa.prexixion.jobs.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.joa.prexixion.jobs.dto.ClienteDTO;
import com.joa.prexixion.jobs.dto.NotificacionDTO;
import com.joa.prexixion.jobs.dto.SunatBuzonResponseDTO;
import com.joa.prexixion.jobs.model.Cliente;
import com.joa.prexixion.jobs.model.Notificacion;
import com.joa.prexixion.jobs.repository.ClienteRepository;
import com.joa.prexixion.jobs.repository.NotificacionRepository;

@Service
public class SunatBuzonService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private NotificacionRepository notificacionRepository;

    @Autowired
    private JobStatusService jobStatusService;

    private final RestTemplate restTemplate = new RestTemplate();

    /** ✅ Método público que lanza la sincronización en segundo plano */
    @Async
    public void ejecutarAsync() {
        sincronizarBuzones();
    }

    public void sincronizarBuzones() {
        final String nombreJob = "SincronizacionSUNAT";
        jobStatusService.actualizar(nombreJob, "EN_PROGRESO", 0.0, "Iniciando sincronización de buzones...");

        List<Cliente> clientes = clienteRepository.obtenerClientesTest10();
        int total = clientes.size();
        int procesados = 0;

        for (Cliente cliente : clientes) {
            procesados++;
            try {
                ClienteDTO clienteDTO = new ClienteDTO(
                        cliente.getRuc(),
                        cliente.getSolU(),
                        cliente.getSolC());

                String url = "http://localhost:3000/sunat/consultar";
                SunatBuzonResponseDTO response = restTemplate.postForObject(url, clienteDTO,
                        SunatBuzonResponseDTO.class);

                if (response != null && response.isSuccess()) {
                    procesarNotificaciones(clienteDTO, response.getNotificaciones());
                    double progreso = (procesados * 100.0) / total;
                    jobStatusService.actualizar(nombreJob, "EN_PROGRESO", progreso,
                            "Procesado cliente " + cliente.getRuc());
                } else {
                    jobStatusService.actualizar(nombreJob, "EN_PROGRESO", (procesados * 100.0) / total,
                            "Cliente " + cliente.getRuc() + " sin nuevas notificaciones");
                }

            } catch (Exception e) {
                jobStatusService.actualizar(nombreJob, "ERROR", (procesados * 100.0) / total,
                        "Error con cliente " + cliente.getRuc() + ": " + e.getMessage());
            }
        }

        jobStatusService.actualizar(nombreJob, "FINALIZADO", 100.0, "Sincronización completada correctamente.");
    }

    private void procesarNotificaciones(ClienteDTO clienteDTO, List<NotificacionDTO> notificacionesDTO) {
        if (notificacionesDTO.isEmpty())
            return;

        List<String> ids = notificacionesDTO.stream().map(NotificacionDTO::getId).toList();
        List<String> existentes = notificacionRepository.findExistentes(clienteDTO.getRuc(), ids);

        List<Notificacion> nuevas = notificacionesDTO.stream()
                .filter(n -> !existentes.contains(n.getId()))
                .map(n -> {
                    Notificacion entidad = new Notificacion();
                    entidad.setRuc(clienteDTO.getRuc());
                    entidad.setIdSunat(n.getId());
                    entidad.setTitulo(n.getTitulo());
                    entidad.setFecha(LocalDateTime.parse(
                            n.getFecha(),
                            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
                    return entidad;
                })
                .toList();

        if (!nuevas.isEmpty()) {
            notificacionRepository.saveAll(nuevas);
            System.out.println("Se guardaron " + nuevas.size() + " nuevas notificaciones para " + clienteDTO.getRuc());
        }
    }
}