package domain.http

sealed trait CreateVendingResult

final case class CreateVendingSucceed(vendingIds: List[String]) extends CreateVendingResult

final case class CreateVendingAllow() extends CreateVendingResult

final case class CreateVendingNotAllow() extends CreateVendingResult

final case class CreateVendingNotFound() extends CreateVendingResult
