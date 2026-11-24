package sk.momosilabs.truckTrack.security

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.keycloak")
data class KeycloakProperties(
    val issuer: String,
    val jwksUri: String,
    val audience: String,
)
