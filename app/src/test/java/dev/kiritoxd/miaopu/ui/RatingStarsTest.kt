package dev.kiritoxd.miaopu.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class RatingStarsTest {
    @Test
    fun `maps ten point average to fractional five star fill`() {
        val fractions = ratingStarFillFractions(scoreAverage = 8.4, scoreCount = 12)

        assertEquals(listOf(1f, 1f, 1f, 1f, 0.2f), fractions)
    }

    @Test
    fun `leaves all stars empty when nobody has rated`() {
        assertEquals(List(5) { 0f }, ratingStarFillFractions(scoreAverage = 10.0, scoreCount = 0))
    }
}
