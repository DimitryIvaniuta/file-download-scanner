package com.scanner.download.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "downloaded_files")
@Getter
@Setter
public class DownloadedFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // URL used to identify the downloaded file
    @Column(nullable = false, unique = true)
    private String fileUrl;

    // Local path where the file was saved
    @Column(nullable = false)
    private String localPath;

    // Timestamp when the file was downloaded
    @Column(nullable = false)
    private LocalDateTime downloadedAt;

    public DownloadedFile() {}

    public DownloadedFile(String fileUrl, String localPath, LocalDateTime downloadedAt) {
        this.fileUrl = fileUrl;
        this.localPath = localPath;
        this.downloadedAt = downloadedAt;
    }

}
