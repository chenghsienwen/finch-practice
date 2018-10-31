package domain.http

import domain.errors.RepoResult

case class CreateVendingResponse(vendingIds: List[String]) extends RepoResult
