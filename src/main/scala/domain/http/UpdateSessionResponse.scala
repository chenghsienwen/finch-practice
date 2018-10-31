package domain.http

import domain.db.Round
import domain.errors.RepoResult

case class UpdateSessionResponse(session: Round) extends RepoResult
