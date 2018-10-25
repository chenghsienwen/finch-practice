package domain.db

case class PrivacyTerm(_id: String = "",
                       promoteName: String = "",
                       clientId: String = "",
                       sessionId: String = "",
                       privacyVersion: String = "",
                       optIn: Boolean = false,
                       ts: Long = 0L)
