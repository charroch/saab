sbtPlugin := true
name := "saab"
organization := "novoda"
publishMavenStyle := true
publishTo := Some(Resolver.file("Local", Path.userHome / "projects" / "novoda.github.com" / "maven2" asFile)(Patterns(true, Resolver.mavenStyleBasePattern)))
