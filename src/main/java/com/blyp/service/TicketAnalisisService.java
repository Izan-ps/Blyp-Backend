package com.blyp.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
public class TicketAnalisisService {

    private static final Logger log = LoggerFactory.getLogger(TicketAnalisisService.class);

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${groq.api-key:}")
    private String apiKey;

    private static final String MODEL = "meta-llama/llama-4-scout-17b-16e-instruct";

    // Prompt para texto extraído de PDF (estructurado, muy preciso)
    private static final String TEXT_PROMPT =
        "Analiza el siguiente texto de un ticket de compra español.\n\n" +
        "CRITERIO DE INCLUSIÓN: incluye SOLO los productos que una persona come o bebe directamente. " +
        "Es decir: frutas, verduras, carnes, pescados, lácteos, huevos, pan, pasta, arroz, legumbres, conservas, " +
        "aceites, condimentos, especias, snacks, dulces, bebidas (agua, zumo, leche, refrescos, vino, cerveza…).\n\n" +
        "PREGUNTA DE VERIFICACIÓN: antes de incluir cada producto, pregúntate '¿se mete en la boca para comer o beber?' " +
        "Si la respuesta es NO, EXCLÚYELO sin excepción.\n\n" +
        "EXCLUYE SIEMPRE — estos productos NUNCA son comestibles:\n" +
        "- Gel de baño, gel de ducha, jabón corporal → NO son comida\n" +
        "- Colonia, perfume, desodorante, crema → NO son comida\n" +
        "- Champú, acondicionador, mascarilla capilar → NO son comida\n" +
        "- Detergente, suavizante, lejía, limpiador → NO son comida\n" +
        "- Papel higiénico, toallitas, compresas, pañales → NO son comida\n" +
        "- Bolsas de plástico, tabaco, pilas, prensa → NO son comida\n\n" +
        "REGLAS DE EXTRACCIÓN:\n" +
        "1. CANTIDAD: el primer número entero de la línea es la cantidad de unidades. " +
        "Ejemplos: '2 ZANAHORIA 1/2 KG 0,89 1,78' → cantidad=2; '2 JUDIA PLANA 1/2 K 1,20 2,40' → cantidad=2. " +
        "Sin número al inicio → cantidad=1. NUNCA uses fracciones ni decimales: cantidad es SIEMPRE entero ≥ 1.\n" +
        "2. PRECIO_UNIDAD: si hay dos precios en la línea (EUROS/UD y EUROS/TOT), el primero es precio_unidad " +
        "(ej: '2 HUEVO M EROSKI 3,10 6,20' → precio_unidad=3,10, NO 6,20).\n" +
        "3. Si cantidad=1 y solo hay un precio, ese es precio_unidad.\n" +
        "4. Productos por peso ('0,520kg 7,49Eu/kg 3,89'): cantidad=1, precio_unidad=importe total (3,89).\n" +
        "5. DESCUENTOS al final ('1 TOMATE FRITO -0,95'): precio_unidad=(EUROS/TOT - |descuento|)÷cantidad.\n" +
        "6. NOMBRE: nombre genérico en español, sin marca ni tamaño, formato título. Ejemplos: 'Leche desnatada', 'Judía plana', 'Pechuga de pollo'.\n\n" +
        "Responde ÚNICAMENTE con un array JSON válido, sin texto, sin markdown. Si no hay alimentos, responde [].\n" +
        "Formato: [{\"nombre\":\"...\",\"cantidad\":N,\"precio_unidad\":X.XX}]\n\n" +
        "TEXTO DEL TICKET:\n";

    // Prompt para imágenes (fotos de tickets en papel)
    private static final String VISION_PROMPT =
        "Analiza esta imagen de un ticket de compra español.\n\n" +
        "CRITERIO DE INCLUSIÓN: incluye SOLO los productos que una persona come o bebe directamente. " +
        "Es decir: frutas, verduras, carnes, pescados, lácteos, huevos, pan, pasta, arroz, legumbres, conservas, " +
        "aceites, condimentos, especias, snacks, dulces, bebidas (agua, zumo, leche, refrescos, vino, cerveza…).\n\n" +
        "PREGUNTA DE VERIFICACIÓN: antes de incluir cada producto, pregúntate '¿se mete en la boca para comer o beber?' " +
        "Si la respuesta es NO, EXCLÚYELO sin excepción.\n\n" +
        "EXCLUYE SIEMPRE — estos productos NUNCA son comestibles:\n" +
        "- Gel de baño, gel de ducha, jabón corporal → NO son comida\n" +
        "- Colonia, perfume, desodorante, crema → NO son comida\n" +
        "- Champú, acondicionador, mascarilla capilar → NO son comida\n" +
        "- Detergente, suavizante, lejía, limpiador → NO son comida\n" +
        "- Papel higiénico, toallitas, compresas, pañales → NO son comida\n" +
        "- Bolsas de plástico, tabaco, pilas, prensa → NO son comida\n\n" +
        "REGLAS DE EXTRACCIÓN:\n" +
        "1. CANTIDAD: el primer número entero de la línea es la cantidad de unidades. " +
        "Ejemplos: '2 ZANAHORIA 1/2 KG ...' → cantidad=2; '2 JUDIA PLANA 1/2 K ...' → cantidad=2. " +
        "Sin número al inicio → cantidad=1. NUNCA uses fracciones ni decimales: cantidad es SIEMPRE entero ≥ 1.\n" +
        "2. PRECIO_UNIDAD: si hay dos precios en la línea, el primero es precio_unidad (nunca el total).\n" +
        "3. Productos por peso: cantidad=1, precio_unidad=importe total de la línea.\n" +
        "4. DESCUENTOS al final del ticket: réstalos del total y recalcula precio_unidad.\n" +
        "5. NOMBRE: nombre genérico en español, sin marca ni tamaño, formato título. Ejemplos: 'Leche desnatada', 'Judía plana', 'Pechuga de pollo'.\n\n" +
        "Responde ÚNICAMENTE con un array JSON válido, sin texto, sin markdown. Si no hay alimentos, responde [].\n" +
        "Formato: [{\"nombre\":\"...\",\"cantidad\":N,\"precio_unidad\":X.XX}]";

    public TicketAnalisisService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
                .baseUrl("https://api.groq.com")
                .build();
    }

    public List<Map<String, Object>> analizarTicket(String imagenBase64) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Servicio de análisis no configurado");
        }

        String mimeType   = "image/jpeg";
        String base64Data = imagenBase64;
        if (imagenBase64.startsWith("data:")) {
            int semi  = imagenBase64.indexOf(';');
            int comma = imagenBase64.indexOf(',');
            if (semi == -1 || comma == -1) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "Formato de imagen inválido. Sube una foto del ticket (JPEG, PNG, WEBP o PDF).");
            }
            mimeType   = imagenBase64.substring(5, semi);
            base64Data = imagenBase64.substring(comma + 1);
        }

        // PDF → extraer texto (mucho más preciso que convertir a imagen)
        if ("application/pdf".equals(mimeType)) {
            String texto = extractPdfText(base64Data);
            if (texto != null && texto.length() > 50) {
                log.debug("PDF con texto extraído ({} chars), usando prompt de texto", texto.length());
                return callGroq(List.of(Map.of("type", "text", "text", TEXT_PROMPT + texto)));
            }
            // PDF sin texto (escaneado) → convertir a imagen como fallback
            log.debug("PDF sin texto extraíble, convirtiendo a imagen");
            base64Data = pdfFirstPageToJpeg(base64Data);
            mimeType   = "image/jpeg";
        }

        if (!mimeType.startsWith("image/")) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Formato no compatible. Sube una foto del ticket (JPEG, PNG, WEBP o PDF).");
        }

        String dataUrl = "data:" + mimeType + ";base64," + base64Data;
        return callGroq(List.of(
                Map.of("type", "image_url", "image_url", Map.of("url", dataUrl)),
                Map.of("type", "text", "text", VISION_PROMPT)
        ));
    }

    private List<Map<String, Object>> callGroq(List<Map<String, Object>> content) {
        Map<String, Object> requestBody = Map.of(
                "model", MODEL,
                "messages", List.of(Map.of("role", "user", "content", content)),
                "temperature", 0,
                "max_tokens", 2048
        );
        try {
            Map<?, ?> response = restClient.post()
                    .uri("/openai/v1/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + apiKey)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            List<?> choices = (List<?>) response.get("choices");
            Map<?, ?> message = (Map<?, ?>) ((Map<?, ?>) choices.get(0)).get("message");
            String text = (String) message.get("content");

            int start = text.indexOf('[');
            int end   = text.lastIndexOf(']') + 1;
            if (start == -1 || end == 0) return List.of();

            return objectMapper.readValue(
                    text.substring(start, end),
                    new TypeReference<List<Map<String, Object>>>() {}
            );
        } catch (ResponseStatusException rse) {
            throw rse;
        } catch (RestClientResponseException rce) {
            log.error("Groq API error {}: {}", rce.getStatusCode(), rce.getResponseBodyAsString());
            throw new ResponseStatusException(
                    rce.getStatusCode().value() == 429 ? HttpStatus.TOO_MANY_REQUESTS : HttpStatus.BAD_GATEWAY,
                    extractErrorMessage(rce.getResponseBodyAsString()));
        } catch (Exception e) {
            log.error("Error llamando a Groq API: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    private String extractPdfText(String base64Pdf) {
        try {
            byte[] pdfBytes = Base64.getDecoder().decode(base64Pdf);
            try (PDDocument doc = Loader.loadPDF(pdfBytes)) {
                PDFTextStripper stripper = new PDFTextStripper();
                return stripper.getText(doc);
            }
        } catch (Exception e) {
            log.warn("No se pudo extraer texto del PDF: {}", e.getMessage());
            return null;
        }
    }

    private String pdfFirstPageToJpeg(String base64Pdf) {
        try {
            byte[] pdfBytes = Base64.getDecoder().decode(base64Pdf);
            try (PDDocument doc = Loader.loadPDF(pdfBytes)) {
                PDFRenderer renderer = new PDFRenderer(doc);
                BufferedImage image = renderer.renderImageWithDPI(0, 150, ImageType.RGB);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(image, "jpeg", baos);
                return Base64.getEncoder().encodeToString(baos.toByteArray());
            }
        } catch (Exception e) {
            log.error("Error al convertir PDF a imagen: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "No se pudo procesar el PDF. Prueba con una imagen del ticket.");
        }
    }

    private String extractErrorMessage(String body) {
        try {
            Map<?, ?> json = objectMapper.readValue(body, Map.class);
            Object error = json.get("error");
            if (error instanceof Map<?, ?> err) {
                Object msg = err.get("message");
                if (msg instanceof String s) {
                    int newline = s.indexOf('\n');
                    return newline > 0 ? s.substring(0, newline) : s;
                }
            }
        } catch (Exception ignored) {}
        return "El servicio de análisis no está disponible temporalmente";
    }
}
