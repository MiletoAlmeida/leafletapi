package com.miletoalmeida.leafletapi.service.scraping;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.miletoalmeida.leafletapi.exception.ScrapingException;
import com.miletoalmeida.leafletapi.dto.LeafletDTO;
import com.miletoalmeida.leafletapi.dto.MedicineDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnvisaScrapingService {

    private static final String ANVISA_BASE_URL = "https://consultas.anvisa.gov.br";
    private static final String ANVISA_SEARCH_PATH = "/api/consulta/medicamentos";
    private static final String ANVISA_LEAFLET_PATH = "/api/consulta/bulario";
    private static final int MAX_RETRIES = 3;
    private static final Duration RETRY_DELAY = Duration.ofSeconds(2);
    private static final Duration MIN_DELAY = Duration.ofSeconds(1);
    private static final Duration MAX_DELAY = Duration.ofSeconds(3);

    private final WebClient webClient;
    private final UserAgentRotator userAgentRotator;
    private final ObjectMapper objectMapper;

    public List<MedicineDTO> searchMedicines(String query) throws ScrapingException {
        try {
            addRandomDelay();
            String searchPayload = createSearchPayload(query);
            ResponseEntity<String> response = executeRequest(ANVISA_SEARCH_PATH, searchPayload);
            return parseResponse(response).orElse(Collections.emptyList());
        } catch (Exception e) {
            log.error("Erro ao pesquisar medicamentos: {}", e.getMessage(), e);
            throw new ScrapingException("Falha ao pesquisar medicamentos", e);
        }
    }

    public LeafletDTO getLeaflet(String registryNumber) throws ScrapingException {
        try {
            addRandomDelay();
            String leafletPayload = createLeafletPayload(registryNumber);
            ResponseEntity<String> response = executeRequest(ANVISA_LEAFLET_PATH, leafletPayload);
            return parseLeafletResponse(response).orElseThrow(() -> 
                new ScrapingException("Bula não encontrada para o registro: " + registryNumber, null));
        } catch (Exception e) {
            log.error("Erro ao obter bula: {}", e.getMessage(), e);
            throw new ScrapingException("Falha ao obter bula", e);
        }
    }

    private ResponseEntity<String> executeRequest(String path, String payload) {
        return webClient.post()
                .uri(ANVISA_BASE_URL + path)
                .headers(headers -> configureHeaders(headers, userAgentRotator.getRandomUserAgent()))
                .body(BodyInserters.fromValue(payload))
                .retrieve()
                .toEntity(String.class)
                .retryWhen(Retry.backoff(MAX_RETRIES, RETRY_DELAY)
                        .filter(throwable -> throwable instanceof WebClientResponseException))
                .block();
    }

    private void configureHeaders(HttpHeaders headers, String userAgent) {
        headers.set(HttpHeaders.USER_AGENT, userAgent);
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.set("Referer", ANVISA_BASE_URL + "/");
        headers.set("Origin", ANVISA_BASE_URL);
    }

    private String createSearchPayload(String query) {
        return String.format("{\"count\":10,\"filter\":{\"nome\":\"%s\"},\"page\":1}", query);
    }

    private String createLeafletPayload(String registryNumber) {
        return String.format("{\"filter\":{\"numeroRegistro\":\"%s\"}}", registryNumber);
    }

    private Optional<List<MedicineDTO>> parseResponse(ResponseEntity<String> response) {
        try {
            if (response == null || response.getBody() == null) {
                return Optional.empty();
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode content = root.get("content");
            
            if (content == null || !content.isArray()) {
                return Optional.empty();
            }

            List<MedicineDTO> medicines = new ArrayList<>();
            content.forEach(node -> medicines.add(createMedicineDTO(node)));
            return Optional.of(medicines);
        } catch (Exception e) {
            log.error("Erro ao analisar resultados da pesquisa: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    private MedicineDTO createMedicineDTO(JsonNode node) {
        return MedicineDTO.builder()
                .productName(getNodeText(node, "nomeProduto"))
                .registryNumber(getNodeText(node, "numeroRegistro"))
                .company(getNodeText(node, "razaoSocial"))
                .activeIngredient(getNodeText(node, "principioAtivo"))
                .build();
    }

    private String getNodeText(JsonNode node, String field) {
        return Optional.ofNullable(node.get(field))
                .map(JsonNode::asText)
                .orElse("");
    }

    private Optional<LeafletDTO> parseLeafletResponse(ResponseEntity<String> response) {
        try {
            if (response == null || response.getBody() == null) {
                return Optional.empty();
            }

            Document doc = Jsoup.parse(response.getBody());
            return Optional.of(LeafletDTO.builder()
                    .patientLeaflet(getLeafletContent(doc, "div.texto-bula"))
                    .professionalLeaflet(getLeafletContent(doc, "div.professional-leaflet"))
                    .build());
        } catch (Exception e) {
            log.error("Erro ao analisar bula: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    private String getLeafletContent(Document doc, String selector) {
        return Optional.ofNullable(doc.select(selector).first())
                .map(Element::html)
                .orElse("");
    }

    private void addRandomDelay() throws ScrapingException {
        try {
            long delay = ThreadLocalRandom.current()
                    .nextLong(MIN_DELAY.toMillis(), MAX_DELAY.toMillis());
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ScrapingException("Interrupção durante o delay", e);
        }
    }
}