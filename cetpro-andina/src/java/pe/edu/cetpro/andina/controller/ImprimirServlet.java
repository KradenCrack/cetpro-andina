package pe.edu.cetpro.andina.controller;

import pe.edu.cetpro.andina.dao.MatriculaDAO;
import pe.edu.cetpro.andina.dao.impl.MatriculaDAOImpl;
import pe.edu.cetpro.andina.entity.Certificado;
import pe.edu.cetpro.andina.entity.UnidadDidactica;
import pe.edu.cetpro.andina.service.CertificadoService;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * GET /api/certificados/{id}/imprimir
 * Devuelve HTML listo para imprimir (Ctrl+P o window.print()).
 */
@WebServlet("/api/certificados/imprimir/*")
public class ImprimirServlet extends HttpServlet {

    private final CertificadoService service = new CertificadoService();
    private final MatriculaDAO matriculaDao = new MatriculaDAOImpl();
    private static final DateTimeFormatter FMT =
        DateTimeFormatter.ofPattern("dd 'de' MMMM 'del' yyyy", new Locale("es", "PE"));

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html;charset=UTF-8");
        String path = req.getPathInfo();
        if (path == null || path.length() < 2) {
            resp.setStatus(400);
            resp.getWriter().write("Falta id");
            return;
        }
        try {
            Integer id = Integer.parseInt(path.substring(1));
            Optional<Certificado> opt = service.buscar(id);
            if (opt.isEmpty()) { resp.setStatus(404); resp.getWriter().write("No encontrado"); return; }
            Certificado c = opt.get();
            PrintWriter out = resp.getWriter();
            if (c.getTipoCertificado() == Certificado.Tipo.MODULAR) {
                renderModular(out, c);
            } else {
                renderCapacitacion(out, c);
            }
        } catch (Exception e) {
            resp.setStatus(500);
            resp.getWriter().write("Error: " + e.getMessage());
        }
    }

    private String head(String titulo) {
        return "<!DOCTYPE html><html lang='es'><head><meta charset='UTF-8'><title>" + titulo + "</title>" +
            "<style>" +
            "@page { size: A4 landscape; margin: 15mm; }" +
            "body { font-family: 'Times New Roman', serif; color: #111; margin: 0; padding: 20px; }" +
            ".cert { border: 6px double #0a4d8c; padding: 30px 50px; max-width: 1100px; margin: 0 auto; }" +
            ".header { text-align: center; margin-bottom: 20px; }" +
            ".header h2 { margin: 0; color: #0a4d8c; font-size: 22px; letter-spacing: 2px; }" +
            ".header .sub { font-size: 14px; margin: 4px 0; }" +
            ".header .rd { font-size: 12px; font-style: italic; }" +
            ".codigo { position: absolute; top: 30px; right: 60px; font-size: 13px; }" +
            ".titulo-cert { text-align: center; font-size: 28px; font-weight: bold; color: #0a4d8c; margin: 25px 0; letter-spacing: 3px; }" +
            ".alumno { text-align: center; font-size: 24px; font-weight: bold; text-transform: uppercase; margin: 20px 0; }" +
            ".modulo { text-align: center; font-size: 18px; font-weight: bold; margin: 15px 0; }" +
            ".cuerpo { text-align: justify; font-size: 15px; line-height: 1.6; margin: 15px 0; }" +
            ".fecha { text-align: right; font-style: italic; margin: 20px 0; }" +
            ".firmas { display: flex; justify-content: space-around; margin-top: 70px; }" +
            ".firma { text-align: center; border-top: 1px solid #000; padding-top: 5px; width: 220px; font-size: 13px; }" +
            "table.ud { width: 100%; border-collapse: collapse; margin: 15px 0; font-size: 12px; }" +
            "table.ud th, table.ud td { border: 1px solid #333; padding: 6px 8px; text-align: left; }" +
            "table.ud th { background: #0a4d8c; color: #fff; }" +
            ".btn-print { position: fixed; top: 15px; right: 15px; padding: 10px 20px; background: #0a4d8c; color: #fff; border: none; border-radius: 6px; cursor: pointer; font-size: 14px; }" +
            "@media print { .btn-print { display: none; } body { padding: 0; } }" +
            "</style></head><body>" +
            "<button class='btn-print' onclick='window.print()'>🖨 Imprimir</button>" +
            "<div class='cert'>" +
            "<div class='header'>" +
            "<h2>CENTRO DE EDUCACIÓN TÉCNICO-PRODUCTIVA PARTICULAR</h2>" +
            "<div class='sub'>&ldquo;ANDINA&rdquo;</div>" +
            "<div class='rd'>R.D.R. N° 0726</div>" +
            "</div>";
    }

    private String foot(Certificado c) {
        String fecha = c.getFechaEmision().format(FMT);
        return "<div class='fecha'>" + c.getLugarEmision() + ", " + fecha + "</div>" +
            "<div class='firmas'>" +
            "<div class='firma'>DIRECTORA CETPRO<br><small>(Firma, post firma y sello)</small></div>" +
            "<div class='firma'>DOCENTE<br><small>(Firma, post firma y sello)</small></div>" +
            "</div></div><script>setTimeout(()=>window.print(), 400);</script></body></html>";
    }

    private void renderCapacitacion(PrintWriter out, Certificado c) {
        out.print(head("Certificado " + c.getCodigoInstitucional()));
        out.print("<div class='codigo'>N° " + c.getCodigoInstitucional() + "</div>");
        out.print("<div class='titulo-cert'>CERTIFICADO DE CAPACITACIÓN</div>");
        out.print("<div class='cuerpo' style='text-align:center;font-size:16px;'>CERTIFICA A:</div>");
        out.print("<div class='alumno'>" + esc(c.getAlumnoNombre()) + "</div>");
        out.print("<div class='cuerpo' style='text-align:center;'>El logro de:</div>");
        out.print("<div class='modulo'>" + esc(c.getModuloNombre()) + "</div>");
        out.print("<div class='cuerpo'>Al aprobar satisfactoriamente el módulo en mérito de haber aprobado " +
            "el curso correspondiente con una duración de <b>" + c.getDuracionHoras() + "</b> horas, " +
            "encontrándose capacitado(a) para desempeñarse en la ocupación del área.</div>");
        out.print("<div class='cuerpo' style='text-align:center;font-size:16px;'>" +
            "<b>Nota Final: " + (c.getNotaFinalTexto() != null ? c.getNotaFinalTexto() : c.getNotaFinal()) +
            "</b></div>");
        out.print(foot(c));
    }

    private void renderModular(PrintWriter out, Certificado c) {
        out.print(head("Certificado " + c.getCodigoInstitucional()));
        out.print("<div class='codigo'>Código: " + c.getCodigoInstitucional() + "</div>");
        out.print("<div class='titulo-cert'>CERTIFICADO MODULAR</div>");
        out.print("<div class='cuerpo' style='text-align:center;'>Otorgado a:</div>");
        out.print("<div class='alumno'>" + esc(c.getAlumnoNombre()) + "</div>");
        out.print("<div class='cuerpo'>Por haber aprobado satisfactoriamente el módulo formativo:</div>");
        out.print("<div class='modulo'>" + esc(c.getModuloNombre()) + "</div>");
        out.print("<div class='cuerpo'>Correspondiente al programa de estudios: <b>" +
            esc(c.getProgramaNombre()) + "</b><br>");
        if (c.getFechaInicioCurso() != null && c.getFechaFinCurso() != null) {
            out.print("Desarrollado del <b>" + c.getFechaInicioCurso() + "</b> al <b>" +
                c.getFechaFinCurso() + "</b>. ");
        }
        out.print("CICLO FORMATIVO: <b>" + esc(c.getCicloFormativo()) + "</b> &nbsp;&nbsp; MODALIDAD: <b>PRESENCIAL</b>");
        out.print("</div>");

        // Tabla de unidades
        List<UnidadDidactica> unidades = matriculaDao.listarUnidadesConNotas(c.getIdMatricula());
        if (!unidades.isEmpty()) {
            out.print("<table class='ud'><thead><tr>" +
                "<th>Unidad Didáctica</th><th>Créditos</th><th>Horas</th><th>Calificación</th>" +
                "</tr></thead><tbody>");
            for (UnidadDidactica u : unidades) {
                out.print("<tr><td>" + esc(u.getNombre()) + "</td>" +
                    "<td>" + u.getCreditos() + "</td>" +
                    "<td>" + u.getHoras() + "</td>" +
                    "<td>" + (u.getCalificacion() != null ? u.getCalificacion() : "-") + "</td></tr>");
            }
            out.print("</tbody></table>");
        }
        if (c.getPromedioFinal() != null) {
            out.print("<div class='cuerpo' style='text-align:right;'><b>Promedio Final: " +
                c.getPromedioFinal() + "</b></div>");
        }
        out.print(foot(c));
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
