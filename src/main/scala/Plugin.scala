package saab

object Plugin extends sbt.Plugin {

  import sbt._
  import Keys._

  object android {

    type Password = String
    type Alias = (String, Password)
    type Certificate = (File, Alias, Password)
    type Directory = File
    type Executable = File

    val Android = config("android")

    val androidHome = SettingKey[String]("android-home", "Android SDK home path - will check ANDROID_HOME system variable")
    val key = SettingKey[Unit]("android", "Project type, can be normal android project, test or library")

    object project {

      // Global project tasks
      val packageApk = TaskKey[File]("package-apk", "Package the project into an APK")
      val debug = SettingKey[Certificate]("debug", "Certificate for debug signing")
      val market = SettingKey[Certificate]("market", "Certificate for market signing")
      val install = TaskKey[Unit]("install", "Install the APK onto a device or emulator")

      // project of type android specific settings
      val manifest = SettingKey[File]("manifest", "The AndroidManifest.xml defined for this project")
      val res = SettingKey[Directory]("res", "The res directory for layout/drawable/string/xml values")
      val assets = SettingKey[Directory]("assets", "The asset directory for layout/drawable/string/xml values")
      val androidJar = SettingKey[File]("android-jar", "The android.jar used for this project")
      val packageName = SettingKey[String]("package-name", "The package name as described in the Android Manifest")
    }

    object sdk {

      val root = SettingKey[Directory]("sdk", "Android root installation directory")
      val dx = SettingKey[Executable]("dx", "dx executable path")
      val aapt = SettingKey[Executable]("aapt", "aapt executable path")
      val aidl = SettingKey[Executable]("aidl", "aidl executable path")
      val installedAddons = SettingKey[Map[String, ModuleID]]("addons", "Installed addons")
      val jars = SettingKey[Map[String, ModuleID]]("jars", "Installed android jars")

      lazy val settings: Seq[Setting[_]] = inConfig(Android)(Seq(
        android.sdk.root := file(System.getenv("ANDROID_HOME")),
        android.sdk.dx <<= android.sdk.root(_ / "platform-tools" / "dx"),
        android.sdk.aapt <<= android.sdk.root(_ / "platform-tools" / "aapt"),
        android.sdk.aidl <<= android.sdk.root(_ / "platform-tools" / "aidl")
        //  android.sdk.installedAddons := addons,
        //  android.sdk.jars := jars
      ))
    }

    object task {

      val dx = TaskKey[Seq[File]]("gen-r", "Generate R file")
      val aidl = TaskKey[Seq[File]]("gen-aidl", "Generate interface against AIDL file")

      def aaptGenerateTask =
        (
          android.project.packageName in androidHome,
          android.sdk.aapt in Android,
          android.project.manifest in androidHome,
          android.project.res in androidHome,
          android.project.androidJar in androidHome,
          sourceManaged in androidHome,
          streams
          ) map {

          (mPackage, aPath, mPath, resPath, jPath, javaPath, log) =>
            generateRFile(mPackage, aPath, mPath, resPath, jPath, javaPath, log.log)
        }

      private[android] def generateRFile(pkg: String, aapt: File, manifest: File, resFolder: File, androidJar: File, outFolder: File, out: Logger): Seq[File] = {
        val process = Process(
          <x>
            {aapt.absolutePath}
            package --auto-add-overlay -m --custom-package
            {pkg}
            -M
            {manifest.absolutePath}
            -S
            {resFolder.absolutePath}
            -I
            {androidJar.absolutePath}
            -J
            {outFolder.absolutePath}
          </x>)
        if (process ! out == 1) sys.error("Can not generate R file for command %s" format process.toString)
        outFolder ** "R.java" get
      }
    }

  }

  def androidSettingsIn(c: Configuration): Seq[Setting[_]] = inConfig(c)(
    Defaults.settings ++ android.sdk.settings ++
      Seq(
        //compile in c ~= _ dependsOn android.task.dx,
        //packageApk in debug in c <<= jarsigner dependsOn apkbuilder,
        //packageApk in market in c <<= zipalign dependsOn jarsigner

        android.task.dx in android.key in c <<= android.task.aaptGenerateTask,
        name in c := "helloWorld"
      ))

  lazy val defaultSettings: Seq[Setting[_]] = androidSettingsIn(Compile)

  // Some defaults values common in most project
  object Defaults {
    lazy val settings: Seq[Setting[_]] = Seq(
      android.project.manifest <<= sourceDirectory(_ / "AndroidManifest.xml"),
      android.project.res <<= sourceDirectory(_ / "res"),
      android.project.assets <<= sourceDirectory(_ / "assets"),
      android.project.packageName <<= android.project.manifest(AndroidManifest(_).pkg),

      android.project.androidJar := file("/opt/android-sdk-linux_x86/platforms/android-14/android.jar")
    )
  }

}




