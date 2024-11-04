package ru.arti1208.digitiscope

import kotlin.random.Random

internal fun <T> Random.nextItem(list: List<T>) = list[nextInt(list.size)]

internal fun <T> List<T>.random() = Random.nextItem(this)