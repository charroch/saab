// Set the project name to the string 'My Project'
name := "My Project"

// The := method used in Name and Version is one of two fundamental methods.
// The other method is <<=
// All other initialization methods are implemented in terms of these.
version := "1.0"

// Add a single dependency
libraryDependencies += "junit" % "junit" % "4.8" % "test"

// Add multiple dependencies
libraryDependencies ++= Seq(
	"net.databinder" %% "dispatch-google" % "0.7.8",
	"net.databinder" %% "dispatch-meetup" % "0.7.8"	
)

// Exclude backup files by default.  This uses ~=, which accepts a function of
//  type T => T (here T = FileFilter) that is applied to the existing value.
// A similar idea is overriding a member and applying a function to the super value:
//  override lazy val defaultExcludes = f(super.defaultExcludes)
//
defaultExcludes ~= (filter => filter | "*~")
/*  Some equivalent ways of writing this:
defaultExcludes ~= (_ | "*~")
defaultExcludes ~= ( (_: FileFilter) | "*~")
defaultExcludes ~= ( (filter: FileFilter) => filter | "*~")
*/

// Use the name and version to define the jar name.
jarName <<= (name, version) {
	(name: String, version: String) => name + "-" + version + ".jar"
}
/* These are equivalent:
jarName <<= (name, version)( (n,v) => n + "-" + v + ".jar")
jarName <<= (name, version)( _ + "-" + _ + ".jar")
*/