package com.miletoalmeida.leafletapi.service.scraping;

import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Component
public class UserAgentRotator {
    private final List<String> userAgents = Arrays.asList(
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:122.0) Gecko/20100101 Firefox/122.0",
        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36"
    );
    
    private final Random random = new Random();

    public String getRandomUserAgent() {
        return userAgents.get(random.nextInt(userAgents.size()));
    }
}