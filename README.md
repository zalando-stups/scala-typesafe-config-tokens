## Scala-Typesafe Config STUPS AccessTokens Support [![Build Status](https://travis-ci.org/zalando-incubator/scala-typesafe-config-tokens.svg?branch=master)](https://travis-ci.org/zalando-incubator/scala-typesafe-config-tokens)

Is a small wrapper around [Tokens](https://github.com/zalando-stups/tokens) with autoconfiguration support in Scala applications
using [Typesafe Config](https://github.com/typesafehub/config). The intention is to make it very easy to get access token details,
especially when using OAuth2 via stups.

The project has minimal dependencies so it can easily be integrated into various Scala projects.

### Build

```sbt
sbt compile
```

### Install

Add the following to your `build.sbt` when using SBT.

```sbt
libraryDependencies += "org.zalando" %% "scala-typesafe-config-tokens" % "0.1"
```

Currently built for Scala 2.11

### Configuration

Place the following in `application.conf` in your resources folder (create
it if it doesn't exist)

```conf
tokens {
  accessTokenUri="http://localhost:9191/access_token?realm=whatever"
  clientCredentialsDirectory="/somepath/credentials"

  tokenConfigurationList = [{
    tokenId=firstService
    scopes=[
      "refole:read",
      "refole:write",
      "refole:all"
    ]
  },{
    tokenId=secondService
    scopes=["singleScope:all"]
  }]
}
```

This is a proper typesafe config, so you can for example use environment
variable substitutions, i.e.

```conf
accessTokenUri="http://localhost:9191/access_token?realm=whatever"
accessTokenUri=${?ZALANDO_STUPS_TOKENS_ACCESS_TOKEN_URI}
```

Below is a table of the configuration options

.conf Configuration Key | AccessTokenBuilder Method | Example Config | Default
------------------------| --------------------------| ---------------| -------
`accessTokenUri` | `Tokens.createAccessTokensWithUri` | `"http://localhost:9191/access_token?realm=whatever"` | N/A
`clientCredentials` | `AccessTokensBuilder.usingClientCredentialsProvider` | `${user.dir}"/somepath/credentials/client.json"` | N/A
`userCredentials` | `AccessTokensBuilder.usingUserCredentialsProvider` | `${user.dir}"/somepath/credentials/user.json"` | N/A
`credentialsDirectory` | N/A (convenience method) | `${user.dir}"/somepath/credentials` | N/A
`userDirectoryCredentialFile` | N/A (only used by `credentialsDirectory`) | `"user.json"` | `"user.json"`
`clientDirectoryCredentialFile` | N/A (only used by `credentialsDirectory`) | `"client.json"` | `"client.json"`
`tokenConfigurationList` | See [TokenConfigurationList Object](#tokenconfigurationlist-object) | Array of [TokenConfigurationList Object](#tokenconfigurationlist-object) | See [TokenConfigurationList Object](#tokenconfigurationlist-object) | N/A
`httpProviderConfiguration` | `AccessTokensBuilder.usingHttpProviderFactory` | See [HttpProviderConfiguration Object](#httpproviderconfiguration-object) | See [HttpProviderConfiguration Object](#httpproviderconfiguration-object) | N/A
`connectionRequestTimeout` | `AccessTokensBuilder.connectionRequestTimeout` | `"1 second"` | Provided by [Tokens](https://github.com/zalando-stups/tokens)
`connectTimeout` | `AccessTokensBuilder.connectTimeout` | `"1 second"` | Provided by [Tokens](https://github.com/zalando-stups/tokens)
`schedulingPeriod` | `AccessTokensBuilder.schedulingTimeUnit && AccessTokensBuilder.schedulingPeriod` | `"1 hour"` | Provided by [Tokens](https://github.com/zalando-stups/tokens)
`metricsListener` | `AccessTokensBuilder.metricsListener` | `some.package.MetricsListenerImplementation` | Provided by [Tokens](https://github.com/zalando-stups/tokens)
`existingExecutorService` | `AccessTokensBuilder.existingExecutorService` | `some.package.ScheduledExecutorServiceImplementation` | Provided by [Tokens](https://github.com/zalando-stups/tokens)
`tokenInfoUri` | `AccessTokensBuilder.tokenInfoUri` | `"http://localhost:9191/tokenInfoUri"` | Provided by [Tokens](https://github.com/zalando-stups/tokens)
`tokenRefresherMcbConfig` | `AccessTokensBuilder.tokenRefresherMcbConfig` | See [MCBConfiguration Object](#mcbconfiguration-object) | See [MCBConfiguration Object](#mcbconfiguration-object) | Provided by [Tokens](https://github.com/zalando-stups/tokens)
`tokenVerifierMcbConfig` | `AccessTokensBuilder.tokenVerifierMcbConfig` | See [MCBConfiguration Object](#mcbconfiguration-object) | See [MCBConfiguration Object](#mcbconfiguration-object) | Provided by [Tokens](https://github.com/zalando-stups/tokens)
`tokenVerifierSchedulingPeriod` | `AccessTokensBuilder.tokenVerifierSchedulingTimeUnit` && `AccessTokensBuilder.tokenVerifierSchedulingPeriod`| `"1 minute"` | Provided by [Tokens](https://github.com/zalando-stups/tokens)
`refreshPercentLeft` | `AccessTokensBuilder.refreshPercentLeft` | `30` | Provided by [Tokens](https://github.com/zalando-stups/tokens)
`warnPercentLeft` | `AccessTokensBuilder.warnPercentLeft` | `30` | Provided by [Tokens](https://github.com/zalando-stups/tokens)

#### TokenConfigurationList Object

.conf Configuration Key | AccessTokenBuilder Method | Example Config | Default
------------------------| --------------------------| ---------------| -------
`tokenId` | `manageToken` | `firstService` | N/A
`scopes` | `addScopes` | `["singleScope:all"]` | N/A

#### HttpProviderConfiguration Object

.conf Configuration Key | AccessTokenBuilder Method | Example Config | Default
------------------------| --------------------------| ---------------| -------
`clientCredentials` | `ClosableHttpProviderFactory.create` | `${user.dir}"/somepath/credentials/client.json"` | N/A
`userCredentials` | `ClosableHttpProviderFactory.create` | `${user.dir}"/somepath/credentials/user.json"` | N/A
`accessTokenUri` | `ClosableHttpProviderFactory.create` | `"http://localhost:9191/access_token?realm=whatever"` | N/A
`httpConfig` | `new HttpConfig` | See [HttpConfiguration Object](#httpconfiguration-object) | Provided by [Tokens](https://github.com/zalando-stups/tokens)

#### HttpConfiguration Object

.conf Configuration Key | AccessTokenBuilder Method | Example Config | Default
------------------------| --------------------------| ---------------| -------
`socketTimeout` | `HttpConfig.setSocketTimeout` | `"1 second"` | Provided by [Tokens](https://github.com/zalando-stups/tokens)
`setConnectTimeout` | `HttpConfig.setConnectTimeout` | `"1 second"` | Provided by [Tokens](https://github.com/zalando-stups/tokens)
`setConnectionRequestTimeout` | `HttpConfig.setConnectionRequestTimeout` | `"1 second"` | Provided by [Tokens](https://github.com/zalando-stups/tokens)
`setStaleConnectionCheckEnabled` | `HttpConfig.setStaleConnectionCheckEnabled` | false | Provided by [Tokens](https://github.com/zalando-stups/tokens)

#### MCBConfiguration Object

.conf Configuration Key | AccessTokenBuilder Method | Example Config | Default
------------------------| --------------------------| ---------------| -------
`errorThreshold` | `MCBConfig.Builder.withErrorThreshold` | `10` | Provided by [Tokens](https://github.com/zalando-stups/tokens)
`timeout` | `MCBConfig.Builder.withTimeUnit` && `MCBConfig.Builder.withTimeout` | `"1 second"` | Provided by [Tokens](https://github.com/zalando-stups/tokens)
`maxMulti` | `MCBConfig.Builder.maxMulti` | `20` | Provided by [Tokens](https://github.com/zalando-stups/tokens)
`name` | `MCBConfig.Builder.withName` | `"my-breaker"` | Provided by [Tokens](https://github.com/zalando-stups/tokens)


### Usage

With a configuration in place, you can then need to create an instance of
`org.zalando.stups.tokens.AccessTokenFactory`. By default the `AccessTokenFactory`
will load the config using `ConfigFactory.load()` however you can provide
your own instance of config.

For typical usage you would do something like this
```scala
import org.zalando.stups.tokens.AccessTokenFactory

object AccessTokensInstance extends AccessTokenFactory()

// And then to get your access tokens you would do

AccessTokensInstance.accessTokens.onSuccess{
  case accessTokens => // Do something here
}
```

Another thing to note is that getting the `org.zalando.stups.tokens.AccessTokens`
returns a `Future[ApplicationTokens]` to prevent any blocking.

### Contributing

Please make sure that you format the code using `scalafmt`. You can do this by running `scalafmt` in sbt before committing.
See [scalafmt](https://olafurpg.github.io/scalafmt/) for more info.

### License

Copyright © 2016 Zalando SE

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.