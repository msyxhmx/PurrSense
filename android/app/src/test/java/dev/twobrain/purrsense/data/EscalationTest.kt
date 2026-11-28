package dev.twobrain.purrsense.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** The pain alert and Deep Check rule, with the defaults from Remote Config (threshold 4, offset 2). */
class EscalationTest {

    private val threshold = 4
    private val offset = 2

    private fun read(total: Int, cat: Boolean = true, signs: List<String> = emptyList()): QuickRead {
        // Spread the total over the five units, each 0-2.
        val units = IntArray(5) { i -> (total - 2 * i).coerceIn(0, 2) }
        return QuickRead(
            cat = cat,
            faceVisible = cat,
            fgs = Grimace(units[0], units[1], units[2], units[3], units[4]),
            signs = signs,
        )
    }

    @Test fun `grimace total 2 alerts once the offset is added`() {
        assertEquals(4, read(2).fgs.painScore(offset))
        assertTrue(needsDeepCheck(read(2), threshold, offset))
    }

    @Test fun `grimace total 1 does not alert`() {
        assertEquals(3, read(1).fgs.painScore(offset))
        assertFalse(needsDeepCheck(read(1), threshold, offset))
    }

    @Test fun `zero offset needs a raw total of 4`() {
        assertFalse(needsDeepCheck(read(3), threshold, 0))
        assertTrue(needsDeepCheck(read(4), threshold, 0))
    }

    @Test fun `visible health signs alert even with a low score`() {
        assertTrue(needsDeepCheck(read(0, signs = listOf("eye discharge")), threshold, offset))
    }

    @Test fun `no cat never escalates`() {
        assertFalse(needsDeepCheck(read(10, cat = false, signs = listOf("squinting")), threshold, offset))
    }

    @Test fun `pain score is capped at 10`() {
        assertEquals(10, read(10).fgs.painScore(offset))
    }

    @Test fun `out-of-range unit scores count as 2 at most`() {
        assertEquals(2, Grimace(ears = 7).total)
        assertEquals(0, Grimace(ears = -3).total)
    }
}
