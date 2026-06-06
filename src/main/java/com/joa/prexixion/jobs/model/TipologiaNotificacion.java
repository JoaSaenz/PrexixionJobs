package com.joa.prexixion.jobs.model;

import java.text.Normalizer;

public enum TipologiaNotificacion {
    DUDA_RAZONABLE("Aduanas", "DUDA RAZONABLE", "DUDA RAZONABLE"),
    ORDEN_PAGO("Deudas", "Orden de Pago", "ORDEN DE PAGO"),
    SOLICITUD_APROBADA("Deudas", "Solicitud Aprobada", "SOLICITUD APROBADA"),
    RESOLUCION_CONCLUSION("Deudas", "Resolución de Conclusión", "RESOLUCION DE CONCLUSION"),
    FRACCIONAMIENTO_APROBATORIA("Deudas", "Fraccionamiento Aprobatoria", "FRACCIONAMIENTO APROBATORIA"),
    EJECUCION_COACTIVA("Deudas", "Ejecución Coactiva", "EJECUCION COACTIVA"),
    RESOLUCION_COACTIVA("Deudas", "Resolución Coactiva", "RESOLUCION COACTIVA"),
    COACTIVA_CONCLUSION("Deudas", "Coactiva de Conclusión", "COACTIVA DE CONCLUSION"),
    REQUERIMIENTO_PAGO("Deudas", "Requerimiento de Pago", "REQUERIMIENTO DE PAGO"),
    LEVANTAMIENTO_EMBARGO("Deudas", "Levantamiento de Embargo", "LEVANTAMIENTO DE EMBARGO"),
    NOTIFICACION_DE00("Deudas", "NOTIFICACION DE00", "NOTIFICACION DE00"),
    COMPENSACION("Deudas", "COMPENSACIÓN", "COMPENSACION"),
    LIBERACION_FONDOS("Devoluciones", "Liberación de Fondos", "LIBERACION DE FONDOS"),
    ABONO_CUENTA("Devoluciones", "Abono en cuenta efectuado", "ABONO EN CUENTA EFECTUADO"),
    RESOLUCION_RECLAMACION("Fiscalizaciones", "Resolución de Reclamación", "RESOLUCION DE RECLAMACION"),
    ESQUELA("Fiscalizaciones", "Esquela", "ESQUELA"),
    CIERRE_ESQUELA("Fiscalizaciones", "Cierre Esquela", "CIERRE ESQUELA"),
    CARTA_INDUCTIVA("Fiscalizaciones", "Carta Inductiva", "CARTA INDUCTIVA"),
    PEDIDO_INFORMACION("Fiscalizaciones", "PEDIDO DE INFORMACION", "PEDIDO DE INFORMACION"),
    BAJA_INSCRIPCION("Otros", "BAJA DE INSCRIPCIÓN", "BAJA DE INSCRIPCION"),
    OTROS_CONCEPTOS("Sin asignar", "OTROS CONCEPTOS");

    private final String tipo;
    private final String nombreCorto;
    private final String[] keywords;

    TipologiaNotificacion(String tipo, String nombreCorto, String... keywords) {
        this.tipo = tipo;
        this.nombreCorto = nombreCorto;
        this.keywords = keywords;
    }

    public String getTipo() {
        return tipo;
    }

    public String getNombreCorto() {
        return nombreCorto;
    }

    public static TipologiaNotificacion clasificar(String titulo) {
        if (titulo == null || titulo.trim().isEmpty()) {
            return OTROS_CONCEPTOS;
        }
        String tituloNormalizado = removeAccents(titulo.toUpperCase());
        
        for (TipologiaNotificacion tipologia : values()) {
            if (tipologia == OTROS_CONCEPTOS) continue;
            for (String keyword : tipologia.keywords) {
                if (tituloNormalizado.contains(keyword)) {
                    return tipologia;
                }
            }
        }
        return OTROS_CONCEPTOS;
    }

    private static String removeAccents(String text) {
        return Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
    }
}
