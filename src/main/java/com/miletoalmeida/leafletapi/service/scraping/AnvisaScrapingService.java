package com.miletoalmeida.leafletapi.service.scraping;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.miletoalmeida.leafletapi.dto.LeafletDTO;
import com.miletoalmeida.leafletapi.dto.MedicineDTO;
import com.miletoalmeida.leafletapi.exception.ScrapingException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class AnvisaScrapingService {

    // URLs da Anvisa
    private static final String ANVISA_BASE_URL = "https://consultas.anvisa.gov.br";
    private static final String ANVISA_SEARCH_URL = ANVISA_BASE_URL + "/api/consulta/medicamentos";
    private static final String ANVISA_LEAFLET_URL = ANVISA_BASE_URL + "/api/consulta/bulario";
    private static final String ANVISA_MEDICINE_DETAILS_URL = ANVISA_BASE_URL + "#/medicamento/%s";

    private final WebClient webClient;
    private final UserAgentRotator userAgentRotator;
    private final ObjectMapper objectMapper;

    @Autowired
    public AnvisaScrapingService(WebClient webClient, UserAgentRotator userAgentRotator, ObjectMapper objectMapper) {
        this.webClient = webClient;
        this.userAgentRotator = userAgentRotator;
        this.objectMapper = objectMapper;
    }

private String executeAnvisaRequest(String url, String payload) throws ScrapingException {
    try {
        // Adiciona delay para simular comportamento humano
        addRandomDelay();

        String userAgent = userAgentRotator.getRandomUserAgent();

        return webClient.post()
                .uri(url)
                .header(HttpHeaders.USER_AGENT, userAgent)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .header("Referer", ANVISA_BASE_URL)
                .header("Origin", ANVISA_BASE_URL)
                .body(BodyInserters.fromValue(payload))
                .retrieve()
                .bodyToMono(String.class)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(2))
                        .filter(throwable -> throwable instanceof WebClientResponseException))
                .block();
    } catch (Exception e) {
        ScrapingException.ScrapingErrorType errorType;
        
        if (e instanceof WebClientResponseException.TooManyRequests) {
            errorType = ScrapingException.ScrapingErrorType.RATE_LIMIT_EXCEEDED;
        } else if (e instanceof WebClientResponseException.ServiceUnavailable) {
            errorType = ScrapingException.ScrapingErrorType.SERVICE_UNAVAILABLE;
        } else {
            errorType = ScrapingException.ScrapingErrorType.NETWORK_ERROR;
        }
        
        throw new ScrapingException(
            "Falha na requisição para a Anvisa: " + e.getMessage(),
            e,
            errorType
        );
    }
}

public List<MedicineDTO> searchMedicines(String query) throws ScrapingException {
    try {
        String searchPayload = String.format("{\"count\":20,\"filter\":{\"nome\":\"%s\"},\"page\":1}", query);
        String responseBody = executeAnvisaRequest(ANVISA_SEARCH_URL, searchPayload);

        if (responseBody == null || responseBody.isEmpty()) {
            throw new ScrapingException(
                "Resposta vazia da API da Anvisa",
                null,
                ScrapingException.ScrapingErrorType.INVALID_RESPONSE
            );
        }

        return parseMedicineSearchResults(responseBody);
    } catch (ScrapingException e) {
        throw e; // Relança exceções de scraping
    } catch (Exception e) {
        throw new ScrapingException(
            "Falha ao processar resultados da busca: " + e.getMessage(),
            e,
            ScrapingException.ScrapingErrorType.PARSING_ERROR
        );
    }
}

public LeafletDTO getLeaflet(String registryNumber) throws ScrapingException {
    try {
        String leafletPayload = String.format("{\"filter\":{\"numeroRegistro\":\"%s\"}}", registryNumber);
        String responseBody = executeAnvisaRequest(ANVISA_LEAFLET_URL, leafletPayload);

        if (responseBody == null || responseBody.isEmpty()) {
            throw new ScrapingException(
                "Resposta vazia da API de bulas da Anvisa",
                null,
                ScrapingException.ScrapingErrorType.INVALID_RESPONSE
            );
        }

        return parseLeafletResult(responseBody);
    } catch (ScrapingException e) {
        throw e; // Relança exceções de scraping
    } catch (Exception e) {
        throw new ScrapingException(
            "Falha ao processar bula: " + e.getMessage(),
            e,
            ScrapingException.ScrapingErrorType.PARSING_ERROR
        );
    }
}

    /**
     * Faz o parse da resposta de busca de medicamentos
     */
    private List<MedicineDTO> parseMedicineSearchResults(String jsonResponse) throws Exception {
        List<MedicineDTO> results = new ArrayList<>();

        JsonNode rootNode = objectMapper.readTree(jsonResponse);
        JsonNode contentNode = rootNode.path("content");

        if (contentNode.isArray()) {
            for (JsonNode medicineNode : contentNode) {
                MedicineDTO medicine = new MedicineDTO();

                // Extrai informações básicas
                medicine.setRegistryNumber(medicineNode.path("numeroRegistro").asText(""));
                medicine.setProductName(medicineNode.path("nomeProduto").asText(""));
                medicine.setCompany(medicineNode.path("razaoSocial").asText(""));
                medicine.setActiveIngredient(medicineNode.path("principioAtivo").asText(""));
                medicine.setTherapeuticClass(medicineNode.path("classesTerapeuticas").asText(""));
                medicine.setRegulatoryType(medicineNode.path("categoriaRegulatoria").asText(""));

                // Informações adicionais
                medicine.setProcessNumber(medicineNode.path("numeroProcesso").asText(""));
                medicine.setCnpj(medicineNode.path("cnpj").asText(""));

                // URL para buscar detalhes completos da bula
                String detailsUrl = String.format(ANVISA_MEDICINE_DETAILS_URL, medicine.getRegistryNumber());
                medicine.setLeafletUrl(detailsUrl);

                results.add(medicine);
            }
        }

        return results;
    }

    /**
     * Faz o parse da resposta de bula
     */
    private LeafletDTO parseLeafletResult(String jsonResponse) throws Exception {
        LeafletDTO leaflet = new LeafletDTO();

        JsonNode rootNode = objectMapper.readTree(jsonResponse);
        JsonNode contentNode = rootNode.path("content");

        if (contentNode.isArray() && !contentNode.isEmpty()) {
            JsonNode firstLeaflet = contentNode.get(0);

            // Verifica se há bula para paciente
            if (firstLeaflet.has("textoRotulagem")) {
                String patientText = firstLeaflet.path("textoRotulagem").asText("");
                leaflet.setPatientLeaflet(cleanHtml(patientText));
            }

            // Verifica se há bula para profissional de saúde
            if (firstLeaflet.has("textoBula")) {
                String professionalText = firstLeaflet.path("textoBula").asText("");
                leaflet.setProfessionalLeaflet(cleanHtml(professionalText));
            }
        }

        return leaflet;
    }

    /**
     * Limpa o HTML da bula, mantendo formatação básica
     */
    private String cleanHtml(String html) {
        if (html == null || html.isEmpty()) {
            return "";
        }

        // Usa JSoup para limpar e formatar o HTML
        Document doc = Jsoup.parse(html);
        doc.select("script, style").remove(); // Remove scripts e estilos

        return doc.body().html();
    }

    /**
     * Adiciona um delay aleatório para simular comportamento humano
     */
    private void addRandomDelay() {
        try {
            // Delay aleatório entre 1-3 segundos
            long delay = ThreadLocalRandom.current().nextLong(1000, 3000);
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}