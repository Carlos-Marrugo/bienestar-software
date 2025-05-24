package com.unicolombo.bienestar.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;

@Service
@Slf4j
public class ImageService {

    private RestTemplate restTemplate;

    //@Value("${supabase.api-url}")
    private String apiUrl;

    //@Value("${supabase.bucket-url}")
    private String bucketUrl;

    //@Value("${supabase.api-key}")
    private String apiKey;

    public ImageService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.apiUrl = "https://mohtjinmlocgdsipupju.supabase.co";
        this.bucketUrl = "bienestar";
        this.apiKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im1vaHRqaW5tbG9jZ2RzaXB1cGp1Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDY2NDUxMDIsImV4cCI6MjA2MjIyMTEwMn0.5VS8dutvz1-nLLPT8l_YJBTZx7SO9Fbwl_EultbGQVA";
    }

    public String uploadInitialsImage(String initials, String fileName) {
        try {
            BufferedImage image = generateImage(initials);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            byte[] imageBytes = baos.toByteArray();

            fileName = fileName + "_" + Instant.now().getEpochSecond() + ".png";

            uploadToSupabase(imageBytes, fileName);
        }catch (IOException e) {
            throw new RuntimeException(e);
        }

        return apiUrl.replace("/rest", "") + "/storage/v1/object/public/" + bucketUrl + "/" + fileName;
    }

    private BufferedImage generateImage(String initials) {
        int width = 200, height = 200;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        g.setColor(Color.BLUE);
        g.setFont(new Font("Arial", Font.BOLD, 72));
        FontMetrics fm = g.getFontMetrics();

        int x = (width - fm.stringWidth(initials)) / 2;
        int y = ((height - fm.getHeight()) / 2) + fm.getAscent();
        g.drawString(initials.toUpperCase(), x, y);
        g.dispose();

        return image;
    }

    private void uploadToSupabase(byte[] data, String fileName) {
        String endpoint = apiUrl + "/storage/v1/object/" + bucketUrl + "/" + fileName;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("x-upsert", "true");
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        HttpEntity<byte[]> request = new HttpEntity<>(data, headers);

        ResponseEntity<String> response = restTemplate.exchange(endpoint, HttpMethod.PUT, request, String.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Upload failed: " + response.getStatusCode() + " - " + response.getBody());
        }
    }
}
