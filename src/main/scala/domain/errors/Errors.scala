package domain.errors

trait Result

case class InternalErrors(code: Int, msg: String) extends Result

case class ContentNotFoundErrors(code: Int, msg: String) extends Result

case class ProcessError(code: Int, msg: String) extends Result

case class AuthError(code: Int, msg: String) extends Result

object Errors {

  val ParseClassErrors                  = InternalErrors(code = 1003, msg = "Server Internal Error")
  val CreateUrlBySessionIdInternalError = InternalErrors(code = 1004, msg = "Server Internal Error")
  val DeleteBySessionIdInternalError    = InternalErrors(code = 1005, msg = "Server Internal Error")
  val GetVendingIdInternalError         = InternalErrors(code = 1006, msg = "Server Internal Error")
  val GetSessionIdInternalError         = InternalErrors(code = 1007, msg = "Server Internal Error")
  val CreateVendingInternalError        = InternalErrors(code = 1008, msg = "Server Internal Error")
  val GetConfigInternalError            = InternalErrors(code = 1009, msg = "Server Internal Error")
  val InsertPrivacyTermInternalError    = InternalErrors(code = 1010, msg = "Server Internal Error")
  val DeletePrivacyTermInternalError    = InternalErrors(code = 1011, msg = "Server Internal Error")

  val S3ObjectNotFoundError = ContentNotFoundErrors(code = 9007, msg = "S3 Object Not Found Error")
  val DeleteSessionNotFound = ContentNotFoundErrors(9009, "delete session Not Found")
  val SessionNotFound       = ContentNotFoundErrors(9010, "session Not Found")
  val ConfigNotFound        = ContentNotFoundErrors(9011, "config Not Found")
  val PrivacyTermNotFound   = ContentNotFoundErrors(9012, "privacy term Not Found")

  val GetAuthTokenError   = ProcessError(4009, "Get Auth Token Error")
  val DeleteS3ObjectError = ProcessError(4010, "Delete S3 Object error")
  val SessionExpireError  = ProcessError(4011, "Session expire error")
  val NotJsonError        = ProcessError(4012, "Not Json error")
  val AccountIdNotFound   = ProcessError(4013, "Account Not Found")

  val ClientIdNotAllowed  = AuthError(5001, "Client id isn't allowed")
  val AccountIdNotAllowed = AuthError(5002, "Account id isn't allowed")

}
