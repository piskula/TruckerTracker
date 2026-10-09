package sk.momosilabs.truckTrack.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .authorizeHttpRequests {
                it.requestMatchers(
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**",
                ).permitAll()
                it.requestMatchers(
                    "/api/v1/version",
                ).permitAll()
                it.requestMatchers(
                    "/actuator/health",
                    "/actuator/health/**",
                ).permitAll()
                it.anyRequest().authenticated()
            }
            .oauth2ResourceServer { it.jwt {} }
        return http.build()
    }

    @Bean
    fun jwtAuthenticationConverter(): JwtAuthenticationConverter {
        val converter = JwtAuthenticationConverter()
        converter.setJwtGrantedAuthoritiesConverter { jwt ->
            val realmAccess = jwt.claims["realm_access"] as? Map<*, *>
            val roles = realmAccess?.get("roles") as? List<*> ?: emptyList<Any>()
            roles.filterIsInstance<String>().map { SimpleGrantedAuthority(it) }
        }
        return converter
    }
}
