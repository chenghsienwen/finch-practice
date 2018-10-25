package domain.http

import domain.errors.Result

case class CreateSessionResponse(sessionId: String) extends Result
