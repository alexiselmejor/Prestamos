package edu.universidad.presta.service;

import edu.universidad.presta.ai.OpenAiChatClient;
import edu.universidad.presta.dao.AsistenteDao;
import edu.universidad.presta.dao.EquipoDao;
import edu.universidad.presta.dao.MantenimientoDao;
import edu.universidad.presta.dao.PrestamoDao;
import edu.universidad.presta.dao.SolicitudDao;
import edu.universidad.presta.model.AlertaMantenimiento;
import edu.universidad.presta.model.Equipo;
import edu.universidad.presta.model.MensajeIA;
import edu.universidad.presta.model.Prestamo;
import edu.universidad.presta.model.Solicitud;
import edu.universidad.presta.model.Usuario;
import edu.universidad.presta.util.Fechas;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Orquesta el asistente: contexto SQL autorizado + proveedor generativo opcional + modo de respaldo. */
public final class AsistenteIAService {
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String INSTRUCCIONES = "Eres el Asistente Inteligente OTI de la Universidad Nacional del Centro del Perú. "
            + "Responde siempre en español claro, amable y breve. Ayuda con el catálogo, solicitudes, préstamos, devoluciones, "
            + "notificaciones y mantenimiento. Usa exclusivamente los datos del contexto proporcionado para afirmar disponibilidad, "
            + "estados, fechas o cantidades. Si un dato no aparece, dilo con honestidad. Nunca inventes equipos ni confirmes reservas, "
            + "aprobaciones, préstamos, devoluciones o cambios que no se hayan ejecutado. No solicites contraseñas, tokens ni datos "
            + "personales sensibles. Para crear una solicitud, guía al usuario al formulario del módulo Equipos; la IA no aprueba "
            + "ni registra operaciones. Respeta el rol indicado y no reveles datos de otros usuarios. Si te piden algo ajeno al sistema, "
            + "responde brevemente y vuelve a ofrecer ayuda sobre PrestaEquipos.";

    private AsistenteIAService() {}

    public static Resultado responder(Usuario usuario, String mensaje, Long conversacionSolicitada) {
        String pregunta = mensaje == null ? "" : mensaje.trim();
        if (pregunta.isEmpty()) throw new IllegalArgumentException("Escribe una pregunta para el asistente.");
        if (pregunta.length() > 1200) pregunta = pregunta.substring(0, 1200);
        pregunta = protegerDatosSensibles(pregunta);

        long conversacionId = conversacionSolicitada != null && conversacionSolicitada > 0
                && AsistenteDao.pertenece(conversacionSolicitada, usuario.getId())
                ? conversacionSolicitada
                : AsistenteDao.crearConversacion(usuario.getId(), pregunta);
        AsistenteDao.guardarMensaje(conversacionId, "user", pregunta);

        String contexto = construirContexto(usuario, pregunta);
        List<Map<String, String>> mensajes = new ArrayList<>();
        mensajes.add(mensaje("developer", INSTRUCCIONES + "\n\nCONTEXTO ACTUAL AUTORIZADO:\n" + contexto));
        for (MensajeIA anterior : AsistenteDao.historial(conversacionId, 8)) {
            if (!"user".equals(anterior.getRol()) || !pregunta.equals(anterior.getContenido())) {
                mensajes.add(mensaje(anterior.getRol(), anterior.getContenido()));
            }
        }
        mensajes.add(mensaje("user", pregunta));

        String respuesta = OpenAiChatClient.responder(mensajes);
        boolean generativa = respuesta != null && !respuesta.isBlank();
        if (!generativa) respuesta = respuestaLocal(usuario, pregunta);
        if (respuesta == null || respuesta.isBlank()) {
            respuesta = "Puedo ayudarte a buscar equipos, preparar una solicitud, revisar tus devoluciones y explicar las notificaciones. ¿Qué necesitas hacer?";
        }
        AsistenteDao.guardarMensaje(conversacionId, "assistant", respuesta);
        return new Resultado(conversacionId, respuesta, generativa ? "ia" : "integrado");
    }

    private static Map<String, String> mensaje(String rol, String contenido) {
        Map<String, String> m = new LinkedHashMap<>(); m.put("role", rol); m.put("content", contenido); return m;
    }

    private static String construirContexto(Usuario u, String pregunta) {
        StringBuilder c = new StringBuilder();
        c.append("Usuario autenticado (sin nombre ni correo enviados al proveedor). Rol: ")
                .append(u.isAdmin() ? "administrador" : u.isEncargado() ? "encargado OTI" : "solicitante").append(".\n");
        c.append("Fecha local: ").append(Fechas.ahora().format(FECHA)).append(".\n");
        c.append("El enlace para iniciar una solicitud es /equipos/listar.\n");
        String lower = pregunta.toLowerCase(Locale.ROOT);
        boolean consultaEquipo = contiene(lower, "equipo", "laptop", "computadora", "proyector", "impresora", "disponible", "buscar", "recomienda");
        if (consultaEquipo) {
            List<Equipo> equipos = EquipoDao.buscarInteligente(pregunta, !u.isEncargado(), 8);
            c.append("Equipos que coinciden con la consulta (máximo 8):\n");
            if (equipos.isEmpty()) c.append("No se encontraron coincidencias.\n");
            for (Equipo e : equipos) c.append("- ").append(e.getNombre()).append(" | código ").append(e.getCodigoInventario())
                    .append(" | estado ").append(e.getEstado()).append(" | categoría ")
                    .append(e.getCategoriaNombre() == null ? "General" : e.getCategoriaNombre()).append(" | ubicación ")
                    .append(e.getUbicacion() == null ? "No indicada" : e.getUbicacion()).append("\n");
        }
        if (!u.isEncargado()) {
            List<Prestamo> prestamos = PrestamoDao.activosDeUsuario(u.getId());
            c.append("Préstamos activos del usuario actual:\n");
            if (prestamos.isEmpty()) c.append("No registra préstamos activos.\n");
            for (Prestamo p : prestamos) c.append("- ").append(p.getEquipoNombre()).append(" (código ")
                    .append(p.getCodigoInventario()).append("), devolución esperada: ")
                    .append(p.getFechaDevolucionEsperada() == null ? "sin fecha" : p.getFechaDevolucionEsperada().format(FECHA))
                    .append(p.isVencido() ? " [VENCIDO]" : "").append(".\n");
            List<Solicitud> solicitudes = SolicitudDao.deUsuario(u.getId());
            c.append("Solicitudes del usuario actual (máximo 8):\n");
            for (int i = 0; i < Math.min(8, solicitudes.size()); i++) {
                Solicitud s = solicitudes.get(i);
                c.append("- ").append(s.getEquipoSolicitado()).append(" | estado ").append(s.getEstado())
                        .append(" | fecha de préstamo ").append(s.getFechaPrestamo() == null ? "sin fecha" : s.getFechaPrestamo().format(FECHA))
                        .append(" | observaciones OTI: ").append(s.getObservaciones() == null || s.getObservaciones().isBlank() ? "sin observaciones" : protegerDatosSensibles(s.getObservaciones()))
                        .append(".\n");
            }
            if (solicitudes.isEmpty()) c.append("No registra solicitudes.\n");
        } else {
            long pendientes = SolicitudDao.contarPendientes();
            long vencidos = PrestamoDao.contarVencidos();
            c.append("Resumen de gestión (sin datos personales): solicitudes pendientes: ").append(pendientes)
                    .append("; préstamos vencidos: ").append(vencidos).append(".\n");
            if (contiene(lower, "mantenimiento", "falla", "preventivo", "revisión")) {
                List<AlertaMantenimiento> alertas = MantenimientoDao.alertasPreventivas();
                c.append("Indicadores preventivos de mantenimiento:\n");
                if (alertas.isEmpty()) c.append("No hay alertas calculadas con los datos actuales.\n");
                for (int i = 0; i < Math.min(5, alertas.size()); i++) {
                    AlertaMantenimiento a = alertas.get(i);
                    c.append("- ").append(a.getEquipoNombre()).append(" ( ").append(a.getCodigoInventario()).append("), prioridad ")
                            .append(a.getPrioridad()).append(", puntaje ").append(a.getPuntaje()).append("/100: ").append(a.getMotivo()).append("\n");
                }
                c.append("Estos indicadores son reglas preventivas y no un diagnóstico técnico ni un modelo entrenado.\n");
            }
        }
        if (contiene(lower, "recomienda", "recomendación", "recomendaciones", "más solicitado", "popular")) {
            c.append("Equipos disponibles recomendados por demanda histórica:\n");
            for (Equipo e : EquipoDao.recomendados(5)) c.append("- ").append(e.getNombre()).append(" ( ").append(e.getCodigoInventario()).append(" )\n");
        }
        return c.toString();
    }

    private static String respuestaLocal(Usuario u, String pregunta) {
        String q = pregunta.toLowerCase(Locale.ROOT);
        if (contiene(q, "notificación", "notificaciones", "aviso", "avisos", "observada", "rechazada", "aprobada")) {
            if (u.isEncargado()) return "Las notificaciones de gestión resumen solicitudes pendientes, préstamos próximos a vencer y devoluciones vencidas. "
                    + "Abre la campana de notificaciones o el módulo Vencidos para revisar cada caso.";
            List<Solicitud> solicitudes = SolicitudDao.deUsuario(u.getId());
            if (solicitudes.isEmpty()) return "No encuentro solicitudes asociadas a tu cuenta. Cuando envíes una, podrás seguir su estado desde Mis Solicitudes.";
            StringBuilder r = new StringBuilder("Puedo ayudarte a interpretar tus avisos. Tus solicitudes recientes muestran:\n");
            for (int i = 0; i < Math.min(5, solicitudes.size()); i++) {
                Solicitud s = solicitudes.get(i);
                r.append("• ").append(s.getEquipoSolicitado()).append(": ").append(s.getEstado()).append(". ");
                if ("pendiente".equalsIgnoreCase(s.getEstado())) r.append("Está esperando revisión de la OTI.");
                else if ("aprobada".equalsIgnoreCase(s.getEstado())) r.append("Fue aprobada; coordina la entrega con la OTI.");
                else if ("rechazada".equalsIgnoreCase(s.getEstado())) r.append("La OTI la rechazó. ");
                if (s.getObservaciones() != null && !s.getObservaciones().isBlank()) r.append("Observación de la OTI: ").append(protegerDatosSensibles(s.getObservaciones()));
                else if ("rechazada".equalsIgnoreCase(s.getEstado())) r.append("No hay observación adicional registrada; consulta con la OTI.");
                r.append("\n");
            }
            return r.toString();
        }
        if (contiene(q, "solicitud", "solicitar", "pedir", "necesito un equipo")) {
            return "Te guío para solicitar un equipo:\n1. Abre **Equipos** y entra al catálogo.\n"
                    + "2. Busca el equipo o una alternativa disponible.\n3. Pulsa **Solicitar**.\n"
                    + "4. Indica la oficina, fecha de préstamo, fecha de devolución y motivo.\n"
                    + "5. Revisa los datos y envía la solicitud.\n\nLa solicitud quedará pendiente de revisión por la OTI. "
                    + "No se aprueba automáticamente. Abre el módulo: /equipos/listar";
        }
        if (contiene(q, "devolución", "devolver", "vencido", "vence", "mis equipos", "préstamo")) {
            if (u.isEncargado()) {
                return "Puedes revisar los préstamos pendientes y vencidos desde el módulo **Vencidos**. El panel muestra los equipos, "
                        + "responsables y fechas de devolución. Para el seguimiento general, utiliza también Reportes.";
            }
            List<Prestamo> activos = PrestamoDao.activosDeUsuario(u.getId());
            if (activos.isEmpty()) return "No encuentro préstamos activos asociados a tu cuenta. Puedes revisar el historial en **Mis Equipos**.";
            StringBuilder r = new StringBuilder("Estos son tus préstamos activos:\n");
            for (Prestamo p : activos) r.append("• ").append(p.getEquipoNombre()).append(" — devolución: ")
                    .append(p.getFechaDevolucionEsperada() == null ? "sin fecha registrada" : p.getFechaDevolucionEsperada().format(FECHA))
                    .append(p.isVencido() ? " (VENCIDO)" : "").append("\n");
            return r.append("Puedes consultar el detalle en **Mis Equipos**.").toString();
        }
        if (contiene(q, "mantenimiento", "falla", "preventivo", "revisión")) {
            if (!u.isEncargado()) return "El seguimiento de mantenimiento está disponible para el personal encargado de la OTI.";
            return "Puedes revisar las alertas preventivas y registrar intervenciones en **Mantenimiento inteligente**. "
                    + "Los indicadores se calculan a partir del uso y del historial registrado; deben validarse mediante una revisión técnica. "
                    + "Abre el módulo: /ia/mantenimiento";
        }
        if (contiene(q, "recomienda", "recomendación", "recomendaciones")) {
            List<Equipo> equipos = EquipoDao.recomendados(5);
            if (equipos.isEmpty()) return "Por el momento no hay equipos disponibles para recomendar.";
            StringBuilder r = new StringBuilder("Puedo recomendarte estos equipos disponibles, priorizando los de mayor demanda histórica:\n");
            for (Equipo e : equipos) r.append("• ").append(e.getNombre()).append(" (código ").append(e.getCodigoInventario()).append(")\n");
            return r.toString();
        }
        if (contiene(q, "hola", "buenos días", "buenas tardes", "buenas noches")) {
            return "¡Hola, " + u.getNombre().split(" ")[0] + "! Soy el Asistente Inteligente OTI. Puedo ayudarte a buscar equipos, "
                    + "preparar solicitudes, revisar devoluciones y entender las notificaciones. ¿Qué necesitas?";
        }
        return "Puedo ayudarte con estas tareas:\n• Buscar equipos disponibles por nombre, categoría o código.\n"
                + "• Orientarte para registrar una solicitud.\n• Consultar tus préstamos y fechas de devolución.\n"
                + "• Recomendar equipos disponibles.\n• Explicar las notificaciones.\n"
                + (u.isEncargado() ? "• Revisar alertas de mantenimiento preventivo.\n" : "")
                + "Escribe, por ejemplo: «Necesito una laptop disponible» o «¿Cuándo debo devolver mi equipo?»";
    }

    /** Oculta credenciales, correos y números de identificación antes de guardar o enviar el texto al proveedor. */
    private static String protegerDatosSensibles(String texto) {
        String limpio = texto.replaceAll("(?i)((?:mi\\s+)?(?:contraseña|password|clave)\\s*(?:es|:)\\s*)[^\\s,;]+", "$1[oculto]");
        limpio = limpio.replaceAll("(?i)\\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}\\b", "[correo oculto]");
        limpio = limpio.replaceAll("(?i)((?:DNI|documento(?:\\s+de\\s+identidad)?)\\s*(?:es|:)?\\s*)\\d{8}\\b", "$1[DNI oculto]");
        limpio = limpio.replaceAll("(?i)((?:celular|teléfono|telefono)\\s*(?:es|:)?\\s*)9\\d{8}\\b", "$1[celular oculto]");
        return limpio;
    }

    private static boolean contiene(String texto, String... palabras) {
        for (String palabra : palabras) if (texto.contains(palabra)) return true;
        return false;
    }

    public static final class Resultado {
        private final long conversacionId;
        private final String respuesta;
        private final String modo;
        public Resultado(long conversacionId, String respuesta, String modo) {
            this.conversacionId = conversacionId; this.respuesta = respuesta; this.modo = modo;
        }
        public long getConversacionId() { return conversacionId; }
        public String getRespuesta() { return respuesta; }
        public String getModo() { return modo; }
    }
}
