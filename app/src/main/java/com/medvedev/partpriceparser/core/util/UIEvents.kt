package com.medvedev.partpriceparser.core.util

sealed class UIEvents {
    data class SnackbarEvent(val message: String) : UIEvents()
    data class NavigateEvent(val route: String) : UIEvents()
}
