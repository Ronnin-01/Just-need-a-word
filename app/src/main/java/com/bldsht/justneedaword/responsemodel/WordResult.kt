package com.bldsht.justneedaword.responsemodel

data class WordResult(
    val word: String,
    val phonetic: String?,
    val meanings: List<Meaning>,
    val sourceUrls: List<String>?
)

data class Meaning(
    val partOfSpeech: String,
    val definitions: List<Definition>,
    val synonyms: List<String>,
    val antonyms: List<String>,
)

data class Definition(
    val definition: String
)