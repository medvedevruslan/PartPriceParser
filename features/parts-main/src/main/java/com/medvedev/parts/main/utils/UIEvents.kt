package com.medvedev.parts.main.utils

sealed class UIEvents {
    data class SnackbarEvent(val message: String) : UIEvents()
    data class NavigateEvent(val route: String) : UIEvents()
}
