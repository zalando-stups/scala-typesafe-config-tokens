package org.zalando.stups.tokens

import java.io.File
import java.net.URI
import java.util.concurrent.{ScheduledExecutorService, TimeUnit}

import com.typesafe.config.{Config, ConfigFactory}
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader.arbitraryTypeValueReader
import org.zalando.stups.tokens.mcb.MCBConfig

import scala.collection.JavaConversions._
import scala.concurrent.duration.FiniteDuration

case class AccessTokenFactory(config: Config = ConfigFactory.load()) {
  private val prefix = "tokens"

  final case class TokenConfiguration(tokenId: String, scopes: Set[String])

  final case class HttpConfiguration(
      socketTimeout: Option[FiniteDuration],
      connectTimeout: Option[FiniteDuration],
      connectionRequestTimeout: Option[FiniteDuration],
      staleConnectionCheckEnabled: Option[Boolean]) {

    def toHttpConfig: HttpConfig = {
      val httpConfig = new HttpConfig
      socketTimeout.foreach { socketTimeout =>
        httpConfig.setSocketTimeout(socketTimeout.toMillis.toInt)
      }
      connectTimeout.foreach { connectTimeout =>
        httpConfig.setConnectTimeout(connectTimeout.toMillis.toInt)
      }
      connectionRequestTimeout.foreach { connectionRequestTimeout =>
        httpConfig.setConnectionRequestTimeout(
          connectionRequestTimeout.toMillis.toInt)
      }
      staleConnectionCheckEnabled.foreach { staleConnectionCheckEnabled =>
        httpConfig.setStaleConnectionCheckEnabled(staleConnectionCheckEnabled)
      }
      httpConfig
    }
  }

  final case class HttpProviderConfiguration(
      clientCredentials: Option[String],
      userCredentials: Option[String],
      accessTokenUri: Option[String],
      httpConfig: Option[HttpConfiguration])

  final case class MCBConfiguration(errorThreshold: Option[Int],
                                    timeout: Option[FiniteDuration],
                                    maxMulti: Option[Int],
                                    name: Option[String]) {
    def toMCBConfig: MCBConfig = {
      val mCBConfigBuilder = new MCBConfig.Builder()
      errorThreshold.foreach { errorThreshold =>
        mCBConfigBuilder.withErrorThreshold(errorThreshold)
      }
      timeout.foreach { timeout =>
        mCBConfigBuilder.withTimeUnit(TimeUnit.MICROSECONDS)
        mCBConfigBuilder.withTimeout(timeout.toMicros.toInt)
      }
      maxMulti.foreach { maxMulti =>
        mCBConfigBuilder.withMaxMulti(maxMulti)
      }
      name.foreach { name =>
        mCBConfigBuilder.withName(name)
      }

      mCBConfigBuilder.build()
    }
  }

  val uri = new URI(config.as[String](s"$prefix.accessTokenUri"))

  val maybeClientCredentials: Option[JsonFileBackedClientCredentialsProvider] =
    config.as[Option[String]](s"$prefix.clientCredentials").map { fileName =>
      new JsonFileBackedClientCredentialsProvider(new File(fileName))
    }

  val maybeUserCredentials: Option[JsonFileBackedUserCredentialsProvider] =
    config
      .as[Option[String]](s"$prefix.userCredentials")
      .map(fileName =>
        new JsonFileBackedUserCredentialsProvider(new File(fileName)))

  val userDirectoryCredentialFile: String = config
    .as[Option[String]](s"$prefix.userDirectoryCredentialFile")
    .getOrElse("user.json")

  val clientDirectoryCredentialFile: String = config
    .as[Option[String]](s"$prefix.clientDirectoryCredentialFile")
    .getOrElse("client.json")

  val maybeCredentialsDirectory: Option[
    (JsonFileBackedClientCredentialsProvider,
     JsonFileBackedUserCredentialsProvider)] =
    config.as[Option[String]](s"$prefix.credentialsDirectory").map {
      directory =>
        val user   = new File(s"$directory/$userDirectoryCredentialFile")
        val client = new File(s"$directory/$clientDirectoryCredentialFile")
        (
          new JsonFileBackedClientCredentialsProvider(client),
          new JsonFileBackedUserCredentialsProvider(user)
        )
    }

  val maybeHttpProviderConfiguration: Option[ClosableHttpProviderFactory] =
    config.as[Option[HttpProviderConfiguration]](s"$prefix.httpProvider").map {
      httpProviderConfiguration =>
        val closableHttpProviderFactory = new ClosableHttpProviderFactory
        closableHttpProviderFactory.create(
          httpProviderConfiguration.clientCredentials
            .map(fileName =>
              new JsonFileBackedClientCredentialsProvider(new File(fileName)).get)
            .orNull,
          httpProviderConfiguration.userCredentials
            .map(fileName =>
              new JsonFileBackedUserCredentialsProvider(new File(fileName)).get)
            .orNull,
          httpProviderConfiguration.accessTokenUri
            .map(accessTokenUri => new URI(accessTokenUri))
            .orNull,
          httpProviderConfiguration.httpConfig
            .map(_.toHttpConfig) getOrElse new HttpConfig
        )
        closableHttpProviderFactory
    }

  val maybeConnectionRequestTimeout: Option[FiniteDuration] =
    config.as[Option[FiniteDuration]](s"$prefix.connectionRequestTimeout")

  val maybeConnectTimeout: Option[FiniteDuration] =
    config.as[Option[FiniteDuration]](s"$prefix.maybeConnectTimeout")

  val maybeStaleConnectionCheckEnabled: Option[Boolean] =
    config.as[Option[Boolean]](s"$prefix.staleConnectionCheckEnabled")

  val maybeSchedulingPeriod: Option[FiniteDuration] =
    config.as[Option[FiniteDuration]](s"$prefix.schedulingPeriod")

  val maybeMetricsListener: Option[MetricsListener] =
    config.as[Option[String]](s"$prefix.metricsListener").map { className =>
      Class.forName(className).asInstanceOf[MetricsListener]
    }

  val maybeExistingExecutorService: Option[ScheduledExecutorService] =
    config.as[Option[String]](s"$prefix.existingExecutorService").map {
      className =>
        Class.forName(className).asInstanceOf[ScheduledExecutorService]
    }

  val maybeTokenInfoUri: Option[URI] =
    config.as[Option[String]](s"$prefix.tokenInfoUri").map { uri =>
      new URI(uri)
    }

  val maybeTokenRefresherMcbConfig: Option[MCBConfig] = config
    .as[Option[MCBConfiguration]](s"$prefix.tokenRefresherMcbConfig")
    .map(_.toMCBConfig)

  val maybeTokenVerifierMcbConfig: Option[MCBConfig] = config
    .as[Option[MCBConfiguration]](s"$prefix.tokenVerifierMcbConfig")
    .map(_.toMCBConfig)

  val maybeTokenVerifierSchedulingPeriod: Option[FiniteDuration] =
    config.as[Option[FiniteDuration]](s"$prefix.tokenVerifierSchedulingPeriod")

  val maybeRefreshPercentLeft: Option[Int] =
    config.as[Option[Int]](s"$prefix.refreshPercentLeft")

  val maybeWarnPercentLeft: Option[Int] =
    config.as[Option[Int]](s"$prefix.warnPercentLeft")

  val tokenConfigurationList: Set[TokenConfiguration] =
    config.as[Set[TokenConfiguration]](s"$prefix.tokenConfigurationList")

  val accessTokensBuilder: AccessTokensBuilder =
    Tokens.createAccessTokensWithUri(uri)

  maybeClientCredentials.foreach { clientCredentialsDirectory =>
    accessTokensBuilder.usingClientCredentialsProvider(
      clientCredentialsDirectory)
  }

  maybeUserCredentials.foreach { userClientCredentialsDirectory =>
    accessTokensBuilder.usingUserCredentialsProvider(
      userClientCredentialsDirectory)
  }

  maybeCredentialsDirectory.foreach {
    case (client, user) =>
      accessTokensBuilder.usingClientCredentialsProvider(client)
      accessTokensBuilder.usingUserCredentialsProvider(user)
  }

  maybeHttpProviderConfiguration.foreach { httpProviderConfiguration =>
    accessTokensBuilder.usingHttpProviderFactory(httpProviderConfiguration)
  }

  maybeConnectionRequestTimeout.foreach { connectionRequestTimeout =>
    accessTokensBuilder.connectionRequestTimeout(
      connectionRequestTimeout.toMillis.toInt)
  }

  maybeConnectTimeout.foreach { connectTimeout =>
    accessTokensBuilder.connectTimeout(connectTimeout.toMillis.toInt)
  }

  maybeStaleConnectionCheckEnabled.foreach { staleConnectionCheckEnabled =>
    accessTokensBuilder.staleConnectionCheckEnabled(
      staleConnectionCheckEnabled)
  }

  maybeSchedulingPeriod.foreach { schedulingPeriod =>
    accessTokensBuilder.schedulingTimeUnit(TimeUnit.MICROSECONDS)
    accessTokensBuilder.schedulingPeriod(schedulingPeriod.toMicros.toInt)
  }

  maybeMetricsListener.foreach { metricsListener =>
    accessTokensBuilder.metricsListener(metricsListener)
  }

  maybeExistingExecutorService.foreach { existingExecutorService =>
    accessTokensBuilder.existingExecutorService(existingExecutorService)
  }

  maybeTokenInfoUri.foreach { tokenInfoUri =>
    accessTokensBuilder.tokenInfoUri(tokenInfoUri)
  }

  maybeTokenRefresherMcbConfig.foreach { tokenRefresherMcbConfig =>
    accessTokensBuilder.tokenRefresherMcbConfig(tokenRefresherMcbConfig)
  }

  maybeTokenVerifierMcbConfig.foreach { tokenVerifierMcbConfig =>
    accessTokensBuilder.tokenVerifierMcbConfig(tokenVerifierMcbConfig)
  }

  maybeTokenVerifierSchedulingPeriod.foreach { tokenVerifierMcbConfig =>
    accessTokensBuilder.tokenVerifierSchedulingTimeUnit(TimeUnit.MICROSECONDS)
    accessTokensBuilder.tokenVerifierSchedulingPeriod(
      tokenVerifierMcbConfig.toMicros.toInt)
  }

  maybeRefreshPercentLeft.foreach { refreshPercentLeft =>
    accessTokensBuilder.refreshPercentLeft(refreshPercentLeft)
  }

  maybeWarnPercentLeft.foreach { warnPercentLeft =>
    accessTokensBuilder.warnPercentLeft(warnPercentLeft)
  }

  tokenConfigurationList.foreach(
    tokenConfiguration =>
      accessTokensBuilder
        .manageToken(tokenConfiguration.tokenId)
        .addScopesTypeSafe(tokenConfiguration.scopes)
        .done()
  )

  val accessTokens: AccessTokens =
    accessTokensBuilder.start()
}
