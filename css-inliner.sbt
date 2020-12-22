name := "css-inliner"

organization := "no.vedaadata"

version := "1.0.1"

scalaVersion := "2.13.4"

crossScalaVersions := Seq("2.11.12", "2.12.10")

libraryDependencies += "com.lihaoyi" %% "cssparse" % "2.3.0"

libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "1.2.0"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.2" % "test"

publishTo := Some("My Maven Repo Publisher" at "https://mymavenrepo.com/repo/j1YxfckeUitD5ZGTAisl")
