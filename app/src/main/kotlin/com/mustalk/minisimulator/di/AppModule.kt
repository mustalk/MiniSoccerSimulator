package com.mustalk.minisimulator.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * @author by MusTalK on 15/07/2024
 *
 * Hilt module for providing dependencies related to the application.
 * This module is installed in the [SingletonComponent], which means the provided dependencies
 * will have a singleton scope and be available throughout the application's lifecycle.
 */

@Module
@InstallIn(SingletonComponent::class)
class AppModule
