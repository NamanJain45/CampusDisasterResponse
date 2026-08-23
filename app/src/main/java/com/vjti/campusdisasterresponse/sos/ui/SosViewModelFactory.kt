package com.vjti.campusdisasterresponse.sos.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vjti.campusdisasterresponse.data.queue.EmergencyQueueRepository

class SosViewModelFactory(
    private val queueRepository: EmergencyQueueRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(SosViewModel::class.java)) {
            return SosViewModel(queueRepository) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}
