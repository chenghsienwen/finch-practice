package domain.swagger

import com.twitter.finatra.request.Header
import com.twitter.finatra.validation.NotEmpty

case class CreateVendingRequestBody(vendingIds: List[String])
