
import sbt.ExclusionRule
import sbt.Keys._

ThisBuild / logLevel := Level.Error

//Organization
ThisBuild / organization := "alp"
ThisBuild / version := "0.1.0"
ThisBuild / scalaVersion := "2.12.14"


ThisBuild / fork := true
ThisBuild / scalacOptions ++= Seq(
  "-unchecked", // able additional warnings where generated code depends on assumptions
  "-deprecation", // emit warning for usages of deprecated APIs
  "-feature", // emit warning usages of features that should be imported explicitly
  // Features enabled by default
  "-encoding", "UTF-8",
  "-language:higherKinds",
  "-language:postfixOps",
  "-language:implicitConversions",
  "-language:experimental.macros",
  "-language:reflectiveCalls"
)


lazy val commonLibs = Seq(
  libraryDependencies ++= Seq(
    Dependencies.Libraries.plotly,
    Dependencies.Libraries.sparkCore,
    Dependencies.Libraries.sparkSql,
    Dependencies.Libraries.sparkMllib
  )
)


lazy val sparkMl = (project in file("spark-ml"))
  .settings(commonLibs: _*)
  .settings(
    publish := {}
  )


val sparkMLLib = (project in file("."))
  .settings(
    publishLocal := {},
    publish := {}
  )
  .aggregate(sparkMl)
