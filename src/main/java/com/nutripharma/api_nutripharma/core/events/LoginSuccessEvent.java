package com.nutripharma.api_nutripharma.core.events;

public record LoginSuccessEvent(
        String usuarioEmail,
        String ipAddress,
        String userAgent
) {}