package com.vjti.campusdisasterresponse.ui.education

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class EducationViewModelTest {

    private lateinit var viewModel: EducationViewModel

    @Before
    fun setUp() {
        viewModel = EducationViewModel()
    }

    @Test
    fun loadInitialData_selectsFirstDisasterAndModule() {
        val selectedDisaster = viewModel.selectedDisaster.value
        val selectedModule = viewModel.selectedModule.value

        assertNotNull(selectedDisaster)
        assertNotNull(selectedModule)
        assertEquals("earthquake", selectedDisaster?.id)
        assertEquals("eq_response", selectedModule?.id)
    }

    @Test
    fun selectDisaster_updatesSelectedDisasterAndFirstModule() {
        val fireDisaster = viewModel.disasterTypes.value.find { it.id == "fire" }
        assertNotNull(fireDisaster)

        fireDisaster?.let { viewModel.selectDisaster(it) }

        assertEquals("fire", viewModel.selectedDisaster.value?.id)
        assertEquals("fire_evac", viewModel.selectedModule.value?.id)
    }
}
