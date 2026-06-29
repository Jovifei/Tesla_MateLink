package com.teslamatelink.data.api.model

/**
 * Human-readable display name for the car.
 * Falls back through name → model → "Tesla".
 */
val CarRaw.displayName: String
    get() = name ?: carDetails?.model?.let { "Model $it" } ?: "Tesla"
