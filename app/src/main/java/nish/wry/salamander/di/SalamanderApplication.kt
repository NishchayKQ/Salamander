package nish.wry.salamander.di

import android.app.Application

class SalamanderApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }
}