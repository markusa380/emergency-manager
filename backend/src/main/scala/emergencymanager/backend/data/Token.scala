package emergencymanager.backend.data

case class Token(
    id: String,
    userId: String,
    lastChallenged: Long
)