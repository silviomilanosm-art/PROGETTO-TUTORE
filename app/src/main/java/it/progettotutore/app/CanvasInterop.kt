package it.progettotutore.app

import android.graphics.Canvas as AndroidCanvas
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.nativeCanvas as composeNativeCanvas

val Canvas.nativeCanvas: AndroidCanvas
    get() = this.composeNativeCanvas
