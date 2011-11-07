package saab

import com.android.sdklib.build.ApkBuilder

object Plugin extends sbt.Plugin {

  import sbt._
  import Keys._

  object android {

    type Password = String
    type Alias = (String, Password)
    type Certificate = (File, Alias, Password)
    type Directory = File
    type Executable = File
    type Jar = File

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

      // hidden
      val resApk = SettingKey[File]("res-apk", "The res packaged apk name")
      val apk = SettingKey[File]("apk", "The res packaged apk name")
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

      val aapt = TaskKey[Seq[File]]("gen-r", "Generate R file")
      val resApk = TaskKey[File]("gen-res-apk", "Generate R file")
      val aidl = TaskKey[Seq[File]]("gen-aidl", "Generate interface against AIDL file")
      val dx = TaskKey[File]("gen-dx", "Dexing classes into dx")

      def aaptPackageTask(aapt: Executable, manifest: File, res: Directory, assets: Directory, android: Jar, outApk: File): File = {
        Process(<x>
          {aapt}
          package --auto-add-overlay -f
          -M
          {manifest}
          -S
          {res}
          -A
          {assets}
          -I
          {android}
          -F
          {outApk}
        </x>).!

        outApk
      }

      def generateRFile(pkg: String, aapt: File, manifest: File, resFolder: File, androidJar: File, outFolder: File, log: Logger): Seq[File] = {
        val out = outFolder / "java"
        if (!out.exists()) out.mkdirs()
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
            {out.absolutePath}
          </x>
        )
        if (process ! log == 1) sys.error("Can not generate R file for command %s" format process.toString)
        out ** "R.java" get
      }

      def generateAIDLFile(sources: Seq[File], aidlExecutable: File, outFolder: File, javaSourceFolder: File, out: Logger): Seq[File] = {
        val outF = outFolder / "java"
        if (!outF.exists()) outF.mkdirs()
        val aidlPaths = sources.map(_ ** "*.aidl").reduceLeft(_ +++ _).get
        if (aidlPaths.isEmpty) {
          out.debug("no AIDL files found, skipping")
          Nil
        } else {
          val processor = aidlPaths.map {
            ap =>
              aidlExecutable.absolutePath ::
                "-o" + outF.absolutePath ::
                "-I" + javaSourceFolder.absolutePath ::
                ap.absolutePath :: Nil
          }.foldLeft(None.asInstanceOf[Option[ProcessBuilder]]) {
            (f, s) =>
              f match {
                case None => Some(s)
                case Some(first) => Some(first #&& s)
              }
          }.get
          out.debug("generating aidl " + processor)
          processor !

          val rPath = outF ** "R.java"
          outF ** "*.java" --- (rPath) get
        }
      }

      def dxTask(scalaInstance: ScalaInstance, dxPath: File, classDirectory: File, classesDexPath: File, l: Logger): File = {
        val inputs = classDirectory --- scalaInstance.libraryJar get
        val uptodate = classesDexPath.exists &&
          !inputs.exists(_.lastModified > classesDexPath.lastModified)
        if (!uptodate) {
          val dxCmd = String.format("%s  --dex --output=%s %s", dxPath, classesDexPath / "classes.dex", inputs.mkString(" "))
          l.debug(dxCmd)
          l.info("Dexing " + classesDexPath)
          l.debug(dxCmd !!)
        } else l.debug("dex file uptodate, skipping")

        classesDexPath / "classes.dex"
      }

      def apkBuilder(outApkFile: File, res: File, dex: File): File = {
        val f = new ApkBuilder(outApkFile, res, dex, null, null)
        f.setDebugMode(true)
        f.sealApk()
        outApkFile
      }
    }

  }

  def androidSettingsIn(c: Configuration): Seq[Setting[_]] = inConfig(c)(
    Defaults.settings ++ android.sdk.settings ++
      Seq(

        unmanagedJars in c <+= android.project.androidJar map {
          p =>
            p
        },

        android.task.aapt in android.key in c <<= (android.project.packageName in android.androidHome, android.sdk.aapt in android.Android,
          android.project.manifest in android.androidHome, android.project.res in android.androidHome, android.project.androidJar in android.androidHome, sourceManaged in android.androidHome, streams) map {
          (mPackage, aPath, mPath, resPath, jPath, javaPath, log) =>
            android.task.generateRFile(mPackage, aPath, mPath, resPath, jPath, javaPath, log.log)
        },

        android.task.aidl in android.key in c <<= (sourceDirectories, android.sdk.aidl in android.Android, sourceManaged in android.androidHome, javaSource, streams) map {
          (sDirs, idPath, javaPath, jSource, s) =>
            android.task.generateAIDLFile(sDirs, idPath, javaPath, jSource, s.log)
        },

        android.task.dx in android.key in c <<= (scalaInstance, android.sdk.dx in android.Android, classDirectory, target, streams) map {
          (scalaInstance, dxPath, classDirectory, classesDexPath, s) =>
            android.task.dxTask(scalaInstance, dxPath, classDirectory, classesDexPath, s.log)
        },

        android.task.resApk in android.key in c <<=
          (android.sdk.aapt in android.Android, android.project.manifest in android.androidHome, android.project.res in android.androidHome,
            android.project.assets in android.androidHome, android.project.androidJar in android.androidHome, android.project.resApk in android.androidHome) map {
            (apPath, manPath, rPath, assetPath, jPath, resApkPath) =>
              android.task.aaptPackageTask(apPath, manPath, rPath, assetPath, jPath, resApkPath)
          },

        android.project.packageApk in android.key in c <<=
          (android.project.apk in android.androidHome, android.task.resApk in android.key, android.task.dx in android.key in c, streams) map {
            (outApkFile, res, dex, log) =>
              log.log.info("Packaging APK in debug mode, " + outApkFile + " res " + res + " dex " + dex)
              android.task.apkBuilder(outApkFile, res, dex)
          },

        android.project.packageApk in android.key in c <<= android.project.packageApk in android.key in c dependsOn (android.task.dx in android.key in c),
        sourceGenerators in c <+= (android.task.aapt in android.key in c).task,
        sourceGenerators in c <+= (android.task.aidl in android.key in c).task

      )

  )

  lazy val defaultSettings: Seq[Setting[_]] = androidSettingsIn(Compile)

  // Some defaults values common in most project
  object Defaults {
    lazy val settings: Seq[Setting[_]] = Seq(
      android.project.manifest <<= sourceDirectory(_ / "AndroidManifest.xml"),
      android.project.res <<= sourceDirectory(_ / "res"),
      android.project.assets <<= sourceDirectory(_ / "assets"),
      android.project.resApk <<= target(_ / "resources.apk"),
      android.project.apk <<= target(_ / "out.apk"),
      android.project.packageName <<= android.project.manifest(AndroidManifest(_).pkg),
      android.project.androidJar := file("/opt/android-sdk-linux_x86/platforms/android-14/android.jar")
    )
  }

}




