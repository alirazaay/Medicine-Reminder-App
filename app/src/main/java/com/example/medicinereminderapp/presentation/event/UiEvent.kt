package com.example.medicinereminderapp.presentation.event

sealed class UiEvent {
    data class ShowSnackbar(val message: String) : UiEvent()
    object NavigateBack : UiEvent()
    data class NavigateTo(val route: String) : UiEvent()
}
