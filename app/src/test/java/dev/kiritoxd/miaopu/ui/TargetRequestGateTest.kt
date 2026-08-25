package dev.kiritoxd.miaopu.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TargetRequestGateTest {
    @Test
    fun `invalidating a target rejects its late completion`() {
        val gate = TargetRequestGate()
        val old = gate.begin("player-a")

        gate.invalidate()

        assertFalse(gate.isCurrent(old))
        assertFalse(gate.complete(old))
    }

    @Test
    fun `an old completion cannot clear the next target`() {
        val gate = TargetRequestGate()
        val old = gate.begin("player-a")
        val current = gate.begin("player-b")

        assertFalse(gate.complete(old))
        assertTrue(gate.isCurrent(current))
        assertTrue(gate.complete(current))
    }

    @Test
    fun `a repeated request for the same target gets a new generation`() {
        val gate = TargetRequestGate()
        val old = gate.begin("player-a")
        val current = gate.begin("player-a")

        assertFalse(gate.complete(old))
        assertTrue(gate.isCurrent(current))
    }
}
