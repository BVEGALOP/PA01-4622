package pe.edu.isil.pedidos.web;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import pe.edu.isil.pedidos.service.PedidoException;
import pe.edu.isil.pedidos.service.PedidoService;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

@WebServlet(value = "/pedidos", loadOnStartup = 1)
public class PedidoServlet extends HttpServlet {

    @EJB
    private PedidoService pedidoService;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("productos", pedidoService.listarProductos());
        request.setAttribute("pedidos", pedidoService.listarPedidos());
        request.getRequestDispatcher("/WEB-INF/views/pedidos.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String cliente = request.getParameter("cliente");
            Long productoId = Long.valueOf(request.getParameter("productoId"));
            int cantidad = Integer.parseInt(request.getParameter("cantidad"));

            pedidoService.registrarPedido(cliente, productoId, cantidad);
            response.sendRedirect(request.getContextPath() + "/pedidos");
        } catch (PedidoException e) {
            request.setAttribute("error", e.getMessage());
            doGet(request, response);
        } catch (Exception e) {
            request.setAttribute("error", "Error inesperado: " + e.getMessage());
            doGet(request, response);
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String, String> params = parseUrlEncoded(request.getInputStream());
        try {
            Long id = Long.valueOf(params.get("id"));
            String cliente = params.get("cliente");
            Long productoId = Long.valueOf(params.get("productoId"));
            int cantidad = Integer.parseInt(params.get("cantidad"));

            pedidoService.actualizarPedido(id, cliente, productoId, cantidad);
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (PedidoException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("text/plain");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(e.getMessage());
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("text/plain");
            response.getWriter().write("Error inesperado en el servidor");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String idParam = request.getParameter("id");
            if (idParam == null || idParam.isEmpty()) {
                throw new PedidoException("ID no proporcionado");
            }
            Long id = Long.valueOf(idParam);
            pedidoService.eliminarPedido(id);
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (PedidoException e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND); // Recurso no encontrado (404)
            response.setContentType("text/plain");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(e.getMessage());
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("text/plain");
            response.getWriter().write("Error inesperado en el servidor");
        }
    }

    // Utilidad para extraer parámetros del body (application/x-www-form-urlencoded) en peticiones PUT
    private Map<String, String> parseUrlEncoded(InputStream is) throws IOException {
        Map<String, String> map = new HashMap<>();
        try (Scanner scanner = new Scanner(is, StandardCharsets.UTF_8.name())) {
            String s = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
            for (String param : s.split("&")) {
                String[] pair = param.split("=");
                if (pair.length == 2) {
                    map.put(URLDecoder.decode(pair[0], StandardCharsets.UTF_8.name()),
                            URLDecoder.decode(pair[1], StandardCharsets.UTF_8.name()));
                }
            }
        }
        return map;
    }
}
