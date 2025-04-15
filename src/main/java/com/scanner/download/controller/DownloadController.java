package com.scanner.download.controller;

import com.scanner.download.dto.DownloadResponseDTO;
import com.scanner.download.model.DownloadedFile;
import com.scanner.download.repository.DownloadedFileRepository;
import com.scanner.download.service.FileDownloadService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class DownloadController {

    private final FileDownloadService fileDownloadService;
    private final DownloadedFileRepository downloadedFileRepository;

    public DownloadController(FileDownloadService fileDownloadService,
                              DownloadedFileRepository downloadedFileRepository) {
        this.fileDownloadService = fileDownloadService;
        this.downloadedFileRepository = downloadedFileRepository;
    }

    /**
     * Triggers the file download process. Must be called with a valid Authorization header.
     */
    @PostMapping("/download-files")
    public ResponseEntity<String> downloadFiles(Authentication authentication) {
        fileDownloadService.fetchAndDownloadFiles(authentication);
        return ResponseEntity.ok("Download process initiated");
    }

    /**
     * Returns a list of all downloaded files.
     */
    @GetMapping("/downloaded-files")
    public ResponseEntity<List<DownloadResponseDTO>> getDownloadedFiles() {
        List<DownloadResponseDTO> files = downloadedFileRepository.findAll().stream()
                .map(file -> new DownloadResponseDTO(file.getFileUrl(),
                        file.getLocalPath(),
                        file.getDownloadedAt()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(files);
    }
}
