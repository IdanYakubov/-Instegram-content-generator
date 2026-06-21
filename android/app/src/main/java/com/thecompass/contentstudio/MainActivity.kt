package com.thecompass.contentstudio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.thecompass.contentstudio.ui.CompassApp
import com.thecompass.contentstudio.ui.PostViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: PostViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CompassApp(viewModel)
        }
    }
}
