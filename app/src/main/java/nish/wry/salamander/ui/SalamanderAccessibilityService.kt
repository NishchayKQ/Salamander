package nish.wry.salamander.ui


import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.graphics.Rect
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nish.wry.salamander.data.UPIApp
import nish.wry.salamander.data.room.life.PendingTransactionRecord
import nish.wry.salamander.di.PaymentRepository
import nish.wry.salamander.di.SalamanderApplication


private const val NYA = "nya"

// regex that works on the paytm/gpay's pay button at bottom
private val amountRegex = Regex("Pay(?: Securely)? ₹([\\d.]+)")
private val merchantNameRegex = Regex("Paying ([\\w ]+)")

// for 'Paying ₹10 securely to'
private val amountInPaymentTransitionScreenRegex = Regex("^Paying ₹([\\w.]+)")


@SuppressLint("AccessibilityPolicy")
class SalamanderAccessibilityService : AccessibilityService() {

    private lateinit var paymentRepository: PaymentRepository

    private var debounceJob: Job? = null
    private val serviceScope = CoroutineScope(SupervisorJob())

    override fun onInterrupt() {}

    override fun onCreate() {
        super.onCreate()

        if (application is SalamanderApplication) {
            paymentRepository = (application as SalamanderApplication).container.paymentRepository
        }
    }


    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val rootNode: AccessibilityNodeInfo? = rootInActiveWindow
        if (rootNode == null) return

        // Cancel any pending job.
        debounceJob?.cancel()

        debounceJob = serviceScope.launch {
            // we wait for 400ms before launching and processing
            delay(400)

            val packageName: String? = event.packageName?.toString()
            if (packageName == null)
                return@launch
            var amount: String? = null
            var merchantName: String? = null

            val queue = ArrayDeque<AccessibilityNodeInfo>()
            queue.add(rootNode)

            while (queue.isNotEmpty()) {
                val nodeInfo = queue.removeFirst()

                // checking if this is perhaps the payment transition screen
                val transitionAmount =
                    findRegexInNode(nodeInfo, amountInPaymentTransitionScreenRegex)
                if (transitionAmount != null) {
                    // next (sibling) node must be the merchant name
                    val transitionMerchantName: String? =
                        queue[0].text?.toString() ?: queue[0].contentDescription.toString()
                    if (transitionMerchantName != null) {
                        withContext(Dispatchers.IO) {
                            paymentRepository.confirmPendingTransaction(
                                PendingTransactionRecord(
                                    amount = transitionAmount.toDoubleOrNull() ?: 0.0,
                                    merchantName = transitionMerchantName,
                                    upiApp = UPIApp.packageNameToUPIAppsEnum(packageName)
                                )
                            )
                        }
                    }
                    break
                }

                if (merchantName == null) {
                    // we try to extract merchant name
                    merchantName = findRegexInNode(nodeInfo, merchantNameRegex)
                }

                if (amount == null) {
                    // extract amount
                    amount = findRegexInNode(nodeInfo, amountRegex)

                }

                if (merchantName != null && amount != null) {
                    // we got out data lets exit
                    withContext(Dispatchers.IO) {
                        paymentRepository.addPendingTransaction(
                            PendingTransactionRecord(
                                amount = amount.toDoubleOrNull() ?: 0.0,
                                merchantName = merchantName,
                                upiApp = UPIApp.packageNameToUPIAppsEnum(packageName)
                            )
                        )
                    }
                    break
                }

                for (i in 0 until nodeInfo.childCount) {
                    val childNode: AccessibilityNodeInfo? = nodeInfo.getChild(i)
                    if (childNode != null) {
                        queue.add(childNode)
                    }
                }

            }

        }

    }

    /**
     * @param regex the supplied regex must have atleast one capture group, capture group 1 will be returned
     * **/
    private fun findRegexInNode(nodeInfo: AccessibilityNodeInfo, regex: Regex): String? {
        val text: String? = nodeInfo.text?.toString()
        val contentDescription: String? = nodeInfo.contentDescription?.toString()

        if (text != null) {
            val match = regex.find(text)
            if (match != null) return match.groupValues[1]
        }
        if (contentDescription != null) {
            val match = regex.find(contentDescription)
            if (match != null) return match.groupValues[1]
        }
        return null
    }

    private fun logNodeTree(node: AccessibilityNodeInfo?, depth: Int = 0) {
        if (node == null) return

        val indent = "  ".repeat(depth)
        val bounds = Rect()
        node.getBoundsInScreen(bounds)

        // Log everything we can about the node
        val info = buildString {
            append("$indent[")
            append("Class: ${node.className}, ")
            append("Text: '${node.text}', ")
            append("ContentDesc: '${node.contentDescription}', ")
            append("ID: '${node.viewIdResourceName}', ") // This is a goldmine if available!
            append("Bounds: $bounds, ")
            append("Clickable: ${node.isClickable}")
            append("]")
        }
        Log.d(NYA, info)

        // Recursively call for all children
        for (i in 0 until node.childCount) {
            logNodeTree(node.getChild(i), depth + 1)
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(NYA, "Service connected nya")

    }
}
