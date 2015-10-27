package info.rgomes.sbt.snippets

import sbt._


trait EvaluationSupport {

  protected def fail(errorMessage: String, state: State): Nothing = {
    state.log.error(errorMessage)
    throw new IllegalArgumentException()
  }

  protected def log(implicit state: State) = state.log

  // our version of http://stackoverflow.com/questions/25246920
  protected implicit class RichSettingKey[A](key: SettingKey[A]) {
    def gimme(implicit c: Context): A =
      gimme(c.pr, c.bs, c.state)
    def gimme(pr: ProjectRef, bs: BuildStructure, state: State): A =
      gimmeOpt(pr, bs) getOrElse { fail(s"Missing setting: ${key.key.label}", state) }
    def gimmeOpt(implicit c: Context): Option[A] =
      gimmeOpt(c.pr, c.bs)
    def gimmeOpt(pr: ProjectRef, bs: BuildStructure): Option[A] =
      key in pr get bs.data
  }

  protected implicit class RichTaskKey[A](key: TaskKey[A]) {
    def run(implicit c: Context): A =
      run(c.pr, c.bs, c.state)
    def run(pr: ProjectRef, bs: BuildStructure, state: State): A =
      runOpt(pr, bs, state).getOrElse { fail(s"Missing task key: ${key.key.label}", state) }
    def runOpt(implicit c: Context): Option[A] =
      runOpt(c.pr, c.bs, c.state)
    def runOpt(pr: ProjectRef, bs: BuildStructure, state: State): Option[A] =
      EvaluateTask(bs, key, state, pr).map(_._2) match {
        case Some(Value(v)) => Some(v)
        case _              => None
      }
  }
}
