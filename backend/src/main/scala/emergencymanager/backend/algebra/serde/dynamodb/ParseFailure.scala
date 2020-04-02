package emergencymanager.backend.algebra.serde.dynamodb

import java.io.IOException

case class ParseFailure(message: String) extends IOException(message)