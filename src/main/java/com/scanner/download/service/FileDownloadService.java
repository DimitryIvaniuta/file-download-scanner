package com.scanner.download.service;

import com.scanner.download.config.DownloadProperties;
import com.scanner.download.config.PortalProperties;
import com.scanner.download.model.DownloadedFile;
import com.scanner.download.repository.DownloadedFileRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
public class FileDownloadService {

    private final PortalProperties portalProperties;
    private final DownloadProperties downloadProperties;
    private final DownloadedFileRepository downloadedFileRepository;
    private final RestTemplate restTemplate;
    private final GoogleOAuth2Service googleOAuth2Service;

    public FileDownloadService(PortalProperties portalProperties,
                               DownloadProperties downloadProperties,
                               DownloadedFileRepository downloadedFileRepository,
                               GoogleOAuth2Service googleOAuth2Service) {
        this.portalProperties = portalProperties;
        this.downloadProperties = downloadProperties;
        this.downloadedFileRepository = downloadedFileRepository;
        this.googleOAuth2Service = googleOAuth2Service;
        this.restTemplate = new RestTemplate();
    }

    /**
     * Fetches the protected portal page using a valid (and refreshed, if needed) Google access token,
     * scrapes file links, and downloads files that have not been downloaded before.
     */
    @Transactional
    public void fetchAndDownloadFiles(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            log.error("User is not authenticated! Aborting download process.");
            return;
        }

        // Retrieve a fresh access token using the OAuth2AuthorizedClientManager.
        String accessToken = googleOAuth2Service.getAccessToken(authentication);
        if (accessToken == null) {
            log.error("Failed to retrieve a valid access token.");
            return;
        }

        // Build request headers including the Bearer token.
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    portalProperties.getResourceUrl(),
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            String htmlContent = response.getBody();
            if (htmlContent == null) {
                log.info("No content returned from portal.");
                return;
            }

            Document doc = Jsoup.parse(htmlContent);
            Elements links = doc.select("a[href]");

            for (Element link : links) {
                String fileUrl = link.absUrl("href");
                if (!isDownloadable(fileUrl)) {
                    continue;
                }
                Optional<DownloadedFile> existingFileOpt = downloadedFileRepository.findByFileUrl(fileUrl);
                if (existingFileOpt.isPresent()) {
                    log.info("File already downloaded: {}", fileUrl);
                    continue;
                }
                downloadFile(fileUrl, headers);
            }
        } catch (Exception ex) {
            log.error("Error fetching or parsing the portal page: {}", ex.getMessage());
        }
    }

    /**
     * Downloads a single file from the provided URL using the given HTTP headers.
     */
    private void downloadFile(String fileUrl, HttpHeaders headers) {
        try {
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
            ResponseEntity<byte[]> fileResponse = restTemplate.exchange(
                    fileUrl, HttpMethod.GET, requestEntity, byte[].class
            );

            if (fileResponse.getStatusCode().is2xxSuccessful() && fileResponse.getBody() != null) {
                String fileName = extractFileNameFromUrl(fileUrl);
                File dir = new File(downloadProperties.getDir());
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                File localFile = new File(dir, fileName);
                try (FileOutputStream fos = new FileOutputStream(localFile)) {
                    fos.write(fileResponse.getBody());
                }
                DownloadedFile downloadedFile = new DownloadedFile(
                        fileUrl,
                        localFile.getAbsolutePath(),
                        LocalDateTime.now()
                );
                downloadedFileRepository.save(downloadedFile);
                log.info("Downloaded and saved file: " + localFile.getAbsolutePath());
            } else {
                log.info("Failed to download file: " + fileUrl
                        + " (HTTP " + fileResponse.getStatusCode() + ")");
            }
        } catch (Exception e) {
            log.error("Error downloading file: {} => {}", fileUrl, e.getMessage());
        }
    }

    /**
     * Determines if a URL points to a file of interest in checking its extension.
     */
    private boolean isDownloadable(String url) {
        String lowerUrl = url.toLowerCase();
        return lowerUrl.endsWith(".pdf")
                || lowerUrl.endsWith(".zip")
                || lowerUrl.endsWith(".docx")
                || lowerUrl.endsWith(".xlsx")
                || lowerUrl.endsWith(".txt");
    }

    /**
     * Extracts a file name from a URL.
     */
    private String extractFileNameFromUrl(String url) {
        String fileName = url.substring(url.lastIndexOf('/') + 1);
        return fileName.isEmpty() ? "file_" + System.currentTimeMillis() : fileName;
    }
}
