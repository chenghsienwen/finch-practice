package domain.http

import domain.TTL
import domain.db.Round

case class UpdateSessionFromCacheRequest(session: Round, ttl: Option[TTL] = None)
