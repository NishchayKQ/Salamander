@file:Suppress("SpellCheckingInspection")

package nish.wry.salamander.data

//    const val PHONE_PE = "com.phonepe.app"
//    const val BHIM = "in.org.npci.upiapp"
//    const val AMAZON_PAY = "in.amazon.mShop.android.shopping"

private const val GOOGLE_PAY_PACKAGE_NAME_STRING = "com.google.android.apps.nbu.paisa.user"
private const val PAYTM_PACKAGE_NAME_STRING = "net.one97.paytm"

enum class UPIApp() {
    GOOGLE_PAY,
    PAYTM,
    /**for cases when user manually adds a transaction**/
    NONE;

    companion object{
        fun packageNameToUPIAppsEnum(packageName: String) : UPIApp{
            return when(packageName){
                GOOGLE_PAY_PACKAGE_NAME_STRING -> GOOGLE_PAY
                PAYTM_PACKAGE_NAME_STRING -> PAYTM
                else -> {
                    throw IllegalArgumentException("unknown packageName : '$packageName'")
                }
            }
        }
    }
}