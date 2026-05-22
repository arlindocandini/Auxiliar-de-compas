package com.example.data.api

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class Part(
    val text: String
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    val responseMimeType: String? = null,
    val temperature: Float? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    val candidates: List<Candidate>?
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content?
)

@JsonClass(generateAdapter = true)
data class AislesAIServiceResponse(
    val categorized: List<AICategorizedItem> = emptyList(),
    val suggestions: List<AISuggestionItem> = emptyList()
)

@JsonClass(generateAdapter = true)
data class AICategorizedItem(
    val name: String,
    val category: String
)

@JsonClass(generateAdapter = true)
data class AISuggestionItem(
    val name: String,
    val category: String,
    val importance: String
)
