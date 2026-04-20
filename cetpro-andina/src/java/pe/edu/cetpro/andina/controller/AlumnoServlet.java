package pe.edu.cetpro.andina.controller;

import pe.edu.cetpro.andina.entity.Alumno;
import pe.edu.cetpro.andina.service.AlumnoService;
import pe.edu.cetpro.andina.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * REST API alumnos.
 * GET  /api/alumnos          -> lista con estado
 * GET  /api/alumnos/{id}     -> detalle
 * GET  /api/alumnos?dni=X    -> buscar DNI
 * GET  /api/alumnos?q=texto  -> buscar nombre
 * POST /api/alumnos          -> crear
 * PUT  /api/alumnos/{id}     -> actualizar
 * DELETE /api/alumnos/{id}   -> baja lógica
 */
@WebServlet("/api/alumnos/*")
public class AlumnoServlet extends HttpServlet {

    private final AlumnoService service = new AlumnoService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        configurar(resp);
        String path = req.getPathInfo();
        try {
            if (path == null || path.equals("/")) {
                String q = req.getParameter("q");
                String dni = req.getParameter("dni");
                List<Alumno> list;
                if (dni != null) {
                    Optional<Alumno> a = service.buscarPorDni(dni);
                    resp.getWriter().write(a.map(JsonUtil::alumnoToJson)
                            .orElse(JsonUtil.error("No encontrado")));
                    if (a.isEmpty()) resp.setStatus(404);
                    return;
                } else if (q != null) {
                    list = service.buscarPorNombre(q);
                } else {
                    list = service.listarConEstado();
                }
                resp.getWriter().write(JsonUtil.listToJson(list, JsonUtil::alumnoToJson));
            } else {
                Integer id = Integer.parseInt(path.substring(1));
                Optional<Alumno> a = service.buscar(id);
                if (a.isPresent()) {
                    resp.getWriter().write(JsonUtil.alumnoToJson(a.get()));
                } else {
                    resp.setStatus(404);
                    resp.getWriter().write(JsonUtil.error("Alumno no encontrado"));
                }
            }
        } catch (Exception e) {
            resp.setStatus(500);
            resp.getWriter().write(JsonUtil.error(e.getMessage()));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        configurar(resp);
        try {
            String body = leerBody(req);
            Alumno a = parsear(body);
            Alumno creado = service.registrar(a);
            resp.setStatus(201);
            resp.getWriter().write(JsonUtil.alumnoToJson(creado));
        } catch (IllegalArgumentException e) {
            resp.setStatus(400);
            resp.getWriter().write(JsonUtil.error(e.getMessage()));
        } catch (Exception e) {
            resp.setStatus(500);
            resp.getWriter().write(JsonUtil.error(e.getMessage()));
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        configurar(resp);
        try {
            Integer id = Integer.parseInt(req.getPathInfo().substring(1));
            String body = leerBody(req);
            Alumno a = parsear(body);
            a.setIdAlumno(id);
            boolean ok = service.actualizar(a);
            resp.getWriter().write(ok ? JsonUtil.ok("Actualizado") : JsonUtil.error("No modificado"));
        } catch (Exception e) {
            resp.setStatus(500);
            resp.getWriter().write(JsonUtil.error(e.getMessage()));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        configurar(resp);
        try {
            Integer id = Integer.parseInt(req.getPathInfo().substring(1));
            boolean ok = service.eliminar(id);
            resp.getWriter().write(ok ? JsonUtil.ok("Eliminado") : JsonUtil.error("No encontrado"));
        } catch (Exception e) {
            resp.setStatus(500);
            resp.getWriter().write(JsonUtil.error(e.getMessage()));
        }
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) {
        configurar(resp);
    }

    private void configurar(HttpServletResponse resp) {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
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

    /** Parser JSON simple (POJO plano). Reemplazar por Jackson/Gson en prod. */
    private Alumno parsear(String json) {
        Alumno a = new Alumno();
        a.setDni(extraer(json, "dni"));
        a.setTipoDocumento(extraer(json, "tipoDocumento"));
        a.setNombres(extraer(json, "nombres"));
        a.setApellidoPaterno(extraer(json, "apellidoPaterno"));
        a.setApellidoMaterno(extraer(json, "apellidoMaterno"));
        a.setGenero(extraer(json, "genero"));
        a.setTelefono(extraer(json, "telefono"));
        a.setEmail(extraer(json, "email"));
        a.setDireccion(extraer(json, "direccion"));
        String fn = extraer(json, "fechaNacimiento");
        if (fn != null && !fn.isBlank()) a.setFechaNacimiento(LocalDate.parse(fn));
        return a;
    }

    static String extraer(String json, String key) {
        String pat = "\"" + key + "\"";
        int i = json.indexOf(pat);
        if (i < 0) return null;
        int c = json.indexOf(':', i);
        if (c < 0) return null;
        int j = c + 1;
        while (j < json.length() && Character.isWhitespace(json.charAt(j))) j++;
        if (j >= json.length()) return null;
        if (json.charAt(j) == '"') {
            int end = json.indexOf('"', j + 1);
            while (end > 0 && json.charAt(end - 1) == '\\') end = json.indexOf('"', end + 1);
            return end < 0 ? null : json.substring(j + 1, end);
        } else {
            int end = j;
            while (end < json.length() && ",}]".indexOf(json.charAt(end)) < 0) end++;
            String v = json.substring(j, end).trim();
            return v.equals("null") ? null : v;
        }
    }
}
