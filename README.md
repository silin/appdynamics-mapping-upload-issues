## Sample branch to reproduce the issue with not working AppD plugin with latest AGP version (7+)

The project is based on standard project generated automatically by Android Studio.

### AppDynamics version 21.6.0

I have updates sample project to latest Android Gradle plugin (7.0.1) and latest AppDynamics version (21.6.0)
When using AGP 7.0.1 project build fails with below error independently if Proguard enabled or not:
```
Some problems were found with the configuration of task ':app:appDynamicsProcessProguardMappingDebug' (type 'ProcessProguardMappingFileTask').
- Type 'com.appdynamics.android.gradle.ProcessProguardMappingFileTask' property 'applicationName' is missing an input or output annotation.
```

It is likely caused that AGP have some new API for handling mapping files which is not expected by AppD plugin.
So AppDynamics plugin becomes blocker for Android projects who want to use latest Android developer tooling