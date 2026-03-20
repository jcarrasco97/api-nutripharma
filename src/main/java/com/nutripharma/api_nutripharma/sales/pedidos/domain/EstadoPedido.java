package com.nutripharma.api_nutripharma.sales.pedidos.domain;

public enum EstadoPedido {
    PENDIENTE_ENVIO,      // Nace así al comprar
    ENVIADO,              // Paco lo ha validado y mandado por mensajería
    LIQUIDADO,            // Ya está pagado
    CANCELADO
}