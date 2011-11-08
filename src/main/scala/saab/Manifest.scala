package saab

import java.io.File
import xml.transform.{RewriteRule, RuleTransformer}
import xml.{Elem, Node, XML}

class AndroidManifest(manifest: scala.xml.Elem) {

  lazy val addons: Seq[String] = {
    Nil
  }

  lazy val version: (String, Int) = {
    ((manifest \ "@{http://schemas.android.com/apk/res/android}versionName").text,
      (manifest \ "@{http://schemas.android.com/apk/res/android}versionCode").text.toInt)
  }

  lazy val sdkVersions: (Option[Int], Option[Int], Option[Int]) = {
    (minSdk, targetSdk, maxSdk)
  }

  lazy val minSdk: Option[Int] = optionSdk("minSdkVersion")
  lazy val maxSdk: Option[Int] = optionSdk("maxSdkVersion")
  lazy val targetSdk: Option[Int] = optionSdk("targetSdkVersion")

  private def optionSdk(which: String): Option[Int] = {
    val m: scala.xml.NodeSeq = (manifest \ "uses-sdk" \ "@{http://schemas.android.com/apk/res/android}%s".format(which))
    if (m.isEmpty) None
    else Some(m.text.toInt)
  }

  lazy val pkg: String = {
    (manifest \ "@package").text
  }

  def upVersion(f: (String, Int) => (String, Int)) = {
//    new RuleTransformer(new RewriteRule {
//      override def transform(n: Node): Seq[Node] = n match {
//        case sn@Elem(_, "subnode", _, _, _*) => rt1(sn)
//        case other => other
//      }
//    })(manifest)
  }
}

object AndroidManifest {
  val NS = "http://schemas.android.com/apk/res/android"

  def apply(f: File) = new AndroidManifest(XML.loadFile(f))
}