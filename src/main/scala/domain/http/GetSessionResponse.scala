package domain.http

import domain.db.Round

trait GetSessionResult

final case class GetSessionResponse(session: Round) extends GetSessionResult

final case class GetSessionNotFound() extends GetSessionResult

final case class GetSessionAllow() extends GetSessionResult

final case class GetSessionNotAllow() extends GetSessionResult