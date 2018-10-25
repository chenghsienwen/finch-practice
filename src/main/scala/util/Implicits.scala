package util

object Implicits {

  implicit class StringOpt(a: String) {

    /**
      * Only hide characters from index = 6 to end
      */
    def hidePartial(): String = {
      val KEPT_LEN: Int = 6
      a.length match {
        case n if n >= KEPT_LEN ⇒
          new Array[Char](KEPT_LEN + 3).zipWithIndex.map {
            case (_, i) if i < KEPT_LEN ⇒ a.charAt(i)
            case _                      ⇒ '*'
          }.mkString
        case n ⇒ a
      }
    }
  }

}
