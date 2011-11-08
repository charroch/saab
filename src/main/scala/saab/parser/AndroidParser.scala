package saab.parser

import util.parsing.combinator.syntactical.StandardTokenParsers
import java.io.File
import com.android.ddmlib.AndroidDebugBridge.IDeviceChangeListener
import com.android.ddmlib.{IShellOutputReceiver, IDevice, AndroidDebugBridge}

object AndroidParser extends StandardTokenParsers {
  lexical.reserved +=
    ("install", "on", "all", "emulators", "devices")

  lexical.delimiters +=("(", ")", ",")

  lazy val actions: Parser[SP] =
    install ~ devices ^^ {
      case v ~ d => {
        new SP(v, d)
      }
    }

  lazy val verbs: Parser[Command] =
    (install | "run" | "instrument" | "uninstall") ^^ {
      case "run" => run
      case "instrument" => instrument
      case "uninstall" => uninstall
    }

  lazy val install: Parser[Command] =
    "install" ~> stringLit ^^ {
      case apkLocation => new Install(new File(apkLocation))
    }

  lazy val devices: Parser[Seq[IDevice]] =
    "on" ~> ("all" | "emulators" | "devices") ^^ {
      case "all" => {
        DeviceManager().bridge.getDevices
      }
    }

  def parse(s: String) = {
    val tokens = new lexical.Scanner(s)
    phrase(actions)(tokens)
  }
}

class SP(i: Command, d: Seq[IDevice]) {


  def execute {

    i match {
      case Install(apk, reinstall) => {
        println("installing on " + d)
        d.foreach(_.installPackage(apk.getAbsolutePath, reinstall))
      }
    }
  }

  override def toString = "Command " + i + " and s " + d
}

trait Command {
  def on(devices: IDevice*) = {
    //devices.forall()
  }
}

case class Install(apk: File, reinstall: Boolean = true) extends Command

case object run extends Command

case object instrument extends Command

case object uninstall extends Command

case object broadcast extends Command


class Device(d: IDevice) {
  implicit def toRichDevice(d: IDevice) = new Device(d)

  def isRooted: Boolean = {
    d.executeShellCommand("which su", new IShellOutputReceiver {
      def addOutput(p1: Array[Byte], p2: Int, p3: Int) {}

      def flush() {}

      def isCancelled = false
    })
    true

  }
}

class DeviceStore {
}

class DeviceManager(adb: String) {
  lazy val bridge: AndroidDebugBridge = {
    AndroidDebugBridge.init(false)
    AndroidDebugBridge.createBridge(adb, true)
  }

  def register {
    AndroidDebugBridge.addDebugBridgeChangeListener(new AndroidDebugBridge.IDebugBridgeChangeListener {
      def bridgeChanged(b: AndroidDebugBridge) {

      }
    })

    AndroidDebugBridge.addDeviceChangeListener(new IDeviceChangeListener() {
      def deviceConnected(p1: IDevice) {}

      def deviceDisconnected(p1: IDevice) {}

      def deviceChanged(p1: IDevice, p2: Int) {}
    })

    AndroidDebugBridge.addDebugBridgeChangeListener(new AndroidDebugBridge.IDebugBridgeChangeListener {
      def bridgeChanged(b: AndroidDebugBridge) {

      }
    })
  }
}

object DeviceManager {

  def apply() = {
    val adb = System.getenv("ANDROID_HOME") + "platform-tools/adb"
    new DeviceManager(adb)
  }
}

object implicits {
  implicit def toRichFile(s: String): File = new File(s)
}

class something {

  import com.android.ddmlib._

  def process(device: IDevice) = {
    println("Uninstalling uk.co.economist on " + device.getSerialNumber)
    device.uninstallPackage("uk.co.economist")
    println("Installing /tmp/eco-debug-auto-download-2.apk on " + device.getSerialNumber)
    device.installPackage("/tmp/eco-debug-auto-download-2.apk", true)
    println("Starting activity on " + device.getSerialNumber)
    device.executeShellCommand("am start -a android.intent.action.MAIN -n uk.co.economist/uk.co.economist.activity.Splash", new NullOutputReceiver)
  }

  AndroidDebugBridge.init(false /* debugger support */);
  AndroidDebugBridge.createBridge
  val bridge = AndroidDebugBridge.createBridge
  Thread.sleep(5000)

  val a = bridge.getDevices
  a.foreach(process)
  println("Batched worked against %i devices " format (bridge.getDevices.size))
  AndroidDebugBridge.terminate
}