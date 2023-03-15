name := "css-inliner"

organization := "no.vedaadata"

version := "1.0.3"

scalaVersion := "2.13.10"

crossScalaVersions := Seq("2.13.10", "3.2.2")

libraryDependencies += "com.lihaoyi" %% "cssparse" % "3.0.0"

libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "2.0.1"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.14" % "test"

publishTo := Some("My Maven Repo Publisher" at "https://mymavenrepo.com/repo/j1YxfckeUitD5ZGTAisl")
