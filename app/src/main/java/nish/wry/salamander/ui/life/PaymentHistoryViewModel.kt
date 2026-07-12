package nish.wry.salamander.ui.life

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import nish.wry.salamander.data.room.life.PaymentRecord
import nish.wry.salamander.di.PaymentRepository
import javax.inject.Inject

@HiltViewModel
class PaymentHistoryViewModel @Inject constructor(paymentRepository: PaymentRepository) : ViewModel() {
    val transactions: Flow<PagingData<PaymentRecord>> =
        paymentRepository.getAllSuccessfulPayments().cachedIn(viewModelScope)



}