package com.miletoalmeida.leafletapi.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@Slf4j
@UtilityClass
public class ScrapingUtils {

    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]*>");
    private static final Pattern MULTIPLE_SPACES_PATTERN = Pattern.compile("\\s+");
    private static final Pattern SPECIAL_CHARS_PATTERN = Pattern.compile("[^a-zA-Z0-9\\s]");
    
    private static final int MAX_TEXT_LENGTH = 5000;
    private static final String ELLIPSIS = "...";

    /**
     * Remove tags HTML e limpa o texto.
     *
     * @param html O texto HTML para ser limpo
     * @return texto limpo sem tags HTML
     */
    public static String cleanHtml(String html) {
        return Optional.ofNullable(html)
                .map(text -> HTML_TAG_PATTERN.matcher(text).replaceAll(""))
                .map(text -> MULTIPLE_SPACES_PATTERN.matcher(text).replaceAll(" "))
                .map(String::trim)
                .orElse("");
    }

    /**
     * Trunca o texto mantendo palavras completas.
     *
     * @param text O texto para ser truncado
     * @param maxLength Comprimento máximo desejado
     * @return texto truncado
     */
    public static String truncateText(String text, int maxLength) {
        if (StringUtils.isBlank(text) || text.length() <= maxLength) {
            return text;
        }

        int endIndex = Math.min(text.length(), maxLength);
        int lastSpace = text.lastIndexOf(' ', endIndex);
        
        if (lastSpace > 0) {
            endIndex = lastSpace;
        }

        return text.substring(0, endIndex).trim() + ELLIPSIS;
    }

    /**
     * Normaliza o texto removendo acentos e caracteres especiais.
     *
     * @param text O texto para ser normalizado
     * @return texto normalizado
     */
    public static String normalizeText(String text) {
        return Optional.ofNullable(text)
                .map(str -> str.replaceAll("[áàãâä]", "a"))
                .map(str -> str.replaceAll("[éèêë]", "e"))
                .map(str -> str.replaceAll("[íìîï]", "i"))
                .map(str -> str.replaceAll("[óòõôö]", "o"))
                .map(str -> str.replaceAll("[úùûü]", "u"))
                .map(str -> str.replaceAll("[ç]", "c"))
                .map(str -> SPECIAL_CHARS_PATTERN.matcher(str).replaceAll(""))
                .map(String::trim)
                .map(String::toLowerCase)
                .orElse("");
    }

    /**
     * Extrai valores específicos de um texto usando expressões regulares.
     *
     * @param text O texto para extrair valores
     * @param patterns Mapa de padrões regex e seus nomes
     * @return Lista de valores extraídos
     */
    public static List<String> extractValues(String text, Map<String, Pattern> patterns) {
        List<String> values = new ArrayList<>();
        
        if (StringUtils.isBlank(text) || patterns == null) {
            return values;
        }

        try {
            patterns.forEach((name, pattern) -> {
                var matcher = pattern.matcher(text);
                if (matcher.find()) {
                    values.add(matcher.group(1));
                }
            });
        } catch (Exception e) {
            log.error("Erro ao extrair valores do texto: {}", e.getMessage(), e);
        }

        return values;
    }

    /**
     * Codifica parâmetros para uso em URLs.
     *
     * @param params Mapa de parâmetros
     * @return String codificada para URL
     */
    public static String encodeUrlParameters(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        try {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (result.length() > 0) {
                    result.append("&");
                }
                result.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8))
                      .append("=")
                      .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            log.error("Erro ao codificar parâmetros da URL: {}", e.getMessage(), e);
            return "";
        }

        return result.toString();
    }

    /**
     * Limita o tamanho do texto preservando a integridade das palavras.
     *
     * @param text O texto para ser limitado
     * @return texto limitado
     */
    public static String limitTextSize(String text) {
        return truncateText(text, MAX_TEXT_LENGTH);
    }

    /**
     * Verifica se o texto contém HTML.
     *
     * @param text O texto para verificar
     * @return true se contiver tags HTML
     */
    public static boolean containsHtml(String text) {
        return text != null && HTML_TAG_PATTERN.matcher(text).find();
    }

    /**
     * Remove duplicatas de espaços em branco e quebras de linha.
     *
     * @param text O texto para ser limpo
     * @return texto limpo
     */
    public static String cleanWhitespace(String text) {
        return Optional.ofNullable(text)
                .map(str -> str.replaceAll("\\s+", " "))
                .map(str -> str.replaceAll("\\n+", "\n"))
                .map(String::trim)
                .orElse("");
    }

    /**
     * Verifica se o texto está vazio ou contém apenas espaços em branco.
     *
     * @param text O texto para verificar
     * @return true se o texto estiver vazio ou contiver apenas espaços em branco
     */
    public static boolean isEmptyOrWhitespace(String text) {
        return StringUtils.isBlank(text);
    }
}