package com.vjti.campusdisasterresponse.ui.education

val sampleModules = listOf(
    DisasterModule(
        id = "1",
        title = "Earthquake Drill",
        summary = "Learn what to do before, during, and immediately after an earthquake on campus.",
        sections = listOf(
            ModuleSection.Instruction(
                title = "Drop, Cover, and Hold On",
                description = "When shaking starts, drop to your hands and knees, cover your head and neck under a sturdy desk or table, and hold on until the shaking stops. If you are indoors, stay inside and move away from windows and objects that can fall.",
                keyTakeaways = listOf(
                    "Drop low and protect your head and neck.",
                    "Take cover under sturdy furniture when available.",
                    "Stay away from glass, shelves, and unsecured objects.",
                    "After shaking stops, follow campus evacuation instructions and expect aftershocks."
                )
            ),
            ModuleSection.Quiz(
                questions = listOf(
                    QuizQuestion("What should you do first when strong shaking begins?", listOf("Run outside immediately", "Drop, cover, and hold on", "Use the elevator", "Stand beside a window"), 1),
                    QuizQuestion("Which location is safest during indoor shaking?", listOf("Beside a window", "Under sturdy furniture", "Near a bookshelf", "In a doorway with glass nearby"), 1),
                    QuizQuestion("What should you expect after a major earthquake?", listOf("No further hazards", "Only a power outage", "Possible aftershocks and secondary hazards", "Automatic campus clearance"), 2)
                )
            ),
            ModuleSection.VideoLearning(
                videoUrl = "",
                title = "Earthquake safety video",
                duration = "YouTube link to be added"
            )
        )
    ),
    DisasterModule("2", "Fire Safety & Evacuation", "Locate emergency exits and learn basic fire response protocols.", emptyList()),
    DisasterModule("3", "First Aid & CPR", "Learn essential first-response principles for common campus emergencies.", emptyList()),
    DisasterModule("4", "Severe Weather & Floods", "Learn shelter and movement guidance for extreme weather events.", emptyList())
)
