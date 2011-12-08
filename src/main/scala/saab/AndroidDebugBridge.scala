package saab

import com.android.ddmlib.{IShellOutputReceiver, AndroidDebugBridge => JADB, IDevice}
import sys.process.ProcessBuilder


class AndroidDebugBridge {
}

class SbtLogger(l: sbt.Logger) extends IShellOutputReceiver {
  def addOutput(data: Array[Byte], offset: Int, length: Int) {
    l.info(new String(data))
  }

  def flush() {}

  def isCancelled = false
}

class Device(device: IDevice) {
  def install(apk: java.io.File) {
    device.installPackage(apk.getAbsolutePath, true)
  }

  def instrument(p: String, runner: String = "android.test.InstrumentationTestRunner")(log: sbt.Logger) {
    log.info("[Shell]: am instrument -w %s/%s" format(p, runner))
    device.executeShellCommand("am instrument -w %s/%s" format(p, runner), new SbtLogger(log))
  }

  def <<(command: String): ProcessBuilder = {
    "adb -s %s %s" format(device.getSerialNumber, command)
  }

}


object Device {
  implicit def toRichDevice(d: IDevice): Device = new Device(d)
}

class DeviceManager {
  JADB.init(false /* debugger support */);
  val bridge = JADB.createBridge("/opt/android-sdk-linux_x86//platform-tools/adb", false)
  //val bridge = JADB.createBridge
}

class DeviceStore(dm: DeviceManager) {

  def devices: Seq[Device] = {
    dm.bridge.getDevices.map(new Device(_))
  }
}

object AdbTasks {

  type action[T] = (Device => T)

  def installTask(apk: java.io.File, ds: DeviceStore, deviceFilter: (Device => Boolean)) = {
    ds
  }

  def instrumentTask() = {

  }
}