package domain.http

import com.twitter.finatra.request.{Header, QueryParam}
import com.twitter.finatra.validation.NotEmpty

//should handle parameter to list by ","
case class GetSessionListRequest(@Header @NotEmpty `X-HTC-Account-Id`: String, @QueryParam sessionId: Option[String])
