package data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.core.DataStore

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ciudades_guardadas")
