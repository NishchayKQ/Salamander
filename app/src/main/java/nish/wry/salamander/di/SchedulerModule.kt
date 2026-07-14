package nish.wry.salamander.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import nish.wry.salamander.scheduler.AlarmScheduler
import nish.wry.salamander.scheduler.Scheduler
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SchedulerModule {
    @Binds
    @Singleton
    abstract fun bindScheduler(alarmSchedulerImpl: AlarmScheduler): Scheduler
}