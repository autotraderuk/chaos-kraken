package uk.co.autotrader.application

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SimulationControllerShould(@Autowired val restTemplate: TestRestTemplate) {

    @Test
    fun `return bad request for unknown failure`() {
        val entity = restTemplate.postForEntity("/simulate/unknown", null, String::class.java)
        assertThat(entity.statusCode, equalTo(HttpStatus.BAD_REQUEST))
        assertThat(entity.body, equalTo("Unrecognised failure. Failed at failing this service."))
    }

}