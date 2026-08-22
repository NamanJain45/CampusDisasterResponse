package com.vjti.campusdisasterresponse.ui.education

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class EducationViewModel : ViewModel() {

    private val _disasterTypes = MutableStateFlow<List<DisasterType>>(emptyList())
    val disasterTypes: StateFlow<List<DisasterType>> = _disasterTypes

    private val _selectedDisaster = MutableStateFlow<DisasterType?>(null)
    val selectedDisaster: StateFlow<DisasterType?> = _selectedDisaster

    private val _selectedModule = MutableStateFlow<DisasterModule?>(null)
    val selectedModule: StateFlow<DisasterModule?> = _selectedModule

    init {
        loadDisasterData()
    }

    private fun loadDisasterData() {
        val initialData = listOf(
            DisasterType(
                id = "earthquake",
                name = "Earthquake",
                modules = listOf(
                    DisasterModule(
                        id = "eq_response",
                        title = "Drop, Cover, & Hold On",
                        summary = "Immediate survival actions required during structural shaking.",
                        sections = listOf(
                            ModuleSection.VisualGuide(
                                steps = listOf(
                                    VisualStep(1, "DROP", "Drop down onto your hands and knees to protect stability."),
                                    VisualStep(2, "COVER", "Cover your head and neck under a sturdy desk or table."),
                                    VisualStep(3, "HOLD ON", "Hold on to your shelter until shaking completely stops.")
                                )
                            ),
                            ModuleSection.Instruction(
                                title = "Indoor Emergency Protocol",
                                description = "Remain inside until shaking stops and it is verified safe to exit.",
                                keyTakeaways = listOf(
                                    "Stay away from windows, glass, and tall unanchored furniture.",
                                    "Do NOT use elevators during or immediately after shaking.",
                                    "Protect head and neck using arms if no cover is available."
                                )
                            ),
                            ModuleSection.VideoLearning(
                                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                                title = "Campus Earthquake Safety Demonstration",
                                duration = "02:30"
                            )
                        )
                    )
                )
            ),
            DisasterType(
                id = "fire",
                name = "Fire Safety",
                modules = listOf(
                    DisasterModule(
                        id = "fire_evac",
                        title = "Building Evacuation & PASS Method",
                        summary = "Guidelines for orderly evacuation and fire extinguisher operation.",
                        sections = listOf(
                            ModuleSection.VisualGuide(
                                steps = listOf(
                                    VisualStep(1, "PULL", "Pull the pin in the handle."),
                                    VisualStep(2, "AIM", "Aim low at the base of the fire."),
                                    VisualStep(3, "SQUEEZE", "Squeeze the lever slowly."),
                                    VisualStep(4, "SWEEP", "Sweep from side to side at the base.")
                                )
                            ),
                            ModuleSection.Instruction(
                                title = "Evacuation Directives",
                                description = "Follow designated safe exit routes to primary campus assembly points.",
                                keyTakeaways = listOf(
                                    "Check door handles with back of hand before opening.",
                                    "Crawl low under smoke to avoid toxic inhalation.",
                                    "Never re-enter a burning structure."
                                )
                            )
                        )
                    )
                )
            )
        )

        _disasterTypes.value = initialData
        _selectedDisaster.value = initialData.firstOrNull()
        _selectedModule.value = initialData.firstOrNull()?.modules?.firstOrNull()
    }

    fun selectDisaster(disaster: DisasterType) {
        _selectedDisaster.value = disaster
        _selectedModule.value = disaster.modules.firstOrNull()
    }

    fun selectModule(module: DisasterModule) {
        _selectedModule.value = module
    }
}
