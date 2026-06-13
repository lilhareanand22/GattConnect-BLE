package anand.jagdish.blevbutton.di

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import anand.jagdish.blevbutton.data.repository.BleRepositoryImpl
import anand.jagdish.blevbutton.domain.repository.BleRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BluetoothModule {

    @Binds
    @Singleton
    abstract fun bindBleRepository(impl: BleRepositoryImpl): BleRepository

    companion object {
        @Provides
        @Singleton
        fun provideBluetoothManager(@ApplicationContext context: Context): BluetoothManager {
            return context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        }

        @Provides
        @Singleton
        fun provideBluetoothAdapter(bluetoothManager: BluetoothManager): BluetoothAdapter? {
            return bluetoothManager.adapter
        }
    }
}
