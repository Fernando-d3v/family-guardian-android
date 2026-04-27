package com.familyguardian.presentation.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.familyguardian.core.utils.isNotificationListenerEnabled
import com.familyguardian.domain.model.NotificationEvent
import com.familyguardian.domain.usecase.GetNotificationEventsUseCase
import com.familyguardian.domain.usecase.SaveNotificationEventUseCase
import com.familyguardian.domain.usecase.SendNotificationEventUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    application: Application,
    private val getNotificationEventsUseCase: GetNotificationEventsUseCase,
    private val saveNotificationEventUseCase: SaveNotificationEventUseCase,
    private val sendNotificationEventUseCase: SendNotificationEventUseCase
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        observeNotificationEvents()
    }

    fun refreshMonitoringStatus() {
        val isActive = getApplication<Application>().isNotificationListenerEnabled()
        _uiState.update { it.copy(isMonitoringActive = isActive) }
        Timber.d("Monitoring status refreshed: active=$isActive")
    }

    fun sendTestEvent() {
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, testResultMessage = null) }

            val testEvent = NotificationEvent(
                id = UUID.randomUUID().toString(),
                packageName = "com.whatsapp",
                appName = "whatsapp",
                title = "Teste FamilyGuardian",
                text = "Mensagem de teste do botao Testar API",
                bigText = "Mensagem de teste enviada manualmente para validar POST /api/events",
                source = "notification",
                timestamp = System.currentTimeMillis()
            )

            runCatching {
                saveNotificationEventUseCase(testEvent)
                sendNotificationEventUseCase(testEvent).getOrThrow()
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        testResultMessage = "Teste enviado com sucesso para /api/events"
                    )
                }
            }.onFailure { error ->
                val message = when (error) {
                    is HttpException -> "Falha HTTP ${error.code()} ao chamar /api/events"
                    is IOException -> "Falha de rede: ${error.message ?: "sem detalhes"}"
                    else -> "Falha ao enviar teste: ${error.message ?: error::class.simpleName.orEmpty()}"
                }

                Timber.e(error, "Manual API test failed")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        testResultMessage = message
                    )
                }
            }
        }
    }

    private fun observeNotificationEvents() {
        viewModelScope.launch {
            getNotificationEventsUseCase()
                .catch { e -> Timber.e(e, "Error collecting notification events") }
                .collect { events ->
                    _uiState.update { it.copy(recentEvents = events) }
                }
        }
    }
}