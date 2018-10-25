package domain.http

import domain.db.Round
import domain.errors.Result

case class GetSessionResponse(session: Round) extends Result
