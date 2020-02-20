package uk.co.autotrader.application

import io.micrometer.core.annotation.Timed
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/echostatus")
@Timed
class EchoStatusController
@Autowired
constructor() {

    @GetMapping("/{status}")
    fun echoStatus(@PathVariable status: String): ResponseEntity<Any> {
        return ResponseEntity(HttpStatus.resolve(status.toInt()))
    }

}