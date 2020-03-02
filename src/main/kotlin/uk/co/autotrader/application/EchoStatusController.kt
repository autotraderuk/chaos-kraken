package uk.co.autotrader.application

import io.micrometer.core.annotation.Timed
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/echostatus")
@Timed
class EchoStatusController {

    private val LOG = LoggerFactory.getLogger(EchoStatusController::class.java)

    @GetMapping("/{status}")
    fun echoStatus(@PathVariable status: String): ResponseEntity<Any> {
        val httpStatus = try {
            HttpStatus.valueOf(status.toInt())
        } catch (illegalArgumentException: IllegalArgumentException) {
            LOG.error("Failed to parse status: $status", illegalArgumentException)
            return ResponseEntity.badRequest().body("Failed to parse provided HTTP status code: $status")
        }

        return ResponseEntity(httpStatus)
    }

}