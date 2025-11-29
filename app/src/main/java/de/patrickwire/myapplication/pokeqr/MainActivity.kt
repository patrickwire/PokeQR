package de.patrickwire.myapplication.pokeqr

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import io.github.g00fy2.quickie.QRResult
import io.github.g00fy2.quickie.ScanQRCode
import de.patrickwire.myapplication.pokeqr.ui.theme.PokeQRTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL

@Serializable
data class PokemonResponse(
    val name: String,
    val sprites: Sprites
)

@Serializable
data class Sprites(
    val front_default: String? = null
)

private val json = Json { ignoreUnknownKeys = true }

fun fetchPokemon(id: String): PokemonResponse {
    val url = URL("https://pokeapi.co/api/v2/pokemon/$id/")
    val connection = url.openConnection() as HttpURLConnection
    connection.requestMethod = "GET"
    val response = connection.inputStream.bufferedReader().readText()
    return json.decodeFromString(response)
}
class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PokeQRTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    PokemonLookupScreen()
                }
            }
        }
    }
}

@Composable
fun PokemonLookupScreen() {
    var input by remember { mutableStateOf("") }
    var pokemon by remember { mutableStateOf<PokemonResponse?>(null) }



    fun loadPokemon() {
        val id = input.toIntOrNull()
        if (id != null && id in 1..151) {
            CoroutineScope(Dispatchers.IO).launch {
                pokemon = fetchPokemon(input)
            }
        }
    }
    val scanner = rememberLauncherForActivityResult(ScanQRCode()) { result ->
        if (result is QRResult.QRSuccess) {
            input = result.content.rawValue ?: ""
            loadPokemon()
        }
    }

    Column(Modifier.padding(16.dp)) {
        Button(onClick = { scanner.launch(null) }) {
            Text("Scan Pokémon-ID QR")
        }

        pokemon?.let {
            Text("Name: ${it.name}")
            AsyncImage(model = it.sprites.front_default, contentDescription = null, modifier = Modifier.size(240.dp))
        }
    }

}