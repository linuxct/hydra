package space.linuxct.hydra.integrity

enum class RemediationResultEnum {
    // https://developer.android.com/google/play/integrity/reference/com/google/android/play/core/integrity/model/IntegrityDialogResponseCode#DIALOG_UNAVAILABLE
    DIALOG_UNAVAILABLE,
    // https://developer.android.com/google/play/integrity/reference/com/google/android/play/core/integrity/model/IntegrityDialogResponseCode#DIALOG_FAILED
    DIALOG_FAILED,
    // https://developer.android.com/google/play/integrity/reference/com/google/android/play/core/integrity/model/IntegrityDialogResponseCode#DIALOG_CANCELLED
    DIALOG_CANCELLED,
    // https://developer.android.com/google/play/integrity/reference/com/google/android/play/core/integrity/model/IntegrityDialogResponseCode#DIALOG_SUCCESSFUL
    DIALOG_SUCCESSFUL;

    companion object {
        fun fromInt(value: Int): RemediationResultEnum {
            return when (value){
                0 -> DIALOG_UNAVAILABLE
                1 -> DIALOG_FAILED
                2 -> DIALOG_CANCELLED
                3 -> DIALOG_SUCCESSFUL
                else -> throw IllegalArgumentException("Invalid value for RemediationResultEnum: $value")
            }
        }
    }
}