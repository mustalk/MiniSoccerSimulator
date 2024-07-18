package com.mustalk.minisimulator.di

import android.content.Context
import com.mustalk.minisimulator.data.deserializers.TeamDeserializer
import com.mustalk.minisimulator.data.local.ITeamRepository
import com.mustalk.minisimulator.data.remote.ITeamApiService
import com.mustalk.minisimulator.data.remote.RemoteTeamRepository
import com.mustalk.minisimulator.data.utils.DataConstants
import com.mustalk.minisimulator.data.utils.helpers.NetworkHelper
import com.mustalk.minisimulator.domain.match.IMatchGenerator
import com.mustalk.minisimulator.domain.match.IMatchSimulator
import com.mustalk.minisimulator.domain.match.MatchGenerator
import com.mustalk.minisimulator.domain.match.MatchSimulator
import com.mustalk.minisimulator.domain.standings.GroupStandings
import com.mustalk.minisimulator.domain.team.ITeamStatsUpdater
import com.mustalk.minisimulator.domain.team.TeamStatsUpdater
import com.mustalk.minisimulator.domain.usecases.matches.GetMatchesUseCase
import com.mustalk.minisimulator.domain.usecases.matches.HasMatchBeenPlayedUseCase
import com.mustalk.minisimulator.domain.usecases.matches.SimulateAllMatchesUseCase
import com.mustalk.minisimulator.domain.usecases.matches.SimulateMatchUseCase
import com.mustalk.minisimulator.domain.usecases.matches.SimulateNextRoundMatchesUseCase
import com.mustalk.minisimulator.domain.usecases.teams.GetTeamSizeUseCase
import com.mustalk.minisimulator.domain.usecases.teams.GetTeamsUseCase
import com.mustalk.minisimulator.domain.usecases.teams.InitializeTeamsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

/**
 * Hilt module for providing dependencies related to the application.
 * This module is installed in the [SingletonComponent], which means the provided dependencies
 * will have a singleton scope and be available throughout the application's lifecycle.
 *
 * @author by MusTalK on 15/07/2024
 */
@Module
@InstallIn(SingletonComponent::class)
class AppModule {
    @Provides
    @Singleton
    fun provideTeamApiService(
        @ApplicationContext context: Context,
        teamDeserializer: TeamDeserializer,
    ): ITeamApiService {
        // Create an OkHttpClient with the interceptor
        val client =
            OkHttpClient
                .Builder()
                .addInterceptor(NetworkHelper.getInterceptor(context))
                .build()

        // Create a Retrofit instance with the OkHttpClient, the deserializer and the base URL
        val retrofit = NetworkHelper.getRetrofit(client, teamDeserializer, DataConstants.BASE_URL)
        return retrofit.create(ITeamApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideRemoteTeamRepository(remoteTeamRepository: RemoteTeamRepository): ITeamRepository = remoteTeamRepository

    @Provides
    @Singleton
    fun provideTeamDeserializer(): TeamDeserializer = TeamDeserializer()

    @Provides
    fun provideTeamStatsUpdater(): ITeamStatsUpdater = TeamStatsUpdater()

    @Provides
    @Singleton
    fun provideGroupStandings(
        teamRepository: ITeamRepository,
        matchGenerator: IMatchGenerator,
        simulateMatchUseCase: SimulateMatchUseCase,
    ): GroupStandings = GroupStandings(teamRepository, matchGenerator, simulateMatchUseCase)

    @Provides
    fun provideGetTeamsUseCase(): GetTeamsUseCase = GetTeamsUseCase()

    @Provides
    fun provideGetTeamSizeUseCase(): GetTeamSizeUseCase = GetTeamSizeUseCase()

    @Provides
    fun provideInitializeTeamsUseCase(): InitializeTeamsUseCase = InitializeTeamsUseCase()

    @Provides
    fun provideMatchGenerator(): IMatchGenerator = MatchGenerator()

    @Provides
    fun provideMatchSimulator(): IMatchSimulator = MatchSimulator()

    @Provides
    fun provideGetMatchesUseCase(): GetMatchesUseCase = GetMatchesUseCase()

    @Provides
    fun provideHasMatchBeenPlayedUseCase(): HasMatchBeenPlayedUseCase = HasMatchBeenPlayedUseCase()

    @Provides
    fun provideSimulateAllMatchesUseCase(): SimulateAllMatchesUseCase = SimulateAllMatchesUseCase()

    @Provides
    @Singleton
    fun provideSimulateMatchUseCase(
        matchSimulator: IMatchSimulator,
        statsUpdater: ITeamStatsUpdater,
    ): SimulateMatchUseCase = SimulateMatchUseCase(matchSimulator, statsUpdater)

    @Provides
    fun provideSimulateNextRoundMatchesUseCase(): SimulateNextRoundMatchesUseCase = SimulateNextRoundMatchesUseCase()
}
