package com.optirout

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

private const val FIXED_ORIGIN_LAT = -8.062742
private const val FIXED_ORIGIN_LON = -34.8739
private const val FIXED_DEST_LAT = -8.117809
private const val FIXED_DEST_LON = -34.900231

private data class ModalOption(
    val id: String,
    val label: String,
    val modoInicial: String,
    val restricaoModal: String? = null,
)

private data class RouteSegmentInfo(
    val titulo: String,
    val tempoHoras: Double,
    val custo: Double,
    val distanciaKm: Double,
)

private val modalOptions = listOf(
    ModalOption("multimodal", "Multimodal", "walk", null),
    ModalOption("walk", "A pé", "walk", "walk"),
    ModalOption("bike", "Bicicleta", "bike", "bike"),
    ModalOption("car", "Carro", "car", "car"),
    ModalOption("moto", "Moto", "moto", "moto"),
    ModalOption("bus", "Ônibus", "bus", "bus"),
    ModalOption("bus_access", "Ônibus + acesso a pé", "walk", "bus_com_acesso"),
    ModalOption("uber_car", "Uber carro", "uber_car", "uber_car"),
    ModalOption("uber_moto", "Uber moto", "uber_moto", "uber_moto"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptiRoutApp() {
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current

    var selectedModal by remember { mutableStateOf(modalOptions.first()) }
    var isCalculating by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showModalDialog by remember { mutableStateOf(false) }
    var showExplanationDialog by remember { mutableStateOf(false) }
    var valorGasto by remember { mutableStateOf("R$ --") }
    var tempoMedio by remember { mutableStateOf("--") }
    var distanciaTotal by remember { mutableStateOf("-- km") }
    var segmentosRota by remember { mutableStateOf(emptyList<RouteSegmentInfo>()) }
    var routePoints by remember {
        mutableStateOf(
            listOf(
                FIXED_ORIGIN_LAT to FIXED_ORIGIN_LON,
                FIXED_DEST_LAT to FIXED_DEST_LON,
            ),
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "OptiRout",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                navigationIcon = {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_menu),
                                contentDescription = "Menu",
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text("Selecionar modal") },
                                onClick = {
                                    showMenu = false
                                    showModalDialog = true
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("Explicacao do calculo") },
                                onClick = {
                                    showMenu = false
                                    showExplanationDialog = true
                                },
                            )
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            RouteMapCard(routePoints = routePoints)
            RouteMetricsCard(
                modalEscolhido = selectedModal.label,
                valorGasto = valorGasto,
                tempoMedio = tempoMedio,
                distanciaTotal = distanciaTotal,
            )
            RouteSegmentsSection(segmentos = segmentosRota)

            Button(
                onClick = {
                    scope.launch {
                        isCalculating = true
                        try {
                            val response = withContext(Dispatchers.IO) {
                                executeRouteCalculation(selectedModal)
                            }
                            val resumo = response.optJSONObject("resumo")
                            val tempoTotal = resumo?.optDouble("tempo_total", 0.0) ?: 0.0
                            val custoTotal = resumo?.optDouble("custo_total", 0.0) ?: 0.0
                            val distancia = resumo?.optDouble("distancia_total", 0.0) ?: 0.0
                            val segmentosJson = response.optJSONArray("segments")
                            val segmentos = segmentosJson?.length() ?: 0

                            valorGasto = "R$ %.2f".format(custoTotal)
                            tempoMedio = if (segmentos > 0) "%.2f min/trecho".format((tempoTotal / segmentos) * 60.0) else "--"
                            distanciaTotal = "%.2f km".format(distancia)
                            segmentosRota = parseRouteSegments(segmentosJson)
                            routePoints = parseRoutePoints(response)
                            Toast.makeText(ctx, "Calculo concluido", Toast.LENGTH_SHORT).show()
                        } catch (ex: Exception) {
                            Toast.makeText(
                                ctx,
                                "Falha ao calcular: ${ex.message}",
                                Toast.LENGTH_LONG,
                            ).show()
                        } finally {
                            isCalculating = false
                        }
                    }
                },
                enabled = !isCalculating,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (isCalculating) "Calculando..." else "Calcular rota multimodal")
            }
        }
    }

    if (showModalDialog) {
        ModalSelectionDialog(
            selectedModal = selectedModal,
            onSelect = {
                selectedModal = it
                showModalDialog = false
            },
            onDismiss = { showModalDialog = false },
        )
    }

    if (showExplanationDialog) {
        ExplanationDialog(onDismiss = { showExplanationDialog = false })
    }
}

private fun executeRouteCalculation(selectedModal: ModalOption): JSONObject {
    val payload = JSONObject().apply {
        put("origem", JSONArray(listOf(FIXED_ORIGIN_LAT, FIXED_ORIGIN_LON)))
        put("destino", JSONArray(listOf(FIXED_DEST_LAT, FIXED_DEST_LON)))
        put("modo_inicial", selectedModal.modoInicial)
        if (selectedModal.restricaoModal != null) {
            put("restricao_modal", selectedModal.restricaoModal)
        }
    }

    val url = URL("http://10.0.2.2:8000/api/calculate")
    val conn = (url.openConnection() as HttpURLConnection).apply {
        requestMethod = "POST"
        doOutput = true
        setRequestProperty("Content-Type", "application/json; charset=utf-8")
        connectTimeout = 10000
        readTimeout = 20000
    }

    conn.outputStream.use { it.write(payload.toString().toByteArray(Charsets.UTF_8)) }

    val code = conn.responseCode
    val body = if (code in 200..299) {
        conn.inputStream.bufferedReader().use { it.readText() }
    } else {
        conn.errorStream?.bufferedReader()?.use { it.readText() } ?: "HTTP $code"
    }
    conn.disconnect()

    if (code !in 200..299) {
        throw IllegalStateException(body)
    }

    return JSONObject(body)
}

private fun parseRoutePoints(response: JSONObject): List<Pair<Double, Double>> {
    val points = mutableListOf<Pair<Double, Double>>()
    val routePoints = response.optJSONArray("route_points")
    if (routePoints != null && routePoints.length() > 0) {
        for (i in 0 until routePoints.length()) {
            val point = routePoints.optJSONArray(i) ?: continue
            if (point.length() == 2) {
                points += point.optDouble(0) to point.optDouble(1)
            }
        }
        return if (points.isEmpty()) fallbackRoutePoints() else points
    }

    val edges = response.optJSONArray("edges") ?: return fallbackRoutePoints()

    for (i in 0 until edges.length()) {
        val edge = edges.optJSONObject(i) ?: continue
        val geometry = edge.optJSONArray("geometry")
        if (geometry != null && geometry.length() > 0) {
            for (j in 0 until geometry.length()) {
                val point = geometry.optJSONArray(j) ?: continue
                if (point.length() == 2) {
                    points += point.optDouble(0) to point.optDouble(1)
                }
            }
            continue
        }

        val origem = edge.optJSONArray("origem")
        val destino = edge.optJSONArray("destino")

        if (origem != null && origem.length() == 2) {
            points += origem.optDouble(0) to origem.optDouble(1)
        }
        if (destino != null && destino.length() == 2) {
            points += destino.optDouble(0) to destino.optDouble(1)
        }
    }

    return if (points.isEmpty()) fallbackRoutePoints() else points
}

private fun parseRouteSegments(segments: JSONArray?): List<RouteSegmentInfo> {
    if (segments == null || segments.length() == 0) return emptyList()

    return buildList {
        for (i in 0 until segments.length()) {
            val segment = segments.optJSONObject(i) ?: continue
            val titulo = when {
                segment.optString("servico").isNotBlank() -> segment.optString("servico")
                segment.optString("meio").isNotBlank() -> segment.optString("meio")
                segment.optString("modo").isNotBlank() -> segment.optString("modo")
                else -> "Segmento ${i + 1}"
            }

            add(
                RouteSegmentInfo(
                    titulo = titulo,
                    tempoHoras = segment.optDouble("tempo", 0.0),
                    custo = segment.optDouble("custo", 0.0),
                    distanciaKm = segment.optDouble("distancia", 0.0),
                ),
            )
        }
    }
}

private fun fallbackRoutePoints(): List<Pair<Double, Double>> = listOf(
    FIXED_ORIGIN_LAT to FIXED_ORIGIN_LON,
    FIXED_DEST_LAT to FIXED_DEST_LON,
)

private data class MapProjection(
    val centerLat: Double,
    val centerLon: Double,
    val zoom: Int,
)

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
private fun RouteMapCard(routePoints: List<Pair<Double, Double>>) {
    var zoomLevel by remember(routePoints) { mutableStateOf(computeInitialZoom(routePoints)) }
    var mapScale by remember(routePoints) { mutableStateOf(1f) }
    var mapOffset by remember(routePoints) { mutableStateOf(Offset.Zero) }
    val projection = remember(routePoints, zoomLevel) { buildMapProjection(routePoints, zoomLevel) }
    val routePixels = remember(routePoints, projection) {
        routePoints.map { latLonToWorldPixel(it.first, it.second, projection.zoom) }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "Mapa da rota",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "Cine Sao Luiz (Rua da Aurora) -> Faculdade Nova Roma (Rua Padre Carapuceiro)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Use pinça, arraste ou os botões para ampliar a rota.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .pointerInput(routePoints) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            mapScale = (mapScale * zoom).coerceIn(1f, 4f)
                            mapOffset += pan
                        }
                    }
            ) {
                val density = LocalDensity.current
                val widthPx = with(density) { maxWidth.toPx() }
                val heightPx = with(density) { maxHeight.toPx() }
                val viewportWidthPx = widthPx.coerceAtLeast(1f)
                val viewportHeightPx = heightPx.coerceAtLeast(1f)
                val tileSizeDp = with(density) { 256f.toDp() }

                val centerWorld = latLonToWorldPixel(projection.centerLat, projection.centerLon, projection.zoom)
                val effectiveWidthPx = viewportWidthPx / mapScale
                val effectiveHeightPx = viewportHeightPx / mapScale
                val topLeftWorldX = centerWorld.first - effectiveWidthPx / 2f - mapOffset.x / mapScale
                val topLeftWorldY = centerWorld.second - effectiveHeightPx / 2f - mapOffset.y / mapScale
                val tileSizePx = 256f
                val firstTileX = kotlin.math.floor(topLeftWorldX / tileSizePx).toInt() - 1
                val firstTileY = kotlin.math.floor(topLeftWorldY / tileSizePx).toInt() - 1
                val lastTileX = kotlin.math.floor((topLeftWorldX + effectiveWidthPx) / tileSizePx).toInt() + 1
                val lastTileY = kotlin.math.floor((topLeftWorldY + effectiveHeightPx) / tileSizePx).toInt() + 1

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFFE6E1D6)),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = mapScale,
                                scaleY = mapScale,
                                translationX = mapOffset.x,
                                translationY = mapOffset.y,
                            ),
                    ) {
                        for (tileX in firstTileX..lastTileX) {
                            for (tileY in firstTileY..lastTileY) {
                                val tilePixelX = tileX * tileSizePx - topLeftWorldX
                                val tilePixelY = tileY * tileSizePx - topLeftWorldY
                                val tileUrl = "https://basemaps.cartocdn.com/light_all/${projection.zoom}/${tileX}/${tileY}.png"
                                val context = LocalContext.current
                                val tileRequest = ImageRequest.Builder(context)
                                    .data(tileUrl)
                                    .addHeader("User-Agent", "OptiRout/1.0 (Android; Recife map preview)")
                                    .addHeader("Referer", "https://optirout.local")
                                    .build()

                                AsyncImage(
                                    model = tileRequest,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .offset(
                                            x = with(density) { tilePixelX.toDp() },
                                            y = with(density) { tilePixelY.toDp() },
                                        )
                                        .size(tileSizeDp),
                                )
                            }
                        }

                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawRoundRect(
                                color = Color(0x1A000000),
                                topLeft = Offset(8f, 8f),
                                size = Size(size.width - 16f, size.height - 16f),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(18f, 18f),
                            )

                            if (routePixels.size >= 2) {
                                val routePath = Path().apply {
                                    routePixels.forEachIndexed { index, point ->
                                        val x = point.first - topLeftWorldX
                                        val y = point.second - topLeftWorldY
                                        if (index == 0) moveTo(x, y) else lineTo(x, y)
                                    }
                                }

                                drawPath(path = routePath, color = Color(0xFF163B66), style = Stroke(width = 10f))
                                drawPath(path = routePath, color = Color(0xFF64D2FF), style = Stroke(width = 4f))

                                val start = routePixels.first()
                                val end = routePixels.last()
                                val startX = start.first - topLeftWorldX
                                val startY = start.second - topLeftWorldY
                                val endX = end.first - topLeftWorldX
                                val endY = end.second - topLeftWorldY

                                drawCircle(Color(0xFF101521), 11f, Offset(startX, startY))
                                drawCircle(Color(0xFF90EE90), 8f, Offset(startX, startY))
                                drawCircle(Color(0xFF101521), 11f, Offset(endX, endY))
                                drawCircle(Color(0xFFFFB74D), 8f, Offset(endX, endY))
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TextButton(
                            onClick = { zoomLevel = (zoomLevel - 1).coerceAtLeast(11) },
                        ) {
                            Text("-")
                        }
                        Text(
                            text = "Z$zoomLevel",
                            color = Color(0xFF1B2430),
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier
                                .background(Color(0xE6FFFFFF), RoundedCornerShape(999.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                        )
                        TextButton(
                            onClick = { zoomLevel = (zoomLevel + 1).coerceAtMost(18) },
                        ) {
                            Text("+")
                        }
                    }

                    Text(
                        text = "Base cartográfica: Carto",
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(10.dp),
                        color = Color(0xCC1B2430),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }
    }
}

private fun buildMapProjection(routePoints: List<Pair<Double, Double>>, zoomLevel: Int): MapProjection {
    val latValues = routePoints.map { it.first }
    val lonValues = routePoints.map { it.second }
    val minLat = latValues.minOrNull() ?: FIXED_ORIGIN_LAT
    val maxLat = latValues.maxOrNull() ?: FIXED_DEST_LAT
    val minLon = lonValues.minOrNull() ?: FIXED_ORIGIN_LON
    val maxLon = lonValues.maxOrNull() ?: FIXED_DEST_LON
    val centerLat = (minLat + maxLat) / 2.0
    val centerLon = (minLon + maxLon) / 2.0
    return MapProjection(
        centerLat = centerLat,
        centerLon = centerLon,
        zoom = zoomLevel,
    )
}

private fun computeInitialZoom(routePoints: List<Pair<Double, Double>>): Int {
    if (routePoints.isEmpty()) return 14

    val latValues = routePoints.map { it.first }
    val lonValues = routePoints.map { it.second }
    val latSpan = (latValues.maxOrNull() ?: 0.0) - (latValues.minOrNull() ?: 0.0)
    val lonSpan = (lonValues.maxOrNull() ?: 0.0) - (lonValues.minOrNull() ?: 0.0)
    val span = kotlin.math.max(latSpan, lonSpan)

    return when {
        span < 0.01 -> 16
        span < 0.03 -> 15
        span < 0.08 -> 14
        span < 0.15 -> 13
        else -> 12
    }
}

private fun latLonToWorldPixel(lat: Double, lon: Double, zoom: Int): Pair<Float, Float> {
    val scale = 256.0 * (1 shl zoom)
    val x = ((lon + 180.0) / 360.0) * scale
    val latRadians = Math.toRadians(lat)
    val y = (1.0 - kotlin.math.ln(kotlin.math.tan(latRadians) + 1.0 / kotlin.math.cos(latRadians)) / kotlin.math.PI) / 2.0 * scale
    return x.toFloat() to y.toFloat()
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ModalSelectionDialog(
    selectedModal: ModalOption,
    onSelect: (ModalOption) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Fechar") }
        },
        title = { Text("Selecionar modal") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Escolha qual modal sera usado no calculo.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    modalOptions.forEach { option ->
                        ModalChip(
                            label = option.label,
                            color = if (option.id == selectedModal.id) MaterialTheme.colorScheme.primary else Color(0xFF9AA7C7),
                        )
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    modalOptions.forEach { option ->
                        TextButton(onClick = { onSelect(option) }) {
                            Text(option.label)
                        }
                    }
                }
            }
        },
    )
}

@Composable
private fun ExplanationDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Fechar") }
        },
        title = { Text("Explicacao do calculo") },
        text = {
            Text(
                "O app envia a origem e o destino fixos para o backend Python, usa o modal escolhido para calcular a rota e recebe o resumo final com os segmentos.",
            )
        },
    )
}

@Composable
private fun RouteMetricsCard(modalEscolhido: String, valorGasto: String, tempoMedio: String, distanciaTotal: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "Resumo do trajeto",
                style = MaterialTheme.typography.titleMedium,
            )
            MetricRow(label = "Modal escolhido", value = modalEscolhido)
            MetricRow(label = "Valor gasto", value = valorGasto)
            MetricRow(label = "Tempo medio", value = tempoMedio)
            MetricRow(label = "Distancia total", value = distanciaTotal)
        }
    }
}

@Composable
private fun RouteSegmentsSection(segmentos: List<RouteSegmentInfo>) {
    if (segmentos.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Segmentos da rota",
            style = MaterialTheme.typography.titleMedium,
        )

        segmentos.forEachIndexed { index, segmento ->
            RouteSegmentCard(
                numero = index + 1,
                segmento = segmento,
            )
        }
    }
}

@Composable
private fun RouteSegmentCard(numero: Int, segmento: RouteSegmentInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Segmento $numero",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = segmento.titulo,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            MetricRow(label = "Tempo", value = "%.2f min".format(segmento.tempoHoras * 60.0))
            MetricRow(label = "Custo", value = "R$ %.2f".format(segmento.custo))
            MetricRow(label = "Distancia", value = "%.2f km".format(segmento.distanciaKm))
        }
    }
}

@Composable
private fun MetricRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ModalChip(label: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.18f),
        shape = RoundedCornerShape(999.dp),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            color = color,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}
