package domain.http

case class UpdateSessionRequest(status: Option[String],
                                handsetItem: Option[String],
                                imageUrl: Option[String])
