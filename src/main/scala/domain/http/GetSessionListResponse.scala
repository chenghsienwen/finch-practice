package domain.http

import domain.db.Round

case class GetSessionListResponse(sessions: List[Round])
