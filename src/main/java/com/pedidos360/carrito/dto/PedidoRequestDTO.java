package com.pedidos360.carrito.dto;

import java.util.List;

/**
 * DTO para recibir el payload de solicitud de registro de pedido.
 */
public class PedidoRequestDTO {
    private String clienteEmail;
    private String direccionEnvio;
    private List<ItemPedidoDTO> items;

    public PedidoRequestDTO() {}

    public PedidoRequestDTO(String clienteEmail, String direccionEnvio, List<ItemPedidoDTO> items) {
        this.clienteEmail = clienteEmail;
        this.direccionEnvio = direccionEnvio;
        this.items = items;
    }

    public String getClienteEmail() {
        return clienteEmail;
    }

    public void setClienteEmail(String clienteEmail) {
        this.clienteEmail = clienteEmail;
    }

    public String getDireccionEnvio() {
        return direccionEnvio;
    }

    public void setDireccionEnvio(String direccionEnvio) {
        this.direccionEnvio = direccionEnvio;
    }

    public List<ItemPedidoDTO> getItems() {
        return items;
    }

    public void setItems(List<ItemPedidoDTO> items) {
        this.items = items;
    }
}
