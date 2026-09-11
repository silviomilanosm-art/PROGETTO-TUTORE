package it.progettotutore.app

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.view.MotionEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.max

@Composable
fun Tutore3DReale(m: Misure, showDimensions: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(360.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0xFFEAF4F1)),
            factory = { context -> TutoreGLView(context, m) },
            update = { it.updateMisure(m) }
        )

        if (showDimensions) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("Quote del tutore 3D", style = MaterialTheme.typography.titleSmall)
                Text("Lunghezza base: ${fmt3d(m.lunghezzaAvambraccio + m.polsoMcp)}")
                Text("Larghezza mano MCP: ${fmt3d(m.larghezzaMcp)}")
                Text("Circonferenza polso: ${fmt3d(m.polso)}")
                Text("Circonferenza avambraccio: ${fmt3d(m.avambraccio)}")
                Text("Polso → MCP: ${fmt3d(m.polsoMcp)}")
                Text("Lunghezza mano: ${fmt3d(m.lunghezzaMano)}")
            }
        }

        Text(
            "Trascina il dito sul modello per ruotarlo a 360°. La forma 3D è una ricostruzione parametrica basata sulle misure inserite, utile per progettazione e anteprima; non sostituisce una scansione 3D o un modello CAD clinicamente validato.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

private fun fmt3d(v: Double): String = if (v > 0.0) "%.1f cm".format(v) else "—"

private class TutoreGLView(context: Context, initial: Misure) : GLSurfaceView(context) {
    private val tutoreRenderer = TutoreRenderer(initial)
    private var previousX = 0f
    private var previousY = 0f

    init {
        setEGLContextClientVersion(2)
        setRenderer(tutoreRenderer)
        renderMode = RENDERMODE_WHEN_DIRTY
        preserveEGLContextOnPause = true
    }

    fun updateMisure(m: Misure) {
        tutoreRenderer.misure = m
        requestRender()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                previousX = event.x
                previousY = event.y
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = event.x - previousX
                val dy = event.y - previousY
                tutoreRenderer.angleY += dx * 0.45f
                tutoreRenderer.angleX = (tutoreRenderer.angleX + dy * 0.35f).coerceIn(-75f, 75f)
                previousX = event.x
                previousY = event.y
                requestRender()
                return true
            }
        }
        return true
    }
}

private class TutoreRenderer(initial: Misure) : GLSurfaceView.Renderer {
    @Volatile var misure: Misure = initial
    var angleX = -12f
    var angleY = -25f

    private var program = 0
    private val projection = FloatArray(16)
    private val view = FloatArray(16)
    private val model = FloatArray(16)
    private val temp = FloatArray(16)
    private val mvp = FloatArray(16)

    private val vertexShader = """
        uniform mat4 uMVP;
        attribute vec3 aPosition;
        void main() { gl_Position = uMVP * vec4(aPosition, 1.0); }
    """.trimIndent()

    private val fragmentShader = """
        precision mediump float;
        uniform vec4 uColor;
        void main() { gl_FragColor = uColor; }
    """.trimIndent()

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.92f, 0.97f, 0.96f, 1f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glEnable(GLES20.GL_CULL_FACE)
        program = createProgram(vertexShader, fragmentShader)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        val ratio = width.toFloat() / max(height, 1).toFloat()
        Matrix.perspectiveM(projection, 0, 42f, ratio, 1f, 20f)
        Matrix.setLookAtM(view, 0, 0f, 0.15f, 7.2f, 0f, 0f, 0f, 0f, 1f, 0f)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        GLES20.glUseProgram(program)

        Matrix.setIdentityM(model, 0)
        Matrix.rotateM(model, 0, angleX, 1f, 0f, 0f)
        Matrix.rotateM(model, 0, angleY, 0f, 1f, 0f)
        Matrix.multiplyMM(temp, 0, view, 0, model, 0)
        Matrix.multiplyMM(mvp, 0, projection, 0, temp, 0)

        val m = misure
        val totalLenCm = ((m.lunghezzaAvambraccio.takeIf { it > 0 } ?: 19.0) + (m.polsoMcp.takeIf { it > 0 } ?: 10.0))
        val bodyH = (totalLenCm / 29.0 * 4.2).toFloat().coerceIn(3.3f, 5.1f)
        val handW = ((m.larghezzaMcp.takeIf { it > 0 } ?: 8.5) / 8.5 * 1.65).toFloat().coerceIn(1.25f, 2.15f)
        val foreCirc = (m.avambraccio.takeIf { it > 0 } ?: 25.0)
        val foreW = (foreCirc / 25.0 * 2.05).toFloat().coerceIn(1.55f, 2.75f)
        val wristCirc = (m.polso.takeIf { it > 0 } ?: 17.0)
        val wristDepth = (wristCirc / 17.0 * 0.78).toFloat().coerceIn(0.58f, 1.08f)

        // Riferimento anatomico interno.
        drawPrism(
            topWidth = handW * 0.82f,
            bottomWidth = foreW * 0.82f,
            height = bodyH * 0.92f,
            depth = wristDepth * 0.78f,
            y = -0.15f,
            z = -0.05f,
            color = floatArrayOf(0.86f, 0.70f, 0.58f, 1f)
        )

        // Scocca principale del tutore: volume 3D parametrico.
        drawPrism(
            topWidth = handW,
            bottomWidth = foreW,
            height = bodyH,
            depth = wristDepth,
            y = -0.10f,
            z = 0.02f,
            color = floatArrayOf(0.08f, 0.48f, 0.80f, 1f)
        )

        // Piastra dorsale della mano.
        drawBox(
            width = handW * 1.04f,
            height = 0.62f,
            depth = wristDepth * 1.04f,
            x = 0f,
            y = bodyH * 0.48f + 0.24f,
            z = 0.06f,
            color = floatArrayOf(0.78f, 0.90f, 0.96f, 1f)
        )

        // Tre fasce di contenimento.
        drawBox(foreW * 1.12f, 0.24f, wristDepth * 1.18f, 0f, bodyH * 0.16f, 0.10f, floatArrayOf(0.05f, 0.08f, 0.11f, 1f))
        drawBox(foreW * 1.16f, 0.26f, wristDepth * 1.20f, 0f, -bodyH * 0.10f, 0.10f, floatArrayOf(0.05f, 0.08f, 0.11f, 1f))
        drawBox(foreW * 1.18f, 0.28f, wristDepth * 1.22f, 0f, -bodyH * 0.34f, 0.10f, floatArrayOf(0.05f, 0.08f, 0.11f, 1f))
    }

    private fun drawPrism(topWidth: Float, bottomWidth: Float, height: Float, depth: Float, y: Float, z: Float, color: FloatArray) {
        val yt = y + height / 2f
        val yb = y - height / 2f
        val zf = z + depth / 2f
        val zb = z - depth / 2f
        val v = floatArrayOf(
            -topWidth/2, yt, zf,   topWidth/2, yt, zf,   bottomWidth/2, yb, zf,  -bottomWidth/2, yb, zf,
            -topWidth/2, yt, zb,   topWidth/2, yt, zb,   bottomWidth/2, yb, zb,  -bottomWidth/2, yb, zb
        )
        drawMesh(v, BOX_INDICES, color)
    }

    private fun drawBox(width: Float, height: Float, depth: Float, x: Float, y: Float, z: Float, color: FloatArray) {
        val x1 = x - width/2f; val x2 = x + width/2f
        val y1 = y - height/2f; val y2 = y + height/2f
        val z1 = z - depth/2f; val z2 = z + depth/2f
        val v = floatArrayOf(
            x1,y2,z2, x2,y2,z2, x2,y1,z2, x1,y1,z2,
            x1,y2,z1, x2,y2,z1, x2,y1,z1, x1,y1,z1
        )
        drawMesh(v, BOX_INDICES, color)
    }

    private fun drawMesh(vertices: FloatArray, indices: ShortArray, color: FloatArray) {
        val vb = ByteBuffer.allocateDirect(vertices.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().apply {
            put(vertices); position(0)
        }
        val ib = ByteBuffer.allocateDirect(indices.size * 2).order(ByteOrder.nativeOrder()).asShortBuffer().apply {
            put(indices); position(0)
        }
        val pos = GLES20.glGetAttribLocation(program, "aPosition")
        val mvpLoc = GLES20.glGetUniformLocation(program, "uMVP")
        val colorLoc = GLES20.glGetUniformLocation(program, "uColor")
        GLES20.glEnableVertexAttribArray(pos)
        GLES20.glVertexAttribPointer(pos, 3, GLES20.GL_FLOAT, false, 12, vb)
        GLES20.glUniformMatrix4fv(mvpLoc, 1, false, mvp, 0)
        GLES20.glUniform4fv(colorLoc, 1, color, 0)
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indices.size, GLES20.GL_UNSIGNED_SHORT, ib)
        GLES20.glDisableVertexAttribArray(pos)
    }

    private fun createProgram(vs: String, fs: String): Int {
        val vertex = compile(GLES20.GL_VERTEX_SHADER, vs)
        val fragment = compile(GLES20.GL_FRAGMENT_SHADER, fs)
        return GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vertex)
            GLES20.glAttachShader(it, fragment)
            GLES20.glLinkProgram(it)
        }
    }

    private fun compile(type: Int, source: String): Int = GLES20.glCreateShader(type).also {
        GLES20.glShaderSource(it, source)
        GLES20.glCompileShader(it)
    }

    companion object {
        private val BOX_INDICES = shortArrayOf(
            0,2,1, 0,3,2,
            4,5,6, 4,6,7,
            0,1,5, 0,5,4,
            3,7,6, 3,6,2,
            0,4,7, 0,7,3,
            1,2,6, 1,6,5
        )
    }
}
