package com.example.newaudio

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {

    private var playerService: PlayerService? = null
    private var isBound = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as PlayerService.LocalBinder
            playerService = binder.getService()
            isBound = true
        }
        override fun onServiceDisconnected(name: ComponentName?) { isBound = false }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val serviceIntent = Intent(this, PlayerService::class.java)
        startForegroundService(serviceIntent)
        bindService(serviceIntent, connection, BIND_AUTO_CREATE)

        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                val viewModel: AudioPlayerViewModel = viewModel()
                val tracks by viewModel.tracks.collectAsState()
                val current by viewModel.currentTrack.collectAsState()
                val isPlaying by viewModel.isPlaying.collectAsState()

                val pickAudio = rememberLauncherForActivityResult(
                    ActivityResultContracts.GetMultipleContents()
                ) { uris ->
                    val audioTracks = uris.map { uri ->
                        AudioTrack(uri.toString(), "Track ${System.currentTimeMillis()}", 180000)
                    }
                    viewModel.loadTracks(audioTracks)
                }

                LaunchedEffect(Unit) {
                    if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(arrayOf(Manifest.permission.READ_MEDIA_AUDIO), 100)
                    }
                }

                Surface(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("🎵 Audio Player", style = MaterialTheme.typography.headlineMedium)

                        Button(onClick = { pickAudio.launch("audio/*") }) {
                            Text("Pick Audio Files from Device")
                        }

                        current?.let {
                            Text("Now playing: ${it.title}", style = MaterialTheme.typography.titleMedium)
                        }

                        Slider(
                            value = 0.5f,
                            onValueChange = { /* viewModel.seekTo(...) */ }
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            IconButton(onClick = { viewModel.previous() }) { Text("⏮") }
                            IconButton(onClick = { viewModel.playPause() }) {
                                Text(if (isPlaying) "⏸" else "▶")
                            }
                            IconButton(onClick = { viewModel.next() }) { Text("⏭") }
                        }

                        Text("Playlist (${tracks.size} tracks)")
                        tracks.forEach { track ->
                            Text(track.title)
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        if (isBound) unbindService(connection)
        super.onDestroy()
    }
}
