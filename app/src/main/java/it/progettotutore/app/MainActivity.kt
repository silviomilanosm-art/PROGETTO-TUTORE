package it.progettotutore.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlin.math.round

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    TutoreApp()
                }
            }
        }
    }
}

data class Misure(
    val polso: Double,
    val avambraccio: Double,
    val lunghezzaAvambraccio: Double,
    val polsoMcp: Double,
    val larghezzaMcp: Double,
    val lunghezzaMano: Double,
    val margineCucitura: Double,
    val sovrapposizioneVelcro: Double
)

data class Pezzo(val nome: String, val dimensione: String, val nota: String)

private fun d1(v: Double): String = "%.1f".format(round(v * 10.0) / 10.0)

private fun calcolaPezzi(m: Misure): List<Pezzo> {
    val baseLunghezza = m.lunghezzaAvambraccio + m.polsoMcp
    val baseDistale = m.larghezzaMcp + (2 * m.margineCucitura)
    val baseProssimale = (m.avambraccio * 0.55) + (2 * m.margineCucitura)
    val fasciaPolso = m.polso + m.sovrapposizioneVelcro
    val fasciaAvambraccio = m.avambraccio + m.sovrapposizioneVelcro
    val fasciaPalmare = (m.larghezzaMcp * 1.35) + m.sovrapposizioneVelcro
    val coperturaMano = (m.lunghezzaMano * 0.55) + m.polsoMcp

    return listOf(
        Pezzo(
            "A · Base avambraccio-mano",
            "L ${d1(baseLunghezza)} cm · larghezza prossimale ${d1(baseProssimale)} cm · distale ${d1(baseDistale)} cm",
            "Sagoma trapezoidale di partenza; rifinire sul paziente."
        ),
        Pezzo(
            "B · Fascia polso",
            "${d1(fasciaPolso)} cm",
            "Comprende la sovrapposizione Velcro impostata."
        ),
        Pezzo(
            "C · Fascia avambraccio",
            "${d1(fasciaAvambraccio)} cm",
            "Lunghezza di taglio; la larghezza della fascia resta a scelta del professionista."
        ),
        Pezzo(
            "D · Fascia palmare",
            "${d1(fasciaPalmare)} cm",
            "Stima geometrica iniziale basata sulla larghezza MCP."
        ),
        Pezzo(
            "E · Copertura dorsale/palmare mano",
            "sviluppo ${d1(coperturaMano)} cm",
            "Da sagomare lasciando libere le zone articolari necessarie."
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutoreApp() {
    var polso by remember { mutableStateOf("") }
    var avambraccio by remember { mutableStateOf("") }
    var lungAvambraccio by remember { mutableStateOf("") }
    var polsoMcp by remember { mutableStateOf("") }
    var largMcp by remember { mutableStateOf("") }
    var lungMano by remember { mutableStateOf("") }
    var margine by remember { mutableStateOf("0.7") }
    var velcro by remember { mutableStateOf("4.0") }
    var risultato by remember { mutableStateOf<List<Pezzo>>(emptyList()) }
    var errore by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Progetto Tutore") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Calcolatore parametrico · prototipo",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Inserisci le misure in centimetri. I risultati sono quote tecniche preliminari da verificare e adattare direttamente sul paziente."
            )

            NumberField("Circonferenza polso", polso) { polso = it }
            NumberField("Circonferenza avambraccio (fine tutore)", avambraccio) { avambraccio = it }
            NumberField("Lunghezza avambraccio utile", lungAvambraccio) { lungAvambraccio = it }
            NumberField("Distanza piega polso → MCP", polsoMcp) { polsoMcp = it }
            NumberField("Larghezza mano a livello MCP", largMcp) { largMcp = it }
            NumberField("Lunghezza mano", lungMano) { lungMano = it }

            HorizontalDivider()
            Text("Parametri di lavorazione", fontWeight = FontWeight.SemiBold)
            NumberField("Margine cucitura per lato", margine) { margine = it }
            NumberField("Sovrapposizione Velcro", velcro) { velcro = it }

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    fun p(s: String) = s.replace(',', '.').toDoubleOrNull()
                    val valori = listOf(
                        p(polso), p(avambraccio), p(lungAvambraccio), p(polsoMcp),
                        p(largMcp), p(lungMano), p(margine), p(velcro)
                    )
                    if (valori.any { it == null || it <= 0.0 }) {
                        errore = "Controlla i campi: servono valori numerici maggiori di zero."
                        risultato = emptyList()
                    } else {
                        val m = Misure(
                            valori[0]!!, valori[1]!!, valori[2]!!, valori[3]!!,
                            valori[4]!!, valori[5]!!, valori[6]!!, valori[7]!!
                        )
                        risultato = calcolaPezzi(m)
                        errore = null
                    }
                }
            ) {
                Text("Calcola pezzi")
            }

            errore?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            if (risultato.isNotEmpty()) {
                Text("Pezzi da preparare", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                risultato.forEach { pezzo ->
                    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(pezzo.nome, fontWeight = FontWeight.Bold)
                            Text(pezzo.dimensione, style = MaterialTheme.typography.titleMedium)
                            Text(pezzo.nota, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                Text(
                    "Nota: questa prima versione usa formule geometriche di prototipazione, non sostituisce la valutazione clinica, il fitting e i controlli di pressione/comfort.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun NumberField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text("$label (cm)") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}
