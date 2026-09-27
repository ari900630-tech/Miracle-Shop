package com.miracleshop.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class StoreApp(val name: String, val description: String, val version: String, val downloadUrl: String, val category: String)

private val demoApps = listOf(
    StoreApp("Miracle Browser", "דפדפן מוגן ומהיר", "1.0.0", "https://github.com/ari900630-tech/Miracle-Shop/releases/latest", "כללי"),
    StoreApp("Miracle Tools", "כלי עזר שימושיים", "1.0.0", "https://github.com/ari900630-tech/Miracle-Shop/releases/latest", "כלים")
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MiracleShopApp() }
    }

    private fun openDownload(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    @Composable
    private fun MiracleShopApp() {
        var query by remember { mutableStateOf(TextFieldValue("")) }
        var selectedTab by remember { mutableIntStateOf(0) }
        var selectedCategory by remember { mutableIntStateOf(0) }
        var dark by remember { mutableStateOf(false) }
        val filtered = demoApps.filter { it.name.contains(query.text, true) || it.description.contains(query.text, true) }
        val background by animateColorAsState(if (dark) Color(0xFF101116) else Color(0xFFF7F7FB), label = "bg")
        val foreground = if (dark) Color(0xFFF4F4F6) else Color(0xFF17181C)
        val card = if (dark) Color(0xFF1C1D24) else Color.White

        MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme()) {
            Surface(Modifier.fillMaxSize(), color = background) {
                Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                    Spacer(Modifier.height(14.dp))
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Miracle Shop", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = foreground)
                            Text("חנות האפליקציות של המפתח המאושר", color = foreground.copy(alpha = .65f))
                        }
                        IconButton(
                            modifier = Modifier.clip(CircleShape).background(card),
                            onClick = { dark = !dark }
                        ) {
                            Icon(if (dark) Icons.Default.LightMode else Icons.Default.DarkMode, "מצב יום/לילה")
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("חיפוש אפליקציות…") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        singleLine = true,
                        shape = RoundedCornerShape(18.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("הכול", "כללי", "כלים").forEachIndexed { index, label ->
                            FilterChip(selected = selectedCategory == index, onClick = { selectedCategory = index }, label = { Text(label) })
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    LazyColumn(
                        Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(filtered.filter { selectedCategory == 0 || it.category == listOf("הכול", "כללי", "כלים")[selectedCategory] }) { app ->
                            AppCard(app, card) { openDownload(app.downloadUrl) }
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

    @Composable
    private fun AppCard(app: StoreApp, card: Color, onDownload: () -> Unit) {
        Card(
            Modifier.fillMaxWidth().clickable { onDownload() },
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = card),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(62.dp).clip(RoundedCornerShape(17.dp)).background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.Apps, null, Modifier.size(34.dp)) }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(app.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(app.description, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("גרסה ${app.version}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                FilledTonalButton(onClick = onDownload) { Text("הורדה") }
            }
        }
    }
}
