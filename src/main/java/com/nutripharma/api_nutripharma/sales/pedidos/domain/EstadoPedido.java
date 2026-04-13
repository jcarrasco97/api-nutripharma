package com.nutripharma.api_nutripharma.sales.pedidos.domain;

public enum EstadoPedido {
    PENDIENTE_ENVIO,      // Nace así al comprar
    ENVIADO,              // Un Admin lo ha validado y mandado por mensajería
    CANCELADO             // Pedido anulado por el Admin
}