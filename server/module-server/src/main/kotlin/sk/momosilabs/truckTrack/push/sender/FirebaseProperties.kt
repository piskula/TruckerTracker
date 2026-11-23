package sk.momosilabs.truckTrack.push.sender

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "firebase")
class FirebaseProperties(
    val credentialsBase64: String = "",
)
