package dev.kiritoxd.miaopu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import dev.kiritoxd.miaopu.ui.MiaopuApp
import dev.kiritoxd.miaopu.ui.MiaopuViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MiaopuViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { MiaopuApp(viewModel) }
    }
}

