package nish.wry.salamander.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
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