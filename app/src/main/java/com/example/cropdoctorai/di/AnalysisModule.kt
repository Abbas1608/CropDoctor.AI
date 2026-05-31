package com.example.cropdoctorai.di

import android.app.Application
import com.example.cropdoctorai.BuildConfig
import com.example.cropdoctorai.data.ml.CropClassifier
import com.example.cropdoctorai.data.pdf.CropReportGenerator
import com.example.cropdoctorai.data.remote.GeminiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing analysis feature dependencies:
 * CropClassifier, GeminiService, and CropReportGenerator.
 */
@Module
@InstallIn(SingletonComponent::class)
object AnalysisModule {

    @Provides
    @Singleton
    fun provideCropClassifier(application: Application): CropClassifier {
        return CropClassifier(application.applicationContext)
    }

    @Provides
    @Singleton
    fun provideGeminiService(): GeminiService {
        return GeminiService(apiKey = BuildConfig.GEMINI_API_KEY)
    }

    @Provides
    @Singleton
    fun provideCropReportGenerator(application: Application): CropReportGenerator {
        return CropReportGenerator(application.applicationContext)
    }
}
