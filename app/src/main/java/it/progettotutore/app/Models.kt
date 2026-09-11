package it.progettotutore.app

import kotlin.math.round

data class Misure(
    val polso: Double = 0.0,
    val avambraccio: Double = 0.0,
    val lunghezzaAvambraccio: Double = 0.0,
    val polsoMcp: Double = 0.0,
    val larghezzaMcp: Double = 0.0,
    val lunghezzaMano: Double = 0.0,
    val margineCucitura: Double = 0.7,
    val sovrapposizioneVelcro: Double = 4.0
)

data class Pezzo(
    val codice: String,
    val nome: String,
    val dimensione: String,
    val nota: String,
    val colorIndex: Int
)

data class Paziente(
    val id: Long,
    val nome: String,
    val note: String = ""
)

data class TutoreSalvato(
    val id: Long,
    val pazienteId: Long,
    val nome: String,
    val creatoIl: Long,
    val misure: Misure
)

fun d1(v: Double): String = "%.1f".format(round(v * 10.0) / 10.0)

fun calcolaPezzi(m: Misure): List<Pezzo> {
    val baseLunghezza = m.lunghezzaAvambraccio + m.polsoMcp
    val baseDistale = m.larghezzaMcp + (2 * m.margineCucitura)
    val baseProssimale = (m.avambraccio * 0.55) + (2 * m.margineCucitura)
    val fasciaPolso = m.polso + m.sovrapposizioneVelcro
    val fasciaAvambraccio = m.avambraccio + m.sovrapposizioneVelcro
    val fasciaPalmare = (m.larghezzaMcp * 1.35) + m.sovrapposizioneVelcro
    val coperturaMano = (m.lunghezzaMano * 0.55) + m.polsoMcp

    return listOf(
        Pezzo("A", "Base avambraccio-mano", "L ${d1(baseLunghezza)} cm · pross. ${d1(baseProssimale)} cm · dist. ${d1(baseDistale)} cm", "Sagoma principale da rifinire sul paziente.", 0),
        Pezzo("B", "Fascia polso", "${d1(fasciaPolso)} cm", "Comprende la sovrapposizione Velcro impostata.", 1),
        Pezzo("C", "Fascia avambraccio", "${d1(fasciaAvambraccio)} cm", "Lunghezza di taglio; larghezza a scelta del professionista.", 2),
        Pezzo("D", "Fascia palmare", "${d1(fasciaPalmare)} cm", "Stima geometrica iniziale basata sulla larghezza MCP.", 3),
        Pezzo("E", "Copertura mano", "sviluppo ${d1(coperturaMano)} cm", "Sagomare lasciando libere le zone articolari necessarie.", 4)
    )
}
