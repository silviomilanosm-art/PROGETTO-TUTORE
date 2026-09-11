package it.progettotutore.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val colors = lightColorScheme(
                primary = Color(0xFF0F766E),
                onPrimary = Color.White,
                secondary = Color(0xFFFFB000),
                tertiary = Color(0xFF6C63FF),
                background = Color(0xFFF6FAF9),
                surface = Color.White,
                surfaceVariant = Color(0xFFEAF4F1)
            )
            MaterialTheme(colorScheme = colors) {
                Surface(modifier = Modifier.fillMaxSize()) { ProgettoTutoreApp() }
            }
        }
    }
}

private enum class Schermata { HOME, NUOVO, ARCHIVIO, DATI }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgettoTutoreApp() {
    val context = LocalContext.current
    val archivio = remember { Archivio(context) }
    var pazienti by remember { mutableStateOf(archivio.caricaPazienti()) }
    var tutori by remember { mutableStateOf(archivio.caricaTutori()) }
    var schermata by remember { mutableStateOf(Schermata.HOME) }
    var messaggio by remember { mutableStateOf<String?>(null) }

    fun persisti(np: List<Paziente> = pazienti, nt: List<TutoreSalvato> = tutori) {
        pazienti = np
        tutori = nt
        archivio.salva(np, nt)
    }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use {
                    it.write(archivio.esporta(pazienti, tutori))
                }
            }.onSuccess { messaggio = "Backup esportato correttamente." }
                .onFailure { messaggio = "Impossibile esportare il backup." }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            runCatching {
                val json = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                    ?: error("File non leggibile")
                archivio.importa(json)
            }.onSuccess { pair ->
                pazienti = pair.first
                tutori = pair.second
                messaggio = "Backup importato: archivio aggiornato."
            }.onFailure { messaggio = "Backup non valido o non leggibile." }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("M&P · Progetto Tutore", fontWeight = FontWeight.Bold)
                        Text("atelier digitale per tutori", style = MaterialTheme.typography.labelSmall)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(selected = schermata == Schermata.HOME, onClick = { schermata = Schermata.HOME }, icon = { Text("⌂") }, label = { Text("Home") })
                NavigationBarItem(selected = schermata == Schermata.NUOVO, onClick = { schermata = Schermata.NUOVO }, icon = { Text("＋") }, label = { Text("Nuovo") })
                NavigationBarItem(selected = schermata == Schermata.ARCHIVIO, onClick = { schermata = Schermata.ARCHIVIO }, icon = { Text("▤") }, label = { Text("Archivio") })
                NavigationBarItem(selected = schermata == Schermata.DATI, onClick = { schermata = Schermata.DATI }, icon = { Text("⇄") }, label = { Text("Dati") })
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            messaggio?.let {
                AssistChip(onClick = { messaggio = null }, label = { Text(it) })
            }

            when (schermata) {
                Schermata.HOME -> HomeScreen(pazienti, tutori) { schermata = Schermata.NUOVO }
                Schermata.NUOVO -> NuovoTutoreScreen(pazienti) { pazienteNome, nomeTutore, misure ->
                    val cleanName = pazienteNome.trim()
                    val existing = pazienti.firstOrNull { it.nome.equals(cleanName, ignoreCase = true) }
                    val patient = existing ?: Paziente(System.currentTimeMillis(), cleanName)
                    val newPatients = if (existing == null) pazienti + patient else pazienti
                    val nuovo = TutoreSalvato(
                        id = System.nanoTime(),
                        pazienteId = patient.id,
                        nome = nomeTutore.ifBlank { "Tutore mano-polso" },
                        creatoIl = System.currentTimeMillis(),
                        misure = misure
                    )
                    persisti(newPatients, listOf(nuovo) + tutori)
                    messaggio = "Tutore salvato nell'archivio."
                    schermata = Schermata.ARCHIVIO
                }
                Schermata.ARCHIVIO -> ArchivioScreen(
                    pazienti = pazienti,
                    tutori = tutori,
                    eliminaPaziente = { id ->
                        persisti(pazienti.filterNot { it.id == id }, tutori.filterNot { it.pazienteId == id })
                    },
                    eliminaTutore = { id -> persisti(pazienti, tutori.filterNot { it.id == id }) }
                )
                Schermata.DATI -> DatiScreen(
                    pazienti = pazienti,
                    tutori = tutori,
                    onExport = { exportLauncher.launch("progetto-tutore-backup.json") },
                    onImport = { importLauncher.launch(arrayOf("application/json", "text/plain")) }
                )
            }

            FooterCopyright()
        }
    }
}

@Composable
private fun HomeScreen(pazienti: List<Paziente>, tutori: List<TutoreSalvato>, onNew: () -> Unit) {
    Text("Il tuo laboratorio, in tasca.", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
    Text("Misure, pezzi, colori, archivio e vista esplosa in un unico flusso di lavoro.")

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        StatCard("Pazienti", pazienti.size.toString(), Modifier.weight(1f))
        StatCard("Tutori", tutori.size.toString(), Modifier.weight(1f))
    }

    ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Parti da una misura reale", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("L'anteprima cambia mentre compili i campi; al termine ottieni i componenti colorati e l'esploso del tutore.")
            Button(onClick = onNew, modifier = Modifier.fillMaxWidth()) { Text("Crea un nuovo tutore") }
        }
    }

    if (tutori.isNotEmpty()) {
        Text("Ultimo lavoro", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        val ultimo = tutori.maxByOrNull { it.creatoIl }!!
        TutorePreview(ultimo.misure, exploded = false)
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    ElevatedCard(modifier = modifier, shape = RoundedCornerShape(20.dp)) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
            Text(label, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun NuovoTutoreScreen(pazienti: List<Paziente>, onSave: (String, String, Misure) -> Unit) {
    var paziente by remember { mutableStateOf("") }
    var nomeTutore by remember { mutableStateOf("Tutore mano-polso") }
    var polso by remember { mutableStateOf("") }
    var avambraccio by remember { mutableStateOf("") }
    var lungAvambraccio by remember { mutableStateOf("") }
    var polsoMcp by remember { mutableStateOf("") }
    var largMcp by remember { mutableStateOf("") }
    var lungMano by remember { mutableStateOf("") }
    var margine by remember { mutableStateOf("0,7") }
    var velcro by remember { mutableStateOf("4,0") }
    var errore by remember { mutableStateOf<String?>(null) }

    fun p(s: String): Double = s.replace(',', '.').toDoubleOrNull() ?: 0.0
    val live = Misure(p(polso), p(avambraccio), p(lungAvambraccio), p(polsoMcp), p(largMcp), p(lungMano), p(margine).takeIf { it > 0 } ?: .7, p(velcro).takeIf { it > 0 } ?: 4.0)
    val pezzi = calcolaPezzi(live)

    Text("Nuovo tutore", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
    Text("Compila le misure in centimetri. L'immagine sotto si adatta in tempo reale.")

    OutlinedTextField(value = paziente, onValueChange = { paziente = it }, label = { Text("Nome / codice paziente") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
    if (pazienti.isNotEmpty()) Text("Pazienti già presenti: ${pazienti.take(4).joinToString { it.nome }}", style = MaterialTheme.typography.bodySmall)
    OutlinedTextField(value = nomeTutore, onValueChange = { nomeTutore = it }, label = { Text("Nome del tutore") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

    TutorePreview(live, exploded = false)

    Text("Misure anatomiche", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    NumberField("Circonferenza polso", polso) { polso = it }
    NumberField("Circonferenza avambraccio", avambraccio) { avambraccio = it }
    NumberField("Lunghezza avambraccio utile", lungAvambraccio) { lungAvambraccio = it }
    NumberField("Piega polso → MCP", polsoMcp) { polsoMcp = it }
    NumberField("Larghezza mano a livello MCP", largMcp) { largMcp = it }
    NumberField("Lunghezza mano", lungMano) { lungMano = it }

    Text("Parametri di lavorazione", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    NumberField("Margine cucitura per lato", margine) { margine = it }
    NumberField("Sovrapposizione Velcro", velcro) { velcro = it }

    Text("Pezzi calcolati", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    LegendaPezzi(pezzi)

    Text("Esploso", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    TutorePreview(live, exploded = true)

    errore?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    Button(
        onClick = {
            val required = listOf(p(polso), p(avambraccio), p(lungAvambraccio), p(polsoMcp), p(largMcp), p(lungMano))
            if (paziente.isBlank()) errore = "Inserisci il nome o il codice del paziente."
            else if (required.any { it <= 0.0 }) errore = "Completa tutte le misure anatomiche con valori maggiori di zero."
            else {
                errore = null
                onSave(paziente, nomeTutore, live)
            }
        },
        modifier = Modifier.fillMaxWidth()
    ) { Text("Salva paziente e tutore") }

    Text("Le quote sono un supporto tecnico alla prototipazione: fitting, pressioni, comfort e indicazione clinica restano da verificare dal professionista.", style = MaterialTheme.typography.bodySmall)
}

@Composable
private fun ArchivioScreen(
    pazienti: List<Paziente>,
    tutori: List<TutoreSalvato>,
    eliminaPaziente: (Long) -> Unit,
    eliminaTutore: (Long) -> Unit
) {
    var deletePatient by remember { mutableStateOf<Paziente?>(null) }
    var deleteTutore by remember { mutableStateOf<TutoreSalvato?>(null) }

    Text("Archivio", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
    if (pazienti.isEmpty()) {
        Text("Nessun paziente salvato. Crea il primo tutore dalla sezione Nuovo.")
    }

    pazienti.forEach { paziente ->
        ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp)) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(paziente.nome, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("${tutori.count { it.pazienteId == paziente.id }} tutori", style = MaterialTheme.typography.bodySmall)
                    }
                    TextButton(onClick = { deletePatient = paziente }) { Text("Elimina") }
                }

                tutori.filter { it.pazienteId == paziente.id }.sortedByDescending { it.creatoIl }.forEach { t ->
                    HorizontalDivider()
                    Text(t.nome, fontWeight = FontWeight.SemiBold)
                    Text(SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ITALY).format(Date(t.creatoIl)), style = MaterialTheme.typography.bodySmall)
                    Text("Vista del tutore salvato", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Puoi passare tra 2D e 3D e, nella vista 3D, mostrare o nascondere le misure.", style = MaterialTheme.typography.bodySmall)
                    TutorePreview(t.misure, exploded = false)
                    Text("Pezzi calcolati", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    LegendaPezzi(calcolaPezzi(t.misure))
                    Text("Esploso", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    TutorePreview(t.misure, exploded = true)
                    OutlinedButton(onClick = { deleteTutore = t }, modifier = Modifier.fillMaxWidth()) { Text("Elimina questo tutore") }
                }
            }
        }
    }

    deletePatient?.let { p ->
        AlertDialog(
            onDismissRequest = { deletePatient = null },
            title = { Text("Eliminare ${p.nome}?") },
            text = { Text("Verranno eliminati anche tutti i tutori associati a questo paziente.") },
            confirmButton = { TextButton(onClick = { eliminaPaziente(p.id); deletePatient = null }) { Text("Elimina") } },
            dismissButton = { TextButton(onClick = { deletePatient = null }) { Text("Annulla") } }
        )
    }
    deleteTutore?.let { t ->
        AlertDialog(
            onDismissRequest = { deleteTutore = null },
            title = { Text("Eliminare il tutore?") },
            text = { Text(t.nome) },
            confirmButton = { TextButton(onClick = { eliminaTutore(t.id); deleteTutore = null }) { Text("Elimina") } },
            dismissButton = { TextButton(onClick = { deleteTutore = null }) { Text("Annulla") } }
        )
    }
}

@Composable
private fun DatiScreen(pazienti: List<Paziente>, tutori: List<TutoreSalvato>, onExport: () -> Unit, onImport: () -> Unit) {
    Text("Dati e sicurezza", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
    Text("I dati restano sul dispositivo anche quando installi una versione più recente sopra quella esistente, purché l'app mantenga lo stesso identificativo e la stessa firma digitale.")

    ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp)) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Backup portatile", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Esporta ${pazienti.size} pazienti e ${tutori.size} tutori in un file JSON. Puoi conservarlo o importarlo su un altro dispositivo.")
            Button(onClick = onExport, modifier = Modifier.fillMaxWidth()) { Text("Esporta dati") }
            OutlinedButton(onClick = onImport, modifier = Modifier.fillMaxWidth()) { Text("Importa dati") }
        }
    }

    Text("Versione ${BuildConfig.VERSION_NAME} · build ${BuildConfig.VERSION_CODE}", style = MaterialTheme.typography.bodySmall)
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

@Composable
private fun FooterCopyright() {
    Spacer(Modifier.height(8.dp))
    HorizontalDivider()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text("© 2026 M&P Project", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text("Tutti i diritti riservati · All rights reserved", style = MaterialTheme.typography.bodySmall)
        Text("Progetto, interfaccia e contenuti M&P Project", style = MaterialTheme.typography.labelSmall)
    }
}
