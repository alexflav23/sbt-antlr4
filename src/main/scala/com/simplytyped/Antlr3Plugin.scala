package com.simplytyped

import sbt.{Def, _}
import Keys._
import sbt.internal.io.Source

import scala.sys.process.Process

object antlr3Plugin extends AutoPlugin {
  object autoImport {
    val Antlr3 = config("antlr3")
    val antlr3Version = settingKey[String]("Version of antlr3")
    val antlr3Generate = taskKey[Seq[File]]("Generate classes from antlr3 grammars")
    val antlr3RuntimeDependency = settingKey[ModuleID]("Library dependency for antlr3 runtime")
    val antlr3Dependency = settingKey[ModuleID]("Build dependency required for parsing grammars")
    val antlr3PackageName = settingKey[Option[String]]("Name of the package for generated classes")
    val antlr3GenListener = settingKey[Boolean]("Generate listener")
    val antlr3GenVisitor = settingKey[Boolean]("Generate visitor")
    val antlr3TreatWarningsAsErrors = settingKey[Boolean]("Treat warnings as errors when generating parser")
  }
  import autoImport._

  private val antlr3BuildDependency = settingKey[ModuleID]("Build dependency required for parsing grammars, scoped to plugin")

  def antlr3GeneratorTask : Def.Initialize[Task[Seq[File]]] = Def.task {
    val targetBaseDir = (javaSource in Antlr3).value
    val classpath = (managedClasspath in Antlr3).value.files
    val log = streams.value.log
    val packageName = (antlr3PackageName in Antlr3).value
    val listenerOpt = (antlr3GenListener in Antlr3).value
    val visitorOpt = (antlr3GenVisitor in Antlr3).value
    val warningsAsErrorOpt = (antlr3TreatWarningsAsErrors in Antlr3).value
    val cachedCompile = FileFunction.cached(streams.value.cacheDirectory / "antlr3", FilesInfo.lastModified, FilesInfo.exists) {
      in : Set[File] =>
        runAntlr(
          srcFiles = in,
          targetBaseDir = targetBaseDir,
          classpath = classpath,
          log = log,
          packageName = packageName,
          listenerOpt = listenerOpt,
          visitorOpt = visitorOpt,
          warningsAsErrorOpt = warningsAsErrorOpt
        )
    }
    cachedCompile(((sourceDirectory in Antlr3).value ** "*.g4").get.toSet).toSeq
  }

  def runAntlr(
      srcFiles: Set[File],
      targetBaseDir: File,
      classpath: Seq[File],
      log: Logger,
      packageName: Option[String],
      listenerOpt: Boolean,
      visitorOpt: Boolean,
      warningsAsErrorOpt: Boolean
  ): Set[File] = {
    val targetDir = packageName.map{_.split('.').foldLeft(targetBaseDir){_/_}}.getOrElse(targetBaseDir)
    val baseArgs = Seq("-cp", Path.makeString(classpath), "org.antlr.Tool", "-o", targetDir.toString)
    val packageArgs = packageName.toSeq.flatMap{p => Seq("-package",p)}
    val listenerArgs = if(listenerOpt) Seq("-listener") else Seq("-no-listener")
    val visitorArgs = if(visitorOpt) Seq("-visitor") else Seq("-no-visitor")
    val warningAsErrorArgs = if (warningsAsErrorOpt) Seq("-Werror") else Seq.empty
    val sourceArgs = srcFiles.map{_.toString}
    val args = baseArgs ++ packageArgs ++ listenerArgs ++ visitorArgs ++ warningAsErrorArgs ++ sourceArgs
    val exitCode = Process("java", args) ! log
    if(exitCode != 0) sys.error(s"Antlr3 failed with exit code $exitCode")
    (targetDir ** "*.java").get.toSet
  }

  override def projectSettings: Seq[Def.Setting[_]] = inConfig(Antlr3)(Seq(
    sourceDirectory := (sourceDirectory in Compile).value / "antlr3",
    javaSource := (sourceManaged in Compile).value / "antlr3",
    managedClasspath := Classpaths.managedJars(configuration.value, classpathTypes.value, update.value),
    antlr3Version := "3.5.2",
    antlr3Generate := antlr3GeneratorTask.value,
    antlr3Dependency := "org.antlr" % "antlr" % antlr3Version.value,
    antlr3RuntimeDependency := "org.antlr" % "antlr-runtime" % antlr3Version.value,
    antlr3BuildDependency := antlr3Dependency.value % Antlr3.name,
    antlr3PackageName := None,
    antlr3GenListener := true,
    antlr3GenVisitor := false,
    antlr3TreatWarningsAsErrors := false
  )) ++ Seq(
    ivyConfigurations += Antlr3,
    managedSourceDirectories in Compile += (javaSource in Antlr3).value,
    sourceGenerators in Compile += (antlr3Generate in Antlr3).taskValue,
    watchSources += new Source(sourceDirectory.value, "*.g", HiddenFileFilter),
    cleanFiles += (javaSource in Antlr3).value,
    libraryDependencies += (antlr3BuildDependency in Antlr3).value,
    libraryDependencies += (antlr3RuntimeDependency in Antlr3).value
  )
}
