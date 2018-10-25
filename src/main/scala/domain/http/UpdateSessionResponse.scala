package domain.http

import domain.db.Round
import domain.errors.Result

case class UpdateSessionResponse(session: Round) extends Result
