package com.pedidos360.carrito.controller;

import com.pedidos360.carrito.dto.ItemPedidoDTO;
import com.pedidos360.carrito.dto.PedidoRequestDTO;
import com.pedidos360.carrito.dto.PedidoResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Controlador REST para la gestión del carrito y procesamiento de pedidos.
 */
@RestController
@RequestMapping("/api/carrito")
public class CarritoController {

    /**
     * Endpoint público de diagnóstico del microservicio
     * GET /api/carrito/status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, String>> getStatus() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "Microservicio de Carrito y Pedidos en ejecución");
        response.put("port", "8081");
        response.put("version", "1.0.0");
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint protegido para registrar pedidos de compra
     * POST /api/carrito/pedidos
     */
    @PostMapping("/pedidos")
    public ResponseEntity<PedidoResponseDTO> registrarPedido(
            @RequestBody(required = false) PedidoRequestDTO request,
            @AuthenticationPrincipal Jwt jwt) {

        // Extraer información del cliente autenticado desde el token JWT de Entra ID si está disponible
        String usuarioAutenticado = (jwt != null) ? jwt.getClaimAsString("preferred_username") : "cliente_anonimo";
        if (usuarioAutenticado == null && jwt != null) {
            usuarioAutenticado = jwt.getSubject();
        }

        // Si no se envía payload, se inicializa una estructura por defecto
        if (request == null) {
            request = new PedidoRequestDTO();
        }

        String emailCliente = (request.getClienteEmail() != null && !request.getClienteEmail().isEmpty())
                ? request.getClienteEmail()
                : usuarioAutenticado;

        String direccion = (request.getDireccionEnvio() != null && !request.getDireccionEnvio().isEmpty())
                ? request.getDireccionEnvio()
                : "Dirección no especificada";

        List<ItemPedidoDTO> items = (request.getItems() != null) ? request.getItems() : new ArrayList<>();

        // Calcular el total a pagar iterando sobre los ítems
        double total = 0.0;
        for (ItemPedidoDTO item : items) {
            if (item.getCantidad() != null && item.getPrecioUnitario() != null) {
                total += item.getCantidad() * item.getPrecioUnitario();
            }
        }

        // Generar identificador único de pedido
        String pedidoId = "PED-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        PedidoResponseDTO response = new PedidoResponseDTO(
                pedidoId,
                "PROCESADO",
                emailCliente,
                direccion,
                total,
                LocalDateTime.now(),
                items
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
