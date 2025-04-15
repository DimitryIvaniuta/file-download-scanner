package com.scanner.download.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.portal")
@Getter
@Setter
public class PortalProperties {

    /**
     * URL of the protected portal page.
     */
    private String resourceUrl;

}
