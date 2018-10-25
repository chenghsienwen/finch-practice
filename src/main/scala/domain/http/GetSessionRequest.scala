package domain.http

import com.twitter.finatra.request.{QueryParam, RouteParam}
import com.twitter.finatra.validation.NotEmpty

//should handle parameter to list by ","
case class GetSessionRequest(@RouteParam @NotEmpty sessionId: String,
                             @QueryParam clientId: String,
                             @QueryParam isTest: Option[Boolean])
