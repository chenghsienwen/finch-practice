package util

//None=>handsetReady.some=>uploadImageReady.some=>gameExpire.some
//None=>handsetSkip
object GameStatus {
  val HANDSET_READY      = "handsetReady" //pulling from vending machine, deprecated
  val CHOOSE_ITEM_READY  = "chooseItemReady" //pulling from vending machine, deprecated
  val UPLOAD_IMAGE_READY = "uploadImageReady" //pulling from phone device
  val GAME_EXPIRE        = "gameExpire"
  val HANDSET_SKIP       = "handsetSkip" //timeout or command on skip handset, deprecated
  val GAME_WIN           = "gameWin"
  val GAME_LOSE          = "gameLose"
  val NO_INVENTORY       = "noInventory"
  val INVALID            = "invalid"
}
