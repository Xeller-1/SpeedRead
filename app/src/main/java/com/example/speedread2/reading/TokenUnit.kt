package com.example.speedread2.reading

data class TokenUnit(
    val raw: String,
    val normalized: String,
    val tokenIndex: Int,
    val lineIndex: Int = 0
)
