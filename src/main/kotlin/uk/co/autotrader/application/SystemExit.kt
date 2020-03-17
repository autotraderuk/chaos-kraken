package uk.co.autotrader.application

import org.springframework.stereotype.Component
import kotlin.system.exitProcess

interface SystemExit {
    fun exit(status: Int)
}

@Component
class SystemExitImpl : SystemExit {
    override fun exit(status: Int) {
        exitProcess(status)
    }
}
