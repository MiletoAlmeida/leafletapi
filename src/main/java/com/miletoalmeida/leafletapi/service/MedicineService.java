package com.miletoalmeida.leafletapi.service;

import com.miletoalmeida.leafletapi.model.MedicineDetail;
import com.miletoalmeida.leafletapi.model.MedicineSummary;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class MedicineService {

    private final WebClient webClient;
    private static final String ANVISA_BASE_URL = "https://consultas.anvisa.gov.br/";
    private static final String SEARCH_URL = ANVISA_BASE_URL + "api/consulta/medicamentos?count=10&filter%5BnomeProduto%5D=";
    private static final String MANUFACTURER_URL = ANVISA_BASE_URL + "api/consulta/medicamentos?count=10&filter%5BrazaoSocial%5D=";
    private static final String DETAIL_URL = ANVISA_BASE_URL + "consulta/medicamentos/25351";
    private static final String LEAFLET_BASE_URL = "https://consultas.anvisa.gov.br/bulario/";

    @Autowired
    public MedicineService(WebClient webClient) {
        this.webClient = webClient;
    }

    public List<MedicineSummary> findByName(String name) {
        try {
            String searchUrl = SEARCH_URL + name.replace(" ", "%20");
            String response = webClient.get()
                    .uri(searchUrl)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            List<MedicineSummary> results = parseMedicineResponse(response);

            // Caso não encontre resultados na ANVISA ou ocorra erro, retorna dados mockados para teste
            if (results.isEmpty()) {
                return generateMockData(name, 5);
            }

            return results;
        } catch (Exception e) {
            System.err.println("Error searching medicine by name: " + e.getMessage());
            // Retorna dados mockados para facilitar o desenvolvimento
            return generateMockData(name, 5);
        }
    }

    public List<MedicineSummary> findByManufacturer(String manufacturer) {
        try {
            String searchUrl = MANUFACTURER_URL + manufacturer.replace(" ", "%20");
            String response = webClient.get()
                    .uri(searchUrl)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            List<MedicineSummary> results = parseMedicineResponse(response);

            // Caso não encontre resultados na ANVISA ou ocorra erro, retorna dados mockados para teste
            if (results.isEmpty()) {
                return generateMockData(manufacturer, 5);
            }

            return results;
        } catch (Exception e) {
            System.err.println("Error searching medicine by manufacturer: " + e.getMessage());
            // Retorna dados mockados para facilitar o desenvolvimento
            return generateMockData(manufacturer, 5);
        }
    }

    public MedicineDetail findDetailById(String id) {
        try {
            // Na ANVISA, geralmente precisamos do número de processo completo
            String detailUrl = DETAIL_URL + id;

            // Aqui faríamos uma requisição para a página de detalhes
            // e extrairíamos as informações usando JSoup

            // Para fins de demonstração, retornamos um objeto mockado
            return generateMockDetail(id);
        } catch (Exception e) {
            System.err.println("Error retrieving medicine details: " + e.getMessage());
            return generateMockDetail(id);
        }
    }

    public String getLeafletUrl(String id) {
        try {
            // Tentaria buscar a URL real da bula na ANVISA
            // Para demonstração, retornamos uma URL fictícia
            return LEAFLET_BASE_URL + "pdf/" + id + ".pdf";
        } catch (Exception e) {
            System.err.println("Error retrieving leaflet URL: " + e.getMessage());
            return null;
        }
    }

    private List<MedicineSummary> parseMedicineResponse(String htmlResponse) {
        List<MedicineSummary> medicines = new ArrayList<>();

        try {
            Document doc = Jsoup.parse(htmlResponse);
            Elements medicineElements = doc.select("div.result-item");

            for (Element medicineElement : medicineElements) {
                MedicineSummary medicine = new MedicineSummary();

                // Gerando um ID único para uso interno na API
                medicine.setId(UUID.randomUUID().toString());

                Element nameElement = medicineElement.selectFirst("h3.product-name");
                if (nameElement != null) {
                    medicine.setName(nameElement.text());
                }

                Element manufacturerElement = medicineElement.selectFirst("p.company-name");
                if (manufacturerElement != null) {
                    medicine.setManufacturer(manufacturerElement.text());
                }

                Element categoryElement = medicineElement.selectFirst("span.category");
                if (categoryElement != null) {
                    medicine.setRegulatoryCategory(categoryElement.text());
                }

                Element registrationElement = medicineElement.selectFirst("span.registration");
                if (registrationElement != null) {
                    medicine.setRegistrationNumber(registrationElement.text());
                }

                Element ingredientElement = medicineElement.selectFirst("div.active-ingredient");
                if (ingredientElement != null) {
                    medicine.setActiveIngredient(ingredientElement.text());
                }

                Element pdfElement = medicineElement.selectFirst("a.pdf-link");
                if (pdfElement != null) {
                    medicine.setPdfUrl(pdfElement.attr("href"));
                }

                medicines.add(medicine);
            }
        } catch (Exception e) {
            System.err.println("Error parsing HTML response: " + e.getMessage());
        }

        return medicines;
    }

    // Método auxiliar para gerar dados mockados para testes
    private List<MedicineSummary> generateMockData(String query, int count) {
        List<MedicineSummary> mockMedicines = new ArrayList<>();
        AtomicInteger idCounter = new AtomicInteger(1);

        String[] classes = {"Anti-inflamatório", "Analgésico", "Antibiótico", "Antialérgico", "Antidepressivo"};
        String[] ingredients = {"Paracetamol", "Ibuprofeno", "Amoxicilina", "Loratadina", "Fluoxetina"};

        for (int i = 0; i < count; i++) {
            MedicineSummary medicine = new MedicineSummary();
            String id = String.valueOf(idCounter.getAndIncrement());
            medicine.setId(id);
            medicine.setName(query.toUpperCase() + " " + id);
            medicine.setManufacturer("Laboratório " + (i % 3 + 1));
            medicine.setRegulatoryCategory("Medicamento " + (i % 2 == 0 ? "Similar" : "Genérico"));
            medicine.setRegistrationNumber("1.0000.0000." + id);
            medicine.setActiveIngredient(ingredients[i % ingredients.length]);
            medicine.setTherapeuticClass(classes[i % classes.length]);
            medicine.setPdfUrl(LEAFLET_BASE_URL + "pdf/mock" + id + ".pdf");
            medicine.setPresentation("Caixa com " + (i + 1) * 10 + " comprimidos");
            mockMedicines.add(medicine);
        }

        return mockMedicines;
    }

    private MedicineDetail generateMockDetail(String id) {
        MedicineDetail detail = new MedicineDetail();
        detail.setId(id);
        detail.setName("MEDICAMENTO DETALHADO " + id);
        detail.setManufacturer("Laboratório Brasileiro S.A.");
        detail.setRegulatoryCategory("Medicamento Similar");
        detail.setRegistrationNumber("1.0000.0000." + id);
        detail.setActiveIngredient("Paracetamol 500mg");
        detail.setTherapeuticClass("Analgésico e Antitérmico");
        detail.setPdfUrl(LEAFLET_BASE_URL + "pdf/mock" + id + ".pdf");
        detail.setPresentation("Caixa com 20 comprimidos revestidos");

        // Campos específicos do detalhe
        detail.setRegistrationDate(LocalDate.now().minusYears(2));
        detail.setExpirationDate(LocalDate.now().plusYears(3));
        detail.setProcessNumber("25351.123456/" + id + "-12");
        detail.setAdministrationForm("Oral");
        detail.setRegisterStatus("Ativo");
        detail.setPackageType("Blister de alumínio");
        detail.setContraindications(Arrays.asList(
                "Hipersensibilidade aos componentes da fórmula",
                "Pacientes com doença hepática grave",
                "Uso concomitante com outros medicamentos contendo paracetamol"
        ));
        detail.setSideEffects(Arrays.asList(
                "Reações alérgicas",
                "Náusea e vômito",
                "Dor abdominal",
                "Elevação de enzimas hepáticas"
        ));
        detail.setDosage("Adultos: 1 comprimido a cada 6 horas. Não exceder 4 comprimidos em 24 horas.");
        detail.setStorage("Conservar em temperatura ambiente (15 a 30ºC). Proteger da luz e umidade.");

        return detail;
    }
}