package com.miracleshop.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.runtime.CompositionLocalProvider
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class StoreApp(
    val name: String,
    val description: String,
    val version: String,
    val downloadUrl: String,
    val category: String,
    val imageUrl: String
)

private const val GITHUB_API = "https://api.github.com/users/ari900630-tech/repos?per_page=100"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MiracleShopApp() }
    }

    @Composable
    private fun MiracleShopApp() {
        var showSplash by remember { mutableStateOf(true) }
        val scope = rememberCoroutineScope()
        val splashScale = remember { Animatable(1.65f) }
        val splashRotation = remember { Animatable(0f) }
        val splashAlpha = remember { Animatable(1f) }

        LaunchedEffect(Unit) {
            launch {
                splashScale.animateTo(
                    0.62f,
                    animationSpec = tween(1250, easing = FastOutSlowInEasing)
                )
            }
            launch {
                splashRotation.animateTo(
                    540f,
                    animationSpec = tween(1250, easing = FastOutSlowInEasing)
                )
            }
            launch {
                delay(850)
                splashAlpha.animateTo(0f, tween(350))
            }
            delay(1350)
            showSplash = false
        }

        if (showSplash) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFF5B3FD3)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Miracle Shop",
                            tint = Color.White,
                            modifier = Modifier
                                .size(116.dp)
                                .graphicsLayer {
                                    scaleX = splashScale.value
                                    scaleY = splashScale.value
                                    rotationZ = splashRotation.value
                                    alpha = splashAlpha.value
                                }
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            "Miracle Shop",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.graphicsLayer { alpha = splashAlpha.value }
                        )
                    }
                }
            }
            return
        }

        var query by remember { mutableStateOf(TextFieldValue("")) }
        var selectedTab by remember { mutableIntStateOf(0) }
        var selectedCategory by remember { mutableIntStateOf(0) }
        var dark by remember { mutableStateOf(false) }
        var apps by remember { mutableStateOf<List<StoreApp>>(emptyList()) }
        var loading by remember { mutableStateOf(true) }
        var error by remember { mutableStateOf<String?>(null) }
        var reloadKey by remember { mutableIntStateOf(0) }

        LaunchedEffect(reloadKey) {
            loading = true
            error = null
            Thread {
                try {
                    val loaded = loadAppsFromGithub()
                    runOnUiThread {
                        apps = loaded
                        loading = false
                        error = if (loaded.isEmpty()) "לא נמצאו קבצי APK ב-Releases של ari900630-tech." else null
                    }
                } catch (_: Exception) {
                    runOnUiThread {
                        loading = false
                        error = "לא ניתן לטעון את האפליקציות כרגע."
                    }
                }
            }.start()
        }

        val filtered = apps.filter {
            (it.name.contains(query.text, true) || it.description.contains(query.text, true)) &&
                (selectedCategory == 0 || it.category == listOf("הכול", "כללי", "כלים")[selectedCategory])
        }

        val background by animateColorAsState(
            if (dark) Color(0xFF101116) else Color(0xFFF7F7FB),
            label = "bg"
        )
        val foreground = if (dark) Color(0xFFF4F4F6) else Color(0xFF17181C)
        val card = if (dark) Color(0xFF1C1D24) else Color.White

        MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme()) {
            Surface(Modifier.fillMaxSize(), color = background) {
                Column(Modifier.fillMaxSize().padding(horizontal = 14.dp)) {
                    Spacer(Modifier.height(14.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            modifier = Modifier.size(48.dp).clip(CircleShape).background(card),
                            onClick = { dark = !dark }
                        ) {
                            Icon(
                                if (dark) Icons.Default.LightMode else Icons.Default.DarkMode,
                                "מצב יום/לילה",
                                tint = foreground
                            )
                        }
                        OutlinedTextField(
                            value = query,
                            onValueChange = { query = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("חיפוש אפליקציות…") },
                            leadingIcon = { Icon(Icons.Default.Search, null) },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp)
                        )
                        IconButton(
                            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(15.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            onClick = { }
                        ) {
                            Icon(Icons.Default.PlayArrow, "Miracle Shop", Modifier.size(30.dp))
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("הכול", "כללי", "כלים").forEachIndexed { index, label ->
                                    FilterChip(
                                        selected = selectedCategory == index,
                                        onClick = { selectedCategory = index },
                                        label = {
                                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                                                Text(label)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))

                    when {
                        loading -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                        error != null -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.CloudOff, null, Modifier.size(48.dp))
                                Spacer(Modifier.height(10.dp))
                                Text(error!!, color = foreground)
                                Spacer(Modifier.height(12.dp))
                                Button(onClick = { reloadKey++ }) { Text("רענון") }
                            }
                        }
                        else -> LazyColumn(
                            Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(filtered) { app ->
                                AppCard(app, card) { openDownload(app.downloadUrl) }
                            }
                        }
                    }

                    NavigationBar(containerColor = card) {
                        NavigationBarItem(selected = selectedTab == 0, onClick = { selectedTab = 0 }, icon = { Icon(Icons.Default.Home, null) }, label = { Text("בית") })
                        NavigationBarItem(selected = selectedTab == 1, onClick = { selectedTab = 1 }, icon = { Icon(Icons.Default.Apps, null) }, label = { Text("אפליקציות") })
                        NavigationBarItem(selected = selectedTab == 2, onClick = { selectedTab = 2 }, icon = { Icon(Icons.Default.Download, null) }, label = { Text("הורדות") })
                    }
                }
            }
        }
    }

    private fun openDownload(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    private fun loadAppsFromGithub(): List<StoreApp> {
        val reposJson = java.net.URL(GITHUB_API).openConnection().apply {
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("User-Agent", "Miracle-Shop")
        }.getInputStream().bufferedReader().use { it.readText() }

        val repos = org.json.JSONArray(reposJson)
        val result = mutableListOf<StoreApp>()

        for (i in 0 until repos.length()) {
            val repo = repos.getJSONObject(i)
            val repoName = repo.getString("name")
            val releasesUrl = "https://api.github.com/repos/ari900630-tech/" + repoName + "/releases?per_page=10"

            try {
                val releasesJson = java.net.URL(releasesUrl).openConnection().apply {
                    setRequestProperty("Accept", "application/vnd.github+json")
                    setRequestProperty("User-Agent", "Miracle-Shop")
                }.getInputStream().bufferedReader().use { it.readText() }

                val releases = org.json.JSONArray(releasesJson)
                for (j in 0 until releases.length()) {
                    val release = releases.getJSONObject(j)
                    val tag = release.optString("tag_name", "latest")
                    val assets = release.optJSONArray("assets") ?: continue

                    for (k in 0 until assets.length()) {
                        val asset = assets.getJSONObject(k)
                        val name = asset.optString("name")
                        val url = asset.optString("browser_download_url")

                        if (name.lowercase().endsWith(".apk") && url.isNotBlank()) {
                            val imageUrl = "https://opengraph.githubassets.com/1/ari900630-tech/" + repoName
                            result.add(
                                StoreApp(
                                    name = repoName,
                                    description = release.optString("name", "אפליקציה מ-" + repoName),
                                    version = tag,
                                    downloadUrl = url,
                                    category = if (repoName.contains("tool", true)) "כלים" else "כללי",
                                    imageUrl = imageUrl
                                )
                            )
                        }
                    }
                }
            } catch (_: Exception) { }
        }
        return result.distinctBy { it.downloadUrl }
    }

    @Composable
    private fun AppCard(app: StoreApp, card: Color, onDownload: () -> Unit) {
        Card(
            Modifier.fillMaxWidth().clickable { onDownload() },
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = card),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = app.imageUrl,
                    contentDescription = app.name,
                    modifier = Modifier.size(72.dp).clip(RoundedCornerShape(18.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(app.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(app.description, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                    Text("גרסה " + app.version, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                FilledTonalButton(onClick = onDownload) { Text("הורדת APK") }
            }
        }
    }
}
