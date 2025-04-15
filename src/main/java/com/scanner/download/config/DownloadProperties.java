package com.scanner.download.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "download")
@Getter
@Setter
public class DownloadProperties {

    /**
     * Directory where downloaded files will be saved.
     */
    private String dir;

}
