package domain.http

import domain.errors.RepoResult

case class DeleteSessionResponse(deletedSessionList: List[String]) extends RepoResult
