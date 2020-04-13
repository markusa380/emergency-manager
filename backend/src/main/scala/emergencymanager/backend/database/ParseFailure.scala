package emergencymanager.backend.database

import java.io.IOException

case class ParseFailure(message: String) extends IOException(message)