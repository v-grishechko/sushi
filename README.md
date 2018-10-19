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
## License
```
Copyright 2018 Vladislav Grishechko

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
