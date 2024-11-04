package ru.arti1208.digitiscope.model

sealed interface Tool {
    data object Move : Tool
    data object Pencil : Tool
    data object Eraser : Tool

    sealed interface Shape : Tool {
        data object Line : Shape
        data object Rectangle : Shape
        data object Square : Shape
        data object Oval : Shape
        data object Circle : Shape
    }
}