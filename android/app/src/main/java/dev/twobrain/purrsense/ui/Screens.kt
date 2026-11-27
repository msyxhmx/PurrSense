package dev.twobrain.purrsense.ui

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.twobrain.purrsense.data.Analysis
import dev.twobrain.purrsense.data.Brain
import dev.twobrain.purrsense.data.Cat
import dev.twobrain.purrsense.data.Grimace
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Warm = Color(0xFFE8833A)

@Composable
fun PurrSenseRoot(vm: MainViewModel, pickPhoto: ((Bitmap) -> Unit) -> Unit, takePhoto: ((Bitmap) -> Unit) -> Unit) {
    val state by vm.state.collectAsState()
    MaterialTheme(colorScheme = lightColorScheme(primary = Warm)) {
        Surface(Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxSize().systemBarsPadding()) {
                when (val s = state.screen) {
                    Screen.Home -> HomeScreen(state, vm, pickPhoto, takePhoto)
                    is Screen.Result -> { BackHandler { vm.goHome() }; ResultScreen(s.photo, s.analysis, vm::goHome) }
                    is Screen.AddCat -> { BackHandler { vm.goHome() }; AddCatScreen(s.photo, s.suggestedAppearance, vm) }
                    is Screen.CatDetail -> { BackHandler { vm.goHome() }; CatDetailScreen(s.cat, state, vm::goHome) }
                }
                state.busy?.let { msg ->
                    Box(Modifier.fillMaxSize().background(Color(0x99000000)), contentAlignment = Alignment.Center) {
                        Card { Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(); Spacer(Modifier.height(12.dp)); Text(msg)
                        } }
                    }
                }
                state.error?.let { err ->
                    AlertDialog(onDismissRequest = vm::dismissError, confirmButton = {
                        TextButton(onClick = vm::dismissError) { Text("OK") }
                    }, text = { Text(err) })
                }
            }
        }
    }
}

@Composable
private fun HomeScreen(
    state: UiState, vm: MainViewModel,
    pickPhoto: ((Bitmap) -> Unit) -> Unit, takePhoto: ((Bitmap) -> Unit) -> Unit,
) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("PurrSense", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Text("Read your cat's face: mood & pain signs", color = Color.Gray)
        Spacer(Modifier.height(12.dp))

        OnDeviceCard(state.onDeviceStatus, vm::downloadModel)
        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { takePhoto(vm::analyze) }, enabled = state.ready, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.PhotoCamera, null); Spacer(Modifier.width(6.dp)); Text("Scan")
            }
            OutlinedButton(onClick = { pickPhoto(vm::analyze) }, enabled = state.ready, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.PhotoLibrary, null); Spacer(Modifier.width(6.dp)); Text("Gallery")
            }
        }
        Spacer(Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("My cats", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            TextButton(onClick = { pickPhoto(vm::startAddCat) }, enabled = state.ready) {
                Icon(Icons.Default.Add, null); Text("Add cat")
            }
        }
        if (state.cats.isEmpty()) {
            Text("Add your cats so PurrSense can tell them apart and track each one over time.", color = Color.Gray)
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.cats, key = { it.id }) { cat ->
                Card(Modifier.fillMaxWidth().clickable { vm.openCat(cat) }) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(40.dp).clip(CircleShape).background(Warm.copy(alpha = .2f)),
                            contentAlignment = Alignment.Center) { Text(cat.name.take(1).uppercase()) }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(cat.name, fontWeight = FontWeight.SemiBold)
                            Text(cat.appearance, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OnDeviceCard(status: String, onDownload: () -> Unit) {
    val available = status.contains("AVAILABLE") && !status.contains("UN")
    Card(colors = CardDefaults.cardColors(containerColor = if (available) Color(0xFFE6F4EA) else Color(0xFFFFF4E5))) {
        Row(Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(if (available) Icons.Default.PhoneAndroid else Icons.Default.Cloud, null)
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(if (available) "On-device AI ready" else "Using cloud AI", fontWeight = FontWeight.SemiBold)
                Text(
                    if (available) "Quick reads stay on your phone: private, offline, free."
                    else "Gemini Nano: $status. Everything still works via the cloud.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            if (status.contains("DOWNLOADABLE")) TextButton(onClick = onDownload) { Text("Download") }
        }
    }
}

@Composable
private fun ResultScreen(photo: Bitmap, a: Analysis, onDone: () -> Unit) {
    val g = a.grimace
    val alert = a.painScore >= a.alertThreshold || a.quick.signs.isNotEmpty()
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Image(photo.asImageBitmap(), null, Modifier.fillMaxWidth().height(260.dp).clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop)
        Spacer(Modifier.height(12.dp))
        Text(a.matchedCat?.name ?: "Unknown cat", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Mood: ${a.quick.mood.replaceFirstChar { it.uppercase() }} · ${(a.quick.moodConfidence * 100).toInt()}% sure",
            style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(12.dp))

        GrimaceCard(g, a.painScore, a.painOffset, a.alertThreshold, alert)
        if (a.quick.signs.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text("Visible signs: " + a.quick.signs.joinToString(), color = Color(0xFFB3261E))
        }
        Spacer(Modifier.height(12.dp))
        Text(a.careNote)

        a.deep?.let { d ->
            Spacer(Modifier.height(12.dp))
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFDECEA))) {
                Column(Modifier.padding(12.dp)) {
                    Text("Deep Check · ${d.urgency.replace('_', ' ')}", fontWeight = FontWeight.Bold)
                    Text(d.summary)
                    d.findings.forEach { Text("• $it", style = MaterialTheme.typography.bodySmall) }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("TwoBrain trace", style = MaterialTheme.typography.titleSmall)
        a.trace.forEach { TraceRow(it.step, it.brain, it.millis) }

        Spacer(Modifier.height(12.dp))
        Text("PurrSense is a screening aid, not a veterinary diagnosis. If you're worried, call your vet.",
            style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        Spacer(Modifier.height(12.dp))
        Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) { Text("Done") }
    }
}

@Composable
private fun GrimaceCard(g: Grimace, painScore: Int, offset: Int, threshold: Int, alert: Boolean) {
    Card(colors = CardDefaults.cardColors(containerColor = if (alert) Color(0xFFFDECEA) else Color(0xFFE6F4EA))) {
        Column(Modifier.padding(12.dp).fillMaxWidth()) {
            Text("Pain score $painScore/10", fontWeight = FontWeight.Bold)
            if (offset > 0) {
                Text("Grimace ${g.total}/10 + $offset safety margin: AI models tend to under-score cat pain.",
                    style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Text(if (alert) "At or above $threshold: possible pain, consider contacting your vet."
                 else "Below $threshold: no strong pain signs in this photo.",
                style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(8.dp))
            listOf("Ears" to g.ears, "Eyes" to g.eyes, "Muzzle" to g.muzzle, "Whiskers" to g.whiskers, "Head" to g.head)
                .forEach { (label, v) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(label, Modifier.width(80.dp), style = MaterialTheme.typography.bodySmall)
                        LinearProgressIndicator(progress = { v / 2f }, modifier = Modifier.weight(1f).height(6.dp))
                        Text(" $v/2", style = MaterialTheme.typography.bodySmall)
                    }
                }
        }
    }
}

@Composable
private fun TraceRow(step: String, brain: Brain, ms: Long) {
    val (label, color) = when (brain) {
        Brain.ON_DEVICE -> "On-device" to Color(0xFF1E8E3E)
        Brain.CLOUD -> "Cloud" to Color(0xFF1A73E8)
        Brain.RULES -> "Rule" to Color.Gray
    }
    Row(Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = Color.White, style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(color).padding(horizontal = 6.dp, vertical = 2.dp))
        Spacer(Modifier.width(8.dp))
        Text(step, Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
        if (ms > 0) Text("${ms} ms", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
    }
}

@Composable
private fun AddCatScreen(photo: Bitmap, suggested: String, vm: MainViewModel) {
    var name by remember { mutableStateOf("") }
    var appearance by remember { mutableStateOf(suggested) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Add a cat", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))
        Image(photo.asImageBitmap(), null, Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(name, { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(appearance, { appearance = it }, label = { Text("Description (coat, markings)") },
            modifier = Modifier.fillMaxWidth())
        Text("Tip: add distinctive marks, e.g. \"white left paw, notch in right ear\".",
            style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        Spacer(Modifier.height(16.dp))
        Button(onClick = { vm.saveCat(name, appearance) }, enabled = name.isNotBlank(), modifier = Modifier.fillMaxWidth()) {
            Text("Save")
        }
        TextButton(onClick = vm::goHome, modifier = Modifier.fillMaxWidth()) { Text("Cancel") }
    }
}

@Composable
private fun CatDetailScreen(cat: Cat, state: UiState, onBack: () -> Unit) {
    val fmt = remember { SimpleDateFormat("EEE d MMM, HH:mm", Locale.US) }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        TextButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null); Text("Back") }
        Text(cat.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(cat.appearance, color = Color.Gray)
        Spacer(Modifier.height(12.dp))
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp)) {
                Text("This week", fontWeight = FontWeight.SemiBold)
                when {
                    state.trend != null -> {
                        Text(state.trend.first)
                        TraceRow("Trend summary", state.trend.second, 0)
                    }
                    state.history.isEmpty() -> Text("No readings yet. Scan ${cat.name} to start tracking.")
                    else -> Text("Summarising…")
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(state.history, key = { it.id }) { r ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(10.dp)) {
                        Text(fmt.format(Date(r.createdAt)), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text("${r.mood.replaceFirstChar { it.uppercase() }} · pain ${r.painScore}/10" +
                            (r.deepUrgency?.let { " · Deep Check: ${it.replace('_', ' ')}" } ?: ""))
                    }
                }
            }
        }
    }
}
