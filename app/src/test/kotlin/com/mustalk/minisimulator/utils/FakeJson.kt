package com.mustalk.minisimulator.utils

/**
 * @author by MusTalK on 16/07/2024
 */

object FakeJson {
    const val VALID_4TEAMS_JSON = """
        [
            { "name": "Team A", "strength": 9 },
            { "name": "Team B", "strength": 4 },
            { "name": "Team C", "strength": 6 },
            { "name": "Team D", "strength": 3 }
        ]
        """

    const val INVALID_B_TEAM_JSON = """
        [
           { "name": "Team B" }
        ]
        """

    const val VALID_2TEAMS_JSON = """
        [
            { "name": "Team A", "strength": 9 },
            { "name": "Team B", "strength": 4 }
        ]
        """

    const val INVALID_TEAMS_JSON = """
        [
            { "invalid": "data"  }
        ]
        """
}
