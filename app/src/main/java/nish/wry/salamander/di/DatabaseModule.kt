package nish.wry.salamander.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import nish.wry.salamander.data.room.SalamanderRoomDatabase
import javax.inject.Singleton
import kotlin.time.Clock


/**hilt @provides methods for places that needs em (mostly database stuff), but has some other provides as well**/
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SalamanderRoomDatabase {
        return Room.databaseBuilder(
            context = context,
            klass = SalamanderRoomDatabase::class.java,
            name = "salamander_database"
        )
            .fallbackToDestructiveMigration(true)
//          .createFromAsset()
            .build()
    }

    @Provides
    fun provideTaskDao(db: SalamanderRoomDatabase) = db.taskDao()

    @Provides
    fun provideChipDao(db: SalamanderRoomDatabase) = db.chipDao()

    @Provides
    fun provideActivityIntervalDao(db: SalamanderRoomDatabase) = db.activityIntervalDao()

    @Provides
    fun provideCategoryDao(db: SalamanderRoomDatabase) = db.categoryDao()

    @Provides
    fun provideDailyLogDao(db: SalamanderRoomDatabase) = db.dailyLogDao()

    @Provides
    fun providePaymentChipDao(db: SalamanderRoomDatabase) = db.paymentChipDao()

    @Provides
    fun providePaymentRecordDao(db: SalamanderRoomDatabase) = db.paymentRecordDao()

    @Provides
    @Singleton
    fun provideApplicationScope(): CoroutineScope = MainScope()

    @Provides
    @Singleton
    fun provideClock(): Clock {
        return Clock.System // Provide the production clock
    }
}
