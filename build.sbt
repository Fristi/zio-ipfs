name := "zio-ipfs"

version := "0.1"

scalaVersion := "2.13.3"

libraryDependencies ++= Seq(
  "dev.zio" %% "zio" % "1.0.0",
  "dev.zio" %% "zio-streams" % "1.0.0",
  "com.github.pathikrit"  %% "better-files"  % "3.9.1",
  "com.github.ipfs" % "java-ipfs-http-client" % "v1.3.2"
)

resolvers += "jitpack" at "https://jitpack.io"