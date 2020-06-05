package uk.co.autotrader.application

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.TEXT_HTML
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.Mono

@Configuration
class Routes(private val echoStatusHandler: EchoStatusHandler, private val failureHandler: FailureHandler) {

    @Bean
    fun root() = router {
        GET("/") { _ -> ServerResponse.ok().contentType(TEXT_HTML).bodyValue(WELCOME_MESSAGE) }
    }

    @Bean
    fun simulate() = router {
        POST("/simulate/{failureType}") { request -> failureHandler.processFailure(request) }
    }

    @Bean
    fun echoStatus() = router {
        GET("/echostatus/{status}") { request -> echoStatusHandler.responseStatus(request) }
    }

}

val WELCOME_MESSAGE = """
        This kraken is running and ready to cause some chaos.
        <p>
        Read the <a href="docs/index.html">docs</a>.
    """.trimIndent()

@Component
class FailureHandler(private val failureSimulator: FailureSimulator) {

    fun processFailure(request: ServerRequest): Mono<ServerResponse> {

        return if (failureSimulator.run(
                        request.pathVariable("failureType"),
                        request.queryParams().toSingleValueMap())
        ) {
            ServerResponse.ok().build()
        } else {
            ServerResponse.badRequest().bodyValue("Unrecognised failure. Failed at failing this service.")
        }
    }
}

@Component
class EchoStatusHandler {
    private val log = LoggerFactory.getLogger(EchoStatusHandler::class.java)

    @Suppress("TooGenericExceptionCaught")
    fun responseStatus(request: ServerRequest): Mono<ServerResponse> {
        val responseHttpStatus = try {
            val requestHttpStatus = request.pathVariable("status").toInt()
            HttpStatus.valueOf(requestHttpStatus)
        } catch (exception: Exception) {
            when (exception) {
                is IllegalArgumentException, is NumberFormatException -> {
                    log.error("Failed to parse status", exception)
                    return ServerResponse.badRequest().bodyValue("Failed to parse the provided HTTP status code")
                }
                else -> throw exception
            }
        }

        return ServerResponse.status(responseHttpStatus).build()
    }
}
