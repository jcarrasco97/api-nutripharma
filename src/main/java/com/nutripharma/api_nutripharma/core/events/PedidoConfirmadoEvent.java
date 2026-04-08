package com.nutripharma.api_nutripharma.core.events;

public record PedidoConfirmadoEvent(
        Long pedidoId,
        String emailFarmacia,
        String nombreFarmacia,
        double totalPedido
) {}