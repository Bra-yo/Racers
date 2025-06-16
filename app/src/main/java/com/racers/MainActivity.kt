package com.racers

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import com.racers.ui.game.GameScreen
import com.racers.ui.theme.RacingGameTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RacingGameTheme {
                Surface(color = MaterialTheme.colors.background) {
                    GameScreen()
                }
            }
        }
    }
}