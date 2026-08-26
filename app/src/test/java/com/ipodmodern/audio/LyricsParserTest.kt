package com.ipodmodern.audio

import com.ipodmodern.audio.core.parser.LyricsParser
import org.junit.Assert.assertEquals
import org.junit.Test

class LyricsParserTest {

    @Test
    fun testParseLrc() {
        val parser = LyricsParser()
        val lrc = """
            [00:04.50]Ticking away the moments that make up a dull day
            [00:11.20]Fritter and waste the hours in an offhand way
            [00:18.00]Kicking around on a piece of ground in your hometown
        """.trimIndent()

        val parsed = parser.parseLrc(lrc)
        assertEquals(3, parsed.size)
        assertEquals(4500L, parsed[0].timeMs)
        assertEquals("Ticking away the moments that make up a dull day", parsed[0].text)
        assertEquals(11200L, parsed[1].timeMs)
        assertEquals(18000L, parsed[2].timeMs)
    }

    @Test
    fun testBinarySearchActiveLyric() {
        val parser = LyricsParser()
        val lrc = """
            [00:05.00]Line 1
            [00:10.00]Line 2
            [00:15.00]Line 3
        """.trimIndent()
        val parsed = parser.parseLrc(lrc)

        // Before first line
        assertEquals(-1, parser.findActiveLyricIndex(parsed, 2000L))

        // Exactly on line 1
        assertEquals(0, parser.findActiveLyricIndex(parsed, 5000L))

        // In between line 1 and 2
        assertEquals(0, parser.findActiveLyricIndex(parsed, 7500L))

        // Line 2
        assertEquals(1, parser.findActiveLyricIndex(parsed, 12000L))

        // After line 3
        assertEquals(2, parser.findActiveLyricIndex(parsed, 25000L))
    }
}
