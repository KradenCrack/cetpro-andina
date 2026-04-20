package pe.edu.cetpro.andina.controller;

import pe.edu.cetpro.andina.entity.Certificado;
import pe.edu.cetpro.andina.entity.UnidadDidactica;
import pe.edu.cetpro.andina.service.CertificadoService;
import pe.edu.cetpro.andina.util.JsonUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * REST API certificados.
 *  GET    /api/certificados
 *  GET    /api/certificados?tipo=MODULAR&anio=2024&entregado=false
 *  GET    /api/certificados/{id}
 *  GET    /api/certificados/alumno/{idAlumno}
 *  POST   /api/certificados/modular
 *  POST   /api/certificados/capacitacion
 *  PATCH  /api/certificados/{id}/impreso      body: {"impreso":true}
 *  PATCH  /api/certificados/{id}/entregado    body: {"entregado":true,"firmaReceptor":"..."}
 *  DELETE /api/certificados/{id}   (anulación)
 */
@WebServlet("/api/certificados/*")
public class CertificadoServlet extends HttpServlet {

    private final CertificadoService service = new CertificadoService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        cfg(resp);
        String path = req.getPathInfo();
        try {
            if (path == null || path.equals("/")) {
                String tipo = req.getParameter("tipo");
                String anio = req.getParameter("anio");
                String ent = req.getParameter("entregado");
                List<Certificado> list = service.filtrar(
                        tipo,
                        (anio == null || anio.isBlank()) ? null : Integer.valueOf(anio),
                        (ent == null || ent.isBlank()) ? null : Boolean.valueOf(ent));
                resp.getWriter().write(JsonUtil.listToJson(list, JsonUtil::certificadoToJson));
            } else if (path.startsWith("/alumno/")) {
                Integer id = Integer.parseInt(path.substring("/alumno/".length()));
                List<Certificado> list = service.listarPorAlumno(id);
                resp.getWriter().write(JsonUtil.listToJson(list, JsonUtil::certificadoToJson));
            } else {
                Integer id = Integer.parseInt(path.substring(1));
                Optional<Certificado> c = service.buscar(id);
                if (c.isPresent()) resp.getWriter().write(JsonUtil.certificadoToJson(c.get()));
                else { resp.setStatus(404); resp.getWriter().write(JsonUtil.error("No encontrado")); }
            }
        } catch (Exception e) {
            resp.setStatus(500);
            resp.getWriter().write(JsonUtil.error(e.getMessage()));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        cfg(resp);
        String path = req.getPathInfo();
        try {
            String body = leerBody(req);
            CertificadoService.EmisionRequest r = parsearRequest(body);
            Certificado c;
            if ("/modular".equals(path)) {
                c = service.emitirModular(r);
            } else if ("/capacitacion".equals(path)) {
                c = service.emitirCapacitacion(r);
            } else {
                resp.setStatus(404);
                resp.getWriter().write(JsonUtil.error("Endpoint no soportado"));
                return;
            }
            resp.setStatus(201);
            resp.getWriter().write(JsonUtil.certificadoToJson(c));
        } catch (IllegalArgumentException e) {
            resp.setStatus(400);
            resp.getWriter().write(JsonUtil.error(e.getMessage()));
        } catch (Exception e) {
            resp.setStatus(500);
            resp.getWriter().write(JsonUtil.error(e.getMessage()));
        }
    }

    /** PATCH se maneja aquí (Tomcat base no tiene doPatch nativo en algunas versiones). */
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if ("PATCH".equalsIgnoreCase(req.getMethod())) {
            doPatch(req, resp);
        } else {
            try { super.service(req, resp); }
            catch (Exception e) {
                resp.setStatus(500);
                resp.getWriter().write(JsonUtil.error(e.getMessage()));
            }
        }
    }

    private void doPatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        cfg(resp);
        String path = req.getPathInfo(); // /{id}/impreso  |  /{id}/entregado
        try {
            String[] parts = path.split("/");
            Integer id = Integer.parseInt(parts[1]);
            String accion = parts[2];
            String body = leerBody(req);
            boolean ok;
            if ("impreso".equals(accion)) {
                boolean v = "true".equalsIgnoreCase(AlumnoServlet.extraer(body, "impreso"));
                ok = service.marcarImpreso(id, v);
            } else if ("entregado".equals(accion)) {
                boolean v = "true".equalsIgnoreCase(AlumnoServlet.extraer(body, "entregado"));
                String firma = AlumnoServlet.extraer(body, "firmaReceptor");
                ok = service.marcarEntregado(id, v, firma);
            } else {
                resp.setStatus(400);
                resp.getWriter().write(JsonUtil.error("Acción inválida"));
                return;
            }
            resp.getWriter().write(ok ? JsonUtil.ok("Actualizado") : JsonUtil.error("No actualizado"));
        } catch (IllegalArgumentException e) {
            resp.setStatus(400);
            resp.getWriter().write(JsonUtil.error(e.getMessage()));
        } catch (Exception e) {
            resp.setStatus(500);
            resp.getWriter().write(JsonUtil.error(e.getMessage()));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        cfg(resp);
        try {
            Integer id = Integer.parseInt(req.getPathInfo().substring(1));
            boolean ok = service.anular(id);
            resp.getWriter().write(ok ? JsonUtil.ok("Anulado") : JsonUtil.error("No encontrado"));
        } catch (Exception e) {
            resp.setStatus(500);
            resp.getWriter().write(JsonUtil.error(e.getMessage()));
        }
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) { cfg(resp); }

    private void cfg(HttpServletResponse resp) {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,PATCH,DELETE,OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
    }

    private String leerBody(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader r = req.getReader()) {
            String l;
            while ((l = r.readLine()) != null) sb.append(l);
        }
        return sb.toString();
    }

    private CertificadoService.EmisionRequest parsearRequest(String json) {
        CertificadoService.EmisionRequest r = new CertificadoService.EmisionRequest();
        String s;
        s = AlumnoServlet.extraer(json, "idAlumno"); if (s != null) r.idAlumno = Integer.valueOf(s);
        s = AlumnoServlet.extraer(json, "idModulo"); if (s != null) r.idModulo = Integer.valueOf(s);
        s = AlumnoServlet.extraer(json, "fechaEmision"); if (s != null) r.fechaEmision = LocalDate.parse(s);
        s = AlumnoServlet.extraer(json, "fechaInicioCurso"); if (s != null && !s.isBlank()) r.fechaInicioCurso = LocalDate.parse(s);
        s = AlumnoServlet.extraer(json, "fechaFinCurso"); if (s != null && !s.isBlank()) r.fechaFinCurso = LocalDate.parse(s);
        r.cicloFormativo = AlumnoServlet.extraer(json, "cicloFormativo");
        r.lugarEmision = AlumnoServlet.extraer(json, "lugarEmision");
        s = AlumnoServlet.extraer(json, "notaFinal"); if (s != null) r.notaFinal = Integer.valueOf(s);
        s = AlumnoServlet.extraer(json, "duracionHoras"); if (s != null) r.duracionHoras = Integer.valueOf(s);
        r.observaciones = AlumnoServlet.extraer(json, "observaciones");
        s = AlumnoServlet.extraer(json, "idUsuarioEmisor"); if (s != null) r.idUsuarioEmisor = Integer.valueOf(s);

        // Notas por unidad: array de {idUnidad, calificacion}
        r.notasPorUnidad = parsearNotas(json);
        return r;
    }

    private List<UnidadDidactica> parsearNotas(String json) {
        List<UnidadDidactica> list = new ArrayList<>();
        int i = json.indexOf("\"notasPorUnidad\"");
        if (i < 0) return list;
        int ini = json.indexOf('[', i);
        int fin = json.indexOf(']', ini);
        if (ini < 0 || fin < 0) return list;
        String arr = json.substring(ini + 1, fin);
        for (String item : arr.split("\\},\\s*\\{")) {
            String clean = item.replace("{", "").replace("}", "");
            UnidadDidactica u = new UnidadDidactica();
            String id = AlumnoServlet.extraer("{" + clean + "}", "idUnidad");
            String cal = AlumnoServlet.extraer("{" + clean + "}", "calificacion");
            if (id != null) u.setIdUnidad(Integer.valueOf(id));
            if (cal != null) u.setCalificacion(Integer.valueOf(cal));
            if (u.getIdUnidad() != null) list.add(u);
        }
        return list;
    }
}
