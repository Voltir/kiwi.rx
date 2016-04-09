val settings = Seq(
  version := "1.1.1",
  scalaVersion := "2.11.7",
  organization := "com.stabletechs",
  testFrameworks += new TestFramework("utest.runner.Framework"),
  resolvers += Resolver.sonatypeRepo("releases"),
  addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.7.1"),
  addCompilerPlugin("com.lihaoyi" %% "acyclic" % "0.1.4"),
  libraryDependencies ++= Seq(
    "com.lihaoyi" %%% "scalarx" % "0.3.1",
    "com.lihaoyi" %%% "sourcecode" % "0.1.1",
    "com.lihaoyi" %%% "pprint" % "0.3.9",
    "com.lihaoyi" %%% "utest" % "0.3.1" % "test",
    "com.lihaoyi" %% "acyclic" % "0.1.4" % "provided"
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
)

val core = crossProject
  .settings(settings:_*)
  .settings(
    name := "core",
    libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value

  )

val coreJVM = core.jvm

val coreJS = core.js

val kiwirx = crossProject
  .settings(settings:_*)
  .dependsOn(core % "compile->compile;test->test")
  .settings(
    name := "kiwirx"
  )

lazy val kiwirxJVM = kiwirx.jvm

lazy val kiwirxJS = kiwirx.js

//val kiwirx = crossProject.settings(
//  version := "1.1.1",
//  name := "kiwirx",
//  scalaVersion := "2.11.7",
//  organization := "com.stabletechs",
//  testFrameworks += new TestFramework("utest.runner.Framework"),
//  resolvers += Resolver.sonatypeRepo("releases"),
//  addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.7.1"),
//  addCompilerPlugin("com.lihaoyi" %% "acyclic" % "0.1.4"),
//  libraryDependencies ++= Seq(
//    "com.lihaoyi" %%% "scalarx" % "0.3.1",
//    "com.lihaoyi" %%% "sourcecode" % "0.1.1",
//    "com.lihaoyi" %%% "pprint" % "0.3.9",
//    "com.lihaoyi" %%% "utest" % "0.3.1" % "test",
//    "com.lihaoyi" %% "acyclic" % "0.1.4" % "provided"
//  ),
//  scmInfo := Some(ScmInfo(
//    url("https://github.com/Voltir/kiwi.rx"),
//    "scm:git:git@github.com/Voltir/kiwi.rx.git",
//    Some("scm:git:git@github.com/Voltir/kiwi.rx.git"))
//  ),
//  publishMavenStyle := true,
//  publishTo := {
//    val nexus = "https://oss.sonatype.org/"
//    if (isSnapshot.value)
//      Some("snapshots" at nexus + "content/repositories/snapshots")
//    else
//      Some("releases" at nexus + "service/local/staging/deploy/maven2")
//  },
//  sonatypeProfileName := "com.stabletechs",
//  homepage := Some(url("http://stabletechs.com/")),
//  licenses += ("MIT License", url("http://www.opensource.org/licenses/mit-license.php")),
//  pomExtra :=
//    <developers>
//      <developer>
//        <id>Voltaire</id>
//        <name>Nick Childers</name>
//        <url>https://github.com/voltir/</url>
//      </developer>
//    </developers>
//  ,
//  pomIncludeRepository := { _ => false }
//).jsSettings(
//  scalaJSStage in Test := FullOptStage,
//  scalaJSUseRhino in Global := false
//)
//
//lazy val kiwirxJVM = kiwirx.jvm
//
//lazy val kiwirxJS = kiwirx.js
