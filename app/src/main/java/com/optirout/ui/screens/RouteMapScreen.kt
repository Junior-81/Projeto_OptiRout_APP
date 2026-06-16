package com.optirout.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.optirout.data.model.Segment
import com.optirout.data.model.Summary
import com.optirout.viewmodel.RouteState
import com.optirout.viewmodel.RouteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteMapScreen(
    viewModel: RouteViewModel,
    onNavigateBack: () -> Unit,
) {
    val routeState by viewModel.routeState.collectAsState()
    val selectedMode by viewModel.selectedMode.collectAsState()
    val response = (routeState as? RouteState.Success)?.response

    BackHandler { onNavigateBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = selectedMode?.displayName ?: "Rota",
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
    ) { innerPadding ->
        if (response == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val allPoints = remember(response) {
            response.edges.flatMap { edge -> edge.geometry.map { LatLng(it[0], it[1]) } }
        }

        val center = remember(allPoints) {
            if (allPoints.isEmpty()) {
                LatLng(-8.09, -34.89)
            } else {
                LatLng(
                    allPoints.sumOf { it.latitude } / allPoints.size,
                    allPoints.sumOf { it.longitude } / allPoints.size,
                )
            }
        }

        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(center, 13f)
        }

        LaunchedEffect(allPoints) {
            if (allPoints.size >= 2) {
                val boundsBuilder = LatLngBounds.Builder()
                allPoints.forEach { boundsBuilder.include(it) }
                runCatching {
                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100),
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            GoogleMap(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(.8f),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = true,
                    myLocationButtonEnabled = false,
                ),
            ) {
                response.edges.forEach { edge ->
                    val points = edge.geometry.map { LatLng(it[0], it[1]) }
                    if (points.size >= 2) {
                        Polyline(
                            points = points,
                            color = modeColor(edge.mode),
                            width = 14f,
                        )
                    }
                }

                allPoints.firstOrNull()?.let { origin ->
                    Marker(state = MarkerState(position = origin), title = "Origem")
                }
                allPoints.lastOrNull()?.let { destination ->
                    Marker(state = MarkerState(position = destination), title = "Destino")
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.2f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item { SummaryCard(summary = response.summary) }
                items(response.segments) { segment -> SegmentCard(segment = segment) }
            }
        }
    }
}

@Composable
private fun SummaryCard(summary: Summary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Resumo do trajeto",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
            ) {
                SummaryMetric("Distância", "%.1f km".format(summary.totalDistance))
                SummaryMetric("Tempo", formatTime(summary.totalTime))
                SummaryMetric(
                    "Custo",
                    if (summary.totalCost == 0.0) "Grátis" else "R$ %.2f".format(summary.totalCost),
                )
                SummaryMetric("Vel. média", "%.0f km/h".format(summary.totalAverageSpeed))
            }
        }
    }
}

@Composable
private fun SummaryMetric(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SegmentCard(segment: Segment) {
    val color = modeColor(segment.mode)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(color),
            )
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .weight(1f),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        color = color.copy(alpha = 0.15f),
                        shape = MaterialTheme.shapes.small,
                    ) {
                        Text(
                            text = modeDisplayName(segment.mode),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = color,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = formatTime(segment.time),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                    )
                }

                Spacer(Modifier.height(6.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "%.2f km".format(segment.distance),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "%.0f km/h".format(segment.averageSpeedKmh),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (segment.cost > 0) {
                        Text(
                            text = "R$ %.2f".format(segment.cost),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                segment.line?.let { line ->
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = line,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

private fun modeColor(mode: String): Color = when (mode.lowercase()) {
    "moto" -> Color(0xFFFF7043)
    "bus" -> Color(0xFF42A5F5)
    "walk" -> Color(0xFF66BB6A)
    "car" -> Color(0xFFAB47BC)
    "uber_car" -> Color(0xFFFFCA28)
    "uber_moto" -> Color(0xFF28F8FF)
    else -> Color(0xFF90A4AE)
}

private fun modeDisplayName(mode: String): String = when (mode.lowercase()) {
    "moto" -> "Moto"
    "bus" -> "Ônibus"
    "walk" -> "A pé"
    "car" -> "Carro"
    "uber_car" -> "Uber"
    "uber_moto" -> "Uber Moto"
    else -> mode.replaceFirstChar { it.uppercaseChar() }
}

private fun formatTime(hours: Double): String {
    val totalMinutes = (hours * 60).toInt()
    return if (totalMinutes < 60) "$totalMinutes min"
    else "${totalMinutes / 60}h ${totalMinutes % 60}min"
}
