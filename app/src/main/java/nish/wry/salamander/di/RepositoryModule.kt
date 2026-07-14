package nish.wry.salamander.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import nish.wry.salamander.domain.repository.ActivityRepository
import nish.wry.salamander.data.repository.OfflineActivityRepository
import nish.wry.salamander.data.repository.OfflinePaymentRepository
import nish.wry.salamander.data.repository.OfflineTaskRepository
import nish.wry.salamander.domain.repository.PaymentRepository
import nish.wry.salamander.domain.repository.TaskRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindTaskRepository(impl: OfflineTaskRepository): TaskRepository

    @Binds
    @Singleton
    abstract fun bindActivityRepository(impl: OfflineActivityRepository): ActivityRepository

    @Binds
    @Singleton
    abstract fun bindPaymentRepository(impl: OfflinePaymentRepository): PaymentRepository
}