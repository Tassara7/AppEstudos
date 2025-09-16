package br.com.appestudos.ui.theme

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically

object AppAnimations {
    const val ANIMATION_DURATION_SHORT = 200
    const val ANIMATION_DURATION_MEDIUM = 300
    const val ANIMATION_DURATION_LONG = 500

    val slideInFromRight: AnimatedContentTransitionScope<*>.() -> EnterTransition = {
        slideInHorizontally(
            initialOffsetX = { fullWidth -> fullWidth },
            animationSpec = tween(ANIMATION_DURATION_MEDIUM, easing = EaseOut)
        ) + fadeIn(animationSpec = tween(ANIMATION_DURATION_MEDIUM))
    }

    val slideOutToLeft: AnimatedContentTransitionScope<*>.() -> ExitTransition = {
        slideOutHorizontally(
            targetOffsetX = { fullWidth -> -fullWidth },
            animationSpec = tween(ANIMATION_DURATION_MEDIUM, easing = EaseOut)
        ) + fadeOut(animationSpec = tween(ANIMATION_DURATION_MEDIUM))
    }

    val slideInFromLeft: AnimatedContentTransitionScope<*>.() -> EnterTransition = {
        slideInHorizontally(
            initialOffsetX = { fullWidth -> -fullWidth },
            animationSpec = tween(ANIMATION_DURATION_MEDIUM, easing = EaseOut)
        ) + fadeIn(animationSpec = tween(ANIMATION_DURATION_MEDIUM))
    }

    val slideOutToRight: AnimatedContentTransitionScope<*>.() -> ExitTransition = {
        slideOutHorizontally(
            targetOffsetX = { fullWidth -> fullWidth },
            animationSpec = tween(ANIMATION_DURATION_MEDIUM, easing = EaseOut)
        ) + fadeOut(animationSpec = tween(ANIMATION_DURATION_MEDIUM))
    }

    val slideInFromBottom: AnimatedContentTransitionScope<*>.() -> EnterTransition = {
        slideInVertically(
            initialOffsetY = { fullHeight -> fullHeight },
            animationSpec = tween(ANIMATION_DURATION_MEDIUM, easing = EaseOut)
        ) + fadeIn(animationSpec = tween(ANIMATION_DURATION_MEDIUM))
    }

    val slideOutToBottom: AnimatedContentTransitionScope<*>.() -> ExitTransition = {
        slideOutVertically(
            targetOffsetY = { fullHeight -> fullHeight },
            animationSpec = tween(ANIMATION_DURATION_MEDIUM, easing = EaseOut)
        ) + fadeOut(animationSpec = tween(ANIMATION_DURATION_MEDIUM))
    }

    val scaleInEnter = scaleIn(
        initialScale = 0.8f,
        animationSpec = tween(ANIMATION_DURATION_MEDIUM, easing = EaseOut)
    ) + fadeIn(animationSpec = tween(ANIMATION_DURATION_MEDIUM))

    val scaleOutExit = scaleOut(
        targetScale = 0.8f,
        animationSpec = tween(ANIMATION_DURATION_MEDIUM, easing = EaseInOut)
    ) + fadeOut(animationSpec = tween(ANIMATION_DURATION_MEDIUM))

    val fadeInEnter = fadeIn(
        animationSpec = tween(ANIMATION_DURATION_MEDIUM, easing = EaseOut)
    )

    val fadeOutExit = fadeOut(
        animationSpec = tween(ANIMATION_DURATION_MEDIUM, easing = EaseInOut)
    )
}