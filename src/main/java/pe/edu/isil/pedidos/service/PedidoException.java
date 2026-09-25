package pe.edu.isil.pedidos.service;

import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class PedidoException extends RuntimeException {
    public PedidoException(String message) {
        super(message);
    }
}
