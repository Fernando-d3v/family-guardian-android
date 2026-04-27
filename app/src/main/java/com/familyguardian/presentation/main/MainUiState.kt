package com.familyguardian.presentation.main

import com.familyguardian.domain.model.NotificationEvent

data class MainUiState(
    val isMonitoringActive: Boolean = false,
    val recentEvents: List<NotificationEvent> = emptyList(),
    val isLoading: Boolean = false,
    val testResultMessage: String? = null
)