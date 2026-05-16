package com.blyp.service;

import com.blyp.model.Nevera;
import com.blyp.model.ProductoNevera;
import com.blyp.repository.NeveraRepository;
import com.blyp.repository.ProductoNeveraRepository;
import com.blyp.repository.UsuarioRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RecetasService {

    private static final Logger log = LoggerFactory.getLogger(RecetasService.class);

    private final UsuarioRepository usuarioRepository;
    private final NeveraRepository neveraRepository;
    private final ProductoNeveraRepository productoRepository;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${groq.api-key:}")
    private String apiKey;

    private static final String MODEL = "llama-3.3-70b-versatile";

    public RecetasService(UsuarioRepository usuarioRepository,
                          NeveraRepository neveraRepository,
                          ProductoNeveraRepository productoRepository,
                          ObjectMapper objectMapper) {
        this.usuarioRepository  = usuarioRepository;
        this.neveraRepository   = neveraRepository;
        this.productoRepository = productoRepository;
        this.objectMapper       = objectMapper;
        this.restClient = RestClient.builder()
                .baseUrl("https://api.groq.com")
                .build();
    }

    public Map<String, Object> generarReceta(String email, String mensaje) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Servicio de recetas no configurado");
        }

        String ingredientes = obtenerIngredientes(email);
        String prompt = buildPrompt(ingredientes, mensaje);

        return callGroq(prompt);
    }

    private String obtenerIngredientes(String email) {
        var usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));

        Optional<Nevera> nevera = neveraRepository.findByUsuarioId(usuario.getId());
        if (nevera.isEmpty()) return "La nevera está vacía.";

        List<ProductoNevera> productos = productoRepository.findByNeveraId(nevera.get().getId());
        if (productos.isEmpty()) return "La nevera está vacía.";

        return productos.stream()
                .map(p -> "- " + p.getNombre() + " (" + p.getCantidad() + " ud)")
                .collect(Collectors.joining("\n"));
    }

    private String buildPrompt(String ingredientes, String mensaje) {
        return "Eres un chef experto en cocina española y mediterránea. " +
               "El usuario tiene los siguientes ingredientes en su nevera:\n\n" +
               ingredientes + "\n\n" +
               "El usuario pide: \"" + mensaje + "\"\n\n" +
               "Genera una receta usando principalmente los ingredientes disponibles. " +
               "IMPORTANTE: respeta estrictamente las cantidades indicadas — si el usuario tiene 1 huevo, la receta no puede pedir 3. " +
               "Adapta las proporciones de la receta a lo que hay disponible, no al revés. " +
               "Si faltan ingredientes básicos imprescindibles (sal, aceite, agua), puedes incluirlos. " +
               "Evita incluir ingredientes que el usuario no tiene salvo los básicos.\n\n" +
               "Responde ÚNICAMENTE con un objeto JSON válido, sin texto ni markdown. Formato exacto:\n" +
               "{\"nombre\":\"...\",\"ingredientes\":[\"...\",\"...\"],\"elaboracion\":\"1. ...\\n2. ...\\n3. ...\",\"tiempo\":\"... minutos\",\"notas\":\"...\"}\n" +
               "El campo \"notas\" es opcional: úsalo si el usuario pide información extra (calorías, valores nutricionales, alérgenos, etc.). Si no aplica, omítelo o ponlo como null.";
    }

    private Map<String, Object> callGroq(String prompt) {
        Map<String, Object> requestBody = Map.of(
                "model", MODEL,
                "messages", List.of(Map.of("role", "user", "content", prompt)),
                "temperature", 0.7,
                "max_tokens", 1024
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

            int start = text.indexOf('{');
            int end   = text.lastIndexOf('}') + 1;
            if (start == -1 || end == 0) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Respuesta inesperada de la IA");
            }

            return objectMapper.readValue(text.substring(start, end), new TypeReference<Map<String, Object>>() {});
        } catch (ResponseStatusException rse) {
            throw rse;
        } catch (RestClientResponseException rce) {
            log.error("Groq API error {}: {}", rce.getStatusCode(), rce.getResponseBodyAsString());
            throw new ResponseStatusException(
                    rce.getStatusCode().value() == 429 ? HttpStatus.TOO_MANY_REQUESTS : HttpStatus.BAD_GATEWAY,
                    "El servicio de recetas no está disponible temporalmente");
        } catch (Exception e) {
            log.error("Error llamando a Groq para recetas: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al generar la receta");
        }
    }
}
