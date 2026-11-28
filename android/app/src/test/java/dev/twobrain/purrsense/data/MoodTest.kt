package dev.twobrain.purrsense.data

import org.junit.Assert.assertEquals
import org.junit.Test

class MoodTest {

    @Test fun `the three labels map to themselves`() {
        assertEquals(Mood.POSITIVE, Mood.normalize("positive"))
        assertEquals(Mood.NEUTRAL, Mood.normalize("neutral"))
        assertEquals(Mood.NEGATIVE, Mood.normalize("negative"))
    }

    @Test fun `case, spaces and punctuation are ignored`() {
        assertEquals(Mood.POSITIVE, Mood.normalize("  Relaxed. "))
        assertEquals(Mood.NEGATIVE, Mood.normalize("NEGATIVE!"))
    }

    @Test fun `short phrases are understood`() {
        assertEquals(Mood.NEUTRAL, Mood.normalize("mostly calm"))
        assertEquals(Mood.POSITIVE, Mood.normalize("content and playful"))
    }

    @Test fun `any negative word wins, so a worrying answer is never shown as fine`() {
        assertEquals(Mood.NEGATIVE, Mood.normalize("relaxed but slightly tense"))
    }

    @Test fun `older eight labels still map`() {
        assertEquals(Mood.NEGATIVE, Mood.normalize("fearful"))
        assertEquals(Mood.NEUTRAL, Mood.normalize("sleepy"))
    }

    @Test fun `unrecognised or empty answers are unknown`() {
        assertEquals("unknown", Mood.normalize(""))
        assertEquals("unknown", Mood.normalize("purple"))
    }
}
