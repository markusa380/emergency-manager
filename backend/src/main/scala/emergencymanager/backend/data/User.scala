package emergencymanager.backend.data

case class User(
    id: String,
    passwordHash: List[Byte],
    salt: List[Byte]
)