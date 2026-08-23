package com.vjti.campusdisasterresponse.ui.map

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vjti.campusdisasterresponse.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampusEvacuationMapScreen(
    onBack: () -> Unit = {}
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
                        text = "VJTI Campus Layout",
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack
                    ) {
                        Icon(
                            imageVector =
                                Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors =
                    TopAppBarDefaults
                        .topAppBarColors(
                            containerColor =
                                Color(0xFF121212)
                        )
            )
        }
    ) { innerPadding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(
                        Color(0xFF101010)
                    )
        ) {
            Image(
                painter =
                    painterResource(
                        R.drawable.vjti_campus_map
                    ),
                contentDescription =
                    "VJTI campus layout reference",
                contentScale =
                    ContentScale.Fit,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            translationX =
                                offset.x
                            translationY =
                                offset.y
                        }
                        .pointerInput(Unit) {
                            detectTransformGestures {
                                    _,
                                    pan,
                                    zoom,
                                    _ ->

                                val newScale =
                                    (scale * zoom)
                                        .coerceIn(
                                            1f,
                                            5f
                                        )

                                scale = newScale

                                if (
                                    newScale <= 1.01f
                                ) {
                                    offset =
                                        Offset.Zero
                                } else {
                                    offset += pan
                                }
                            }
                        }
            )

            Column(
                modifier =
                    Modifier
                        .align(
                            Alignment.BottomCenter
                        )
                        .background(
                            Color(0xDD000000)
                        )
                        .padding(10.dp)
            ) {
                Text(
                    text =
                        "Reference campus layout • Pinch to zoom • Drag to pan",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight =
                        FontWeight.SemiBold
                )

                Text(
                    text =
                        "Photo: Ankitdaf / Wikimedia Commons • CC BY-SA 3.0 • 2012",
                    color =
                        Color.LightGray,
                    fontSize = 10.sp
                )
            }
        }
    }
}
