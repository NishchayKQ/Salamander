package nish.wry.salamander.di

import android.app.Application
import kotlinx.coroutines.MainScope

class SalamanderApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        // TODO check if this scope is okay
        container = AppDataContainer(coroutineScope = MainScope(), context = this)
    }

}