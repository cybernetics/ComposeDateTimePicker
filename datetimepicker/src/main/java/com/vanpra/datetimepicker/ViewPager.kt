package com.vanpra.datetimepicker


import androidx.animation.AnimationEndReason
import androidx.animation.PhysicsBuilder
import androidx.compose.Composable
import androidx.compose.onPreCommit
import androidx.compose.remember
import androidx.compose.state
import androidx.ui.animation.animatedFloat
import androidx.ui.core.*
import androidx.ui.foundation.Box
import androidx.ui.foundation.animation.AnchorsFlingConfig
import androidx.ui.foundation.animation.fling
import androidx.ui.foundation.gestures.DragDirection
import androidx.ui.foundation.gestures.draggable
import androidx.ui.graphics.Color
import androidx.ui.util.fastForEach
import androidx.ui.util.fastMap
import androidx.ui.util.fastMaxBy
import kotlin.math.abs
import kotlin.math.sign

interface ViewPagerScope {
    val index: Int

    fun next()
    fun previous()
}

private data class ViewPagerImpl(
    override val index: Int,
    val increment: (Int) -> Unit
) : ViewPagerScope {
    override fun next() {
        increment(1)
    }

    override fun previous() {
        increment(-1)
    }
}


@Composable
fun ViewPager(
    modifier: Modifier = Modifier,
    onNext: () -> Unit = {},
    onPrevious: () -> Unit = {},
    useAlpha: Boolean = false,
    enabled: Boolean = true,
    screenItem: @Composable() ViewPagerScope.(page: Int) -> Unit
) {
    Box(backgroundColor = Color.Transparent) {
        WithConstraints {
            val alphas = remember { mutableListOf(1f, 1f, 1f) }
            val width = constraints.maxWidth.toFloat()
            val offset = animatedFloat(width)
            offset.setBounds(0f, 2 * width)

            val anchors = remember { listOf(0f, width, 2 * width) }
            val index = state { 0 }

            val flingConfig = AnchorsFlingConfig(anchors,
                animationBuilder = PhysicsBuilder(dampingRatio = 0.9f),
                onAnimationEnd = { reason, end, _ ->
                    if (reason != AnimationEndReason.Interrupted) {
                        if (end == width * 2) {
                            onNext()
                            index.value += 1
                        } else if (end == 0f) {
                            onPrevious()
                            index.value -= 1
                        }
                    }
                })

            onPreCommit(index.value) {
                alphas.forEachIndexed { index, it ->
                    alphas[index] = 1f
                }
                offset.snapTo(width)
            }

            onPreCommit(offset.value) {
                if (useAlpha && !anchors.contains(offset.value)) {
                    if (offset.value < width) {
                        alphas[0] = 1 - offset.value / width
                    } else if (offset.value > width) {
                        alphas[2] = ((offset.value - width) / width)
                    }
                    alphas[1] = 1 - abs(offset.value - width) / width
                }
            }

            val increment = { increment: Int ->
                offset.animateTo(
                    width * sign(increment.toDouble()).toFloat() + width,
                    onEnd = { animationEndReason, _ ->
                        if (animationEndReason != AnimationEndReason.Interrupted) {
                            index.value += increment
                        }
                    })
            }

            println("RECOMPOSITION")

            val draggable = modifier.draggable(
                dragDirection = DragDirection.Horizontal,
                onDragDeltaConsumptionRequested = {
                    val old = offset.value
                    offset.snapTo(offset.value - (it * 0.55f))
                    offset.value - old
                }, onDragStopped = { offset.fling(flingConfig, -it) },
                enabled = enabled
            )

            Layout(children = {
                for (x in -1..1) {
                    Box(Modifier.tag(x + 1).drawOpacity(alphas[x + 1])) {
                        screenItem(
                            ViewPagerImpl(index.value + x, increment),
                            index.value + x
                        )
                    }
                }
            }, modifier = draggable) { measurables, constraints, layoutDirection ->
                val height =
                    measurables.fastMap { it.maxIntrinsicHeight(constraints.maxHeight) }.max() ?: 0
                layout(constraints.maxWidth, height) {
                    measurables
                        .fastMap { it.measure(constraints) to it.tag }
                        .fastForEach { (placeable, tag) ->
                            if (tag is Int) {
                                placeable.place(
                                    x = (constraints.maxWidth * tag) - offset.value.toInt(),
                                    y = 0
                                )
                            }
                        }
                }
            }
        }
    }
}