## Sample repo to reproduce the issue with uploading Proguard mapping file to AppDynamics automatically

[Link to the original issue thread reported to AppDynamics](https://help.appdynamics.com/hc/en-us/requests/215430)

The project is based on standard project generated automatically by Android Studio.

### AppDynamics version 21.5.0

I have updates sample project to latest Android Gradle plugin (4.2.1) and latest AppDynamics version (21.5.0)
After updating to `21.5.0` the issue is fixed, but there is a blocker for using it straight away.
After bumping AD version, below error appears:
    "Cannot get property 'buildID' on extra properties extension as it does not exist"
    This field is not described anywhere in official docs, so looks like plugin devs just shipped in public API
    some internally used field. It fails builds if AD is enabled.
    It can be easily workarounded (with unknown side effects) by adding below line to `app/build.gradle`
    ```
    project.ext.buildID = "any non empty text"
    ```

    After this is added project compiles and mapping file is automatically uploaded (checked via API)

But after bumping the plugin new issue appeared in our main app (do not how to reproduce this on sample):
```
org.gradle.api.tasks.TaskExecutionException: Execution failed for task ':app:appDynamicsProcessProguardMappingIgUsRelease'.
    ...
Caused by: : java.lang.ClassCastException: org.apache.xerces.parsers.XIncludeAwareParserConfiguration cannot be cast to org.apache.xerces.xni.parser.XMLParserConfiguration
            at com.appdynamics.android.gradle.ProguardUtils.findMappingFile(ProguardUtils.groovy:117)
            at com.appdynamics.android.gradle.ProguardUtils$findMappingFile$4.call(Unknown Source)
            at com.appdynamics.android.gradle.ProcessProguardMappingFileTask.injectAndUploadMappingTxt(ProcessProguardMappingFileTask.groovy:39)
            at org.gradle.internal.reflect.JavaMethod.invoke(JavaMethod.java:104)
            ...
Caused by: java.lang.ClassCastException: org.apache.xerces.parsers.XIncludeAwareParserConfiguration cannot be cast to org.apache.xerces.xni.parser.XMLParserConfiguration
        at org.apache.xerces.parsers.SAXParser.<init>(Unknown Source)
        at org.apache.xerces.parsers.SAXParser.<init>(Unknown Source)
        at org.apache.xerces.jaxp.SAXParserImpl$JAXPSAXParser.<init>(Unknown Source)
        at org.apache.xerces.jaxp.SAXParserImpl.<init>(Unknown Source)
        at org.apache.xerces.jaxp.SAXParserFactoryImpl.newSAXParser(Unknown Source)
        at org.apache.tools.ant.util.JAXPUtils.newSAXParser(JAXPUtils.java:217)
        ...
```


### AppDynamics version 20.12.1

Then AppDynamics `adeum` Gradle plugin was applied to it:
 1. Root `build.gradle` - classpath with version `20.12.1` added
 2. `app/build.gradle` - `adeum` plugin applied and configured
 3. `TestApp` - Android Application class with enabling AppDynamics instrumentation

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