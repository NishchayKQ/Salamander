package nish.wry.salamander.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import nish.wry.salamander.domain.repository.PaymentRepository
import nish.wry.salamander.domain.repository.TaskRepository
import nish.wry.salamander.data.room.life.PaymentChip
import nish.wry.salamander.data.room.task.Chip
import nish.wry.salamander.domain.usecase.GetAllChipsUseCase

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    // Teach Hilt how to build GetAllChipsUseCase<Chip> for tasks
    @Provides
    fun provideTaskGetAllChipsUseCase(
        repository: TaskRepository
    ): GetAllChipsUseCase<Chip> {
        return GetAllChipsUseCase(repository::getAllChips)
    }

    // similarly for payment chips
    @Provides
    fun providePaymentGetAllChipsUseCase(
        repository: PaymentRepository
    ): GetAllChipsUseCase<PaymentChip> {
        return GetAllChipsUseCase(repository::getAllPaymentChips)
    }
}