package saab.xsbt


class w {

  //  def f[T]: (ProcessBuilder => T) = {
  //    p: ProcessBuilder =>
  //      new T
  //  }

  //val h = ProcessTask[String]("fefe", "fefwe")

  class ProcessTask[T, R](s: => sys.process.ProcessBuilder, d: sys.process.ProcessBuilder => T => R) {
    def run: R = {
      println(s.toString)
      d(s).andThen(i)
      //((s !).asInstanceOf[T])
    }
   // http://daily-scala.blogspot.com/2010/02/type-alias-for-partialfunction.html
    val i: (ProcessBuilder => Int) = {
      case x: Int => println("int found"); x
    }

  }

  object ProcessTask {
    def apply[T, R](s: => sys.process.ProcessBuilder)(d: sys.process.ProcessBuilder => T => R) = new ProcessTask[T, R](s, d)
  }

  import sys.process._

  val a = ProcessTask[Int, String]("ls" :: "-l" :: Nil) {
    d: ProcessBuilder => {
      i: Int =>
        i match {
          case 1 => "go"
          case _ => "no go"
        }
    }
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