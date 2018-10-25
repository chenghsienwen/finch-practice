package domain.http

import domain.errors.Result

case class DeleteSessionResponse(deletedSessionList: List[String]) extends Result
