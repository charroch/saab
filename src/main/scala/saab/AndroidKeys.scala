package saab

import sbt._

object AndroidKeys {

  type Password = String
  type Alias = (String, Password)
  type Certificate = (File, Alias, Password)
  type Directory = File
  type Executable = File
  type Jar = File

  val Android = config("android")

  // Environment specific keys
  val sdkHome = SettingKey[Directory]("sdk-home", "Root directory where the android SDK is installed")
  val sdk = SettingKey[SDK]("sdk", "Root directory where the android SDK is installed")

  // project specific
  val manifestFile = SettingKey[File]("manifest-file", "The AndroidManifest.xml file defined for this project")
  val manifest = SettingKey[AndroidManifest]("manifest", "The AndroidManifest.xml file defined for this project")
}