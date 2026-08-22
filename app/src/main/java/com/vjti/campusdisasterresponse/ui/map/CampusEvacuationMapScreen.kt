package com.vjti.campusdisasterresponse.ui.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vjti.campusdisasterresponse.data.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampusEvacuationMapScreen(
    mapData: CampusMapData
) {
    var scale by remember {
        mutableFloatStateOf(1f)
    }

    var offset by remember {
        mutableStateOf(Offset.Zero)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Evacuation Map — ${mapData.campusName}",
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF121212)
                )
            )
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF181818))
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom)
                            .coerceIn(0.5f, 4.0f)

                        offset += pan
                    }
                }
        ) {

            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                val width = size.width
                val height = size.height

                // Buildings
                mapData.buildings.forEach { building ->

                    val bLeft =
                        building.left * width * scale +
                            offset.x

                    val bTop =
                        building.top * height * scale +
                            offset.y

                    val bWidth =
                        (building.right - building.left) *
                            width * scale

                    val bHeight =
                        (building.bottom - building.top) *
                            height * scale

                    drawRect(
                        color = Color(0xFF2D2D2D),
                        topLeft = Offset(
                            bLeft,
                            bTop
                        ),
                        size = Size(
                            bWidth,
                            bHeight
                        )
                    )

                    drawRect(
                        color = Color(0xFF757575),
                        topLeft = Offset(
                            bLeft,
                            bTop
                        ),
                        size = Size(
                            bWidth,
                            bHeight
                        ),
                        style = Stroke(
                            width = 2f
                        )
                    )
                }

                // Evacuation Routes
                mapData.routes.forEach { route ->

                    if (route.pathPoints.size > 1) {

                        val path = Path()

                        val start =
                            route.pathPoints.first()

                        path.moveTo(
                            start.x * width * scale +
                                offset.x,
                            start.y * height * scale +
                                offset.y
                        )

                        for (
                            i in 1 until
                                route.pathPoints.size
                        ) {
                            val point =
                                route.pathPoints[i]

                            path.lineTo(
                                point.x * width * scale +
                                    offset.x,
                                point.y * height * scale +
                                    offset.y
                            )
                        }

                        drawPath(
                            path = path,
                            color =
                                if (route.isPrimary) {
                                    Color(0xFF00E676)
                                } else {
                                    Color(0xFFFFD600)
                                },
                            style = Stroke(
                                width = 8f * scale,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }
                }

                // Location Markers
                mapData.markers.forEach { marker ->

                    val cx =
                        marker.position.x *
                            width *
                            scale +
                            offset.x

                    val cy =
                        marker.position.y *
                            height *
                            scale +
                            offset.y

                    val markerColor =
                        when (marker.type) {
                            MarkerType.EXIT ->
                                Color(0xFFFF1744)

                            MarkerType.ASSEMBLY_POINT ->
                                Color(0xFF2979FF)

                            MarkerType.FIRST_AID ->
                                Color(0xFFF50057)

                            MarkerType.SAFE_AREA ->
                                Color(0xFF00E676)
                        }

                    drawCircle(
                        color = markerColor,
                        radius = 14f * scale,
                        center = Offset(cx, cy)
                    )
                }
            }

            // Map Legend
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
                    .background(
                        Color(0xDD000000)
                    )
                    .padding(12.dp)
            ) {
                Text(
                    text = "Map Legend",
                    color = Color.White,
                    fontSize = 14.sp
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                LegendRow(
                    color = Color(0xFF00E676),
                    label = "Primary Evacuation Route"
                )

                LegendRow(
                    color = Color(0xFFFFD600),
                    label = "Secondary Route"
                )

                LegendRow(
                    color = Color(0xFFFF1744),
                    label = "Exit Point"
                )

                LegendRow(
                    color = Color(0xFF2979FF),
                    label = "Assembly Point"
                )

                LegendRow(
                    color = Color(0xFFF50057),
                    label = "First-Aid Location"
                )

                LegendRow(
                    color = Color(0xFF00E676),
                    label = "Designated Safe Area"
                )
            }
        }
    }
}

@Composable
private fun LegendRow(
    color: Color,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(
            vertical = 2.dp
        )
    ) {

        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color)
        )

        Spacer(
            modifier = Modifier.width(8.dp)
        )

        Text(
            text = label,
            color = Color.White,
            fontSize = 11.sp
        )
    }
}
