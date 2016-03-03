
val kiwirx = crossProject.settings(
  version := "1.1.1",
  name := "kiwirx",
  scalaVersion := "2.11.7",
  organization := "com.stabletechs",
  testFrameworks += new TestFramework("utest.runner.Framework"),
  addCompilerPlugin("com.lihaoyi" %% "acyclic" % "0.1.3"),
  libraryDependencies ++= Seq(
    "com.lihaoyi" %%% "scalarx" % "0.3.1",
    "com.lihaoyi" %%% "utest" % "0.3.1" % "test"
  ),
  scmInfo := Some(ScmInfo(
    url("https://github.com/Voltir/kiwi.rx"),
    "scm:git:git@github.com/Voltir/kiwi.rx.git",
    Some("scm:git:git@github.com/Voltir/kiwi.rx.git"))
  ),
  publishMavenStyle := true,
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases" at nexus + "service/local/staging/deploy/maven2")
  },
  sonatypeProfileName := "com.stabletechs",
  homepage := Some(url("http://stabletechs.com/")),
  licenses += ("MIT License", url("http://www.opensource.org/licenses/mit-license.php")),
  pomExtra :=
    <developers>
      <developer>
        <id>Voltaire</id>
        <name>Nick Childers</name>
        <url>https://github.com/voltir/</url>
      </developer>
    </developers>
  ,
  pomIncludeRepository := { _ => false }
).jsSettings(
  scalaJSStage in Test := FullOptStage,
  scalaJSUseRhino in Global := false
)

lazy val kiwirxJVM = kiwirx.jvm

lazy val kiwirxJS = kiwirx.js
