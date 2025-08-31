package nish.wry.salamander.ui.life

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.flow.Flow
import nish.wry.salamander.data.room.life.PaymentRecord
import nish.wry.salamander.di.PaymentRepository

class PaymentHistoryViewModel(paymentRepository: PaymentRepository) : ViewModel() {
    val transactions: Flow<PagingData<PaymentRecord>> =
        paymentRepository.getAllSuccessfulPayments().cachedIn(viewModelScope)



}