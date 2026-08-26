package com.ipodmodern.audio.core.parser

import com.ipodmodern.audio.core.model.LyricLine
import java.util.regex.Pattern

class LyricsParser {

    private val lrcPattern = Pattern.compile("\\[(\\d{2}):(\\d{2})\\.(\\d{2,3})\\](.*)")

    fun parseLrc(lrcContent: String): List<LyricLine> {
        val list = mutableListOf<LyricLine>()

        for (line in lrcContent.lines()) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue

            val matcher = lrcPattern.matcher(trimmed)
            if (matcher.matches()) {
                val min = matcher.group(1)?.toLongOrNull() ?: 0L
                val sec = matcher.group(2)?.toLongOrNull() ?: 0L
                val fractionStr = matcher.group(3) ?: "00"
                val fractionMs = if (fractionStr.length == 2) {
                    (fractionStr.toLongOrNull() ?: 0L) * 10L
                } else {
                    fractionStr.toLongOrNull() ?: 0L
                }

                val totalMs = (min * 60L + sec) * 1000L + fractionMs
                val text = matcher.group(4)?.trim() ?: ""

                list.add(LyricLine(timeMs = totalMs, text = text))
            }
        }

        return list.sortedBy { it.timeMs }
    }

    /**
     * Binary search to find currently active lyric line index at timeMs
     */
    fun findActiveLyricIndex(lyrics: List<LyricLine>, currentMs: Long): Int {
        if (lyrics.isEmpty() || currentMs < lyrics[0].timeMs) return -1

        var low = 0
        var high = lyrics.size - 1
        var result = 0

        while (low <= high) {
            val mid = (low + high) ushr 1
            if (lyrics[mid].timeMs <= currentMs) {
                result = mid
                low = mid + 1
            } else {
                high = mid - 1
            }
        }

        return result
    }
}
