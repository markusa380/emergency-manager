package emergencymanager

package object frontend {

    val maxErrorMessageLength = 80

    def limitErrorMessageLength(message: String) =
        if (message.length > maxErrorMessageLength) message.take(maxErrorMessageLength - 3) + "..."
        else message
}
