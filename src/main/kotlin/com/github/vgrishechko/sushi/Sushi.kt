package com.github.vgrishechko.sushi

import com.squareup.moshi.*
import java.lang.reflect.Type


object Sushi {

    var isStrict: Boolean = false

    val FACTORY = object : JsonAdapter.Factory {
        override fun create(type: Type, annotations: MutableSet<out Annotation>, moshi: Moshi): JsonAdapter<*>? {
            val adapter = moshi.nextAdapter<Any>(this, type, annotations)

            if (adapter == null) {
                return null
            } else {
                return SafeJsonAdapter(adapter)
            }
        }
    }

    private var errorHandler: ErrorHandler = DefaultErrorHandler()

    @JvmStatic
    fun registerErrorHandler(errorHandler: ErrorHandler) {
        this.errorHandler = errorHandler
    }

    @JvmStatic
    fun throwError(throwable: Throwable) {
        errorHandler.handleError(throwable)
    }

    class DefaultErrorHandler : ErrorHandler {
        override fun handleError(throwable: Throwable) {
            throw throwable
        }
    }
}