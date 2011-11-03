import _root_.saab.AndroidManifest
import org.specs2.mutable._

class ManifestSpecs extends Specification {

  "The AndroidManifest XML" should {
    "get the correct package name" in {
      val xml = <manifest package="com.test" xmlns:android="http://schemas.android.com/apk/res/android"/>
      new AndroidManifest(xml).pkg must be_==("com.test")
    }

    "get the correct version code and name" in {
      val xml = <manifest android:versionCode="3" android:versionName="0.0.3-SNAPSHOT" xmlns:android="http://schemas.android.com/apk/res/android"/>
      new AndroidManifest(xml).version must be_==("0.0.3-SNAPSHOT", 3)
    }

    "get the correct sdk version" in {
      val xml = <manifest xmlns:android="http://schemas.android.com/apk/res/android">
          <uses-sdk android:minSdkVersion="9"
                    android:targetSdkVersion="10"
                    android:maxSdkVersion="11"/>
      </manifest>
      new AndroidManifest(xml).sdkVersions must be_==(Some(9), Some(10), Some(11))
    }

    "get the correct sdk version" in {
      val xml = <manifest xmlns:android="http://schemas.android.com/apk/res/android">
          <uses-sdk android:minSdkVersion="9"
                    android:targetSdkVersion="10"/>
      </manifest>
      new AndroidManifest(xml).sdkVersions must be_==(Some(9), Some(10), None)
    }

    "get no sdk version" in {
      val xml = <manifest xmlns:android="http://schemas.android.com/apk/res/android"></manifest>
      new AndroidManifest(xml).sdkVersions must be_==(None, None, None)
    }
  }

}