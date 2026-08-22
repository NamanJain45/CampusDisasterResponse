package com.vjti.campusdisasterresponse.ui.education

data class DisasterModule(
    val id: String,
    val title: String,
    val description: String,
    val progress: Float,
    val category: String
)

val sampleModules = listOf(
    DisasterModule(
        id = "1",
        title = "Earthquake Drill",
        description = "Learn drop, cover, and hold protocols during structural shaking.",
        progress = 0.75f,
        category = "Interactive"
    ),
    DisasterModule(
        id = "2",
        title = "Fire Safety & Evacuation",
        description = "Locate emergency exits and learn basic fire extinguisher operation.",
        progress = 0.40f,
        category = "Video Guide"
    ),
    DisasterModule(
        id = "3",
        title = "First Aid & CPR",
        description = "Essential wound dressing, triage, and basic CPR techniques.",
        progress = 0.10f,
        category = "Checkpoint Quiz"
    ),
    DisasterModule(
        id = "4",
        title = "Severe Weather & Floods",
        description = "Emergency shelter protocols for extreme weather events.",
        progress = 0.00f,
        category = "Interactive"
    )
)
