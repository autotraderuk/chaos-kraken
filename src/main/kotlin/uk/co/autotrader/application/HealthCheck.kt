package uk.co.autotrader.application

import org.springframework.boot.actuate.health.AbstractHealthIndicator
import org.springframework.boot.actuate.health.Health
import org.springframework.stereotype.Component

@Component
class HealthCheck: AbstractHealthIndicator() {
    var healthy: Boolean = true

    override fun doHealthCheck(builder: Health.Builder?) {
        if(healthy) builder?.up() else builder?.down()
    }
}