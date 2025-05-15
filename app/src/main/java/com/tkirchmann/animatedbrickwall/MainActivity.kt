package com.tkirchmann.animatedbrickwall

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.tkirchmann.animatedbrickwall.ui.AnimatedBrickWall
import com.tkirchmann.animatedbrickwall.ui.AnimationConfig
import com.tkirchmann.animatedbrickwall.ui.BrickWallColors
import com.tkirchmann.animatedbrickwall.ui.BrickWallDefaults
import com.tkirchmann.animatedbrickwall.ui.BrushPaintDispersion
import com.tkirchmann.animatedbrickwall.ui.PaintMode
import com.tkirchmann.animatedbrickwall.ui.textureBrush
import com.tkirchmann.animatedbrickwall.ui.theme.AnimatedBrickWallTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AnimatedBrickWallTheme {
                val context = LocalContext.current
                val clayBrush = textureBrush(context, R.drawable.clay)
                val groundBrush = textureBrush(context, R.drawable.ground)

                val groundBrushDispersion = BrushPaintDispersion.Random(setOf(groundBrush))
                val clayBrushDispersion = BrushPaintDispersion.Random(setOf(clayBrush))

                AnimatedBrickWall(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    wallDimensions = BrickWallDefaults.defaultDimensions(
                        bottomLeft = Offset(0.2f, 0.8f),
                        wallWidthRatio = 0.6f,
                        wallHeightRatio = 0.4f,
                        brickRows = 12,
                        bricksPerRow = 8
                    ),
                    mortar = BrickWallDefaults.defaultMortar(
                        paintMode = PaintMode.BrushMode(groundBrush),
                        thicknessRatio = 0.002f,
                        drawOuterEdges = true
                    ),
                    colors = BrickWallColors.Brush(clayBrushDispersion),
                    animationConfig = AnimationConfig(
                        animationSpec = tween(100, easing = FastOutSlowInEasing),
                        delay = 60L
                    )
                ) {
                    Log.d("AnimatedBrickWall", "All animations finished!")
                }
            }
        }
    }
}