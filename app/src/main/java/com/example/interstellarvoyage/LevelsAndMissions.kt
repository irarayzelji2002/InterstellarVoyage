package com.example.interstellarvoyage

data class Mission(val id: String, val name: String)

data class Level(val id: Int, val name: String)

object GameData {
    val levels = listOf(
        Level(0, "Earth’s Great Dilemma"),
        Level(1, "Search for New Habitat"),
        Level(2, "Beacon in the Galaxy"),
        Level(3, "The Cosmic Council")
    )

    val missions = listOf(
        Mission("0.1", "Population Crisis"),
        Mission("0.2", "Move to Mars"),
        Mission("0.3", "Hailing to Home"),
        Mission("0.4", "Construction Committee"),
        Mission("0.5", "Gaia Greenlight"),
        Mission("1.1", "Comfort of Cryosleep"),
        Mission("1.2", "Awakenings Aboard"),
        Mission("1.3", "Leaving to Land"),
        Mission("1.4", "Lively and Lookin"),
        Mission("1.5", "Extra Exploration"),
        Mission("2.1", "Scrambled Sounds"),
        Mission("2.2", "Forward Fiasco"),
        Mission("2.3", "Trailing Tech"),
        Mission("2.4", "Quiet Queue"),
        Mission("2.5", "Review Recourse"),
        Mission("3.1", "Warm Welcome"),
        Mission("3.2", "Unique Universe"),
        Mission("3.3", "Diaspora Dilemma"),
        Mission("3.4", "Population Proposition"),
        Mission("3.5", "Migration in Motion")
    )
}
