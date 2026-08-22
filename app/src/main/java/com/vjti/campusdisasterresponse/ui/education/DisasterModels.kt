package com.vjti.campusdisasterresponse.ui.education

sealed class ModuleSection {
    data class VisualGuide(
        val steps: List<VisualStep>
    ) : ModuleSection()

    data class Instruction(
        val title: String,
        val description: String,
        val keyTakeaways: List<String>
    ) : ModuleSection()

    data class VideoLearning(
        val videoUrl: String,
        val title: String,
        val duration: String
    ) : ModuleSection()
}

data class VisualStep(
    val stepNumber: Int,
    val title: String,
    val description: String
)

data class DisasterModule(
    val id: String,
    val title: String,
    val summary: String,
    val sections: List<ModuleSection>
)

data class DisasterType(
    val id: String,
    val name: String,
    val modules: List<DisasterModule>
)
