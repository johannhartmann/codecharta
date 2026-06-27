ThisBuild / scalaVersion := "3.3.3"

lazy val root = project
  .in(file("."))
  .settings(
    name := "sample",
    libraryDependencies += "org.typelevel" %% "cats-core" % "2.10.0"
  )
