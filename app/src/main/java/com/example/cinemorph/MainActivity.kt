package com.example.cinemorph

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject

private val Bg = Color(0xFF080B12)
private val Surface = Color(0xFF111827)
private val Purple = Color(0xFF8B5CF6)
private val Pink = Color(0xFFEC4899)

class MainActivity : ComponentActivity() {
    private var exportJson = "{}"
    private val createDocument = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) contentResolver.openOutputStream(uri)?.use { it.write(exportJson.toByteArray()) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { CineMorphApp(onExport = { json -> exportJson = json; createDocument.launch("cinemorph-project.json") }) }
    }
}

@Composable
private fun CineMorphApp(onExport: (String) -> Unit) {
    var tab by remember { mutableIntStateOf(0) }
    var prompt by remember { mutableStateOf("") }
    var scenes by remember { mutableStateOf(listOf<String>()) }
    var status by remember { mutableStateOf("Ready — offline mode") }
    val titles = listOf("Home", "Studio", "AI Script", "Library")

    fun buildPlan() {
        if (prompt.isBlank()) return
        scenes = (0 until 8).map { i ->
            val start = i * 7.5
            val end = start + 7.5
            "Scene ${i + 1} • ${"%.1f".format(start)}–${"%.1f".format(end)}s • ${prompt.trim()}"
        }
        status = "60-second storyboard created • 8 scenes • 7.5s each"
    }

    fun exportProject() {
        val root = JSONObject().apply {
            put("app", "CineMorph AI")
            put("durationSeconds", 60)
            put("prompt", prompt)
            put("scenes", JSONArray(scenes))
        }
        onExport(root.toString(2))
        status = "Project JSON exported"
    }

    MaterialTheme(colorScheme = darkColorScheme(primary = Purple, background = Bg, surface = Surface)) {
        Scaffold(
            containerColor = Bg,
            bottomBar = {
                NavigationBar(containerColor = Surface) {
                    listOf(Icons.Default.Home, Icons.Default.Movie, Icons.Default.AutoAwesome, Icons.Default.Folder).forEachIndexed { i, icon ->
                        NavigationBarItem(selected = tab == i, onClick = { tab = i }, icon = { Icon(icon, null) }, label = { Text(titles[i]) })
                    }
                }
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
                contentPadding = PaddingValues(vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text("CineMorph AI", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                    Text("60-second cinematic movie workspace", color = Color(0xFF9CA3AF), fontSize = 14.sp)
                }
                item {
                    Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Surface)) {
                        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Text(if (tab == 2) "Script & Storyboard" else "Create your movie", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.SemiBold)
                            OutlinedTextField(value = prompt, onValueChange = { prompt = it }, modifier = Modifier.fillMaxWidth(), minLines = 4, placeholder = { Text("Describe your story, characters, action and cinematic style...") })
                            Button(onClick = { buildPlan() }, enabled = prompt.isNotBlank(), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                                Icon(Icons.Default.AutoAwesome, null); Spacer(Modifier.width(8.dp)); Text("Build 60-sec Movie Plan")
                            }
                            OutlinedButton(onClick = { exportProject() }, enabled = scenes.isNotEmpty(), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                                Icon(Icons.Default.Create, null); Spacer(Modifier.width(8.dp)); Text("Export Project")
                            }
                            Text(status, color = Color(0xFF86EFAC), fontWeight = FontWeight.Medium)
                        }
                    }
                }
                if (scenes.isNotEmpty()) {
                    item { Text("60-second Timeline", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold) }
                    items(scenes) { scene ->
                        Card(colors = CardDefaults.cardColors(containerColor = Surface), shape = RoundedCornerShape(14.dp)) {
                            Row(Modifier.fillMaxWidth().padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Movie, null, tint = Pink)
                                Spacer(Modifier.width(12.dp))
                                Text(scene, color = Color.White, fontSize = 14.sp)
                            }
                        }
                    }
                }
                item { Text("Offline-ready: storyboard, timeline and project export work without an API. Real AI video generation requires a provider later.", color = Color(0xFF9CA3AF), fontSize = 12.sp) }
            }
        }
    }
}
