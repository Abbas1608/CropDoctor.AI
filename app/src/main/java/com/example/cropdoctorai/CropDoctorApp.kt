package com.example.cropdoctorai

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * CropDoctor.AI Application class.
 * Annotated with @HiltAndroidApp to trigger Hilt code generation
 * and serve as the application-level dependency container.
 */
@HiltAndroidApp
class CropDoctorApp : Application()
