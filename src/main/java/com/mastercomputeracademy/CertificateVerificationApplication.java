package com.mastercomputeracademy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Master Computer Academy – Certificate Verification System
 *
 * Address : Wathoda Layout, Lok Kalyan Society, Anmol Nagar,
 *           Dighori, Nagpur, Maharashtra 440034
 * Phone   : 9156348591
 */
@SpringBootApplication
@EnableJpaAuditing
public class CertificateVerificationApplication {

    public static void main(String[] args) {
        SpringApplication.run(CertificateVerificationApplication.class, args);
    }
}
