package com.ipodmodern.audio

import com.ipodmodern.audio.core.parser.CueSheetParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class CueSheetParserTest {

    @Test
    fun testTimecodeFrameConversion() {
        val parser = CueSheetParser()

        // 01:23:45 -> 1 min (60,000ms) + 23 sec (23,000ms) + 45 frames (45 * 1000 / 75 = 600ms)
        // Total = 83,600 ms
        val ms = parser.parseTimecodeToMs("01:23:45")
        assertEquals(83600L, ms)

        // 00:00:00 -> 0 ms
        assertEquals(0L, parser.parseTimecodeToMs("00:00:00"))

        // 00:01:00 -> 1000 ms
        assertEquals(1000L, parser.parseTimecodeToMs("00:01:00"))
    }

    @Test
    fun testParseFullCueSheet() {
        val parser = CueSheetParser()
        val cueText = """
            REM GENRE "Progressive Rock"
            REM DATE 1973
            PERFORMER "Pink Floyd"
            TITLE "The Dark Side of the Moon"
            FILE "dark_side.flac" WAVE
              TRACK 01 AUDIO
                TITLE "Speak to Me"
                PERFORMER "Pink Floyd"
                INDEX 01 00:00:00
              TRACK 02 AUDIO
                TITLE "Breathe"
                PERFORMER "Pink Floyd"
                INDEX 01 01:30:00
              TRACK 03 AUDIO
                TITLE "On the Run"
                PERFORMER "Pink Floyd"
                INDEX 01 04:13:50
        """.trimIndent()

        val cueSheet = parser.parseContent(cueText)
        assertEquals("Pink Floyd", cueSheet.performer)
        assertEquals("The Dark Side of the Moon", cueSheet.title)
        assertEquals("dark_side.flac", cueSheet.audioFileName)
        assertEquals(3, cueSheet.tracks.size)

        // Track 1
        val t1 = cueSheet.tracks[0]
        assertEquals("Speak to Me", t1.title)
        assertEquals(0L, t1.startMs)
        assertEquals(90000L, t1.endMs) // 1 min 30 sec = 90,000 ms

        // Track 2
        val t2 = cueSheet.tracks[1]
        assertEquals("Breathe", t2.title)
        assertEquals(90000L, t2.startMs)
    }
}
