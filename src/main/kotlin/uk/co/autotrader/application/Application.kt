package uk.co.autotrader.application

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
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
@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
