package com.trainig.articletrainer.data

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * CSV Parser for German nouns.
 *
 * Parses CSV from assets/german_nouns_a1.csv with format:
 * german,german example,english,english example
 *
 * The German column contains the article and noun, e.g.:
 * - "die Ansage, -n"
 * - "der Anschluss"
 *
 * Parsing logic:
 * 1. Extract article (first word: der/die/das)
 * 2. Extract base noun (text after article, before comma or end)
 * 3. Trim quotes, spaces, and plural markers
 */
class CsvParser {

    /**
     * Parse the CSV file from assets and return a list of NounEntry objects.
     */
    fun parseFromAssets(context: Context, fileName: String = "german_nouns_a1.csv"): List<NounEntry> {
        val entries = mutableListOf<NounEntry>()

        try {
            context.assets.open(fileName).use { inputStream ->
                BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
                    // Skip header line
                    reader.readLine()

                    // Process each line
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        line?.let {
                            if (it.isNotBlank()) {
                                parseNounEntry(it)?.let { entry ->
                                    entries.add(entry)
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return entries
    }

    /**
     * Parse a single CSV line into a NounEntry.
     *
     * CSV format: german,german example,english,english example
     * Where fields may be quoted if they contain commas.
     */
    private fun parseNounEntry(line: String): NounEntry? {
        val columns = parseCsvLine(line)

        if (columns.size < 4) {
            return null
        }

        val germanFull = columns[0]
        val germanExample = columns[1]
        val english = columns[2]
        val englishExample = columns[3]

        // Parse the German column to extract article and noun
        val (article, noun) = extractArticleAndNoun(germanFull) ?: return null

        return NounEntry(
            article = article,
            noun = noun,
            germanExample = germanExample,
            english = english,
            englishExample = englishExample
        )
    }

    /**
     * Extract article and base noun from German column.
     *
     * Examples:
     * - "die Ansage, -n" -> ("die", "Ansage")
     * - "der Anschluss" -> ("der", "Anschluss")
     * - "das Auto, -s" -> ("das", "Auto")
     */
    private fun extractArticleAndNoun(germanFull: String): Pair<String, String>? {
        val cleaned = germanFull.trim().trim('"').trim()
        val parts = cleaned.split(" ", limit = 3)

        if (parts.isEmpty()) return null

        val article = parts[0].lowercase()
        if (article !in listOf("der", "die", "das")) {
            return null
        }

        if (parts.size < 2) return null

        // Extract noun: everything after article, before comma
        val remaining = parts.subList(1, parts.size).joinToString(" ")
        val noun = remaining.split(",")[0].trim()

        return article to noun
    }

    /**
     * Parse a CSV line handling quoted fields.
     * Simple CSV parser that handles quotes around fields containing commas.
     */
    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val currentField = StringBuilder()
        var inQuotes = false

        var i = 0
        while (i < line.length) {
            val char = line[i]

            when {
                char == '"' -> {
                    inQuotes = !inQuotes
                }
                char == ',' && !inQuotes -> {
                    result.add(currentField.toString().trim())
                    currentField.clear()
                }
                else -> {
                    currentField.append(char)
                }
            }
            i++
        }

        // Add last field
        result.add(currentField.toString().trim())

        return result
    }
}

