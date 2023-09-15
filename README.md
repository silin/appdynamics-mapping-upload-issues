## Sample branch to reproduce the issue with AppD Gradle plugin and Maven repo with token authentication

[Link to the original issue thread reported to AppDynamics](https://help.appdynamics.com/hc/en-us/requests/374512)

The project is based on standard project generated automatically by Android Studio.

### AppDynamics version 23.7.1 (latest at the moment)

Project/environment setup:
1. Android Gradle Plugin: 8.1.1
2. AppDynamics Android agent: 23.7.1
3. Gradle version: 8.3
3. Java 17

In our company we need to migrate to new Maven repository (GitLab) and need to use header credentials for this new maven repo. Exactly as described in GitLab docs here:

https://docs.gitlab.com/ee/user/packages/maven_repository/?tab=gradle#edit-the-client-configuration

So, basically our Maven repository configuration looks approximately like this:

```
maven {
    url = uri("https://repo.url")
    name = "GitLab"
    credentials(HttpHeaderCredentials::class) {
        name = "TokenHeader"
        value = "TokenValue"
    }
    authentication {
        create("header", HttpHeaderAuthentication::class)
    }
}
```

The problem is that once we add this to project configuration, the project starts failing on Gradle configuration stage with below stacktrace:

FAILURE: Build failed with an exception.

* What went wrong:
  A problem occurred configuring project ':app'.
> Can not use getCredentials() method when not using PasswordCredentials; please use getCredentials(Class)