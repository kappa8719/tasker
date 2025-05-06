package kr.entropi.tasker.util

import java.net.NetworkInterface
import java.security.SecureRandom
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * A Snowflake ID generator that produces unique, time-sortable 64-bit IDs.
 * Thread-safe implementation suitable for distributed systems.
 */
class SnowflakeIdGenerator @JvmOverloads constructor(
    val nodeId: Long = createNodeId(),
    val epoch: Long = TWITTER_EPOCH
) {
    companion object {
        private const val UNUSED_BITS = 1 // Sign bit, always 0
        private const val TIMESTAMP_BITS = 41
        private const val NODE_ID_BITS = 10
        private const val SEQUENCE_BITS = 12

        private const val MAX_NODE_ID = (1L shl NODE_ID_BITS) - 1
        private const val MAX_SEQUENCE = (1L shl SEQUENCE_BITS) - 1

        private val TWITTER_EPOCH = LocalDateTime.of(2015, 1, 1, 0, 0, 0)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        /**
         * Creates a node ID based on machine's MAC address or random number if unavailable
         */
        fun createNodeId(): Long {
            try {
                val sb = StringBuilder()
                val networkInterfaces = NetworkInterface.getNetworkInterfaces()

                while (networkInterfaces.hasMoreElements()) {
                    val networkInterface = networkInterfaces.nextElement()
                    val mac = networkInterface.hardwareAddress

                    if (mac != null) {
                        for (macByte in mac) {
                            sb.append(String.format("%02X", macByte))
                        }
                    }
                }

                return (sb.toString().hashCode() and 0xfffffff).toLong()
            } catch (ex: Exception) {
                // If we cannot get the MAC address, use a random number
                return SecureRandom().nextInt(1024).toLong()
            }
        }
    }

    @Volatile
    private var lastTimestamp = -1L

    @Volatile
    private var sequence = 0L

    init {
        require(nodeId in 0..MAX_NODE_ID) {
            "Node ID must be between 0 and $MAX_NODE_ID"
        }
    }

    /**
     * Generates a new Snowflake ID
     * @return a unique Snowflake ID
     */
    @Synchronized
    fun nextId(): Snowflake {
        var currentTimestamp = timestamp()

        check(currentTimestamp >= lastTimestamp) {
            "Clock moved backwards! Refusing to generate ID for ${lastTimestamp - currentTimestamp} milliseconds"
        }

        if (currentTimestamp == lastTimestamp) {
            sequence = (sequence + 1) and MAX_SEQUENCE
            if (sequence == 0L) {
                // Sequence exhausted, wait for next millisecond
                currentTimestamp = waitNextMillis(currentTimestamp)
            }
        } else {
            // Reset sequence for new millisecond
            sequence = 0L
        }

        lastTimestamp = currentTimestamp

        return Snowflake(
            (currentTimestamp shl (NODE_ID_BITS + SEQUENCE_BITS)) or
                    (nodeId shl SEQUENCE_BITS) or
                    sequence
        )
    }

    /**
     * Parses a Snowflake ID back into its components
     * @return Array of [timestamp, nodeId, sequence]
     */
    fun parse(id: Snowflake): ParsedSnowflake {
        val timestamp = (id.value shr (NODE_ID_BITS + SEQUENCE_BITS)) + epoch
        val nodeId = (id.value shr SEQUENCE_BITS) and MAX_NODE_ID
        val sequence = id.value and MAX_SEQUENCE

        return ParsedSnowflake(
            timestamp = timestamp,
            nodeId = nodeId.toInt(),
            sequence = sequence.toInt()
        )
    }

    /**
     * Gets the timestamp in milliseconds since custom epoch
     */
    private fun timestamp(): Long {
        return Instant.now().toEpochMilli() - epoch
    }

    /**
     * Waits until next millisecond
     */
    private fun waitNextMillis(currentTimestamp: Long): Long {
        var timestamp = currentTimestamp
        while (timestamp == lastTimestamp) {
            timestamp = timestamp()
        }
        return timestamp
    }
}
