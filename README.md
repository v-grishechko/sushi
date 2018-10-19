# Sushi

Sushi is the wrapper for [Moshi](https://github.com/square/moshi) adapters, which add the capability to handle errors during parsing.

### Motivation

When JSON response come from API, it can be corrupted or fields have a different type than in models (POJO) objects. In current realization of Moshi, such response cause exception. It's not bad, but we should catch these exceptions and somehow process it.

This library adds ability process exceptions in a different way. When some error happens during parsing, it will return `null` and send the exception to `ErrorHandler`, which can somehow process it. For example in debug builds crash application, but in release build only log the exception.

## Setup 

```kotlin
val moshi = Moshi.Builder()
            .add(Sushi.FACTORY)
            .build()
```

Default `ErrorHandler` throws an exception, but useful for the release build, for example, log in crashlytics.

```kotlin
class CrashlyticsErrorHandler: ErrorHandler {
     override fun handleError(throwable: Throwable) {
         Crashlytics.logException(throwable)
     }
}

Sushi.registerErrorHandler(CrashlyticsErrorHandler())
```