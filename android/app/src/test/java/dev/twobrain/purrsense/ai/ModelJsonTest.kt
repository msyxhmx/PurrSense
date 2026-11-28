package dev.twobrain.purrsense.ai

import dev.twobrain.purrsense.data.DeepCheck
import dev.twobrain.purrsense.data.IdMatch
import dev.twobrain.purrsense.data.QuickRead
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ModelJsonTest {

    private val clean = """{"cat":true,"face_visible":true,"appearance":"grey tabby","mood":"neutral",
        "mood_confidence":0.7,"fgs":{"ears":1,"eyes":0,"muzzle":1,"whiskers":0,"head":0},"signs":[]}"""

    @Test fun `clean JSON is read`() {
        val r = ModelJson.decode<QuickRead>(clean)!!
        assertTrue(r.cat)
        assertEquals(2, r.fgs.total)
        assertEquals("grey tabby", r.appearance)
    }

    @Test fun `JSON inside a code fence and prose is read`() {
        val text = "Here is the result:\n```json\n$clean\n```\nLet me know if you need more."
        assertEquals(2, ModelJson.decode<QuickRead>(text)!!.fgs.total)
    }

    @Test fun `a note with braces after the object does not break it`() {
        val text = "$clean\nNote: scores use {0,1,2}."
        assertNotNull(ModelJson.decode<QuickRead>(text))
    }

    @Test fun `braces inside strings are ignored`() {
        val text = """{"cat":true,"appearance":"white {patch} on chest","fgs":{"ears":2}}"""
        val r = ModelJson.decode<QuickRead>(text)!!
        assertEquals("white {patch} on chest", r.appearance)
        assertEquals(2, r.fgs.total)
    }

    @Test fun `scores written as decimals or strings are rounded, not dropped`() {
        val text = """{"cat":true,"fgs":{"ears":1.0,"eyes":"2","muzzle":"1.4","whiskers":"high","head":null}}"""
        val r = ModelJson.decode<QuickRead>(text)!!
        assertEquals(1, r.fgs.ears)
        assertEquals(2, r.fgs.eyes)
        assertEquals(1, r.fgs.muzzle)
        assertEquals(0, r.fgs.whiskers)
        assertEquals(0, r.fgs.head)
    }

    @Test fun `yes-no flags written as words are read`() {
        val r = ModelJson.decode<QuickRead>("""{"cat":"yes","face_visible":"false"}""")!!
        assertTrue(r.cat)
        assertFalse(r.faceVisible)
    }

    @Test fun `a single sign as a string becomes a list, and none becomes empty`() {
        assertEquals(listOf("eye discharge"), ModelJson.decode<QuickRead>("""{"signs":"eye discharge"}""")!!.signs)
        assertEquals(emptyList<String>(), ModelJson.decode<QuickRead>("""{"signs":"none"}""")!!.signs)
        assertEquals(emptyList<String>(), ModelJson.decode<QuickRead>("""{"signs":["none"]}""")!!.signs)
    }

    @Test fun `trailing commas are accepted`() {
        assertNotNull(ModelJson.decode<QuickRead>("""{"cat":true,"signs":[],}"""))
    }

    @Test fun `deep check and id match are read the same way`() {
        val d = ModelJson.decode<DeepCheck>("""```{"urgency":"vet_soon","fgs":{"eyes":"2"},"findings":"squinting","summary":"ok"}```""")!!
        assertEquals("vet_soon", d.urgency)
        assertEquals(2, d.fgs.eyes)
        assertEquals(listOf("squinting"), d.findings)
        assertEquals("abc", ModelJson.decode<IdMatch>("""{"cat_id":"abc","confidence":"0.8"}""")!!.catId)
    }

    @Test fun `no JSON at all gives null`() {
        assertNull(ModelJson.decode<QuickRead>(null))
        assertNull(ModelJson.decode<QuickRead>(""))
        assertNull(ModelJson.decode<QuickRead>("Sorry, I can't see a cat."))
        assertNull(ModelJson.decode<QuickRead>("""{"cat":true,"""))
    }
}
