package uk.co.autotrader.application

import io.micrometer.core.annotation.Timed
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

    @GetMapping("/{status}")
    fun echoStatus(@PathVariable status: String): ResponseEntity<Any> {
        val httpStatus = try {
            HttpStatus.valueOf(status.toInt())
        } catch (e: Exception) {
            return ResponseEntity.badRequest().body(e.message)
        }

        return ResponseEntity(httpStatus)
    }

}