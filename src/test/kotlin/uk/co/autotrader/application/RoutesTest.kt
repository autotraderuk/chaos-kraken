package uk.co.autotrader.application

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.restdocs.RestDocumentationContextProvider
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint
import org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.document
import org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.documentationConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import java.net.URI
import java.nio.charset.Charset

@ExtendWith(SpringExtension::class, RestDocumentationExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DefaultRouteShould(private val context: ApplicationContext) {

    private lateinit var webTestClient: WebTestClient

    @BeforeEach
    fun setup(restDocumentation: RestDocumentationContextProvider) {
        this.webTestClient = webTestClient(context, restDocumentation)
    }

    @Test
    fun `return a welcome message`() {
        webTestClient
                .get()
                .uri("/")
                .exchange()
                .expectStatus().isOk
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .expectBody()
                .consumeWith { exchangeResult ->
                    val body = exchangeResult.responseBody!!.toString(Charset.forName("UTF-8"))
                    assertThat(body).contains("This kraken is running and ready to cause some chaos.")
                    assertThat(body).contains("Read the <a href=\"docs/index.html\">docs</a>.")
                }
                .consumeWith(document("welcome"))

    }
}

@ExtendWith(SpringExtension::class, RestDocumentationExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SimulateRouteShould(
        private val context: ApplicationContext,
        @LocalServerPort val randomServerPort: Int,
        @Qualifier("custom") val customFailure: CustomFailure) {

    @MockBean
    lateinit var systemExit: SystemExit

    private lateinit var webTestClient: WebTestClient

    @BeforeEach
    fun setup(restDocumentation: RestDocumentationContextProvider) {
        this.webTestClient = webTestClient(context, restDocumentation)
    }

    @Test
    fun `respond with bad request for unknown failure`() {
        webTestClient.post().uri("/simulate/unknown")
                .exchange()
                .expectStatus().isBadRequest
                .expectBody<String>().isEqualTo("Unrecognised failure. Failed at failing this service.")
    }

    @Test
    fun `toggle health to service unavailable`() {
        webTestClient.post()
                .uri("/simulate/toggle-service-health")
                .exchange()
                .expectStatus().isOk

        webTestClient.get()
                .uri(URI("http://localhost:${randomServerPort}/actuator/health"))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
    }

    @Test
    fun `delegate to specific failure type`() {
        val expectedParams = mapOf(Pair("key1", "value1"), Pair("key2", "value2"))

        webTestClient.post()
                .uri("/simulate/custom?key1=value1&key2=value2")
                .exchange()
                .expectStatus().isOk

        assertThat(customFailure.actualParams).isEqualTo(expectedParams)
    }

    @Test
    fun `trigger killapp failure`() {
        webTestClient.post()
                .uri("/simulate/killapp")
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .consumeWith { exchangeResult ->
                    assertThat(exchangeResult.status).isEqualTo(HttpStatus.OK)
                }
                .consumeWith(document("killapp"))

        verify(systemExit, times(1)).exitProcess(1)
    }
}

@ExtendWith(SpringExtension::class, RestDocumentationExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EchoStatusRouteShould(private val context: ApplicationContext) {

    private lateinit var webTestClient: WebTestClient

    @BeforeEach
    fun setup(restDocumentation: RestDocumentationContextProvider) {
        this.webTestClient = webTestClient(context, restDocumentation)
    }

    @Test
    fun `respond with provided valid status code`() {
        webTestClient.get().uri("/echostatus/418")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.I_AM_A_TEAPOT)
    }

    @Test
    fun `respond with bad request for invalid status code`() {
        webTestClient.get().uri("/echostatus/999")
                .exchange()
                .expectStatus().isBadRequest
    }

    @Test
    fun `respond with bad request non-numeric status code`() {
        webTestClient.get().uri("/echostatus/sdfsdf")
                .exchange()
                .expectStatus().isBadRequest
    }
}

fun webTestClient(context: ApplicationContext, restDocumentation: RestDocumentationContextProvider): WebTestClient {
    return WebTestClient
            .bindToApplicationContext(context)
            .configureClient()
            .filter(documentationConfiguration(restDocumentation)
                    .operationPreprocessors()
                    .withRequestDefaults(prettyPrint())
                    .withResponseDefaults(prettyPrint())
            )
            .build()
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
    fun custom(): Failure {
        return CustomFailure()
    }
}
