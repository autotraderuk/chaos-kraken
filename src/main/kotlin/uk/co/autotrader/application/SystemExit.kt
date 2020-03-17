package uk.co.autotrader.application

import org.springframework.stereotype.Component

interface SystemExit {
    fun exitProcess(status: Int)
}

@Component
class SystemExitImpl : SystemExit {
    override fun exitProcess(status: Int) {
        exitProcess(status)
    }
}
