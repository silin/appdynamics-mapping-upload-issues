## Sample repo to reproduce the issue with uploading Proguard mapping file to AppDynamics automatically

[Link to the original issue thread reported to AppDynamics](https://help.appdynamics.com/hc/en-us/requests/215430)

The project is based on standard project generated automatically by Android Studio.

Then AppDynamics `adeum` Gradle plugin was applied to it:
 1. Root `build.gradle` - classpath with version `20.12.1` added
 2. `app/build.gradle` - `adeum` plugin applied and configured
 3. `gradle.properties` - setup in same way as we have in our project
 4. `TestApp` - Android Application class with enabling AppDynamics instrumentation

 # Setup
 Just add license configuration and AppDynamics app key to `app/build.gradle` to make the project
 compilable (all of them are marked as with `//TODO` in the file)

 # Initial investigation

 From what I see there are 2 issues:
 1. `proguardMappingFileUpload.failBuildOnUploadFailure` does not work, because the build is never failed if
   the file is not uploaded
   ```
   proguardMappingFileUpload {
           failBuildOnUploadFailure true
           enabled true
       }
   ```
 2. The mapping file is not uploaded

 From what I see the issue is in the AppDynamics Gradle plugin itself - the task for uploading is called
 way before the file itself is generated. Here is the snipped from build logs for `./gradlew assembleDebug` command:
 ```
> Task :app:preBuild UP-TO-DATE
> Task :app:extractProguardFiles
> Task :app:preDebugBuild
> Task :app:compileDebugAidl NO-SOURCE
> Task :app:compileDebugRenderscript NO-SOURCE
> Task :app:generateDebugBuildConfig
> Task :app:checkDebugAarMetadata
> Task :app:generateDebugResValues
> Task :app:generateDebugResources
> Task :app:createDebugCompatibleScreenManifests
> Task :app:extractDeepLinksDebug
> Task :app:processDebugMainManifest
> Task :app:processDebugManifest
> Task :app:mergeDebugNativeDebugMetadata NO-SOURCE
> Task :app:javaPreCompileDebug

> Task :app:appDynamicsUploadProguardMappingDebug
<UploadProguardMappingFileTask_Decorated> Proguard is enabled but mapping file was not generated. Please check your build configuration.

> Task :app:mergeDebugShaders
> Task :app:compileDebugShaders NO-SOURCE
> Task :app:generateDebugAssets UP-TO-DATE
> Task :app:mergeDebugAssets
> Task :app:compressDebugAssets
> Task :app:processDebugJavaRes NO-SOURCE
> Task :app:mergeDebugJniLibFolders
> Task :app:validateSigningDebug
> Task :app:processDebugManifestForPackage
> Task :app:checkDebugDuplicateClasses
> Task :app:mergeDebugResources
> Task :app:mergeDebugNativeLibs
> Task :app:processDebugResources
> Task :app:compileDebugKotlin
> Task :app:compileDebugJavaWithJavac
> Task :app:compileDebugSources
> Task :app:mergeDebugGeneratedProguardFiles UP-TO-DATE
> Task :app:mergeDebugJavaResource
> Task :app:transformClassesWithAppDynamicsForDebug

> Task :app:minifyDebugWithR8

> Task :app:stripDebugDebugSymbols NO-SOURCE
> Task :app:shrinkDebugRes
> Task :app:packageDebug
> Task :app:assembleDebug
 ```

 As you can see `:app:appDynamicsUploadProguardMappingDebug` called too early

Running the task second time with assumption that it will try to upload already generated file
also does not work, because the task is considered `UP-TO-DATE` :)

```
> Task :app:preBuild UP-TO-DATE
...
> Task :app:appDynamicsUploadProguardMappingDebug UP-TO-DATE
...
> Task :app:assembleDebug UP-TO-DATE
```