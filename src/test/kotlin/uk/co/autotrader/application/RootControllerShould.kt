package uk.co.autotrader.application

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.getForEntity
import org.springframework.http.HttpStatus

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RootControllerShould(@Autowired val restTemplate: TestRestTemplate) {
    @Test
    fun `return a welcome message`() {
        val entity = restTemplate.getForEntity<String>("/")
        assertThat(entity.statusCode, equalTo(HttpStatus.OK))
        assertThat(entity.body, equalTo("This kraken is running and ready to cause some chaos."))
    }
}

