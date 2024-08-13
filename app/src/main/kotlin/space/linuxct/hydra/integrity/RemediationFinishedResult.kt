package space.linuxct.hydra.integrity

data class RemediationFinishedResult (
    val isFailure: Boolean,
    val remediationResult: String,
    val errorMessage: String?
)