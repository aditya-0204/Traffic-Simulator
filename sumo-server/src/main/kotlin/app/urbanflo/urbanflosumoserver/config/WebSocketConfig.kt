package app.urbanflo.urbanflosumoserver.config

import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

/**
 * Configuration class for Spring WebSocket.
 */
@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig : WebSocketMessageBrokerConfigurer {

    override fun configureMessageBroker(config: MessageBrokerRegistry) {
        config.setApplicationDestinationPrefixes("/app")
        config.enableSimpleBroker("/topic", "/queue/") // topic is for broadcast, and queue is for private
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        // this path should be in the main socket url
        registry.addEndpoint("/simulation-socket").setAllowedOriginPatterns("*")
        registry.addEndpoint("/simulation-socket").setAllowedOriginPatterns("*").withSockJS()
    }
}