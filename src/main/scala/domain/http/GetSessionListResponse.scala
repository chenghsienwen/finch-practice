package domain.http

import domain.db.Round
import domain.errors.RepoResult

case class GetSessionListResponse(sessions: List[Round]) extends RepoResult
