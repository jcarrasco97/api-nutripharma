package com.nutripharma.api_nutripharma.core.events;

import com.nutripharma.api_nutripharma.sales.pedidos.service.FacturaPdfService; // <-- Importar tu nuevo servicio
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificacionEventListener {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final FacturaPdfService facturaPdfService; // Inyectamos el creador de PDFs

    // Traemos la URL desde el application.properties
    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePedidoConfirmado(PedidoConfirmadoEvent event) {
        log.info("Iniciando generación de plantilla, imagen y PDF para el pedido {}...", event.pedidoId());

        try {
            // 1. VARIABLES PARA EL HTML
            Context context = new Context();
            context.setVariable("pedidoId", event.pedidoId());
            context.setVariable("nombreFarmacia", event.nombreFarmacia());
            context.setVariable("totalPedido", event.totalPedido());
            context.setVariable("urlLogin", frontendUrl); // Enlace dinámico seguro

            String htmlBody = templateEngine.process("email-pedido", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("pedidos@nutripharma.es", "NutriPharma");
            helper.setTo(event.emailFarmacia());
            helper.setSubject("Factura y Confirmación de Pedido #" + event.pedidoId());
            helper.setText(htmlBody, true);

            // 2. INCRUSTAR LOGO INLINE (CID)
            // Busca la imagen en src/main/resources/static/logo.png
            ClassPathResource logoImage = new ClassPathResource("static/logo.png");
            helper.addInline("logoNutripharma", logoImage); // El nombre debe coincidir con 'cid:logoNutripharma' en el HTML

            // 3. GENERAR Y ADJUNTAR LA FACTURA PDF REAL
            byte[] pdfFinal = facturaPdfService.generarPdfFactura(
                    event.pedidoId(),
                    event.nombreFarmacia(),
                    event.totalPedido()
            );

            helper.addAttachment("Factura_Pedido_" + event.pedidoId() + ".pdf", new ByteArrayResource(pdfFinal));

            // 4. ENVIAR A MAILTRAP
            mailSender.send(message);

            log.info("✅ Email HTML con logo incrustado y factura PDF enviado a {}", event.emailFarmacia());

        } catch (Exception e) {
            log.error("❌ Fallo crítico al enviar email al pedido {}: {}", event.pedidoId(), e.getMessage(), e);
        }
    }
}