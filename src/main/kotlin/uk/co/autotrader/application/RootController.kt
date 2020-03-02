package uk.co.autotrader.application

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/")
class RootController {

    @GetMapping
    fun hello(): ResponseEntity<String> {
        return ResponseEntity.ok("This kraken is running and ready to cause some chaos.")
    }
}