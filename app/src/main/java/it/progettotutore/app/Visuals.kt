package it.progettotutore.app

import android.graphics.Paint
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
import androidx.compose.ui.graphics.toArgb
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
                    label = { Text(if (mostraQuote) "Nascondi quote" else "Mostra quote") }
                )
            }
        }

        if (vista3D && !exploded) {
            Tutore3DPreview(m, showDimensions = mostraQuote)
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
private fun Tutore3DPreview(m: Misure, showDimensions: Boolean) {
    val safeHand = (m.larghezzaMcp.takeIf { it > 0 } ?: 8.0).coerceIn(5.0, 12.0)
    val safeFore = (m.avambraccio.takeIf { it > 0 } ?: 24.0).coerceIn(15.0, 40.0)
    val safeLen = (m.lunghezzaAvambraccio.takeIf { it > 0 } ?: 18.0).coerceIn(10.0, 30.0)

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (showDimensions) 370.dp else 310.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(22.dp))
            .padding(14.dp)
    ) {
        val w = size.width
        val h = size.height
        val cx = w * .50f
        val top = h * .14f
        val bodyH = h * (.50f + ((safeLen - 10.0) / 20.0 * .08f).toFloat())
        val foreW = w * (.29f + ((safeFore - 15.0) / 25.0 * .08f).toFloat())
        val handW = w * (.22f + ((safeHand - 5.0) / 7.0 * .08f).toFloat())
        val depth = w * .11f

        val skin = Color(0xFFE6C2A8)
        val sideShade = Color(0xFFD6A98A)
        val armFront = Path().apply {
            moveTo(cx - handW * .45f, top)
            lineTo(cx + handW * .45f, top)
            lineTo(cx + foreW * .50f, top + bodyH)
            lineTo(cx - foreW * .50f, top + bodyH)
            close()
        }
        drawPath(armFront, skin.copy(alpha = .55f))

        val armSide = Path().apply {
            moveTo(cx + handW * .45f, top)
            lineTo(cx + handW * .45f + depth, top - depth * .45f)
            lineTo(cx + foreW * .50f + depth, top + bodyH - depth * .45f)
            lineTo(cx + foreW * .50f, top + bodyH)
            close()
        }
        drawPath(armSide, sideShade.copy(alpha = .45f))

        val shellFront = Path().apply {
            moveTo(cx - handW * .52f, top + h * .05f)
            lineTo(cx + handW * .52f, top + h * .05f)
            lineTo(cx + foreW * .58f, top + bodyH * .95f)
            lineTo(cx - foreW * .58f, top + bodyH * .95f)
            close()
        }
        drawPath(shellFront, PieceColors[0].copy(alpha = .92f))
        drawPath(shellFront, Color.Black.copy(alpha = .18f), style = Stroke(2f))

        val shellSide = Path().apply {
            moveTo(cx + handW * .52f, top + h * .05f)
            lineTo(cx + handW * .52f + depth, top + h * .05f - depth * .45f)
            lineTo(cx + foreW * .58f + depth, top + bodyH * .95f - depth * .45f)
            lineTo(cx + foreW * .58f, top + bodyH * .95f)
            close()
        }
        drawPath(shellSide, PieceColors[0].copy(alpha = .58f))

        val handPiece = Path().apply {
            moveTo(cx - handW * .52f, top - h * .015f)
            lineTo(cx + handW * .52f, top - h * .015f)
            lineTo(cx + handW * .52f + depth * .70f, top - h * .07f)
            lineTo(cx - handW * .52f + depth * .70f, top - h * .07f)
            close()
        }
        drawPath(handPiece, PieceColors[4])

        fun strap(y: Float, width: Float, color: Color, shift: Float = 0f) {
            val band = Path().apply {
                moveTo(cx - width * .55f, y)
                lineTo(cx + width * .55f, y)
                lineTo(cx + width * .55f + depth * .82f + shift, y - depth * .36f)
                lineTo(cx - width * .55f + depth * .82f + shift, y - depth * .36f)
                close()
            }
            drawPath(band, color)
            drawPath(band, Color.Black.copy(alpha = .14f), style = Stroke(1.5f))
        }

        strap(top + bodyH * .35f, foreW * 1.12f, PieceColors[1])
        strap(top + bodyH * .69f, foreW * 1.17f, PieceColors[2])
        strap(top + bodyH * .12f, handW * 1.32f, PieceColors[3], shift = depth * .10f)

        drawOval(
            color = Color.Black.copy(alpha = .10f),
            topLeft = Offset(cx - foreW * .72f + depth * .35f, top + bodyH + h * .025f),
            size = Size(foreW * 1.55f, h * .06f)
        )

        if (showDimensions) {
            val quoteColor = Color(0xFF263238)
            val lineWidth = 2.2f
            val tick = 8f
            val paint = Paint().apply {
                isAntiAlias = true
                color = quoteColor.toArgb()
                textSize = 27f
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            }
            val smallPaint = Paint(paint).apply { textSize = 24f }

            fun horizontalQuote(x1: Float, x2: Float, y: Float, label: String) {
                drawLine(quoteColor, Offset(x1, y), Offset(x2, y), lineWidth)
                drawLine(quoteColor, Offset(x1, y - tick), Offset(x1, y + tick), lineWidth)
                drawLine(quoteColor, Offset(x2, y - tick), Offset(x2, y + tick), lineWidth)
                val tw = paint.measureText(label)
                drawContext.canvas.nativeCanvas.drawText(label, (x1 + x2 - tw) / 2f, y - 10f, paint)
            }

            fun verticalQuote(x: Float, y1: Float, y2: Float, label: String) {
                drawLine(quoteColor, Offset(x, y1), Offset(x, y2), lineWidth)
                drawLine(quoteColor, Offset(x - tick, y1), Offset(x + tick, y1), lineWidth)
                drawLine(quoteColor, Offset(x - tick, y2), Offset(x + tick, y2), lineWidth)
                drawContext.canvas.nativeCanvas.save()
                drawContext.canvas.nativeCanvas.rotate(-90f, x - 12f, (y1 + y2) / 2f)
                val tw = paint.measureText(label)
                drawContext.canvas.nativeCanvas.drawText(label, x - 12f - tw / 2f, (y1 + y2) / 2f - 10f, paint)
                drawContext.canvas.nativeCanvas.restore()
            }

            val quoteTop = (top - h * .10f).coerceAtLeast(25f)
            horizontalQuote(cx - handW * .52f, cx + handW * .52f, quoteTop, "MCP ${fmtCm(m.larghezzaMcp)}")
            verticalQuote(cx - foreW * .82f, top + h * .05f, top + bodyH * .95f, "L ${fmtCm(m.lunghezzaAvambraccio)}")

            val wristY = top + bodyH * .35f
            val foreY = top + bodyH * .69f
            drawContext.canvas.nativeCanvas.drawText("Polso ${fmtCm(m.polso)}", cx + foreW * .70f, wristY + 5f, smallPaint)
            drawContext.canvas.nativeCanvas.drawText("Avambr. ${fmtCm(m.avambraccio)}", cx + foreW * .72f, foreY + 5f, smallPaint)

            if (m.polsoMcp > 0.0) {
                drawContext.canvas.nativeCanvas.drawText("Polso→MCP ${fmtCm(m.polsoMcp)}", 18f, h - 20f, smallPaint)
            }
            if (m.lunghezzaMano > 0.0) {
                val handLabel = "Mano ${fmtCm(m.lunghezzaMano)}"
                val tw = smallPaint.measureText(handLabel)
                drawContext.canvas.nativeCanvas.drawText(handLabel, w - tw - 18f, h - 20f, smallPaint)
            }
        }
    }

    Text(
        if (showDimensions)
            "Vista 3D quotata · le etichette riportano le misure inserite; il disegno resta una rappresentazione schematica."
        else
            "Vista 3D schematica · rappresenta forma e disposizione dei pezzi in prospettiva; non è ancora un modello CAD anatomico.",
        style = MaterialTheme.typography.bodySmall
    )
}

private fun fmtCm(value: Double): String = if (value > 0.0) "${"%.1f".format(value)} cm" else "—"

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
