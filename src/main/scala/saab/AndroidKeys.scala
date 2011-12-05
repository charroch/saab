package saab

import sbt._
import sbt.Keys

object AndroidKeys {

  type Password = String
  type Alias = (String, Password)
  type Certificate = (File, Alias, Password)
  type Directory = File

  type Executable = {
   // def apply(implicit os: OS): File
  }

//  object OS extends Enumeration("Mac", "Win", "Unix") {
//    type WeekDay = Value
//    val Sun, Mon, Tue, Wed, Thu, Fri, Sat = Value
//  }

  type Jar = File

  val Android = config("android")

  // Environment specific keys
  val sdkHome = SettingKey[Directory]("sdk-home", "Root directory where the android SDK is installed")
  val aapt = TaskKey

  // project specific
  val manifestFile = SettingKey[File]("manifest-file", "The AndroidManifest.xml file defined for this project")
  val manifest = SettingKey[AndroidManifest]("manifest", "The AndroidManifest.xml file defined for this project")

  // ADB
  val deviceManager = SettingKey[DeviceManager]("device-manager", "Managed the lifecycle of all devices - adding listener is done here")
  val deviceStore = SettingKey[DeviceStore]("device-store", "A map of all attached devices")
  val deviceFilter = SettingKey[(Device => Boolean)]("device-filter", "Filter for devices to act upon when task are executed")
  val install = TaskKey[Unit]("install", "Install the APK to the corresponding devices")
  val instrument = TaskKey[Unit]("instrument", "Install the APK to the corresponding devices")
}