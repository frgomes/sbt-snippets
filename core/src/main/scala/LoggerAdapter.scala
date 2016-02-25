package info.rgomes.sbt.snippets

trait LoggerAdapter {
  def error(text: String)
  def warn (text: String)
  def info (text: String)
  def debug(text: String)

  def error(e: Exception)
  def warn (e: Exception)
  def info (e: Exception)
  def debug(e: Exception)
}
