package com.racers.ui.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.compose.ui.geometry.Offset
import com.racers.ui.game.model.Car
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

data class Obstacle(
    val position: Offset,
    val size: Float = 40f
)

class GameViewModel(
    private val soundHelper: SoundHelper,
    private val highScoreManager: HighScoreManager
) : ViewModel() {
    var carPosition by mutableStateOf(0f)
        private set
    
    var carSpeed by mutableStateOf(0f)
        private set
        
    var steeringAngle by mutableStateOf(0f)
        private set
    
    private val maxSpeed = 300f
    private val minSpeed = 0f
    private val accelerationRate = 5f
    private val brakeRate = 8f
    private val steeringRate = 3f
    private val naturalDeceleration = 2f

    var score by mutableStateOf(0)
        private set
        
    var isGameOver by mutableStateOf(false)
        private set
    
    var gameSpeed by mutableStateOf(1f)
        private set

    var isPaused by mutableStateOf(false)
        private set
    
    var currentSpeed by mutableStateOf(0f)
        private set
    
    var difficulty by mutableStateOf(Difficulty.NORMAL)
        private set

    val highScore = highScoreManager.highScore.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        0
    )

    enum class Difficulty {
        EASY, NORMAL, HARD
    }

    private val _aiCars = MutableStateFlow<List<Car>>(emptyList())
    val aiCars = _aiCars.asStateFlow()

    private val lanePositions = listOf(-100f, 0f, 100f) // Left, Center, Right lanes
    private val aiCarColors = listOf(
        Color.Yellow,
        Color.Magenta,
        Color.Cyan,
        Color.White
    )

    private val carSizes = listOf(
        Size(40f, 60f),  // Sports car
        Size(50f, 70f),  // Sedan
        Size(60f, 90f)   // Truck
    )

    init {
        startAICarGeneration()
        soundHelper.startBackgroundMusic()
    }

    private fun startAICarGeneration() {
        viewModelScope.launch {
            while (true) {
                delay(Random.nextLong(2000, 4000))
                if (!isGameOver) {
                    generateAICar()
                }
            }
        }
    }

    private fun generateAICar() {
        val availableLanes = lanePositions.toMutableList()
        // Remove lanes that are occupied by AI cars near the spawn point
        _aiCars.value.forEach { car ->
            if (car.position.y < -100f) {
                availableLanes.remove(lanePositions[car.lane + 1])
            }
        }

        if (availableLanes.isNotEmpty()) {
            val behavior = AIBehavior.values().random()
            val baseSpeed = when (behavior) {
                AIBehavior.AGGRESSIVE -> Random.nextFloat() * 3f + 4f  // 4-7
                AIBehavior.NORMAL -> Random.nextFloat() * 2f + 3f     // 3-5
                AIBehavior.CAUTIOUS -> Random.nextFloat() * 2f + 2f   // 2-4
            }

            val lane = lanePositions.indexOf(availableLanes.random())
            val newCar = Car(
                id = Random.nextInt(),
                position = Offset(availableLanes.random(), -100f),
                speed = baseSpeed,
                color = aiCarColors.random(),
                isAICar = true,
                lane = lane - 1,
                size = carSizes.random(),
                behavior = behavior,
                acceleration = 0f
            )
            _aiCars.value = _aiCars.value + newCar
        }
    }

    fun moveCarLeft() {
        if (carPosition > -200f) carPosition -= 20f
    }

    fun moveCarRight() {
        if (carPosition < 200f) carPosition += 20f
    }

    fun accelerate() {
        currentSpeed = (currentSpeed + accelerationRate).coerceAtMost(maxSpeed)
        soundHelper.playEngineSound(currentSpeed / maxSpeed)
    }

    fun brake() {
        currentSpeed = (currentSpeed - brakeRate).coerceAtLeast(0f)
        soundHelper.playEngineSound(currentSpeed / maxSpeed)
    }

    fun steer(angle: Float) {
        steeringAngle = angle.coerceIn(-45f, 45f)
        // Convert steering angle to lateral movement
        val lateralMovement = (steeringAngle / 45f) * steeringRate * (carSpeed / maxSpeed)
        carPosition = (carPosition + lateralMovement).coerceIn(-200f, 200f)
    }

    fun updateCarPhysics() {
        // Natural deceleration when not accelerating
        if (carSpeed > 0) {
            carSpeed = (carSpeed - naturalDeceleration).coerceAtLeast(minSpeed)
        }
        
        // Reset steering angle gradually
        steeringAngle *= 0.95f
    }

    fun updateScore() {
        score += 1
        if (score % 100 == 0) gameSpeed += 0.2f
    }

    fun checkCollision(carPosition: Offset, otherPosition: Offset, carSize: Size, otherSize: Size): Boolean {
        return abs(carPosition.x - otherPosition.x) < (carSize.width + otherSize.width) / 2 &&
               abs(carPosition.y - otherPosition.y) < (carSize.height + otherSize.height) / 2
    }

    fun updateGameState() {
        // Check collisions with AI cars only
        _aiCars.value.forEach { aiCar ->
            if (checkCollision(
                Offset(carPosition, 500f),
                aiCar.position,
                Size(40f, 40f),
                aiCar.size
            )) {
                handleCollision()
            }
        }
    }

    private fun handleCollision() {
        isGameOver = true
        soundHelper.playCollisionSound()
        viewModelScope.launch {
            highScoreManager.updateHighScore(score)
        }
    }

    fun togglePause() {
        isPaused = !isPaused
    }

    fun setDifficulty(newDifficulty: Difficulty) {
        difficulty = newDifficulty
        gameSpeed = when (difficulty) {
            Difficulty.EASY -> 0.8f
            Difficulty.NORMAL -> 1.0f
            Difficulty.HARD -> 1.2f
        }
    }

    fun resetGame() {
        carPosition = 0f
        score = 0
        gameSpeed = 1f
        isGameOver = false
        _aiCars.value = emptyList()
    }

    override fun onCleared() {
        super.onCleared()
        soundHelper.release()
    }
}