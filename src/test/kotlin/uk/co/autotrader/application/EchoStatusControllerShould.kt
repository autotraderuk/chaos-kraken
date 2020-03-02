package uk.co.autotrader.application

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EchoStatusControllerShould(@Autowired val restTemplate: TestRestTemplate) {

    @Test
    fun `respond with valid status code`() {
        val response = restTemplate.getForEntity("/echostatus/418", String::class.java)
        assertThat(response.statusCode, equalTo(HttpStatus.I_AM_A_TEAPOT))
    }

    @Test
    fun `respond with bad request for invalid status code`() {
        val response = restTemplate.getForEntity("/echostatus/999", String::class.java)
        assertThat(response.statusCode, equalTo(HttpStatus.BAD_REQUEST))
    }
}