package info.rgomes.sbt.snippets

import sbt._


case class Context(_state: State) {
  val state: State = _state
  val xt: Extracted = Project.extract(state)
  val bs: BuildStructure = xt.structure

  /** The project root */
  val pr: ProjectRef = ProjectRef(bs.root, bs.rootProject(bs.root))
}