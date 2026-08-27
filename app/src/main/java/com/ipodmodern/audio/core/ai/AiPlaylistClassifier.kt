package com.ipodmodern.audio.core.ai

import com.ipodmodern.audio.core.database.entity.PlaylistEntity
import com.ipodmodern.audio.core.model.Track

data class AiGeneratedPlaylistResult(
    val entity: PlaylistEntity,
    val trackIds: List<Long>
)

object AiPlaylistClassifier {

    private val TELUGU_ARTISTS_KEYWORDS = setOf(
        "telugu", "tollywood", "anirudh", "thaman", "dsp", "devi sri prasad", "sid sriram",
        "ramana gogula", "mani sharma", "keerthiswaran", "sai abhyankkar", "dude", "pushpa",
        "guntur kaaram", "kalki", "devara", "salaar", "rrr", "svp", "ala vaikunthapurramuloo",
        "spb", "s.p. balasubrahmanyam", "chitra", "geetha madhuri", "anurag kulkarni", "m.m. keeravani",
        "keeravani", "mickey j meyer", "vivek sagar", "radhan", "heisenberg", "hangova"
    )

    private val TAMIL_ARTISTS_KEYWORDS = setOf(
        "tamil", "kollywood", "yuvan", "yuvan shankar raja", "harris jayaraj", "santhosh narayanan",
        "gv prakash", "g. v. prakash", "ilayaraja", "ilayaraaja", "leo", "jailer", "vikram",
        "master", "ponniyin selvan", "goat", "amaran", "dhruv vikram", "karthik", "sid sriram",
        "dhibu ninan thomas", "sam c.s.", "hiphop tamizha", "sean roldan", "anantha sriram"
    )

    private val HINDI_ARTISTS_KEYWORDS = setOf(
        "hindi", "bollywood", "arijit", "arijit singh", "pritam", "atif aslam", "shreya ghoshal",
        "kumar sanu", "udit narayan", "sonu nigam", "badshah", "honey singh", "yo yo honey singh",
        "animal", "jawan", "pathaan", "aashiqui", "kabir singh", "rockstar", "brahmastra",
        "vishal mishra", "jubin nautiyal", "neha kakkar", "armaan malik", "amit trivedi",
        "sachin-jigar", "mithoon", "mohit chauhan", "kk", "darshan raval", "jasleen royal"
    )

    private val PUNJABI_ARTISTS_KEYWORDS = setOf(
        "punjabi", "diljit", "diljit dosanjh", "ap dhillon", "karan aujla", "sidhu moosewala",
        "sidhu moose wala", "b praak", "shubh", "jassi gill", "guru randhawa", "harrdy sandhu",
        "ammy virk", "jass manak", "tarsame jassar", "garry sandhu", "parmish verma", "bohemia"
    )

    private val MALAYALAM_ARTISTS_KEYWORDS = setOf(
        "malayalam", "mollywood", "sushin shyam", "jakes bejoy", "vineeth sreenivasan",
        "hesham abdul wahab", "manjummel boys", "premalu", "aavesham", "shaan rahman",
        "alphons joseph", "rex vijayan", "job kurian", "vidyasagar"
    )

    private val KANNADA_ARTISTS_KEYWORDS = setOf(
        "kannada", "sandalwood", "ravi basrur", "arjun janya", "kgf", "kantara", "charan raj",
        "v. harikrishna", "ajaneesh loknath", "vijay prakash", "sanjith hegde"
    )

    private val ENGLISH_ARTISTS_KEYWORDS = setOf(
        "queen", "daft punk", "beatles", "the beatles", "taylor swift", "eminem", "drake",
        "ed sheeran", "coldplay", "the weeknd", "bruno mars", "billie eilish", "post malone",
        "dua lipa", "imagine dragons", "michael jackson", "hans zimmer", "linkin park",
        "maroon 5", "avicii", "alan walker", "chainsmokers", "david guetta", "calvin harris",
        "adele", "shakira", "rihanna", "justin bieber", "charlie puth", "sia", "marshmello",
        "travis scott", "kanye west", "kendrick lamar", "twenty one pilots", "green day"
    )

    private val KPOP_ARTISTS_KEYWORDS = setOf(
        "k-pop", "kpop", "bts", "blackpink", "twice", "newjeans", "stray kids", "exo",
        "seventeen", "enhypen", "aespa", "itzy", "red velvet", "txt", "ive", "le sserafim"
    )

    private val CHILL_KEYWORDS = setOf(
        "acoustic", "chill", "lo-fi", "lofi", "piano", "unplugged", "instrumental", "ambient",
        "relax", "slowed", "sleep", "calm", "midnight", "peaceful", "soft"
    )

    private val WORKOUT_KEYWORDS = setOf(
        "gym", "workout", "fitness", "bass", "edm", "trap", "energy", "club", "drop",
        "remix", "power", "hype", "fast", "drill", "motivation", "beast"
    )

    /**
     * Analyzes all tracks in the user's library and generates categorized smart AI playlists.
     */
    fun classifyLibrary(allTracks: List<Track>): List<AiGeneratedPlaylistResult> {
        if (allTracks.isEmpty()) return emptyList()

        val teluguTracks = mutableListOf<Long>()
        val tamilTracks = mutableListOf<Long>()
        val hindiTracks = mutableListOf<Long>()
        val punjabiTracks = mutableListOf<Long>()
        val malayalamTracks = mutableListOf<Long>()
        val kannadaTracks = mutableListOf<Long>()
        val englishTracks = mutableListOf<Long>()
        val kpopTracks = mutableListOf<Long>()
        val chillTracks = mutableListOf<Long>()
        val workoutTracks = mutableListOf<Long>()

        for (track in allTracks) {
            val textToInspect = "${track.title} ${track.artist} ${track.album} ${track.genre} ${track.filePath}".lowercase()

            // 1. Check Unicode Scripts
            var matchedScript = false
            for (char in textToInspect) {
                when (char) {
                    in '\u0C00'..'\u0C7F' -> { teluguTracks.add(track.id); matchedScript = true; break }
                    in '\u0B80'..'\u0BFF' -> { tamilTracks.add(track.id); matchedScript = true; break }
                    in '\u0900'..'\u097F' -> { hindiTracks.add(track.id); matchedScript = true; break }
                    in '\u0A00'..'\u0A7F' -> { punjabiTracks.add(track.id); matchedScript = true; break }
                    in '\u0D00'..'\u0D7F' -> { malayalamTracks.add(track.id); matchedScript = true; break }
                    in '\u0C80'..'\u0CFF' -> { kannadaTracks.add(track.id); matchedScript = true; break }
                    in '\uAC00'..'\uD7AF' -> { kpopTracks.add(track.id); matchedScript = true; break }
                }
            }

            if (!matchedScript) {
                // 2. Check Linguistic Keywords & Catalog Match
                var isLanguageMatched = false

                if (containsAnyKeyword(textToInspect, TELUGU_ARTISTS_KEYWORDS)) {
                    teluguTracks.add(track.id)
                    isLanguageMatched = true
                }
                if (containsAnyKeyword(textToInspect, TAMIL_ARTISTS_KEYWORDS)) {
                    tamilTracks.add(track.id)
                    isLanguageMatched = true
                }
                if (containsAnyKeyword(textToInspect, HINDI_ARTISTS_KEYWORDS)) {
                    hindiTracks.add(track.id)
                    isLanguageMatched = true
                }
                if (containsAnyKeyword(textToInspect, PUNJABI_ARTISTS_KEYWORDS)) {
                    punjabiTracks.add(track.id)
                    isLanguageMatched = true
                }
                if (containsAnyKeyword(textToInspect, MALAYALAM_ARTISTS_KEYWORDS)) {
                    malayalamTracks.add(track.id)
                    isLanguageMatched = true
                }
                if (containsAnyKeyword(textToInspect, KANNADA_ARTISTS_KEYWORDS)) {
                    kannadaTracks.add(track.id)
                    isLanguageMatched = true
                }
                if (containsAnyKeyword(textToInspect, KPOP_ARTISTS_KEYWORDS)) {
                    kpopTracks.add(track.id)
                    isLanguageMatched = true
                }
                if (containsAnyKeyword(textToInspect, ENGLISH_ARTISTS_KEYWORDS)) {
                    englishTracks.add(track.id)
                    isLanguageMatched = true
                }

                // If not matched to regional, classify general Latin/English as Global/English
                if (!isLanguageMatched) {
                    englishTracks.add(track.id)
                }
            }

            // 3. Mood & Energy Categorization
            if (containsAnyKeyword(textToInspect, CHILL_KEYWORDS)) {
                chillTracks.add(track.id)
            }
            if (containsAnyKeyword(textToInspect, WORKOUT_KEYWORDS)) {
                workoutTracks.add(track.id)
            }
        }

        val results = mutableListOf<AiGeneratedPlaylistResult>()

        fun addIfNotEmpty(name: String, description: String, colorHex: Long, trackIds: List<Long>) {
            val uniqueIds = trackIds.distinct()
            if (uniqueIds.isNotEmpty()) {
                results.add(
                    AiGeneratedPlaylistResult(
                        entity = PlaylistEntity(
                            name = name,
                            description = description,
                            colorHex = colorHex,
                            isAiGenerated = true,
                            createdAt = System.currentTimeMillis()
                        ),
                        trackIds = uniqueIds
                    )
                )
            }
        }

        addIfNotEmpty("Telugu Hits & Melodies", "Smart AI curated Telugu chartbusters and soundtracks", 0xFFE65100, teluguTracks)
        addIfNotEmpty("Tamil Vibes", "Kollywood favorites, melodies, and high-energy scores", 0xFF8E24AA, tamilTracks)
        addIfNotEmpty("Bollywood & Hindi Classics", "Curated Bollywood soundtracks, pop, and soulful melodies", 0xFFD81B60, hindiTracks)
        addIfNotEmpty("Punjabi Beats", "High-octane Punjabi hits and folk fusion", 0xFFF57F17, punjabiTracks)
        addIfNotEmpty("Malayalam Melodies", "Soulful Mollywood tracks and acoustic gems", 0xFF00897B, malayalamTracks)
        addIfNotEmpty("Kannada Hits", "Sandalwood anthems and chart-toppers", 0xFF3949AB, kannadaTracks)
        addIfNotEmpty("English & Global Hits", "International pop, rock, electronic, and classics", 0xFF1E88E5, englishTracks)
        addIfNotEmpty("K-Pop & Asian Beats", "Dynamic K-Pop anthems and Asian pop favorites", 0xFFEC407A, kpopTracks)
        addIfNotEmpty("Late Night Chill", "Mellow acoustic, lo-fi, and relaxing ambient vibes", 0xFF546E7A, chillTracks)
        addIfNotEmpty("High-Energy Workout", "Bass-boosted high-tempo gym & motivation tracks", 0xFF43A047, workoutTracks)

        return results
    }

    private fun containsAnyKeyword(text: String, keywords: Set<String>): Boolean {
        for (kw in keywords) {
            if (text.contains(kw, ignoreCase = true)) {
                return true
            }
        }
        return false
    }
}
