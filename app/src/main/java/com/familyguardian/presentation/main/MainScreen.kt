package com.familyguardian.presentation.main

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.familyguardian.domain.model.NotificationEvent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MainScreen(
	viewModel: MainViewModel = hiltViewModel()
) {
	val uiState by viewModel.uiState.collectAsStateWithLifecycle()
	val context = LocalContext.current
	val lifecycleOwner = LocalLifecycleOwner.current

	LaunchedEffect(lifecycleOwner) {
		lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
			viewModel.refreshMonitoringStatus()
		}
	}

	Column(
		modifier = Modifier
			.fillMaxSize()
			.padding(16.dp)
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier.padding(bottom = 16.dp)
		) {
			Icon(
				imageVector = Icons.Filled.Shield,
				contentDescription = null,
				tint = MaterialTheme.colorScheme.primary,
				modifier = Modifier.size(28.dp)
			)
			Spacer(modifier = Modifier.width(8.dp))
			Text(
				text = "Family Guardian",
				style = MaterialTheme.typography.headlineMedium,
				fontWeight = FontWeight.Bold
			)
		}

		MonitoringStatusCard(
			isActive = uiState.isMonitoringActive,
			onEnableClick = {
				context.startActivity(
					Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
						addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
					}
				)
			}
		)

		Spacer(modifier = Modifier.height(12.dp))

		Button(
			onClick = { viewModel.sendTestEvent() },
			enabled = !uiState.isLoading,
			modifier = Modifier.fillMaxWidth()
		) {
			Text(if (uiState.isLoading) "Enviando teste..." else "Testar API Agora")
		}

		uiState.testResultMessage?.let { message ->
			Spacer(modifier = Modifier.height(8.dp))
			Text(
				text = message,
				style = MaterialTheme.typography.bodySmall,
				color = if (message.startsWith("Teste enviado")) {
					MaterialTheme.colorScheme.primary
				} else {
					MaterialTheme.colorScheme.error
				}
			)
		}

		Spacer(modifier = Modifier.height(20.dp))

		Text(
			text = "Mensagens Recentes",
			style = MaterialTheme.typography.titleMedium,
			fontWeight = FontWeight.SemiBold,
			modifier = Modifier.padding(bottom = 8.dp)
		)

		if (uiState.recentEvents.isEmpty()) {
			EmptyEventsMessage()
		} else {
			LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
				items(
					items = uiState.recentEvents,
					key = { it.id }
				) { event ->
					NotificationEventItem(event = event)
				}
			}
		}
	}
}

@Composable
private fun MonitoringStatusCard(
	isActive: Boolean,
	onEnableClick: () -> Unit
) {
	Card(
		modifier = Modifier.fillMaxWidth(),
		shape = RoundedCornerShape(12.dp),
		colors = CardDefaults.cardColors(
			containerColor = if (isActive) {
				MaterialTheme.colorScheme.primaryContainer
			} else {
				MaterialTheme.colorScheme.errorContainer
			}
		)
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			Icon(
				imageVector = if (isActive) Icons.Filled.CheckCircle else Icons.Filled.Warning,
				contentDescription = if (isActive) "Ativo" else "Inativo",
				tint = if (isActive) {
					MaterialTheme.colorScheme.primary
				} else {
					MaterialTheme.colorScheme.error
				},
				modifier = Modifier.size(32.dp)
			)
			Spacer(modifier = Modifier.width(12.dp))
			Column(modifier = Modifier.weight(1f)) {
				Text(
					text = "Monitoramento de Notificações",
					style = MaterialTheme.typography.labelMedium,
					color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
				)
				Text(
					text = if (isActive) "Ativo" else "Inativo",
					style = MaterialTheme.typography.titleMedium,
					fontWeight = FontWeight.Bold,
					color = if (isActive) {
						MaterialTheme.colorScheme.primary
					} else {
						MaterialTheme.colorScheme.error
					}
				)
			}
			if (!isActive) {
				Button(onClick = onEnableClick) {
					Text("Ativar")
				}
			}
		}
	}
}

@Composable
private fun NotificationEventItem(event: NotificationEvent) {
	val dateFormat = remember { SimpleDateFormat("dd/MM HH:mm:ss", Locale.getDefault()) }

	Card(
		modifier = Modifier.fillMaxWidth(),
		shape = RoundedCornerShape(8.dp),
		colors = CardDefaults.cardColors(
			containerColor = MaterialTheme.colorScheme.surfaceVariant
		)
	) {
		Column(modifier = Modifier.padding(12.dp)) {
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.CenterVertically
			) {
				Text(
					text = event.appName,
					style = MaterialTheme.typography.labelSmall,
					color = MaterialTheme.colorScheme.primary,
					fontWeight = FontWeight.SemiBold
				)
				Text(
					text = dateFormat.format(Date(event.timestamp)),
					style = MaterialTheme.typography.labelSmall,
					color = MaterialTheme.colorScheme.outline
				)
			}
			if (event.title.isNotBlank()) {
				Text(
					text = event.title,
					style = MaterialTheme.typography.bodyMedium,
					fontWeight = FontWeight.Medium,
					modifier = Modifier.padding(top = 4.dp)
				)
			}
			if (event.text.isNotBlank()) {
				Text(
					text = event.text,
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
					modifier = Modifier.padding(top = 2.dp)
				)
			}
		}
	}
}

@Composable
private fun EmptyEventsMessage() {
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.padding(vertical = 48.dp),
		contentAlignment = Alignment.Center
	) {
		Text(
			text = "Nenhuma mensagem capturada ainda.\nAtive o monitoramento para começar.",
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurfaceVariant,
			textAlign = TextAlign.Center
		)
	}
}
