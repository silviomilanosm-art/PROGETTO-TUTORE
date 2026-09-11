package it.progettotutore.app

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
    val safeHand = (m.larghezzaMcp.takeIf { it > 0 } ?: 8.0).coerceIn(5.0, 12.0)
    val safeFore = (m.avambraccio.takeIf { it > 0 } ?: 24.0).coerceIn(15.0, 40.0)
    val safeLen = (m.lunghezzaAvambraccio.takeIf { it > 0 } ?: 18.0).coerceIn(10.0, 30.0)
    val spread = if (exploded) 1f else 0f

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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

            // A - base principale
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

            // E - copertura mano
            val eShift = if (exploded) Offset(w * .20f, -h * .02f) else Offset.Zero
            drawRoundRect(
                color = PieceColors[4],
                topLeft = Offset(cx - handW * .46f + eShift.x, top - h * .08f + eShift.y),
                size = Size(handW * .92f, h * .18f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(20f, 20f)
            )

            // B - fascia polso
            val bShift = if (exploded) Offset(w * .22f, h * .18f) else Offset.Zero
            drawRoundRect(
                color = PieceColors[1],
                topLeft = Offset(cx - baseW * .63f + bShift.x, top + bodyH * .34f + bShift.y),
                size = Size(baseW * 1.26f, h * .075f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(18f, 18f)
            )

            // C - fascia avambraccio
            val cShift = if (exploded) Offset(-w * .22f, h * .08f) else Offset.Zero
            drawRoundRect(
                color = PieceColors[2],
                topLeft = Offset(cx - baseW * .66f + cShift.x, top + bodyH * .69f + cShift.y),
                size = Size(baseW * 1.32f, h * .075f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(18f, 18f)
            )

            // D - fascia palmare
            val dShift = if (exploded) Offset(w * .25f, -h * .11f) else Offset.Zero
            drawRoundRect(
                color = PieceColors[3],
                topLeft = Offset(cx - handW * .66f + dShift.x, top + h * .07f + dShift.y),
                size = Size(handW * 1.32f, h * .06f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
            )

            if (exploded) {
                // linee guida dell'esploso
                drawLine(Color.Black.copy(alpha = .18f), Offset(cx, top + h * .12f), Offset(cx, top + bodyH * .93f), strokeWidth = 2f)
            }
        }

        Text(
            if (exploded) "Esploso del tutore · ogni componente è separato e codificato per colore" else "Anteprima dinamica · cambia mentre inserisci le misure",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun LegendaPezzi(pezzi: List<Pezzo>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        pezzi.forEach { p ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .background(PieceColors[p.colorIndex], RoundedCornerShape(5.dp))
                )
                Column {
                    Text("${p.codice} · ${p.nome}", fontWeight = FontWeight.SemiBold)
                    Text(p.dimensione, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
