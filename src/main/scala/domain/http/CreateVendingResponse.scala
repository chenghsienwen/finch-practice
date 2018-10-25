package domain.http

import domain.errors.Result

case class CreateVendingResponse(vendingIds: List[String]) extends Result
