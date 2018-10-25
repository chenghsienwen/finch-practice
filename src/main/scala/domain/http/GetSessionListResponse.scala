package domain.http

import domain.db.Round
import domain.errors.Result

case class GetSessionListResponse(sessions: List[Round]) extends Result
