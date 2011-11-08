package saab.parser

import com.android.ddmlib.IDevice

class Device2(d: IDevice) {

  type Uri = String
  type Action = String
  type Data = String
  type MimeType = String
  type Categories = Seq[String]
  type Extras = Map[String, Any]
  type Component = (String, String)
  type Flags = Seq[String]
  type Intent = (Uri, Action, Data, MimeType, Categories, Extras, Component, Flags)

  def start(i: Intent) {
  }

  def broadcast(i: Intent) {
  }

  def instrument() {
  }
}

object Device2 {
  implicit def toRichDevice(d: IDevice) = new Device2(d);
}