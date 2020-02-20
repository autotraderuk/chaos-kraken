package uk.co.autotrader.application

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import javax.annotation.PostConstruct

@SpringBootApplication
class Application
@Autowired
constructor(private val failureSimulator: FailureSimulator) {
    @PostConstruct
    fun failOnStart() {
        val failureType: String? = System.getenv("FAIL_ON_START")
        failureSimulator.run(failureType)
    }
}

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}
