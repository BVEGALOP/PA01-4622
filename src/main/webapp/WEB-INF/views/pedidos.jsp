<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Sistema de Pedidos</title>
    <link rel="stylesheet" href="assets/css/app.css">
</head>
<body>
    <div class="container">
        <h1>Sistema de Pedidos</h1>

        <c:if test="${not empty error}">
            <div class="alert alert-error">${error}</div>
        </c:if>
        <div id="js-error" class="alert alert-error" style="display: none;"></div>
        <div id="js-success" class="alert alert-success" style="display: none;"></div>

        <div class="grid">
            <!-- Stock Information -->
            <div class="card">
                <h2>Stock de Productos</h2>
                <table>
                    <thead>
                        <tr>
                            <th>Producto</th>
                            <th>Precio</th>
                            <th>Stock Disponible</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="p" items="${productos}">
                            <tr>
                                <td>${p.nombre}</td>
                                <td>S/ ${p.precio}</td>
                                <td>${p.stock}</td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>

            <!-- Create/Edit Form -->
            <div class="card">
                <h2 id="form-title">Registrar Pedido</h2>

                <!-- Create Form (POST) -->
                <form id="formulario-crear" action="${pageContext.request.contextPath}/pedidos" method="post">
                    <div class="form-group">
                        <label>Cliente:</label>
                        <input type="text" name="cliente" required>
                    </div>
                    <div class="form-group">
                        <label>Producto:</label>
                        <select name="productoId" required>
                            <c:forEach var="p" items="${productos}">
                                <option value="${p.id}">${p.nombre} (Disp: ${p.stock})</option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="form-group">
                        <label>Cantidad:</label>
                        <input type="number" name="cantidad" min="1" required>
                    </div>
                    <button type="submit" class="btn btn-primary">Registrar</button>
                </form>

                <!-- Edit Form (PUT via JS) -->
                <form id="formulario-editar" style="display:none;" onsubmit="enviarEdicion(event)">
                    <input type="hidden" id="edit_id" name="id">
                    <div class="form-group">
                        <label>Cliente:</label>
                        <input type="text" id="edit_cliente" name="cliente" required>
                    </div>
                    <div class="form-group">
                        <label>Producto:</label>
                        <select id="edit_productoId" name="productoId" required>
                            <c:forEach var="p" items="${productos}">
                                <option value="${p.id}">${p.nombre}</option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="form-group">
                        <label>Cantidad:</label>
                        <input type="number" id="edit_cantidad" name="cantidad" min="1" required>
                    </div>
                    <div class="form-actions">
                        <button type="submit" class="btn btn-warning">Actualizar</button>
                        <button type="button" class="btn btn-secondary" onclick="cancelarEdicion()">Cancelar</button>
                    </div>
                </form>
            </div>
        </div>

        <!-- Orders Table -->
        <div class="card mt-2">
            <h2>Lista de Pedidos</h2>
            <table>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Cliente</th>
                        <th>Producto</th>
                        <th>Cantidad</th>
                        <th>Total</th>
                        <th>Acciones</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="pedido" items="${pedidos}">
                        <tr>
                            <td>${pedido.id}</td>
                            <td>${pedido.cliente}</td>
                            <td>${pedido.producto.nombre}</td>
                            <td>${pedido.cantidad}</td>
                            <td>S/ ${pedido.total}</td>
                            <td>
                                <button class="btn btn-warning btn-sm" onclick="cargarEdicion(${pedido.id}, '${pedido.cliente}', ${pedido.producto.id}, ${pedido.cantidad})">Editar</button>
                                <button class="btn btn-danger btn-sm" onclick="eliminarPedido(${pedido.id})">Eliminar</button>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty pedidos}">
                        <tr>
                            <td colspan="6" style="text-align:center;">No hay pedidos registrados.</td>
                        </tr>
                    </c:if>
                </tbody>
            </table>
        </div>
    </div>

    <script>
        function mostrarError(msg) {
            const errDiv = document.getElementById('js-error');
            errDiv.innerText = msg;
            errDiv.style.display = 'block';
            setTimeout(() => errDiv.style.display = 'none', 5000);
        }

        function cargarEdicion(id, cliente, productoId, cantidad) {
            document.getElementById('edit_id').value = id;
            document.getElementById('edit_cliente').value = cliente;
            document.getElementById('edit_productoId').value = productoId;
            document.getElementById('edit_cantidad').value = cantidad;

            document.getElementById('formulario-crear').style.display = 'none';
            document.getElementById('formulario-editar').style.display = 'block';
            document.getElementById('form-title').innerText = 'Editar Pedido #' + id;
            window.scrollTo({ top: 0, behavior: 'smooth' });
        }

        function cancelarEdicion() {
            document.getElementById('formulario-crear').style.display = 'block';
            document.getElementById('formulario-editar').style.display = 'none';
            document.getElementById('form-title').innerText = 'Registrar Pedido';
        }

        function enviarEdicion(event) {
            event.preventDefault();
            const id = document.getElementById('edit_id').value;
            const cliente = document.getElementById('edit_cliente').value;
            const productoId = document.getElementById('edit_productoId').value;
            const cantidad = document.getElementById('edit_cantidad').value;

            fetch('pedidos', {
                method: 'PUT',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: new URLSearchParams({ id, cliente, productoId, cantidad })
            })
            .then(async res => {
                if (res.ok) {
                    window.location.reload();
                } else {
                    const errorMsg = await res.text();
                    mostrarError('Error al actualizar: ' + errorMsg);
                }
            })
            .catch(err => mostrarError('Error de conexión al actualizar'));
        }

        function eliminarPedido(id) {
            if (confirm('¿Seguro que desea eliminar el pedido #' + id + '? \n\nEl stock del producto será repuesto.')) {
                fetch('pedidos?id=' + id, { method: 'DELETE' })
                .then(async res => {
                    if (res.ok) {
                        window.location.reload();
                    } else {
                        const errorMsg = await res.text();
                        mostrarError('Error al eliminar: ' + errorMsg);
                    }
                })
                .catch(err => mostrarError('Error de conexión al eliminar'));
            }
        }
    </script>
</body>
</html>
