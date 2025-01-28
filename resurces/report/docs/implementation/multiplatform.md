# Multiplatform Kotlin Module
Kotlin Multiplatform is a technology that allows you to share code between different platforms. 
It is a great way to reduce the amount of code that needs to be written for each platform.
This approach follows the write once build anywhere, in which a software is written using a sort of
"super language" and compiled for other platforms. In this case the "super language" is Kotlin and the targets
are NodeJS and JVM.


We leveraged this technology to share code between `friendship-service` and `user-service` (JVM) 
and `content-service` (NodeJS). To maintain the independence of the microservices, 
the elements included in this module are minimal.
However, we considered it important to define a single source of truth for them:

- Events
- DDD building blocks
- Endpoints
- Status codes

## Usage
Here an example of a shared module with notation to let export the defined class:
```kotlin
@JsExport open class ID<I> (open val id: I) : ValueObject
```
To use the shared module from Typescript, you need to add the generated or add it as a dependency in your project. 
Then you can import the classes and interfaces, specifying full package name:
```typescript
import {social} from "../commons-lib";
import ID = social.common.ddd.ID;
export class UserID extends ID<string> {}
```
To generate the JS library, you can use the following gradle task:
```shell
./gradlew :events:jsNodeProductionLibraryDistribution
```
It will output the file in events/build/dist/js/productionLibrary directory.

The generation of the JS library and import of the dependencies has been automated during the build of `content-service`.

## Tips
Other useful tips for Kotlin Multiplatform configuration and typescript usage are:

- optIn("kotlin.js.ExperimentalJsExport"): 
This annotation allows to export classes and interfaces to javascript without specifying it in every file.
- generateTypeScriptDefinitions(): 
This function generates a .d.ts file with the type definitions of the exported classes and interfaces, 
allowing to use them in typescript code.

[« Back to Index](../docs.md) | [« Previous](./testing.md) | [Next »](../devops/build-system.md)