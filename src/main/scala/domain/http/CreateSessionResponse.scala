package domain.http

import domain.errors.RepoResult

case class CreateSessionResponse(sessionId: String) extends RepoResult
