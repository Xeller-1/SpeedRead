package com.example.speedread2.reading

enum class ReadingMode(
    val profile: Profile
) {
    TONGUE_TWISTER(
        Profile(
            windowBack = 2,
            windowForward = 8,
            weakSimilarityThresholdHigh = 0.88,
            weakSimilarityThresholdLow = 0.75,
            minAcceptScore = 35,
            allowedSoftSkips = 0,
            keepActiveLineCentered = false,
            strictLineRespect = false
        )
    ),
    POEM(
        Profile(
            windowBack = 3,
            windowForward = 10,
            weakSimilarityThresholdHigh = 0.84,
            weakSimilarityThresholdLow = 0.70,
            minAcceptScore = 26,
            allowedSoftSkips = 1,
            keepActiveLineCentered = true,
            strictLineRespect = true
        )
    ),
    FABLE(
        Profile(
            windowBack = 4,
            windowForward = 16,
            weakSimilarityThresholdHigh = 0.80,
            weakSimilarityThresholdLow = 0.66,
            minAcceptScore = 20,
            allowedSoftSkips = 2,
            keepActiveLineCentered = false,
            strictLineRespect = false
        )
    ),
    STORY(
        Profile(
            windowBack = 4,
            windowForward = 18,
            weakSimilarityThresholdHigh = 0.80,
            weakSimilarityThresholdLow = 0.64,
            minAcceptScore = 18,
            allowedSoftSkips = 3,
            keepActiveLineCentered = false,
            strictLineRespect = false
        )
    );

    data class Profile(
        val windowBack: Int,
        val windowForward: Int,
        val weakSimilarityThresholdHigh: Double,
        val weakSimilarityThresholdLow: Double,
        val minAcceptScore: Int,
        val allowedSoftSkips: Int,
        val keepActiveLineCentered: Boolean,
        val strictLineRespect: Boolean
    )
}
