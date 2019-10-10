name := "css-inliner"

organization := "no.vedaadata"

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.8"

crossScalaVersions := Seq("2.11.12", "2.12.10")

libraryDependencies += "com.lihaoyi" %% "cssparse" % "2.1.2"

// add scala-xml dependency when needed (for Scala 2.11 and newer) in a robust way
// this mechanism supports cross-version publishing
libraryDependencies ++= {
  CrossVersion.partialVersion(scalaVersion.value) match {
    // if scala 2.11+ is used, add dependency on scala-xml module
    case Some((2, scalaMajor)) if scalaMajor >= 11 =>
      Seq("org.scala-lang.modules" %% "scala-xml" % "1.0.6")
    case _ =>
      Nil
  }
}

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % "test"
