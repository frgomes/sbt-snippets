package info.rgomes.sbt.snippets

import sbt._


trait Runners {

  /** Runs an application on the operating system */
  def osRunner(app : String,
             args: Seq[String],
             cwd : Option[File] = None,
             env : Map[String, String] = Map.empty,
             log : Option[Logger],
             fork: Boolean = false): Int = {
    import scala.collection.JavaConverters._

    val cmd: Seq[String] = app +: args
    val pb = new java.lang.ProcessBuilder(cmd.asJava)
    if (cwd.isDefined) pb.directory(cwd.get)
    pb.inheritIO
    //TODO: set environment
    val process = pb.start()
    if(fork) 0 else {
      def cancel() = {
        if(log.isDefined) log.get.warn("Run canceled.")
        process.destroy()
        15
      }
      try process.waitFor catch { case e: InterruptedException => cancel() }
    }
  }

  /** Fork a Java application with ``ForkOptions`` */
  def javaRunner(args: Seq[String],
                 classpath: Option[Seq[File]],
                 mainClass: Option[String],
                 log: Option[Logger],
                 o: ForkOptions): Unit =
    javaRunner(
      args,
      classpath, mainClass, Some("java"), log, true,
      o.workingDirectory, o.envVars, 
      o.runJVMOptions, o.javaHome,
      o.connectInput, o.outputStrategy)


  /** Runs or forks a Java application with fine-grained arguments */
  def javaRunner(args: Seq[String],
                 classpath: Option[Seq[File]] = None,
                 mainClass: Option[String] = None,
                 javaTool : Option[String] = None,
                 log  : Option[Logger] = None,
                 fork : Boolean = false,
                 cwd: Option[File] = None,
                 env: Map[String, String] = Map.empty,
                 jvmOptions: Seq[String] = Nil,
                 javaHome  : Option[File] = None,
                 connectInput  : Boolean = false,
                 outputStrategy: Option[OutputStrategy] = Some(StdoutOutput)): Unit = {

    val app : String      = javaHome.fold("") { p => p.absolutePath + "/bin/" } + javaTool.getOrElse("java")
    val jvm : Seq[String] = jvmOptions.map(p => p.toString)
    val cp  : Seq[String] =
      classpath
        .fold(Seq.empty[String]) { paths =>
          Seq(
            "-cp",
            paths
              .map(p => p.absolutePath)
              .mkString(java.io.File.pathSeparator))
        }
    val klass = mainClass.fold(Seq.empty[String]) { name => Seq(name) }
    val xargs : Seq[String] = jvm ++ cp ++ klass ++ args

    if(log.isDefined) {
      log.get.info(if(fork) "Forking" else "Running")
      log.get.info(s"${app} " + xargs.mkString(" "))
    }

    if (cwd.isDefined) IO.createDirectory(cwd.get)
    val errno = osRunner(app, xargs, cwd, env, log, fork)
    if(errno!=0) throw new IllegalStateException(s"errno = ${errno}")
  }
}
