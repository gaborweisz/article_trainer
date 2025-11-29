package com.trainig.articletrainer.data

/**
 * Represents a German noun with its article and translations.
 *
 * @property article The German article: "der", "die", or "das"
 * @property noun The base noun without article (e.g., "Ansage")
 * @property germanExample Example sentence in German
 * @property english English translation of the noun
 * @property englishExample Example sentence in English
 */
data class NounEntry(
    val article: String,
    val noun: String,
    val germanExample: String,
    val english: String,
    val englishExample: String
) {
    /**
     * Returns the full German noun with article (e.g., "die Ansage")
     */
    val fullGermanNoun: String
        get() = "$article $noun"
}

