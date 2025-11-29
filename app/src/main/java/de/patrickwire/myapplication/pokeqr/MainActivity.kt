package de.patrickwire.myapplication.pokeqr

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
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
        if (input.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                pokemon = fetchPokemon(input)
            }
        }
    }

    Column(Modifier.padding(16.dp)) {

        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            label = { Text("Pokémon ID") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { loadPokemon() })
        )

        Spacer(Modifier.height(12.dp))

        Button(onClick = { loadPokemon() }) {
            Text("Laden")
        }

        Spacer(Modifier.height(24.dp))

        pokemon?.let {
            Text("Name: ${it.name.replaceFirstChar { char -> char.uppercase() }}")

            it.sprites.front_default?.let { imageUrl ->
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    modifier = Modifier.size(120.dp)
                )
            }
        }
    }
}