package uk.co.autotrader.application

import io.micrometer.core.annotation.Timed
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/simulate")
@Timed
class SimulationController(private var failureSimulator: FailureSimulator) {

    @PostMapping("/{failureName}")
    fun simulateFailure(@PathVariable failureName: String, @RequestParam params: Map<String, String>): ResponseEntity<String> {

        return if (failureSimulator.run(failureName, params))
            ResponseEntity.ok().build()
        else
            ResponseEntity.badRequest().body("Unrecognised failure. Failed at failing this service.")
    }

}