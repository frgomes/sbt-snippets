package info.rgomes.sbt.snippets

import java.io.File


trait RunnerSnippets {

  /** Runs an application on the operating system */
  def osRunner(app : String,
               args: Seq[String],
               cwd : Option[File] = None,
               env : Map[String, String] = Map.empty,
               //TODO: Option[Pipe.SourceChannel] //stdin
               //TODO: Option[Pipe.SinkChannel]   //stdout
               //TODO: Option[Pipe.SinkChannel]   //stderr
               //TODO: Option[Pipe.SinkChannel]   //log
               fork: Boolean = false): Int = {
    import scala.collection.JavaConverters._

    val cmd: Seq[String] = app +: args
    val pb = new java.lang.ProcessBuilder(cmd.asJava)
    if (cwd.isDefined) pb.directory(cwd.get)

    pb.redirectInput()

    pb.inheritIO
    //TODO: set environment
    val process = pb.start()
    if(fork) 0 else {
      def cancel() = {
        //TODO: if(log.isDefined) log.get.warn("Run canceled.")
        process.destroy()
        15
      }
      try process.waitFor catch { case e: InterruptedException => cancel() }
    }
  }

  /** Runs or forks a Java application with fine-grained arguments */
  def javaRunner(args: Seq[String],
                 classpath: Option[Seq[File]] = None,
                 mainClass: Option[String] = None,
                 javaTool : Option[String] = None,
                 fork : Boolean = false,
                 cwd: Option[File] = None,
                 env: Map[String, String] = Map.empty,
                 //TODO: Option[Pipe.SourceChannel] //stdin
                 //TODO: Option[Pipe.SinkChannel]   //stdout
                 //TODO: Option[Pipe.SinkChannel]   //stderr
                 //TODO: Option[Pipe.SinkChannel]   //log
                 jvmOptions: Seq[String] = Nil,
                 javaHome  : Option[File] = None): Unit = {

    val app : String      = javaHome.fold("") { p => p.getAbsolutePath + "/bin/" } + javaTool.getOrElse("java")
    val jvm : Seq[String] = jvmOptions.map(p => p.toString)
    val cp  : Seq[String] =
      classpath
        .fold(Seq.empty[String]) { paths =>
          Seq(
            "-cp",
            paths
              .map(p => p.getAbsolutePath)
              .mkString(java.io.File.pathSeparator))
        }
    val klass = mainClass.fold(Seq.empty[String]) { name => Seq(name) }
    val xargs : Seq[String] = jvm ++ cp ++ klass ++ args

    //TODO: if(log.isDefined) {
    //TODO:   log.get.info(if(fork) "Forking" else "Running")
    //TODO:   log.get.info(s"${app} " + xargs.mkString(" "))
    //TODO: }

    if (cwd.isDefined) cwd.get.mkdirs
    val errno = osRunner(app, xargs, cwd, env, fork)
    if(errno!=0) throw new IllegalStateException(s"errno = ${errno}")
  }
}
