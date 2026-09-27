package com.miracleshop.app

import android.content.Intent
import android.net.Uri
import android.app.DownloadManager
import android.os.Environment
import android.webkit.URLUtil
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.Animatable
import androidx.compose.animation.FastOutSlowInEasing
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.URLEncoder

data class StoreApp(
    val name: String,
    val description: String,
    val version: String,
    val downloadUrl: String,
    val category: String,
    val imageUrl: String
)

private const val FDROID_SEARCH_API = "https://search.f-droid.org/api/search_apps?q="
private const val FDROID_PACKAGE_API = "https://f-droid.org/api/v1/packages/"
private const val FDROID_APK_BASE = "https://f-droid.org/repo/"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MiracleShopApp() }
    }

    @Composable
    private fun MiracleShopApp() {
        var showSplash by remember { mutableStateOf(true) }
        val splashScale = remember { Animatable(1.65f) }
        val splashRotation = remember { Animatable(0f) }
        val splashAlpha = remember { Animatable(1f) }

        LaunchedEffect(Unit) {
            launch {
                splashScale.animateTo(0.62f, tween(1250, easing = FastOutSlowInEasing))
            }
            launch {
                splashRotation.animateTo(540f, tween(1250, easing = FastOutSlowInEasing))
            }
            launch {
                delay(850)
                splashAlpha.animateTo(0f, tween(350))
            }
            delay(1350)
            showSplash = false
        }

        if (showSplash) {
            Surface(Modifier.fillMaxSize(), color = Color(0xFF5B3FD3)) {
                Box(contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.PlayArrow,
                            "Miracle Shop",
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

        var query by remember { mutableStateOf(TextFieldValue("android")) }
        var selectedTab by remember { mutableIntStateOf(0) }
        var selectedCategory by remember { mutableIntStateOf(0) }
        var dark by remember { mutableStateOf(false) }
        var apps by remember { mutableStateOf<List<StoreApp>>(emptyList()) }
        var loading by remember { mutableStateOf(true) }
        var error by remember { mutableStateOf<String?>(null) }
        var reloadKey by remember { mutableIntStateOf(0) }

        LaunchedEffect(reloadKey, query.text) {
            val search = query.text.trim()
            if (search.length < 2) return@LaunchedEffect

            loading = true
            error = null
            Thread {
                try {
                    val loaded = loadAppsFromFDroid(search)
                    runOnUiThread {
                        apps = loaded
                        loading = false
                        error = if (loaded.isEmpty()) "לא נמצאו אפליקציות עבור החיפוש." else null
                    }
                } catch (_: Exception) {
                    runOnUiThread {
                        loading = false
                        error = "לא ניתן לטעון את האפליקציות כרגע."
                    }
                }
            }.start()
        }

        val categories = listOf("הכול", "כללי", "כלים")
        val filtered = apps.filter {
            selectedCategory == 0 || it.category == categories[selectedCategory]
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
                            onClick = { reloadKey++ }
                        ) {
                            Icon(Icons.Default.Refresh, "רענון", Modifier.size(28.dp))
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                categories.forEachIndexed { index, label ->
                                    FilterChip(
                                        selected = selectedCategory == index,
                                        onClick = { selectedCategory = index },
                                        label = {
                                            CompositionLocalProvider(
                                                LocalLayoutDirection provides LayoutDirection.Rtl
                                            ) {
                                                Text(label)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(6.dp))
                    Text(
                        "קטלוג APK ציבורי • F-Droid",
                        modifier = Modifier.padding(horizontal = 4.dp),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))

                    when {
                        loading -> Box(
                            Modifier.weight(1f).fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }

                        error != null -> Box(
                            Modifier.weight(1f).fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
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
                        NavigationBarItem(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            icon = { Icon(Icons.Default.Home, null) },
                            label = { Text("בית") }
                        )
                        NavigationBarItem(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            icon = { Icon(Icons.Default.Apps, null) },
                            label = { Text("אפליקציות") }
                        )
                        NavigationBarItem(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            icon = { Icon(Icons.Default.Download, null) },
                            label = { Text("הורדות") }
                        )
                    }
                }
            }
        }
    }

    private fun openDownload(url: String) {
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle("Miracle Shop")
            .setDescription("מוריד קובץ APK")
            .setMimeType("application/vnd.android.package-archive")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                URLUtil.guessFileName(url, null, "application/vnd.android.package-archive")
            )
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val manager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        manager.enqueue(request)
    }

    private fun loadAppsFromFDroid(searchText: String): List<StoreApp> {
        val encoded = URLEncoder.encode(searchText, "UTF-8")
        val searchJson = java.net.URL(FDROID_SEARCH_API + encoded).openConnection().apply {
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "Miracle-Shop")
        }.getInputStream().bufferedReader().use { it.readText() }

        val apps = org.json.JSONArray(org.json.JSONObject(searchJson).optString("apps", "[]"))
        val result = mutableListOf<StoreApp>()

        for (i in 0 until apps.length()) {
            val item = apps.getJSONObject(i)
            val name = item.optString("name", "אפליקציה")
            val summary = item.optString("summary", "")
            val icon = item.optString("icon", "")
            val pageUrl = item.optString("url", "")
            val packageName = pageUrl.substringAfterLast("/").takeIf { it.isNotBlank() } ?: continue

            try {
                val packageJson = java.net.URL(FDROID_PACKAGE_API + packageName).openConnection().apply {
                    setRequestProperty("Accept", "application/json")
                    setRequestProperty("User-Agent", "Miracle-Shop")
                }.getInputStream().bufferedReader().use { it.readText() }

                val packageObj = org.json.JSONObject(packageJson)
                val packages = packageObj.optJSONArray("packages") ?: continue
                if (packages.length() == 0) continue

                val latest = packages.getJSONObject(0)
                val versionName = latest.optString("versionName", "latest")
                val versionCode = latest.optLong("versionCode", 0L)
                if (versionCode <= 0L) continue

                val apkUrl = FDROID_APK_BASE + packageName + "_" + versionCode + ".apk"
                val category = if (
                    name.contains("tool", true) ||
                    summary.contains("utility", true) ||
                    summary.contains("tool", true)
                ) "כלים" else "כללי"

                result.add(
                    StoreApp(
                        name = name,
                        description = summary,
                        version = versionName,
                        downloadUrl = apkUrl,
                        category = category,
                        imageUrl = icon
                    )
                )
            } catch (_: Exception) {
                // Skip an app whose metadata is temporarily unavailable.
            }
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
                    Text(
                        app.description,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                    Text(
                        "גרסה " + app.version,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                FilledTonalButton(onClick = onDownload) {
                    Text("הורדת APK")
                }
            }
        }
    }
}
