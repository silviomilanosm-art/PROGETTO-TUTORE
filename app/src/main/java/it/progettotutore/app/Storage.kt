package it.progettotutore.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class Archivio(context: Context) {
    private val prefs = context.getSharedPreferences("progetto_tutore_archivio", Context.MODE_PRIVATE)

    fun caricaPazienti(): List<Paziente> {
        val raw = prefs.getString("pazienti", "[]") ?: "[]"
        val arr = JSONArray(raw)
        return buildList {
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                add(Paziente(o.getLong("id"), o.getString("nome"), o.optString("note", "")))
            }
        }
    }

    fun caricaTutori(): List<TutoreSalvato> {
        val raw = prefs.getString("tutori", "[]") ?: "[]"
        val arr = JSONArray(raw)
        return buildList {
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val m = o.getJSONObject("misure")
                add(
                    TutoreSalvato(
                        id = o.getLong("id"),
                        pazienteId = o.getLong("pazienteId"),
                        nome = o.getString("nome"),
                        creatoIl = o.getLong("creatoIl"),
                        misure = Misure(
                            polso = m.optDouble("polso", 0.0),
                            avambraccio = m.optDouble("avambraccio", 0.0),
                            lunghezzaAvambraccio = m.optDouble("lunghezzaAvambraccio", 0.0),
                            polsoMcp = m.optDouble("polsoMcp", 0.0),
                            larghezzaMcp = m.optDouble("larghezzaMcp", 0.0),
                            lunghezzaMano = m.optDouble("lunghezzaMano", 0.0),
                            margineCucitura = m.optDouble("margineCucitura", 0.7),
                            sovrapposizioneVelcro = m.optDouble("sovrapposizioneVelcro", 4.0)
                        )
                    )
                )
            }
        }
    }

    fun salva(pazienti: List<Paziente>, tutori: List<TutoreSalvato>) {
        prefs.edit()
            .putString("pazienti", pazientiToJson(pazienti).toString())
            .putString("tutori", tutoriToJson(tutori).toString())
            .apply()
    }

    fun esporta(pazienti: List<Paziente>, tutori: List<TutoreSalvato>): String =
        JSONObject()
            .put("schema", 1)
            .put("app", "Progetto Tutore")
            .put("pazienti", pazientiToJson(pazienti))
            .put("tutori", tutoriToJson(tutori))
            .toString(2)

    fun importa(json: String): Pair<List<Paziente>, List<TutoreSalvato>> {
        val root = JSONObject(json)
        prefs.edit()
            .putString("pazienti", root.optJSONArray("pazienti")?.toString() ?: "[]")
            .putString("tutori", root.optJSONArray("tutori")?.toString() ?: "[]")
            .apply()
        return caricaPazienti() to caricaTutori()
    }

    private fun pazientiToJson(items: List<Paziente>): JSONArray = JSONArray().apply {
        items.forEach { p ->
            put(JSONObject().put("id", p.id).put("nome", p.nome).put("note", p.note))
        }
    }

    private fun tutoriToJson(items: List<TutoreSalvato>): JSONArray = JSONArray().apply {
        items.forEach { t ->
            val m = t.misure
            put(
                JSONObject()
                    .put("id", t.id)
                    .put("pazienteId", t.pazienteId)
                    .put("nome", t.nome)
                    .put("creatoIl", t.creatoIl)
                    .put("misure", JSONObject()
                        .put("polso", m.polso)
                        .put("avambraccio", m.avambraccio)
                        .put("lunghezzaAvambraccio", m.lunghezzaAvambraccio)
                        .put("polsoMcp", m.polsoMcp)
                        .put("larghezzaMcp", m.larghezzaMcp)
                        .put("lunghezzaMano", m.lunghezzaMano)
                        .put("margineCucitura", m.margineCucitura)
                        .put("sovrapposizioneVelcro", m.sovrapposizioneVelcro)
                    )
            )
        }
    }
}
