package emergencymanager.backend.data

case class ApplicationConf(
    secretsPath: String,
    mongodbHost: String,
    mongodbPort: String,
    mongodbUser: String,
    mongodbPasswordFile: String,
    mongodbDb: String
)