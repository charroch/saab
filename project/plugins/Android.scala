import sbt._
import Keys._

object AndroidPlugin extends Plugin {

  override lazy val settings = Seq(commands += myCommand)

  lazy val myCommand =
    Command.command("hello") { (state: State) ⇒
      println("Hi!")
      state
    }
  
  lazy val dx =
	  Command.command("dx", f)

  // configuration points, like the built in `version`, `libraryDependencies`, or `compile`
  // by implementing Plugin, these are automatically imported in a user's `build.sbt`
  val newTask = TaskKey[Unit]("new-task")
  val newSetting = SettingKey[String]("new-setting")

  // a group of settings ready to be added to a Project
  // to automatically add them, do 
  val newSettings = Seq(
    newSetting := "test",
    newTask <<= newSetting map { str ⇒ println(str) })

  lazy val grizzled = uri("git://github.com/bmc/grizzled-scala.git")

  // alternatively, by overriding `settings`, they could be automatically added to a Project
  // override val settings = Seq(...)
}