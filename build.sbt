import com.typesafe.sbt.license.{DepModuleInfo}

val dottyVersion = "0.22.0-RC1"

val commonSettings: Seq[Setting[_]] = Seq(
  organization := "com.jsuereth.sauerkraut",
  organizationName := "Google",
  startYear := Some(2019),
  licenses += ("Apache-2.0", new URL("https://www.apache.org/licenses/LICENSE-2.0.txt")),  
  version := "0.1.0",  
  scalaVersion := dottyVersion,
  libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test",
  licenseReportTitle := "third_party_licenses",
  licenseReportDir := baseDirectory.value / "third_party",
  licenseReportTypes := Seq(MarkDown),
  licenseReportNotes := {
    case DepModuleInfo(group, id, version) if group contains "com.google.protobuf" => "Google Protocol Buffers"
    case DepModuleInfo(group, id, version) if id contains "junit" => "Used for testing"
    case DepModuleInfo(group, id, version) if id contains "protocjar" => "Used to compile proto files to Java."
  },
)

val core = project
  .settings(commonSettings:_*)

val json = project
  .settings(commonSettings:_*)
  .dependsOn(core)

val pb = project
  .settings(commonSettings:_*)
  .dependsOn(core)
  .settings(
    libraryDependencies += "com.google.protobuf" % "protobuf-java" % "3.11.3"
  )

val pbtest = project
  .settings(commonSettings:_*)
  .dependsOn(pb)
  .enablePlugins(ProtobufPlugin)
  .settings(
    skip in publish := true,
    libraryDependencies += "com.google.protobuf" % "protobuf-java" % "3.11.3",
    protobufRunProtoc in ProtobufConfig := { args =>
      com.github.os72.protocjar.Protoc.runProtoc("-v370" +: args.toArray)
    }
  )

val root = project.in(file(".")).aggregate(core,json,pb,pbtest).settings(skip in publish := true)