package social.utils.docker

import org.apache.logging.log4j.LogManager
import java.io.File

/**
 * Abstract class to be extended by tests that need to start docker-compose services.
 */
abstract class DockerTest {
    private val logger = LogManager.getLogger(this::class)

    fun executeDockerComposeCmd(composeFile: File, vararg arguments: String) {
        if (!composeFile.exists()) {
            throw IllegalStateException("File not found: ${composeFile.absolutePath}")
        }

        val command = mutableListOf("docker", "compose", "-f", composeFile.absolutePath)
        command.addAll(arguments)

        val processBuilder = ProcessBuilder()
            .command(command)
            .redirectErrorStream(true)
            .directory(composeFile.parentFile)

        val process = processBuilder.start()
        val output = process.inputStream.bufferedReader().use { it.readText() } // Capture output

        val exitCode = process.waitFor()

        if (exitCode != 0) {
            logger.error("Error when starting docker-compose: $output")
            throw RuntimeException("Failed to start docker-compose: Exit code $exitCode, output: $output")
        }
    }
}
