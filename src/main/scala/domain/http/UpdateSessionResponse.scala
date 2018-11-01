package domain.http

import domain.db.Round

trait UpdateSessionResult

final case class UpdateSessionResponse(session: Round) extends UpdateSessionResult

final case class UpdateSessionAllow() extends UpdateSessionResult

final case class UpdateSessionNotAllow() extends UpdateSessionResult
