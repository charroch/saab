package saab.xsbt

import scala.Function1


class w {

  class P[T](implicit val manifest: scala.reflect.Manifest[T])

  class ProcessTask2[T, R](val m: scala.reflect.Manifest[T])

  object ProcessTask2 {
    val intType = manifest[Int]
    val stringType = manifest[String]

    def apply[T, R](implicit m: scala.reflect.Manifest[T]) = {
      m.toString match {
        case "java.lang.String" => println("hello") ;
        case "Int" => println("sting")
        case _ => println("else")
      }
      new ProcessTask2[T, R] (m)
    }
  }

  class ProcessTask[T, R](s: => sys.process.ProcessBuilder, d: T => R)(implicit m: scala.reflect.Manifest[T]) {

    import sys.process._

    def run: R = {
      println(s.toString)
      m match {
        case process => d(s.run.asInstanceOf[T])
        case stream  => d(s.lines_!.asInstanceOf[T])
        case int  => d(s.!.asInstanceOf[T])
        case string => d(s.!!.asInstanceOf[T])
      }
    }

    val process = manifest[Process]
    val stream = manifest[Stream[String]]
    val int = manifest[Int]
    val string = manifest[String]
  }

  object ProcessTask {
    val intType = manifest[Int]
    val stringType = manifest[String]

    def apply[T, R](s: => sys.process.ProcessBuilder)(d: T => R)(implicit m: scala.reflect.Manifest[T]) = {
      m.erasure.toString match {
        case "java.lang.String" => println("hello") ;
        case "Int" => println("sting")
        case _ => println("else")
      }
      new ProcessTask[T, R](s, d)
    }
  }

  val a = ProcessTask[Int, String]("ls" :: "-l" :: Nil) {
    i: Int =>
      i match {
        case 1 => "go"
        case _ => "no go"
      }
  }

  val b = ProcessTask[String, String]("ls" :: "-l" :: Nil) {
    i: String =>
      i match {
        case "d" => "go"
        case "nogo" => "no go"
      }
  }

  trait p

  case class s2s(f: String => String)

  case class i2s(f: Int => String)

  List({
    i: Int => "i2s"
  }, {
    i: String => "s2s"
  }).foreach(_ match {

    case i2s => println("i2s")
    case s2s => println("s2s")
  })

  import scala.reflect._

  def matchFunction[T, R](f: Function1[T, R], t: T)(implicit mt: Manifest[T], mr: Manifest[R]) = {
    val result = if ((mt.erasure, mr.erasure) ==(classOf[Any], classOf[Boolean])) "any, boolean " + f(t)
    else if ((mt.erasure, mr.erasure) ==(classOf[Int], classOf[Int])) "int, int " + f(t)
    else "Unknown " + f(t)
    println(result)
  }


  //
  //  h <<= ( 1package in Compile) map {
  //    (s) => ProcessTask[String]("ls", "-l") {
  //      d: ProcessBuilder =>
  //        s
  //    }
  //  }
}


class My {
  def s[P <: (P => Process), R, T](block: P => R => T) = {
  }
}