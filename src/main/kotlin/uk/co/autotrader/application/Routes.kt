package uk.co.autotrader.application

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.Mono


@Configuration
class Routes(private val failureHandler: FailureHandler) {

    @Bean
    fun router() = router {
        GET("/") { _ ->
            ServerResponse.ok().bodyValue("This kraken is running and ready to cause some chaos.")
        }
        POST("/simulate/{failureType}") {
            request -> failureHandler.processFailure(request)
        }
    }

}

@Component
class FailureHandler(private val failureSimulator: FailureSimulator) {

    fun processFailure(request: ServerRequest): Mono<ServerResponse> {

        return if (failureSimulator.run(request.pathVariable("failureType"), request.queryParams().toSingleValueMap())) {
            ServerResponse.ok().build()
        } else {
            ServerResponse.badRequest().bodyValue("Unrecognised failure. Failed at failing this service.")
        }

    }

}

