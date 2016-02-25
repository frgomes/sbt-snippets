package info.rgomes.sbt.snippets

import java.io.File


trait RunnerSnippets {

  /** Runs an application on the operating system */
  def osRunner(app: String,
               args: Seq[String],
               cwd: Option[File] = None,
               env: Map[String, String] = Map.empty,
               //TODO: Option[Pipe.SourceChannel] //stdin
               //TODO: Option[Pipe.SinkChannel]   //stdout
               //TODO: Option[Pipe.SinkChannel]   //stderr
               log: Option[LoggerAdapter] = None,
               fork: Boolean = false): Int = {
    import scala.collection.JavaConverters._

    // command to be executed
    val cmd: Seq[String] = app +: args
    val pb = new java.lang.ProcessBuilder(cmd.asJava)
    if (cwd.isDefined) pb.directory(cwd.get)
    // populate environment
    env.foreach { case (k,v) => pb.environment.put(k, v) }
    //FIXME: should optionally wire stdin, stdout, stderr instead of inheriting IO
    pb.inheritIO
    // run command optionally on a separate process
    if(log.isDefined) log.get.debug(s"""${app} ${args.mkString(" ")}""")
    val process = pb.start()
    val errno =
      if (fork) 0
      else {
        def cancel() = {
          if (log.isDefined) log.get.warn("osRunner: Run canceled.")
          process.destroy()
          15
        }
        try process.waitFor catch {
          case e: InterruptedException => cancel()
        }
      }
    if(log.isDefined) log.get.debug(errno.toString)
    errno
  }


  def javaClasspath(classpath: Option[Seq[File]] = None): Seq[String] =
    classpath
      .fold(Seq.empty[String]) {
        case paths =>
          Seq(
            "-cp",
            paths
              .map(p => p.getAbsolutePath)
              .mkString(java.io.File.pathSeparator))
      }


  /** Runs or forks a Java application with fine-grained arguments */
  def javaRunner(classpath: Option[Seq[File]] = None,
                 runJVMOptions: Seq[String] = Nil,
                 mainClass: Option[String] = None,
                 args: Seq[String],
                 cwd: Option[File] = None,
                 javaHome: Option[File] = None,
                 javaTool: Option[String] = None,
                 envVars: Map[String, String] = Map.empty,
                 //TODO: Option[Pipe.SourceChannel] //stdin
                 //TODO: Option[Pipe.SinkChannel]   //stdout
                 //TODO: Option[Pipe.SinkChannel]   //stderr
                 log: Option[LoggerAdapter] = None,
                 fork: Boolean = false): Unit = {

    val app: String = javaHome.fold("") { p => p.getAbsolutePath + "/bin/" } + javaTool.getOrElse("java")
    val jvm: Seq[String] = runJVMOptions.map(p => p.toString)
    val cp: Seq[String] = javaClasspath(classpath)
    val klass = mainClass.fold(Seq.empty[String]) { name => Seq(name) }
    val xargs: Seq[String] = jvm ++ cp ++ klass ++ args

    if (cwd.isDefined)
      if(!cwd.get.mkdirs)
        throw new RuntimeException(s"""Could not create directory "${cwd.get}""")
    val errno = osRunner(app, xargs, cwd, envVars, log, fork)
    if (errno != 0) throw new IllegalStateException(s"""Application "${app}" returned errno = ${errno}""")
  }

}
