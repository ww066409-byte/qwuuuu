package com.pashto.quranicgrammar.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class QuranDatabase(
    @SerializedName("version") val version: Int,
    @SerializedName("lastUpdated") val lastUpdated: String,
    @SerializedName("targetLanguage") val targetLanguage: String,
    @SerializedName("totalSurahs") val totalSurahs: Int,
    @SerializedName("sourceNote") val sourceNote: String?,
    @SerializedName("surahs") val surahs: List<Surah>
) : Serializable

data class Surah(
    @SerializedName("number") val number: Int,
    @SerializedName("nameArabic") val nameArabic: String,
    @SerializedName("namePashto") val namePashto: String,
    @SerializedName("nameEnglish") val nameEnglish: String,
    @SerializedName("revelationType") val revelationType: String,
    @SerializedName("revelationPashto") val revelationPashto: String,
    @SerializedName("ayahCount") val ayahCount: Int,
    @SerializedName("juzNumber") val juzNumber: Int,
    @SerializedName("summaryPashto") val summaryPashto: String?,
    @SerializedName("ayahs") val ayahs: List<Ayah>
) : Serializable

data class Ayah(
    @SerializedName("ayahNumber") val ayahNumber: Int,
    @SerializedName("textArabic") val textArabic: String,
    @SerializedName("textPashto") val textPashto: String,
    @SerializedName("overallTarkibPashto") val overallTarkibPashto: String?,
    @SerializedName("words") val words: List<WordAnalysis>
) : Serializable

data class WordAnalysis(
    @SerializedName("id") val id: String,
    @SerializedName("wordArabic") val wordArabic: String,
    @SerializedName("partOfSpeech") val partOfSpeech: String,
    @SerializedName("partOfSpeechPashto") val partOfSpeechPashto: String,
    @SerializedName("grammaticalRolePashto") val grammaticalRolePashto: String,
    @SerializedName("caseEndingPashto") val caseEndingPashto: String,
    @SerializedName("harakah") val harakah: String?,
    @SerializedName("detailedIrabPashto") val detailedIrabPashto: String,
    @SerializedName("rootWord") val rootWord: String?
) : Serializable