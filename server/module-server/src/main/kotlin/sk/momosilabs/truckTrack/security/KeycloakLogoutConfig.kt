package sk.momosilabs.truckTrack.security

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(KeycloakProperties::class)
class KeycloakLogoutConfig
