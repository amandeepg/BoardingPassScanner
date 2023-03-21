package com.google.mlkit.md.barcodedetection

import android.graphics.Canvas
import com.google.mlkit.md.camera.GraphicOverlay

internal class BarcodeScrimGraphic(overlay: GraphicOverlay) : BarcodeGraphicBase(overlay) {
    override fun draw(canvas: Canvas) {
        drawDarkBackground(canvas)
    }
}
