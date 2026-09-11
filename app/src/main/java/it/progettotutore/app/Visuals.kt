package it.progettotutore.app

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

val PieceColors = listOf(
    Color(0xFF19A974),
    Color(0xFFFFB000),
    Color(0xFF6C63FF),
    Color(0xFFE55B78),
    Color(0xFF00A8CC)
)

@Composable
fun TutorePreview(m: Misure, exploded: Boolean = false) {
    var vista3D by remember { mutableStateOf(false) }
    var mostraQuote by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (!exploded) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = !vista3D,
                    onClick = { vista3D = false },
                    label = { Text("Vista 2D") }
                )
                FilterChip(
                    selected = vista3D,
                    onClick = { vista3D = true },
                    label = { Text("Vista 3D") }
                )
            }

            if (vista3D) {
                FilterChip(
                    selected = mostraQuote,
                    onClick = { mostraQuote = !mostraQuote },
                    label = { Text(if (mostraQuote) "Nascondi misure" else "Mostra misure") }
                )
            }
        }

        if (vista3D && !exploded) {
            Tutore3DReale(m, showDimensions = mostraQuote)
        } else {
            Tutore2DPreview(m, exploded)
        }
    }
}

@Composable
private fun Tutore2DPreview(m: Misure, exploded: Boolean) {
    val safeHand = (m.larghezzaMcp.takeIf { it > 0 } ?: 8.0).coerceIn(5.0, 12.0)
    val safeFore = (m.avambraccio.takeIf { it > 0 } ?: 24.0).coerceIn(15.0, 40.0)
    val safeLen = (m.lunghezzaAvambraccio.takeIf { it > 0 } ?: 18.0).coerceIn(10.0, 30.0)

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (exploded) 330.dp else 250.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(22.dp))
            .padding(12.dp)
    ) {
        val w = size.width
        val h = size.height
        val cx = w * 0.50f
        val baseW = (w * (0.34f + ((safeFore - 15.0) / 25.0 * 0.10f).toFloat())).coerceAtMost(w * .48f)
        val handW = (w * (0.24f + ((safeHand - 5.0) / 7.0 * .09f).toFloat())).coerceAtMost(w * .38f)
        val bodyH = (h * (0.48f + ((safeLen - 10.0) / 20.0 * .10f).toFloat())).coerceAtMost(h * .63f)
        val top = if (exploded) h * .22f else h * .16f

        val aShift = if (exploded) Offset(-w * .18f, h * .16f) else Offset.Zero
        val path = Path().apply {
            moveTo(cx - handW / 2 + aShift.x, top + aShift.y)
            lineTo(cx + handW / 2 + aShift.x, top + aShift.y)
            lineTo(cx + baseW / 2 + aShift.x, top + bodyH + aShift.y)
            lineTo(cx - baseW / 2 + aShift.x, top + bodyH + aShift.y)
            close()
        }
        drawPath(path, PieceColors[0])
        drawPath(path, Color.Black.copy(alpha = .18f), style = Stroke(width = 2f))

        val eShift = if (exploded) Offset(w * .20f, -h * .02f) else Offset.Zero
        drawRoundRect(PieceColors[4], Offset(cx - handW * .46f + eShift.x, top - h * .08f + eShift.y), Size(handW * .92f, h * .18f), CornerRadius(20f, 20f))

        val bShift = if (exploded) Offset(w * .22f, h * .18f) else Offset.Zero
        drawRoundRect(PieceColors[1], Offset(cx - baseW * .63f + bShift.x, top + bodyH * .34f + bShift.y), Size(baseW * 1.26f, h * .075f), CornerRadius(18f, 18f))

        val cShift = if (exploded) Offset(-w * .22f, h * .08f) else Offset.Zero
        drawRoundRect(PieceColors[2], Offset(cx - baseW * .66f + cShift.x, top + bodyH * .69f + cShift.y), Size(baseW * 1.32f, h * .075f), CornerRadius(18f, 18f))

        val dShift = if (exploded) Offset(w * .25f, -h * .11f) else Offset.Zero
        drawRoundRect(PieceColors[3], Offset(cx - handW * .66f + dShift.x, top + h * .07f + dShift.y), Size(handW * 1.32f, h * .06f), CornerRadius(16f, 16f))

        if (exploded) {
            drawLine(Color.Black.copy(alpha = .18f), Offset(cx, top + h * .12f), Offset(cx, top + bodyH * .93f), strokeWidth = 2f)
        }
    }

    Text(
        if (exploded) "Esploso del tutore · ogni componente è separato e codificato per colore" else "Anteprima dinamica 2D · cambia mentre inserisci le misure",
        style = MaterialTheme.typography.bodySmall
    )
}

@Composable
fun LegendaPezzi(pezzi: List<Pezzo>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        pezzi.forEach { p ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(modifier = Modifier.size(18.dp).background(PieceColors[p.colorIndex], RoundedCornerShape(5.dp)))
                Column {
                    Text("${p.codice} · ${p.nome}", fontWeight = FontWeight.SemiBold)
                    Text(p.dimensione, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun GalleriaPezzi(pezzi: List<Pezzo>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        pezzi.forEachIndexed { index, p ->
            ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Canvas(
                        modifier = Modifier
                            .size(width = 110.dp, height = 92.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                            .padding(10.dp)
                    ) {
                        val c = PieceColors[p.colorIndex]
                        when (index) {
                            0 -> {
                                val path = Path().apply {
                                    moveTo(size.width * .33f, size.height * .12f)
                                    lineTo(size.width * .67f, size.height * .12f)
                                    lineTo(size.width * .82f, size.height * .88f)
                                    lineTo(size.width * .18f, size.height * .88f)
                                    close()
                                }
                                drawPath(path, c)
                            }
                            1, 2, 3 -> drawRoundRect(c, Offset(size.width * .08f, size.height * .38f), Size(size.width * .84f, size.height * .25f), CornerRadius(18f, 18f))
                            else -> drawRoundRect(c, Offset(size.width * .18f, size.height * .18f), Size(size.width * .64f, size.height * .64f), CornerRadius(22f, 22f))
                        }
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("${p.codice} · ${p.nome}", fontWeight = FontWeight.Bold)
                        Text(p.dimensione, style = MaterialTheme.typography.bodyMedium)
                        Text(p.nota, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
