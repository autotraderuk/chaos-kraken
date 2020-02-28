package uk.co.autotrader.application

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyMap
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.getForEntity
import org.springframework.http.HttpStatus

const val emptyRequestBody = ""

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SimulationControllerWithRealFailureSimulatorShould(@Autowired val restTemplate: TestRestTemplate) {

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
}

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SimulationControllerWithFakeFailureSimulatorShould(@Autowired val restTemplate: TestRestTemplate) {

    @MockBean
    private lateinit var failureSimulator: FailureSimulator

    @Test
    fun `simulate killapp failure`() {
        whenever(failureSimulator.run(eq("killapp"), anyMap())).thenReturn(true)

        val response = restTemplate.postForEntity("/simulate/killapp", emptyRequestBody, String::class.java)
        assertThat(response.statusCode, equalTo(HttpStatus.OK))

        verify(failureSimulator).run(eq("killapp"), anyMap())

    }
}
