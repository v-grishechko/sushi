package com.github.vgrishechko.sushi

interface ErrorHandler {
    fun handleError(throwable: Throwable)
}