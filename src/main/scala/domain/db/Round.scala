package domain.db

case class Round(_id: String = "",
                 clientId: Option[String] = None,
                 status: Option[String] = None,
                 handsetItem: Option[String] = None,
                 imageUrl: Option[String] = None,
                 createTime: Long = 0L,
                 updateTime: Long = 0L)
