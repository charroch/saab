libraryDependencies <++= (sbtVersion) {
  sv => Seq(
    "org.scala-tools.sbt" %% "scripted-plugin" % sv,
    "org.scala-tools.sbt" %% "scripted-sbt" % sv)
}
