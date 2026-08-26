package com.ipodmodern.audio.core.parser

import com.ipodmodern.audio.core.model.CueSheet
import com.ipodmodern.audio.core.model.CueTrack
import java.io.File
import java.util.regex.Pattern

class CueSheetParser {

    fun parse(cueFile: File): CueSheet? {
        if (!cueFile.exists() || !cueFile.canRead()) return null
        return parseContent(cueFile.readText(), cueFile.parentFile?.absolutePath ?: "")
    }

    fun parseContent(content: String, baseDirPath: String = ""): CueSheet {
        var globalPerformer = "Unknown Artist"
        var globalTitle = "Unknown Album"
        var audioFileName = ""

        val rawTracks = mutableListOf<ParsedTrack>()
        var currentTrack: ParsedTrack? = null

        val lines = content.lines()
        for (rawLine in lines) {
            val line = rawLine.trim()
            if (line.isEmpty() || line.startsWith("REM")) continue

            val upper = line.uppercase()
            when {
                upper.startsWith("PERFORMER") -> {
                    val value = extractQuotedOrRest(line.substring(9).trim())
                    if (currentTrack == null) {
                        globalPerformer = value
                    } else {
                        currentTrack.performer = value
                    }
                }
                upper.startsWith("TITLE") -> {
                    val value = extractQuotedOrRest(line.substring(5).trim())
                    if (currentTrack == null) {
                        globalTitle = value
                    } else {
                        currentTrack.title = value
                    }
                }
                upper.startsWith("FILE") -> {
                    // FILE "audio.flac" WAVE
                    val lastSpace = line.lastIndexOf(' ')
                    if (lastSpace > 4) {
                        val filePart = line.substring(4, lastSpace).trim()
                        audioFileName = extractQuotedOrRest(filePart)
                    }
                }
                upper.startsWith("TRACK") -> {
                    // TRACK 01 AUDIO
                    currentTrack?.let { rawTracks.add(it) }
                    val parts = line.split("\\s+".toRegex())
                    val num = parts.getOrNull(1)?.toIntOrNull() ?: (rawTracks.size + 1)
                    currentTrack = ParsedTrack(trackNumber = num)
                }
                upper.startsWith("INDEX") -> {
                    // INDEX 01 04:12:35
                    val parts = line.split("\\s+".toRegex())
                    val indexNum = parts.getOrNull(1)?.toIntOrNull() ?: 1
                    val timecode = parts.getOrNull(2) ?: "00:00:00"
                    val ms = parseTimecodeToMs(timecode)

                    if (indexNum == 1) {
                        currentTrack?.startMs = ms
                    }
                }
            }
        }
        currentTrack?.let { rawTracks.add(it) }

        // Compute endMs for each track
        val cueTracks = mutableListOf<CueTrack>()
        for (i in 0 until rawTracks.size) {
            val curr = rawTracks[i]
            val next = rawTracks.getOrNull(i + 1)
            val endMs = next?.startMs

            cueTracks.add(
                CueTrack(
                    trackNumber = curr.trackNumber,
                    title = curr.title ?: "Track ${curr.trackNumber}",
                    performer = curr.performer ?: globalPerformer,
                    startMs = curr.startMs,
                    endMs = endMs
                )
            )
        }

        return CueSheet(
            title = globalTitle,
            performer = globalPerformer,
            audioFileName = audioFileName,
            tracks = cueTracks
        )
    }

    /**
     * Converts mm:ss:ff frame timecode to milliseconds.
     * In Red Book CD audio, 1 second = 75 frames.
     * 1 frame = 1000 / 75 = 13.33333 ms
     */
    fun parseTimecodeToMs(timecode: String): Long {
        val parts = timecode.split(":")
        if (parts.size != 3) return 0L

        val minutes = parts[0].toLongOrNull() ?: 0L
        val seconds = parts[1].toLongOrNull() ?: 0L
        val frames = parts[2].toLongOrNull() ?: 0L

        val totalSecondsMs = (minutes * 60L + seconds) * 1000L
        val frameMs = (frames * 1000L) / 75L
        return totalSecondsMs + frameMs
    }

    private fun extractQuotedOrRest(text: String): String {
        val trimmed = text.trim()
        return if (trimmed.startsWith("\"") && trimmed.endsWith("\"") && trimmed.length >= 2) {
            trimmed.substring(1, trimmed.length - 1)
        } else {
            trimmed
        }
    }

    private data class ParsedTrack(
        val trackNumber: Int,
        var title: String? = null,
        var performer: String? = null,
        var startMs: Long = 0L
    )
}
