package uk.co.autotrader.application

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/")
class RootController
@Autowired
constructor() {

    @GetMapping
    fun hello(): ResponseEntity<String> {
        return ResponseEntity.ok("This monkey is running and ready to cause some chaos.")
    }
}