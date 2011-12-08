package saab

import com.android.sdklib.build.ApkBuilder
import sbt.complete.Parser
import java.security.cert.X509Certificate

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

    object release {
      val debug = SettingKey[Certificate]("debug", "Certificate for debug signing")
      val market = SettingKey[Certificate]("market", "Certificate for market signing")
    }

    object project {

      // Global project tasks
      val packageApk = TaskKey[File]("package-apk", "Package the project into an APK")
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
        // resolvers += Resolver.url("my-test-repo", url)( Patterns("[organisation]/[module]/[revision]/[artifact].[ext]") )
      ))
    }

    object task {

      val aapt = TaskKey[Seq[File]]("gen-r", "Generate R file")
      val resApk = TaskKey[File]("gen-res-apk", "Generate R file")
      val aidl = TaskKey[Seq[File]]("gen-aidl", "Generate interface against AIDL file")
      val dx = TaskKey[File]("gen-dx", "Dexing classes into dx")


      def aaptPackageTask(aaptEx: Executable, manifest: File, res: Directory, assets: Directory, android: Jar, outApk: File, s: TaskStreams): File = {
        val aapt = Seq(aaptEx.absolutePath, "package", "--auto-add-overlay", "-f",
          "-M", manifest.absolutePath,
          "-S", res.absolutePath,
          "-A", assets.absolutePath,
          "-I", android.absolutePath,
          "-F", outApk.absolutePath)
        s.log.debug("packaging: " + aapt.mkString(" "))
        if (aapt.run(false).exitValue != 0) sys.error("error packaging resources")
        outApk
      }

      def generateRFile(pkg: String, aapt: File, manifest: File, resFolder: File, androidJar: File, outFolder: File, s: TaskStreams): Seq[File] = {
        val out = outFolder / "java"
        if (!out.exists()) out.mkdirs()

        val aaptP = aapt.absolutePath :: "package" :: "--auto-add-overlay" :: "-m" :: "--custom-package" :: pkg :: "-M" :: manifest.absolutePath :: "-S" :: resFolder.absolutePath :: "-I" :: androidJar.absolutePath :: "-J" :: out.absolutePath :: Nil
        s.log.debug("packaging: " + aaptP.mkString(" "))
        if (aaptP.run(false).exitValue != 0) sys.error("Can not generate R file for command %s" format aapt.toString)
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

      def dxTask(scalaInstance: ScalaInstance, dxPath: File, classDirectory: File, cp: Classpath, classesDexPath: File, l: Logger): File = {

        val inputs = classDirectory +++ cp.map(_.data) --- new File("/opt/android-sdk-linux_x86/platforms/android-14/android.jar") --- scalaInstance.libraryJar get

        val uptodate = classesDexPath.exists && !inputs.exists(_.lastModified > classesDexPath.lastModified)
        if (!uptodate) {
          val dxCmd = String.format("%s  --dex --core-library --output=%s %s", dxPath, classesDexPath, inputs.mkString(" "))
          l.info(dxCmd)
          l.info("Dexing " + classesDexPath)
          l.debug(dxCmd !!)
        } else l.debug("dex file uptodate, skipping")

        classesDexPath
      }

      def apkBuilder(outApkFile: File, res: File, dex: File)(implicit c: Certificate): File = {
        import java.security._
        val (path, (alias, aliasPassword), certPassword) = c

        val ks = KeyStore.getInstance(KeyStore.getDefaultType());
        val password = certPassword.toCharArray;
        val fis = new java.io.FileInputStream(path);
        ks.load(fis, password);
        val cert = ks.getCertificate(alias);
        fis.close();

        val pkEntry = ks.getEntry(alias, new KeyStore.PasswordProtection(aliasPassword.toCharArray));
        val privateKey = pkEntry.asInstanceOf[KeyStore.PrivateKeyEntry].getPrivateKey();

        val f = new ApkBuilder(outApkFile, res, dex, privateKey.asInstanceOf[PrivateKey], cert.asInstanceOf[X509Certificate], scala.Console.out)
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
            android.task.generateRFile(mPackage, aPath, mPath, resPath, jPath, javaPath, log)
        },

        android.task.aidl in android.key in c <<= (sourceDirectories, android.sdk.aidl in android.Android, sourceManaged in android.androidHome, javaSource, streams) map {
          (sDirs, idPath, javaPath, jSource, s) =>
            android.task.generateAIDLFile(sDirs, idPath, javaPath, jSource, s.log)
        },

        android.task.dx in android.key in c <<= (scalaInstance, android.sdk.dx in android.Android, classDirectory, fullClasspath in c, target, streams) map {
          (scalaInstance, dxPath, classDirectory, fc, classesDexPath, s) =>
            android.task.dxTask(scalaInstance, dxPath, classDirectory, fc, classesDexPath / "classes.dex", s.log)
        },
        android.task.dx in android.key in c <<= android.task.dx in android.key in c dependsOn (compile in c),


        android.task.resApk in android.key in c <<=
          (android.sdk.aapt in android.Android, android.project.manifest in android.androidHome, android.project.res in android.androidHome,
            android.project.assets in android.androidHome, android.project.androidJar in android.androidHome, android.project.resApk in android.androidHome, streams) map {
            (apPath, manPath, rPath, assetPath, jPath, resApkPath, s) =>
              android.task.aaptPackageTask(apPath, manPath, rPath, assetPath, jPath, resApkPath, s)
          },

        android.project.packageApk in android.release.debug in c <<=
          (android.project.apk in android.androidHome, android.task.resApk in android.key, android.task.dx in android.key in c, android.release.debug, streams) map {
            (outApkFile, res, dex, ce, log) =>
              android.task.apkBuilder(outApkFile, res, dex)(ce)
          },

        //android.project.packageApk in android.key in c <<= android.project.packageApk in android.key in c dependsOn (android.task.dx in android.key in c),
        sourceGenerators in c <+= (android.task.aapt in android.key in c).task,
        sourceGenerators in c <+= (android.task.aidl in android.key in c).task,

        android.project.packageApk in android.release.debug in c <<=
          android.project.packageApk in android.release.debug in c dependsOn(compile in c, android.task.dx in android.key in c),

        android.project.packageApk in c <<= (android.project.packageApk in android.release.debug in c),
        Keys.`package` in c <<= (android.project.packageApk in c)

      )

  )

  lazy val defaultSettings: Seq[Setting[_]] = androidSettingsIn(Compile)


  object AdbSettings {

    import AndroidKeys._

    lazy val settings: Seq[Setting[_]] = Seq(
      deviceManager := new DeviceManager,

      deviceStore <<= deviceManager(new DeviceStore(_)),

      deviceFilter in install := {
        d: Device =>
          true
      },

      install <<= (deviceStore, deviceFilter in install, android.project.packageApk in sbt.IntegrationTest) map {
        (store, filter, apk) =>
          store.devices.filter(filter).foreach(_.install(apk))
      },

      deviceFilter in instrument := {
        d: Device =>
          true
      },

      instrument <<= (deviceStore, deviceFilter in instrument, android.project.packageName in sbt.IntegrationTest, streams) map {
        (store, filter, pname, s) =>
          store.devices.filter(filter).foreach(_.instrument(pname)(s.log))
      }

    )

  }

  object Robolectric {
    lazy val settings: Seq[Setting[_]] = Seq(
      libraryDependencies += "com.pivotallabs" % "robolectric" % "1.0" % "test",
      libraryDependencies += "com.novocode" % "junit-interface" % "0.7" % "test->default"
    )
  }

  // Some defaults values common in most project
  object Defaults {

    lazy val settings: Seq[Setting[_]] = Seq(
      android.project.manifest <<= sourceDirectory(_ / "AndroidManifest.xml"),
      android.project.res <<= sourceDirectory(_ / "res"),
      android.project.assets <<= sourceDirectory(_ / "assets"),
      android.project.resApk <<= target(_ / "resources.apk"),
      android.project.apk <<= target(_ / "out.apk"),
      android.project.packageName <<= android.project.manifest(AndroidManifest(_).pkg),
      android.project.androidJar := file("/opt/android-sdk-linux_x86/platforms/android-10/android.jar"),
      android.release.debug :=(Path.userHome / ".android" / "debug.keystore", ("androiddebugkey", "android"), "android"),


      // disable .jar publishing
      publishArtifact in(Compile, packageBin) := false,

      // create an Artifact for publishing the .war file
      artifact in(Compile, android.project.packageApk in android.release.debug) ~= {
        (art: Artifact) =>
          art.copy(`type` = "apk", extension = "apk")
      },

      artifacts <<= (artifact, artifacts)(_ +: _),
      packagedArtifacts <<= (packagedArtifacts, artifact, android.project.packageApk in android.release.debug) map ((pas, a, file) => pas updated(a, file)),

      demo <<= InputTask(parser)(taskDef)
    )

    import complete.DefaultParsers._

    import sbt._

    val demo = InputKey[Unit]("demo")

    val helloTask2 = TaskKey[Unit]("hello", "Prints 'Hello World'") := {
      println("Hello World")
    }

    val parser: sbt.Project.Initialize[State => Parser[(String, String)]] =
      (scalaVersion, sbtVersion) {
        (scalaV: String, sbtV: String) =>
          (state: State) =>

            (token(Space ~> "scala" <~ Space) ~ token(scalaV)) |
              (token(Space ~> "sbt" <~ Space) ~ token(sbtV)) |
              (token(Space ~> "commands" <~ Space) ~
                token(state.remainingCommands.size.toString))

      }


    //    val color: Parser[String] = {
    //      (Space ~> "something") | (Space ~> "esle")
    //      List("hello", "world").map((Space) ~> _).reduceLeft(Space)(_.1 | _ .2)
    //    }

    val taskDef = (parsedTask: TaskKey[(String, String)]) => {
      // we are making a task, so use 'map'
      (parsedTask, packageBin) map {
        case ((tpe: String, value: String), pkg: File) =>
          println("Type: " + tpe)
          println("Value: " + value)
          println("Packaged: " + pkg.getAbsolutePath)
      }
    }
  }

  object adb {
    val globalSettings: Seq[Setting[_]] = Seq(
      onLoad in Global <<= onLoad in Global apply (_ andThen {
        state =>
        //sys.error("HELO WPR")
          state
      }
        ),
      onUnload in Global <<= onUnload in Global apply (_ andThen {
        state =>
          state
      }))
  }

}




