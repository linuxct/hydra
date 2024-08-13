package space.linuxct.hydra.integrity

enum class RemediationTypeEnum {
    // https://developer.android.com/google/play/integrity/remediation#get-licensed-dialog
    PLAY_INTEGRITY_GET_LICENSED,
    // https://developer.android.com/google/play/integrity/remediation#close-unknown-access-risk-dialog
    PLAY_INTEGRITY_CLOSE_UNKNOWN_ACCESS_RISK,
    // https://developer.android.com/google/play/integrity/remediation#close-all-access-risk-dialog
    PLAY_INTEGRITY_CLOSE_ALL_ACCESS_RISK;

    companion object {
        fun toInt(value: RemediationTypeEnum): Int {
            return when (value){
                PLAY_INTEGRITY_GET_LICENSED -> 1
                PLAY_INTEGRITY_CLOSE_UNKNOWN_ACCESS_RISK -> 2
                PLAY_INTEGRITY_CLOSE_ALL_ACCESS_RISK -> 3
                else -> throw IllegalArgumentException("Invalid value for RemediationTypeEnum: $value")
            }
        }
    }
}