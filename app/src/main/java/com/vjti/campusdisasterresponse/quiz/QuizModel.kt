package com.vjti.campusdisasterresponse.quiz

data class Question(
    val id: Int,
    val text: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String
)

object QuizRepository {
    val questions = listOf(
        Question(
            id = 1,
            text = "During an earthquake inside a classroom, what is the immediate recommended action?",
            options = listOf(
                "Run towards the main staircase",
                "Drop, Cover, and Hold On under a desk",
                "Stand next to tall glass windows",
                "Attempt to carry heavy furniture outside"
            ),
            correctAnswerIndex = 1,
            explanation = "Drop, Cover, and Hold On protects your head and body from falling debris during severe shaking."
        ),
        Question(
            id = 2,
            text = "When evacuating a corridor filled with heavy smoke, how should you navigate?",
            options = listOf(
                "Walk upright at maximum speed",
                "Crawl low to the floor under the smoke line",
                "Take the elevator to descend faster",
                "Close your eyes and run forward"
            ),
            correctAnswerIndex = 1,
            explanation = "Toxic smoke rises; breathing air closer to the floor reduces inhalation risks."
        ),
        Question(
            id = 3,
            text = "If local cell towers shut down during a emergency, what communication system is utilized?",
            options = listOf(
                "Standard SMS Gateway",
                "BitChat Local Mesh Communication (BLE/Wi-Fi Direct)",
                "Public Voice Call Network",
                "Central Cloud Webserver"
            ),
            correctAnswerIndex = 1,
            explanation = "BitChat relies on peer-to-peer signals to pass local updates without active internet or cellular infrastructure."
        )
    )
}
