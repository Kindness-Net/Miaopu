package dev.kiritoxd.miaopu.ui

internal data class TargetRequestToken(
    val targetKey: String,
    val generation: Long,
)

/** Keeps late completions from an older target from mutating the active screen state. */
internal class TargetRequestGate {
    private var generation = 0L
    private var activeToken: TargetRequestToken? = null

    fun begin(targetKey: String): TargetRequestToken = TargetRequestToken(
        targetKey = targetKey,
        generation = ++generation,
    ).also { activeToken = it }

    fun isCurrent(token: TargetRequestToken): Boolean = activeToken == token

    fun complete(token: TargetRequestToken): Boolean {
        if (!isCurrent(token)) return false
        activeToken = null
        return true
    }

    fun invalidate() {
        generation++
        activeToken = null
    }
}
