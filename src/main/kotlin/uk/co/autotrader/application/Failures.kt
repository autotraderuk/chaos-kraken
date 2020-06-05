package uk.co.autotrader.application

import org.apache.commons.lang3.RandomUtils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import uk.co.autotrader.application.FileWriter.writeRandomBytes
import java.io.*
import java.net.ServerSocket
import java.net.Socket
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.stream.Collectors
import java.util.stream.IntStream
import java.util.stream.Stream
import kotlin.collections.ArrayList
import kotlin.concurrent.timer

interface Failure {
    fun fail(params: Map<String, String> = emptyMap())
}

private val LOG = LoggerFactory.getLogger(FailureSimulator::class.java)

@Component
class FailureSimulator(private val failures: Map<String, Failure>) {

    fun run(type: String?, params: Map<String, String> = emptyMap()): Boolean {
        if (!type.isNullOrBlank()) {
            val failure: Failure? = failures[type]
            return if (failure == null) {
                LOG.error("Unknown failure '{}'", type)
                false
            } else {
                LOG.info("Triggering '{}'", type)
                failure.fail(params)
                true
            }
        }
        return false
    }
}

@Component("toggle-service-health")
class ToggleServiceHealth(private val healthCheck: HealthCheck) : Failure {
    override fun fail(params: Map<String, String>) {
        healthCheck.healthy = !healthCheck.healthy
    }
}

@Component("unhealthy-service")
class UnhealthyService(private val healthCheck: HealthCheck) : Failure {
    override fun fail(params: Map<String, String>) {
        healthCheck.healthy = false
    }
}

@Component("memoryleak")
class MemoryLeak : Failure {
    override fun fail(params: Map<String, String>) {
        val allocatedMemory = ArrayList<ByteArray>()

        while (true) {
            try {
                allocatedMemory.add(ByteArray(ONE_KB))
            } catch (outOfMemory: OutOfMemoryError) {
                LOG.debug("Swallowing OOM")
            }
        }
    }
}

@Component("memoryleak-oom")
class MemoryLeakOom : Failure {
    override fun fail(params: Map<String, String>) {
        val allocatedMemory = ArrayList<ByteArray>()

        while (true) {
            allocatedMemory.add(ByteArray(ONE_KB))
        }
    }
}

@Component("wastecpu")
class WasteCpu : Failure {

    override fun fail(params: Map<String, String>) {
        IntStream.range(0, Runtime.getRuntime().availableProcessors())
                .forEach { _ -> Thread(Runnable { this.hashRandomBytes() }).start() }
    }

    private fun hashRandomBytes() {
        while (true) {
            val digest = MessageDigest.getInstance("MD5")
            digest.update(UUID.randomUUID().toString().toByteArray())
        }
    }
}

@Component("threadbomb")
class ThreadBomb : Failure {

    override fun fail(params: Map<String, String>) {
        while (true) {
            val thread = Thread { while (true); }
            thread.start()
        }
    }
}

object FileWriter {

    @Throws(IOException::class)
    fun writeRandomBytes(file: File, size: Int) {
        BufferedOutputStream(FileOutputStream(file)).use { output ->
            var i = 0
            while (i < size) {
                output.write(RandomUtils.nextBytes(1))
                i += 1
            }
        }
    }
}

private const val ONE_GB = 1024 * 1024 * 1024

@Component("diskbomb")
class DiskBomb : Failure {

    override fun fail(params: Map<String, String>) {
        val directoryPaths = listAllDirectories()

        while (true) {
            try {
                val randomFile = findRandomElement(directoryPaths)
                        .resolve(UUID.randomUUID().toString() + ".disk-bomb.run")
                        .toFile()
                writeRandomBytes(randomFile, ONE_GB)
            } catch (ignored: IOException) {
            }
        }
    }

    private fun <R> findRandomElement(list: List<R>): R {
        return list[Random().nextInt(list.size)]
    }

    private fun listAllDirectories(): List<Path> {
        return Arrays.stream(File.listRoots())
                .map { it.toPath() }
                .flatMap { this.listAllSubDirectories(it) }
                .collect(Collectors.toList())
    }

    private fun listAllSubDirectories(root: Path): Stream<Path> {
        return Files.walk(root)
                .filter { path -> path.toFile().isDirectory }
    }
}

@Component("stdoutbomb")
class StandardOutBomb : Failure {
    override fun fail(params: Map<String, String>) {
        timer(
                period = params["periodMillis"]?.toLongOrNull() ?: 1L,
                action = {
                    println("Standard Out Bomb: " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                }
        )
    }
}

@Component("filehandlebomb")
class FileHandleBomb : Failure {
    override fun fail(params: Map<String, String>) {
        val readers = ArrayList<FileReader>()

        while (true) {
            try {
                val tempFile = File.createTempFile(UUID.randomUUID().toString(), ".handle.run")
                val fileReader = FileReader(tempFile)
                readers.add(fileReader)
            } catch (ignored: IOException) {
            }
        }
    }
}

private const val ONE_KB = 1_024

@Component("filecreator")
class FileCreator : Failure {

    override fun fail(params: Map<String, String>) {
        while (true) {
            try {
                val tempFile = File.createTempFile(UUID.randomUUID().toString(), ".file-creator.run")
                writeRandomBytes(tempFile, ONE_KB)
            } catch (ignored: IOException) {
            }
        }
    }
}

@Component("killapp")
class KillApp(val systemExit: SystemExit) : Failure {

    override fun fail(params: Map<String, String>) {
        LOG.error("Application was killed by calling the 'killapp' failure")
        systemExit.exit(1)
    }
}

private const val CONNECTIONS = 5000

@Component("selfconnectionsbomb")
class SelfConnectionsBomb : Failure {

    override fun fail(params: Map<String, String>) {
        val openConnections = ArrayList<Socket>()

        try {
            ServerSocket(0, CONNECTIONS).use { serverSocket ->
                for (i in 0 until CONNECTIONS) {
                    openConnections.add(createLoopbackSocket(serverSocket.localPort))
                }
            }
        } catch (ignored: IOException) {
        } finally {
            closeConnections(openConnections)
        }
    }

    @Throws(IOException::class)
    private fun createLoopbackSocket(port: Int): Socket {
        return Socket(null as String?, port)
    }

    private fun closeConnections(openConnections: List<Socket>) {
        for (socket in openConnections) {
            try {
                socket.close()
            } catch (ignored: IOException) {
            }
        }
    }
}

@Component("directmemoryleak")
class DirectMemoryLeak : Failure {
    override fun fail(params: Map<String, String>) {
        val allocatedMemory = ArrayList<ByteBuffer>()
        val check =
                params["limitMB"]?.toIntOrNull()?.let { limit ->
                    {
                        limit > allocatedMemory.size
                    }
                } ?: { true }

        while (check.invoke()) {
            allocatedMemory.add(ByteBuffer.allocateDirect(ONE_KB * ONE_KB))
        }
    }
}
