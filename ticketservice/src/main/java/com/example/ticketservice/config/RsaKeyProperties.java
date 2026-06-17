// TicketService/config/RsaKeyProperties.java
package com.example.ticketservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.security.interfaces.RSAPublicKey;

@Component
@ConfigurationProperties(prefix = "rsa")
@Data
public class RsaKeyProperties {
    private RSAPublicKey publicKey;
}