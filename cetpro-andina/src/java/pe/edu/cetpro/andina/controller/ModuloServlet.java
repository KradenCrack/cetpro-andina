package pe.edu.cetpro.andina.controller;

import pe.edu.cetpro.andina.entity.Modulo;
import pe.edu.cetpro.andina.entity.UnidadDidactica;
import pe.edu.cetpro.andina.service.ModuloService;
import pe.edu.cetpro.andina.util.JsonUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@WebServlet("/api/modulos/*")
public class ModuloServlet extends HttpServlet {

    private final ModuloService service = new ModuloService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");
        String path = req.getPathInfo();
        try {
            if (path == null || path.equals("/")) {
                List<Modulo> list = service.listar();
                resp.getWriter().write(JsonUtil.listToJson(list, JsonUtil::moduloToJson));
            } else if (path.endsWith("/unidades")) {
                Integer id = Integer.parseInt(path.split("/")[1]);
                List<UnidadDidactica> u = service.listarUnidades(id);
                resp.getWriter().write(JsonUtil.listToJson(u, JsonUtil::unidadToJson));
            } else {
                Integer id = Integer.parseInt(path.substring(1));
                Optional<Modulo> m = service.buscarConUnidades(id);
                if (m.isPresent()) resp.getWriter().write(JsonUtil.moduloToJson(m.get()));
                else { resp.setStatus(404); resp.getWriter().write(JsonUtil.error("No encontrado")); }
            }
        } catch (Exception e) {
            resp.setStatus(500);
            resp.getWriter().write(JsonUtil.error(e.getMessage()));
        }
    }
}
