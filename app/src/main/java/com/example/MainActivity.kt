package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.data.AddaBuzzRepository
import com.example.data.LiveKitManager
import com.example.data.SessionManager
import com.example.ui.navigation.AddaBuzzApp
import com.example.ui.theme.AddaBuzzTheme

class MainActivity : ComponentActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var liveKitManager: LiveKitManager
    private lateinit var repository: AddaBuzzRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        sessionManager = SessionManager(applicationContext)
        liveKitManager = LiveKitManager(applicationContext)
        repository = AddaBuzzRepository(applicationContext, sessionManager, liveKitManager)

        setContent {
            AddaBuzzTheme {
                AddaBuzzApp(repository = repository)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        liveKitManager.release()
    }
}
