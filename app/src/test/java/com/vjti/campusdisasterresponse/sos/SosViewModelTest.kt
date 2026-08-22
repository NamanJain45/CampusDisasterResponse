package com.vjti.campusdisasterresponse.sos

import com.vjti.campusdisasterresponse.sos.ui.SosUiState
import com.vjti.campusdisasterresponse.sos.ui.SosViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SosViewModelTest {

    @Test
    fun testInitialStateIsIdle() {
        val viewModel = SosViewModel()

        assertEquals(
            SosUiState.Idle,
            viewModel.uiState.value
        )
    }

    @Test
    fun testUpdateProgressSetsHoldingState() {
        val viewModel = SosViewModel()

        viewModel.updateProgress(0.5f)

        val state = viewModel.uiState.value

        assertTrue(
            state is SosUiState.Holding
        )

        assertEquals(
            0.5f,
            (state as SosUiState.Holding).progress
        )
    }

    @Test
    fun testCancelHoldResetsToIdle() {
        val viewModel = SosViewModel()

        viewModel.updateProgress(0.4f)
        viewModel.cancelHold()

        assertEquals(
            SosUiState.Idle,
            viewModel.uiState.value
        )
    }

    @Test
    fun testTriggerEmergencyCreatesEvent() {
        val viewModel = SosViewModel()

        viewModel.triggerEmergency()

        val state = viewModel.uiState.value

        assertTrue(
            state is SosUiState.Triggered
        )
    }
}
