package example

object ScalaSample:
  // line comment
  /*
   * block comment
   */
  def sum(a: Int, b: Int): Int =
    if a > 0 && b > 0 then a + b else 0

  def classify(x: Int): String =
    x match
      case 0 => "zero"
      case n if n > 0 => "positive"
      case _ => "negative"

  val rendered = List(1, 2, 3)
    .map(x => x * 2)
    .filter(_ > 2)
    .mkString(",")
