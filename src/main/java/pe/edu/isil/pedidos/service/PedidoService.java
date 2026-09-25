package pe.edu.isil.pedidos.service;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import pe.edu.isil.pedidos.domain.Pedido;
import pe.edu.isil.pedidos.domain.Producto;
import java.math.BigDecimal;
import java.util.List;

@Stateless
public class PedidoService {

    @PersistenceContext
    private EntityManager em;

    public List<Producto> listarProductos() {
        return em.createQuery("SELECT p FROM Producto p ORDER BY p.id", Producto.class).getResultList();
    }

    public List<Pedido> listarPedidos() {
        return em.createQuery("SELECT p FROM Pedido p ORDER BY p.id", Pedido.class).getResultList();
    }

    public void registrarPedido(String cliente, Long productoId, int cantidad) {
        if (cantidad <= 0) throw new PedidoException("La cantidad debe ser mayor a 0");
        if (cliente == null || cliente.trim().isEmpty()) throw new PedidoException("El nombre del cliente no puede estar vacío");

        Producto p = em.find(Producto.class, productoId);
        if (p == null) throw new PedidoException("Producto no encontrado");

        p.reducirStock(cantidad);

        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setProducto(p);
        pedido.setCantidad(cantidad);
        pedido.setTotal(p.getPrecio().multiply(new BigDecimal(cantidad)));

        em.persist(pedido);
    }

    public void actualizarPedido(Long id, String cliente, Long productoId, int cantidad) {
        if (cantidad <= 0) throw new PedidoException("La cantidad debe ser mayor a 0");
        if (cliente == null || cliente.trim().isEmpty()) throw new PedidoException("El nombre del cliente no puede estar vacío");

        Pedido pedido = em.find(Pedido.class, id);
        if (pedido == null) throw new PedidoException("Pedido no encontrado. ID: " + id);

        Producto oldProducto = pedido.getProducto();
        int oldCantidad = pedido.getCantidad();

        // Si se cambia el producto
        if (!oldProducto.getId().equals(productoId)) {
            // 1. Devolver el stock al producto anterior
            oldProducto.aumentarStock(oldCantidad);

            // 2. Buscar nuevo producto y reducir stock
            Producto newProducto = em.find(Producto.class, productoId);
            if (newProducto == null) throw new PedidoException("Nuevo producto no encontrado");
            newProducto.reducirStock(cantidad);

            pedido.setProducto(newProducto);
            pedido.setTotal(newProducto.getPrecio().multiply(new BigDecimal(cantidad)));
        } else {
            // Mismo producto, evaluar cambio de cantidad
            int diff = cantidad - oldCantidad;
            if (diff > 0) {
                oldProducto.reducirStock(diff); // Requiere más stock
            } else if (diff < 0) {
                oldProducto.aumentarStock(Math.abs(diff)); // Devuelve stock
            }
            pedido.setTotal(oldProducto.getPrecio().multiply(new BigDecimal(cantidad)));
        }

        pedido.setCliente(cliente);
        pedido.setCantidad(cantidad);
    }

    public void eliminarPedido(Long id) {
        Pedido pedido = em.find(Pedido.class, id);
        if (pedido == null) throw new PedidoException("Pedido no encontrado al intentar eliminar. ID: " + id);

        // Reponer stock
        pedido.getProducto().aumentarStock(pedido.getCantidad());
        em.remove(pedido);
    }
}
