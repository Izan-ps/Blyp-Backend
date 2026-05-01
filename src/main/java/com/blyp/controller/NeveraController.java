package com.blyp.controller;

import com.blyp.dto.ProductoNeveraDto;
import com.blyp.dto.ProductoRequest;
import com.blyp.service.NeveraService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class NeveraController {

    private final NeveraService neveraService;

    public NeveraController(NeveraService neveraService) {
        this.neveraService = neveraService;
    }

    @GetMapping("/nevera")
    public ResponseEntity<List<ProductoNeveraDto>> listar(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(neveraService.listarProductos(user.getUsername()));
    }

    @PostMapping("/nevera/productos")
    public ResponseEntity<ProductoNeveraDto> añadir(
            @AuthenticationPrincipal UserDetails user,
            @Valid @RequestBody ProductoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(neveraService.añadirProducto(user.getUsername(), request));
    }

    @PatchMapping("/nevera/productos/{id}")
    public ResponseEntity<ProductoNeveraDto> editar(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable UUID id,
            @Valid @RequestBody ProductoRequest request) {
        return ResponseEntity.ok(neveraService.editarProducto(user.getUsername(), id, request));
    }

    @DeleteMapping("/nevera/productos/{id}")
    public ResponseEntity<Void> eliminar(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable UUID id) {
        neveraService.eliminarProducto(user.getUsername(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/productos/barcode/{code}")
    public ResponseEntity<?> buscarPorBarcode(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable String code) {

        java.util.Optional<ProductoNeveraDto> guardado = neveraService.buscarPorCodigoBarras(user.getUsername(), code);
        if (guardado.isPresent()) {
            ProductoNeveraDto p = guardado.get();
            return ResponseEntity.ok(java.util.Map.of(
                    "nombre", p.getNombre(),
                    "categoria", p.getCategoria() != null ? p.getCategoria() : "",
                    "nombrePersonalizado", true
            ));
        }

        String url = "https://world.openfoodfacts.org/api/v2/product/" + code + ".json?fields=product_name,categories_tags";

        try {
            java.net.URI uri = java.net.URI.create(url);
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest req = java.net.http.HttpRequest.newBuilder(uri)
                    .header("User-Agent", "Blyp/1.0")
                    .GET()
                    .build();

            java.net.http.HttpResponse<String> response = client.send(req,
                    java.net.http.HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(java.util.Map.of("error", "Producto no encontrado"));
            }

            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(response.body());

            if (root.path("status").asInt() != 1) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(java.util.Map.of("error", "Producto no encontrado en Open Food Facts"));
            }

            com.fasterxml.jackson.databind.JsonNode product = root.path("product");
            String nombre = product.path("product_name").asText("Producto desconocido");

            String categoria = "";
            com.fasterxml.jackson.databind.JsonNode tags = product.path("categories_tags");
            if (tags.isArray()) {
                for (com.fasterxml.jackson.databind.JsonNode t : tags) {
                    String raw = t.asText("");
                    if (raw.startsWith("es:")) {
                        String tag = raw.substring(3).replace('-', ' ');
                        categoria = Character.toUpperCase(tag.charAt(0)) + tag.substring(1);
                        break;
                    }
                }
            }

            return ResponseEntity.ok(java.util.Map.of(
                    "nombre", nombre,
                    "categoria", categoria,
                    "nombrePersonalizado", false
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("error", "Error al consultar Open Food Facts"));
        }
    }
}
