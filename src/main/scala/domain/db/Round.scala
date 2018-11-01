package domain.db

trait dbResult

final case class Round(_id: String = "",
                 clientId: Option[String] = None,
                 status: Option[String] = None,
                 handsetItem: Option[String] = None,
                 imageUrl: Option[String] = None,
                 createTime: Long = 0L,
                 updateTime: Long = 0L) extends dbResult

final case class RoundNotFound() extends dbResult

final case class RoundInternalError() extends dbResult