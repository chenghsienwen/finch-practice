package domain.http

trait CreateSessionResult

final case class CreateSessionResponse(sessionId: String) extends CreateSessionResult