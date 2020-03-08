package uk.co.autotrader.application

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DefaultRouteShould(@LocalServerPort val randomServerPort: Int) {

    val webClient = WebTestClient
            .bindToServer()
            .baseUrl("http://localhost:$randomServerPort")
            .build()

    @Test
    fun `return a welcome message`() {
        webClient.get()
                .exchange()
                .expectStatus().isOk
                .expectBody<String>().isEqualTo("This kraken is running and ready to cause some chaos.")
    }
}

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SimulateRouteShould(@LocalServerPort val randomServerPort: Int, @Qualifier("custom") val customFailure: CustomFailure) {

    val webClient = WebTestClient
            .bindToServer()
            .baseUrl("http://localhost:$randomServerPort")
            .build()

    @Test
    fun `respond with bad request for unknown failure`() {
        webClient.post().uri("/simulate/unknown")
                .exchange()
                .expectStatus().isBadRequest
                .expectBody<String>().isEqualTo("Unrecognised failure. Failed at failing this service.")
    }

    @Test
    fun `toggle health to service unavailable`() {
        webClient.post()
                .uri("/simulate/toggle-service-health")
                .exchange()
                .expectStatus().isOk

        webClient.get()
                .uri("/actuator/health")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
    }

    @Test
    fun `delegate to specific failure type`() {
        val expectedParams = mapOf(Pair("key1", "value1"), Pair("key2", "value2"))

        webClient.post()
                .uri("/simulate/custom?key1=value1&key2=value2")
                .exchange()
                .expectStatus().isOk

        assertThat(customFailure.actualParams, equalTo(expectedParams))
    }
}

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EchoStatusRouteShould(@LocalServerPort val randomServerPort: Int) {

    val webClient = WebTestClient
            .bindToServer()
            .baseUrl("http://localhost:$randomServerPort")
            .build()

    @Test
    fun `respond with provided valid status code`() {
        webClient.get().uri("/echostatus/418")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.I_AM_A_TEAPOT)
    }

    @Test
    fun `respond with bad request for invalid status code`() {
        webClient.get().uri("/echostatus/999")
                .exchange()
                .expectStatus().isBadRequest
    }

    @Test
    fun `respond with bad request non-numeric status code`() {
        webClient.get().uri("/echostatus/sdfsdf")
                .exchange()
                .expectStatus().isBadRequest
    }
}

class CustomFailure : Failure {
    lateinit var actualParams: Map<String, String>

    override fun fail(params: Map<String, String>) {
        actualParams = params
    }
}

@Configuration
class TestConfig {
    @Bean("custom")
    fun customFailure(): Failure {
        return CustomFailure()
    }
}