package domain.http

import domain.db.Round
import domain.errors.RepoResult

case class GetSessionResponse(session: Round) extends RepoResult
