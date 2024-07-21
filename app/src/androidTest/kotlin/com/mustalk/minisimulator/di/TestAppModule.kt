package com.mustalk.minisimulator.di

import android.content.Context
import com.mustalk.minisimulator.data.local.AssetTeamRepository
import com.mustalk.minisimulator.data.local.ITeamRepository
import com.mustalk.minisimulator.domain.match.IMatchGenerator
import com.mustalk.minisimulator.domain.match.IMatchSimulator
import com.mustalk.minisimulator.domain.match.MatchGenerator
import com.mustalk.minisimulator.domain.match.MatchSimulator
import com.mustalk.minisimulator.domain.standings.GroupStandings
import com.mustalk.minisimulator.domain.team.ITeamStatsUpdater
import com.mustalk.minisimulator.domain.team.TeamStatsUpdater
import com.mustalk.minisimulator.domain.usecases.matches.SimulateMatchUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

/**
 * @author by MusTalK on 15/07/2024
 */

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [AppModule::class]
)
object TestAppModule {
    @Provides
    fun provideAssetTeamRepository(
        @ApplicationContext context: Context,
    ): ITeamRepository = AssetTeamRepository(context)

    @Provides
    fun provideMatchGenerator(): IMatchGenerator = MatchGenerator()

    @Provides
    fun provideMatchSimulator(): IMatchSimulator = MatchSimulator()

    @Provides
    fun provideTeamStatsUpdater(): ITeamStatsUpdater = TeamStatsUpdater()

    @Provides
    @Singleton
    fun provideGroupStandings(
        teamRepository: ITeamRepository,
        matchGenerator: IMatchGenerator,
        simulateMatchUseCase: SimulateMatchUseCase,
    ): GroupStandings = GroupStandings(teamRepository, matchGenerator, simulateMatchUseCase)
}
