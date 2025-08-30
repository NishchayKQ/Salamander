package nish.wry.salamander.data

// TODO deprecated
// https://www.npci.org.in/PDF/npci/upi/circular/2017/Circular18_BankCompliances_to_enbaleUPIMerchantecosystem_0.pdf
// https://developers.google.com/pay/india/api/android/in-app-payments
object UPIConstants {
    const val SCHEME = "upi"
    const val AUTHORITY = "pay"
    const val UPI_ID = "pa"
    const val MERCHANT_NAME = "pn"
    const val MERCHANT_CODE = "mc"
    const val AMOUNT = "am"
    const val TRANSACTION_REFERENCE_NUMBER = "tr"
    const val TRANSACTION_NOTE = "tn"
    const val CURRENCY = "cu"
    const val INR = "INR"
}

// Required:
// pa - Payee address (VPA, e.g., merchant@bank)
// pn - Payee name
// cu - Currency code (e.g., INR)
// mc - Merchant category code (MCC) maybe optional?
// Optional:
// tid Terminal ID (unique to the device)
// tr Transaction reference ID Optional
// tn Transaction note Optional
// am Transaction amount Optional
