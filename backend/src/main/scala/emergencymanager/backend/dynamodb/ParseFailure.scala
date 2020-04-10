package emergencymanager.backend.dynamodb

import java.io.IOException

case class ParseFailure(message: String) extends IOException(message)