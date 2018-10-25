package domain.http

import com.twitter.finatra.request.{Header, QueryParam}
import com.twitter.finatra.validation.NotEmpty

case class DeleteSessionRequest(@Header @NotEmpty `X-HTC-Account-Id`: String, @QueryParam @NotEmpty sessionId: String)
