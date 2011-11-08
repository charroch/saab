import org.specs2._
import saab.parser.AndroidParser

class AndroidParserTest extends Specification {
  // install on (HTCE2132132, ergreg)
  // install on emulators
  // install on devices
  // install on all
  // install on HT*
  // install on all where ro.name.manufacturer is | startsWith | etc... "Sony Ericsson" and

  // val task = TaskKey("blu") = on all {
  //    install ...
  //    run ...
  //    instrument ...
  // }


   // "on all" should apply on a Seq[IDevice] the PartialFunction
  //

  // run <activity or main or etc..> on ...
  // instrument <package> (optional with "uk.co.economist.customInstrumentation") on ...
  // e.g. instrument uk.co.novoda with uk.co.novoda.CustomrInstrumentation on ...

                                           /*
  la> val f = (i: Int) => i + 1
f: (Int) => Int = <function>

scala> val g = (i: Int) => i * 2
g: (Int) => Int = <function>


scala> (f andThen g)(2)
res15: Int = 6

                        (Device => Device)

                        */

  // shell "ls -lef"! on emulators
  def is =
    "DSL for android" ^
      p ^
      "The selection of devices should" ^
      "accept the string 'install on all' " ! e1 ^
      end

  def e1 = AndroidParser.parse("""install "test.apk" on all""").successful should beTrue
}