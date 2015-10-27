package info.rgomes.sbt.snippets


trait ClasspathSnippets {

  import java.io.File
  import java.net.URL

  type Location = Function2[String, String, Set[String]]


  /**
   * Function factory which returns a function which accepts a ``module`` and a ``task``
   * so that it returns the location where compiled target classes can be found.
   */
  def targetLocation(scalav: String, target: String, updir: String = ""): Location = {
    case (module: String, task: String) =>
      Set(s"${updir}${module}/${target}/scala-${scalav}/${task}")
  }

  /**
   * Function factory which returns a function which accepts a ``module`` and a ``task``
   * so that it returns the location where SBT cached information about classpaths.
   */
  def cachedLocation(scalav: String, target: String, updir: String = ""): Location = {
    case (module: String, task: String) =>
      val file = new java.io.FileInputStream(s"${updir}${module}/${target}/streams/${task}/$$global/streams/export")
      scala.io.Source.fromInputStream(file, "UTF-8").getLines.mkString.split(":").toSet
  }

  /**
   * Builds a CLASSPATH from cached information generated from SBT.
   * <p/>
   * This artifice is particularly handy at development time, when you are not interested on
   * packaging and deploying binaries. All you need to do is simply `test:compile` and obtain the
   * CLASSPATH as the example below shows:
   *
   * {{{
   * val (target, updir) = ("target", "") // see: https://www.pivotaltracker.com/story/show/106446724
   * val scalav = "2.11"
   * val modules: Set[String] = Set("ModuleA", "ModuleB", "ModuleC")
   * val cp: Seq[URL] = makeClasspath(modules, scalav, target, updir) {
   * }}}
   */
  def makeClasspath(modules: Set[String],
                    scalav: String,
                    target: String,
                    updir: String = ""): Seq[URL] =
    makeClasspathPF(modules, scalav, target, updir)(Seq.empty[URL])

  /**
   * Builds a CLASSPATH from cached information generated from SBT and URLs passed by the user.
   * <p/>
   * This artifice is particularly handy at development time, when you are not interested on
   * packaging and deploying binaries. All you need to do is simply `test:compile` and obtain the
   * CLASSPATH as the example below shows:
   *
   * {{{
   * val (target, updir) = ("target", "") // see: https://www.pivotaltracker.com/story/show/106446724
   * val scalav = "2.11"
   * val modules: Set[String] = Set("ModuleA", "ModuleB", "ModuleC")
   * val cp: Seq[URL] = makeClasspathPF(modules, scalav, target, updir) {
   *   Seq(new java.net.URL("file:/tmp/fakeClasspath", new java.net.URL("http://example.com/my.jar")) }
   * }}}
   */
  def makeClasspathPF(modules: Set[String],
                      scalav: String,
                      target: String,
                      updir: String = "")
                     (custom: Seq[URL] = Seq.empty[URL]): Seq[URL] = {
    val resources = Set("classes", "test-classes")
    val classpath = Set("test/internalDependencyClasspath", "test/unmanagedClasspath", "test/managedClasspath")
    val internal : Seq[URL] = makeClasspathPF(modules, resources)(targetLocation(scalav, target, updir))
    val libraries: Seq[URL] = makeClasspathPF(modules, classpath)(cachedLocation(scalav, target, updir))
    custom ++ internal ++ libraries
  }

  /**
   * Builds a CLASSPATH from cached information generated from SBT and a
   * policy for building items of the CLASSPATH.
   * <p/>
   * This artifice is particularly handy at development time, when you are not interested on
   * packaging and deploying binaries. All you need to do is simply `test:compile` and obtain the
   * CLASSPATH as the example below shows:
   *
   * {{{
   * val (target, updir) = ("target", "") // see: https://www.pivotaltracker.com/story/show/106446724
   * val scalav = "2.11"
   * val modules: Set[String] = Set("ModuleA", "ModuleB", "ModuleC")
   * val tasks: Set[String] = Set("classes", "test-classes")
   * val cp: Seq[URL] = makeClasspathPF(modules, tasks)(cachedLocation(scalav, target, updir)
   * }}}
   */
  def makeClasspathPF(modules: Set[String], tasks: Set[String])
                     (location: Location): Seq[URL] = {
    tasks.map({
      task =>
        modules.map({
          module =>
            location(module, task)
        }).reduce(_ ++ _)
          .map(path =>
            if(path.indexOf(":") > -1)
              new URL(path)
            else
              new File(path).toURI.toURL)
          .toSeq
    }).reduce(_ ++ _)
  }

}
