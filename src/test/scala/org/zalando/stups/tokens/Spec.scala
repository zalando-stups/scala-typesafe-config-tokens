package org.zalando.stups.tokens

import org.specs2._

object AccessTokenFactoryInstance extends AccessTokenFactory

class Spec extends Specification {

  def is =
    s2"""
     Reading from test application.conf
      accessTokenUri should match $accessTokenUriMatch
      credentialsDirectory should match $credentialsDirectoryMatch
      tokenConfigurationList should match $tokenConfigurationList
    """

  def accessTokenUriMatch = {
    AccessTokenFactoryInstance.uri.toString mustEqual "http://localhost:9191/access_token?realm=whatever"
  }

  def credentialsDirectoryMatch = {
    AccessTokenFactoryInstance.maybeCredentialsDirectory.map {
      case (clientCredentialsProvider, userCredentialsProvider) =>
        val clientCredentials = clientCredentialsProvider.get()
        val userCredentials   = userCredentialsProvider.get()

        (clientCredentials.getId,
         clientCredentials.getSecret,
         userCredentials.getUsername,
         userCredentials.getPassword)
    } mustEqual Option(("foo", "bar", "abc", "xyz"))
  }

  def tokenConfigurationList = {
    import scala.collection.JavaConversions._

    AccessTokenFactoryInstance.accessTokensBuilder.getAccessTokenConfigurations
      .map { accessTokenConfiguration =>
        (accessTokenConfiguration.getTokenId.asInstanceOf[String],
         accessTokenConfiguration.getScopes
           .map(_.asInstanceOf[String])
           .to[Set])
      }
      .to[Set]
  } mustEqual Set(
    ("firstService",
     Set(
       "refole:read",
       "refole:write",
       "refole:all"
     )),
    ("secondService",
     Set(
       "singleScope:all"
     ))
  )

}
