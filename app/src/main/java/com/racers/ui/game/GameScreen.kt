package com.racers.ui.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.racers.ui.game.model.Car
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun GameScreen(
    viewModel: GameViewModel = viewModel()
) {
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(16L) // ~60 FPS
            if (!viewModel.isGameOver) {
                viewModel.updateScore()
                viewModel.updateCarPhysics()
                viewModel.updateAICars()
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Green.copy(alpha = 0.3f))
        ) {
            // Score
            Text(
                text = "Score: ${viewModel.score}",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.h6
            )

            // Track
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                // Draw racing track
                drawRect(
                    color = Color.Gray,
                    topLeft = Offset(size.width * 0.2f, 0f),
                    size = size.copy(width = size.width * 0.6f)
                )
                
                // Draw track lines with animation
                val lineSpacing = size.height / 20
                for (i in 0..20) {
                    val yOffset = (i * lineSpacing + (viewModel.score * viewModel.gameSpeed)) % size.height
                    drawLine(
                        color = Color.White,
                        start = Offset(size.width * 0.5f, yOffset),
                        end = Offset(size.width * 0.5f, yOffset + lineSpacing / 2),
                        strokeWidth = 5f
                    )
                }

                // Draw AI Cars only
                viewModel.aiCars.collectAsState().value.forEach { aiCar ->
                    drawRect(
                        color = aiCar.color,
                        topLeft = Offset(
                            size.width / 2 + aiCar.position.x - aiCar.size.width / 2,
                            aiCar.position.y - aiCar.size.height / 2
                        ),
                        size = aiCar.size
                    )
                    
                    // Draw behavior indicator
                    val indicatorColor = when (aiCar.behavior) {
                        AIBehavior.AGGRESSIVE -> Color.Red
                        AIBehavior.NORMAL -> Color.Yellow
                        AIBehavior.CAUTIOUS -> Color.Green
                    }
                    drawCircle(
                        color = indicatorColor,
                        radius = 5f,
                        center = Offset(
                            size.width / 2 + aiCar.position.x,
                            aiCar.position.y - aiCar.size.height / 2
                        )
                    )
                }
            }
            
            // Car
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .offset(
                        x = viewModel.carPosition.dp,
                        y = 500.dp
                    )
                    .background(Color.Blue)
            )

            // Game Controls
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.BottomCenter)
            ) {
                // Steering Wheel
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 16.dp)
                        .rotate(viewModel.steeringAngle)
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                viewModel.steer(viewModel.steeringAngle + dragAmount.x * 0.5f)
                            }
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = Color.DarkGray,
                            radius = size.minDimension / 2,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 8f)
                        )
                        // Draw steering wheel spokes
                        for (i in 0..2) {
                            drawLine(
                                color = Color.DarkGray,
                                start = center,
                                end = Offset(
                                    x = center.x + cos(i * Math.PI / 3) * size.width / 2,
                                    y = center.y + sin(i * Math.PI / 3) * size.height / 2
                                ),
                                strokeWidth = 8f
                            )
                        }
                    }
                }

                // Pedals
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Brake Pedal
                    Button(
                        onClick = { },
                        modifier = Modifier
                            .size(80.dp)
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = { viewModel.brake() },
                                    onDragEnd = { },
                                    onDragCancel = { },
                                    onDrag = { _, _ -> viewModel.brake() }
                                )
                            },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red)
                    ) {
                        Text("BRAKE", color = Color.White)
                    }

                    // Accelerator Pedal
                    Button(
                        onClick = { },
                        modifier = Modifier
                            .size(80.dp)
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = { viewModel.accelerate() },
                                    onDragEnd = { },
                                    onDragCancel = { },
                                    onDrag = { _, _ -> viewModel.accelerate() }
                                )
                            },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Green)
                    ) {
                        Text("GAS", color = Color.White)
                    }
                }
            }

            // Game Over Dialog
            if (viewModel.isGameOver) {
                AlertDialog(
                    onDismissRequest = { },
                    title = { Text("Game Over") },
                    text = { Text("Score: ${viewModel.score}") },
                    confirmButton = {
                        Button(onClick = { viewModel.resetGame() }) {
                            Text("Play Again")
                        }
                    }
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                // Speedometer
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Speed: ${(viewModel.currentSpeed * 100).toInt()} km/h",
                        color = Color.White,
                        style = MaterialTheme.typography.h6
                    )
                }

                // High Score
                val highScore by viewModel.highScore.collectAsState()
                Text(
                    text = "High Score: $highScore",
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp),
                    color = Color.White,
                    style = MaterialTheme.typography.h6
                )

                // Pause Button
                IconButton(
                    onClick = { viewModel.togglePause() },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = if (viewModel.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = "Pause/Resume",
                        tint = Color.White
                    )
                }

                // Visual effects for acceleration
                if (viewModel.currentSpeed > 0) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val speedLines = (viewModel.currentSpeed / viewModel.maxSpeed * 10).toInt()
                        repeat(speedLines) {
                            drawLine(
                                color = Color.White.copy(alpha = 0.3f),
                                start = Offset(Random.nextFloat() * size.width, 0f),
                                end = Offset(Random.nextFloat() * size.width, size.height),
                                strokeWidth = 2f
                            )
                        }
                    }
                }

                // Pause Menu
                if (viewModel.isPaused) {
                    PauseMenu(
                        onResume = { viewModel.togglePause() },
                        onDifficultyChanged = { viewModel.setDifficulty(it) },
                        currentDifficulty = viewModel.difficulty
                    )
                }
            }
        }
    }
}

@Composable
private fun PauseMenu(
    onResume: () -> Unit,
    onDifficultyChanged: (GameViewModel.Difficulty) -> Unit,
    currentDifficulty: GameViewModel.Difficulty
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("PAUSED", style = MaterialTheme.typography.h4, color = Color.White)
            
            GameViewModel.Difficulty.values().forEach { difficulty ->
                Button(
                    onClick = { onDifficultyChanged(difficulty) },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = if (difficulty == currentDifficulty) 
                            MaterialTheme.colors.primary else MaterialTheme.colors.surface
                    )
                ) {
                    Text(difficulty.name)
                }
            }
            
            Button(onClick = onResume) {
                Text("RESUME")
            }
        }
    }
}