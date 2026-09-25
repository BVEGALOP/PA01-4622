# Sistema de Pedidos - Versión Avanzada con PUT y DELETE reales

Este proyecto es una extensión del Sistema de Pedidos base (Servlet + JSP + Maven + JPA + H2). Cumple con el **RETO AVANZADO** y permite alcanzar la nota máxima (20/20) al implementar la actualización y eliminación utilizando los verbos HTTP `PUT` y `DELETE` desde el navegador mediante JavaScript `fetch()`.

## Requisitos
- JDK 21
- Maven 3.9+
- Puerto 8081 disponible

## Ejecución
1. Abre una terminal en la raíz del proyecto.
2. Ejecuta el comando:
   ```bash
   mvn clean wildfly:run
   ```
3. Abre el navegador en: [http://localhost:8081/sistema-pedidos/](http://localhost:8081/sistema-pedidos/)
4. Para detener la aplicación, presiona `Ctrl + C` en la terminal.

## Arquitectura y Diagrama
Se mantiene la separación de responsabilidades y la arquitectura en capas:

```mermaid
flowchart TD
    Browser[Navegador Web / JSP]
    Controller[PedidoServlet]
    Service[PedidoService @Stateless]
    JPA[EntityManager / JPA]
    DB[(H2 Database)]

    Browser -- "GET (Ver)" --> Controller
    Browser -- "POST (Crear)" --> Controller
    Browser -- "PUT (Editar) via JS Fetch" --> Controller
    Browser -- "DELETE (Eliminar) via JS Fetch" --> Controller

    Controller -- "Reglas de negocio" --> Service
    Service -- "Transacciones / Persistencia" --> JPA
    JPA -- "SQL" --> DB
`

---
**Nota para el informe:** Recuerda incluir capturas de la herramienta *Network/Red* del navegador evidenciando estas llamadas HTTP, además de las pantallas de la interfaz para cada caso práctico antes y después del cambio.
