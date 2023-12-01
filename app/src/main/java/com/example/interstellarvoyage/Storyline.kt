package com.example.interstellarvoyage

data class Line(val id: String, val btnText: Int, val line: String)
//For btnText, 0 -> NEXT, 1 -> START MISSION, 2 -> NEXT LEVEL, 3 -> CONTINUE

object Storyline {
    val lines = listOf(
        // LEVEL 0
        Line("0.1.1", 0, "Today is December 14, 3055. The cities are overpopulated, with approximately 100,000 people per square kilometer. All countries worldwide lost control of regulating their population despite efforts to reduce it."),
        Line("0.1.2", 0, "In the previous decades, food technology has advanced so much, allowing people to extend their life expectancy significantly. Consequently, death rates have lowered a lot year by year, while birth rates are increasing."),
        Line("0.1.3", 1, "Research how the food in this generation increases life expectancy by reaching 100 clicks."),

        Line("0.2.1", 0,  "Through all efforts to balance the birth and death rates, it failed and had only one solution left for Earth. Scientists and government officials established a project to find life on Mars and make it habitable."),
        Line("0.2.2", 1,  "Life on Mars has thrived for several centuries with roofs above their heads, food and water everyday. Go to Mars to explore the citizen’s way of living by reaching 200 clicks."),

        Line("0.3.1", 0,  "Although this has succeeded, it became uninhabitable because the effects of making it habitable were reversing. Astrophysicists must find another planet to support life forms beyond the solar system."),
        Line("0.3.2", 1,  "Help the astrophysicists find a solution to Earth’s Dilemma by reaching 300 clicks."),

        Line("0.4.1", 0,  "Officials around the world gathered to discuss the issue and came up with a plan to gather earth’s most notable scientists, engineers, and astrophysicists."),
        Line("0.4.2", 0,  "Earth's high-class scientists, engineers, and astrophysicists began planning and building The Cosmic Gateway, a space station at the end of the solar system that serves as a research facility and a resting place for space travelers."),
        Line("0.4.3", 1,  "Help the professionals build the space station by reaching 400 clicks."),

        Line("0.5.1", 0,  "After years of construction and work, it was a marvel of engineering, having its own artificial ecosystem that suits the living needs of people."),
        Line("0.5.2", 0,  "The government created a project called “Project Gaia” to search for life on the far ends of space, and they hired professionals to study and research the plan to find a new habitable planet beyond the solar system."),
        Line("0.5.3", 1,  "Study the space beyond the solar system to prepare for the project by reaching 500 clicks."),

        Line("0.6.1", 2,  "After formulating plans and several backup plans, the team prepares for a long journey ahead. The team is now prepared for the long expedition ahead.  Congratulations! You have completed Level 0. Click “Next Level” to go to the next level."),

        // LEVEL 1
        Line("1.1.1", 0,  "The Gaia crew assembled on The Cosmic Gateway and planned for everything they would need for the long trip. It's April 22, 4010, the day of the warp drive going outside of the solar system."),
        Line("1.1.2", 1,  "Help the crew prepare for hibernation by reaching 200 clicks."),

        Line("1.2.1", 0,  "The crew is put to sleep to be woken up after a decade, traveling light years faster than the speed of light. After the long hibernation, the team woke up and entered Alpha Centauri, full of different never-seen galaxies and heavenly bodies."),
        Line("1.2.2", 1,  "Plan a meeting on the next steps of the mission by reaching 400 clicks."),

        Line("1.3.1", 0,  "The crew traveled to the nearest planet to search for potential habitat. Gaia Team is ready to go outside of the spaceship, unexpected happenings await them in the future."),
        Line("1.3.2", 1,  "Help the crew get ready for the expedition by reaching 600 clicks."),

        Line("1.4.1", 0,  "Upon stepping onto the planet, the team searches for artifacts, recorded findings, the environment's temperature, humidity, and the ground and sky's composition."),
        Line("1.4.2", 1,  "They’ve discovered a lot of useless objects and they were unhelpful to know more about the planet’s history. Assist the crew to find something useful on the planet by reaching 800 clicks."),

        Line("1.5.1", 0,  "After 30 minutes on the ground, the detectors started beeping and found a mysterious artifact. The artifact was uncovered and it is unidentifiable; the team kept it and continued their search for potential life on the planet."),
        Line("1.5.2", 1,  "Hunt for more artifacts by reaching 1000 clicks."),

        Line("1.6.1", 0,  "They searched quite long and decided to go back to the spaceship and study their findings."),
        Line("1.6.2", 0,  "After returning to the spaceship, it took them a while to decipher some of the mysterious artifact’s secrets, which opened the door to further interstellar voyages."),
        Line("1.6.3", 2,  "Congratulations! You have completed Level 1. Click “Next Level” to go to the next level."),

        // LEVEL 2
        Line("2.1.1", 0,  "The team received a signal from a faraway galaxy that appeared to be some beacon because of deciphering some secrets. They tracked it through their technology, yet its location still can't be identified."),
        Line("2.1.2", 1,  "Try to help the team identify the source of the signal by reaching 300 clicks."),

        Line("2.2.1", 0,  "They decided to follow the signal by going in its direction. They traveled through galaxies and wormholes and realized how far it actually was."),
        Line("2.2.2", 1,  "Motivate the team on continuing its mission to find the source of the signal by reaching 600 clicks."),

        Line("2.3.1", 1,  "They stopped and thought that whoever was responsible for sending the signal must have been a very advanced alien with more knowledge than them. Continue to follow the signal by reaching 900 clicks."),

        Line("2.4.1", 1,  "They followed the signal and reached the point, yet they still saw nothing. It looks like it's there, but it's not. They figured that something was wrong. Brainstorm with the team on what’s wrong by reaching 1200 clicks."),

        Line("2.5.1", 1,  "They tried to observe the mysterious artifact again. Help the team decipher the artifact by reaching 1500 clicks."),

        Line("2.6.1", 0,  "The team got to decipher another secret. Suddenly, a group of aliens appeared before them, introducing themselves as The Cosmic Council's leader and inviting them to join them."),
        Line("2.6.2", 2,  "Congratulations! You have completed Level 2. Click “Next Level” to go to the next level."),

        // LEVEL 3
        Line("3.1.1", 1,  "The team decided to follow The Cosmic Council's leader. The leader invites them to a grand assembly of various species across the universe. Discuss with the team about the invitation by reaching 400 clicks."),

        Line("3.2.1", 1,  "Shocked by how different everyone is, they can't believe their eyes. Everyone is distinct, unique, and different, yet all are unified. Explore the place by reaching 800 clicks."),

        Line("3.3.1", 1,  "The council discussed each species' status, issues, and challenges. Prepare for the team’s discussion by reaching 1200 clicks."),

        Line("3.4.1", 1,  "When it was humanity’s turn, they discussed how overpopulation is struggling the species and they’re finding a new habitat for them in the galaxy. Expound on the discussion and add details to it by reaching 1600 clicks."),

        Line("3.5.1", 1,  "From then on, the council welcomed humanity into a place where they could live on a new planet. It maintained a peaceful alliance with The Cosmic Council's members to protect the universe and its species."),
        Line("3.5.2", 1,  "Help humans migrate to the new habitat by reaching 2000 clicks."),

        Line("3.6.1", 3,  "Humanity's journey into the cosmos had only just begun. Mysteries and discoveries are yet to come in this era of interstellar voyages. Congratulations! You have completed All levels.")
    )
}