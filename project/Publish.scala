/*
 * Copyright (C) 2016-2020 Lightbend Inc. <https://www.lightbend.com>
 */

package akka

import scala.language.postfixOps
import sbt._, Keys._

/**
 * For projects that are not published.
 */
object NoPublish extends AutoPlugin {
  override def requires = plugins.JvmPlugin

  override def projectSettings = Seq(
    publishArtifact := false,
    publish := {},
    publishLocal := {}
  )

}

object Publish extends AutoPlugin {
  import bintray.BintrayPlugin
  import bintray.BintrayPlugin.autoImport._

  override def trigger = allRequirements
  override def requires = BintrayPlugin

  override def projectSettings = Seq(
    bintrayOrganization := Some("akka"),
    bintrayPackage := "akka-http",
    bintrayRepository := (if (isSnapshot.value) "snapshots" else "maven")
  )
}

object DeployRsync extends AutoPlugin {
  import scala.sys.process._
  import sbt.complete.DefaultParsers._

  override def requires = plugins.JvmPlugin

  trait Keys {
    val deployRsyncArtifact = taskKey[Seq[(File, String)]]("File or directory and a path to deploy to")
    val deployRsync = inputKey[Unit]("Deploy using SCP")
  }

  object autoImport extends Keys
  import autoImport._

  override def projectSettings = Seq(
    deployRsync := {
      val (_, host) = (Space ~ StringBasic).parsed
      deployRsyncArtifact.value.foreach {
        case (from, to) =>
          val result = Seq("rsync", "-rvz", s"$from/", s"$host:$to").!
          require(result == 0, "rsync should return success")
      }
    }
  )
}
