package saab.util

trait ProcessArgument
case class DoubleDash(v:String) extends ProcessArgument
case class DoubleDashKV(k:String, v:String) extends ProcessArgument
case class SingleDash(k:String, v:String) extends ProcessArgument
case class NoDash(v:String) extends ProcessArgument

