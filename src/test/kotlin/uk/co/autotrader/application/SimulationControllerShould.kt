package uk.co.autotrader.application

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.getForEntity
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus

const val emptyRequestBody = ""

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SimulationControllerShould(@Autowired val restTemplate: TestRestTemplate) {

    @Test
    fun `return bad request for unknown failure`() {
        val response = restTemplate.postForEntity("/simulate/unknown", emptyRequestBody, String::class.java)
        assertThat(response.statusCode, equalTo(HttpStatus.BAD_REQUEST))
        assertThat(response.body, equalTo("Unrecognised failure. Failed at failing this service."))
    }

    @Test
    fun `toggle health to service unavailable`() {
        val response = restTemplate.postForEntity("/simulate/toggle-service-health", emptyRequestBody, String::class.java)
        assertThat(response.statusCode, equalTo(HttpStatus.OK))

        val serviceHealth = restTemplate.getForEntity<String>("/actuator/health")
        assertThat(serviceHealth.statusCode, equalTo(HttpStatus.SERVICE_UNAVAILABLE))
    }

    @Test
    fun `echo status code`() {
        val response = restTemplate.getForEntity("/echostatus/418", String::class.java)
        assertThat(response.statusCode, equalTo(HttpStatus.I_AM_A_TEAPOT))
    }

    @Test
    fun `delegate to specific failure type`() {
        val response = restTemplate.postForEntity("/simulate/custom", emptyRequestBody, String::class.java)
        assertThat(response.statusCode, equalTo(HttpStatus.OK))
    }
}

class TestFailure() : Failure {
    override fun fail(params: Map<String, String>) {

    }
}

@Configuration
class TestConfig {
    @Bean("custom")
    fun customFailure(): Failure {
        return TestFailure()
    }
}
