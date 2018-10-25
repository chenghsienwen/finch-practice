package domain.swagger

import com.twitter.finatra.request.{QueryParam, RouteParam}
import com.twitter.finatra.validation.NotEmpty

case class UpdateSessionRequestBody(status: Option[String], handsetItem: Option[String], imageUrl: Option[String])
