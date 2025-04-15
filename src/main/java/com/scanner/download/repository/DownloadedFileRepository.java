package com.scanner.download.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import com.scanner.download.model.DownloadedFile;
import java.util.Optional;

public interface DownloadedFileRepository extends JpaRepository<DownloadedFile, Long> {
    Optional<DownloadedFile> findByFileUrl(String fileUrl);
}
