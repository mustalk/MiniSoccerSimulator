module.exports = {
    branches: [
        "main",
      ],
    plugins: [
        [
        "@semantic-release/commit-analyzer",
            {
                "preset": "conventionalcommits",
                "releaseRules": [
                    { "breaking": true, "release": "major" },
                    { "type": "feat", "release": "minor" },
                    { "type": "fix", "release": "patch" },
                    { "type": "style", "release": "patch" },
                    { "type": "refactor", "release": "patch" },
                    { "type": "perf", "release": "patch" },
                    { "type": "build", "release": "patch" },
                    { "type": "ci", "release": "patch" },
                    { "type": "chore", "release": "patch" },
                    { "type": "revert", "release": "patch" }
                ]
            }
        ],
        [
            "@semantic-release/release-notes-generator",
            {
                "preset": "conventionalcommits",
                "releaseRules": [
                    { "breaking": true, "release": "major" },
                    { "type": "feat", "release": "minor" },
                    { "type": "fix", "release": "patch" },
                    { "type": "style", "release": "patch" },
                    { "type": "refactor", "release": "patch" },
                    { "type": "perf", "release": "patch" },
                    { "type": "build", "release": "patch" },
                    { "type": "ci", "release": "patch" },
                    { "type": "chore", "release": "patch" },
                    { "type": "revert", "release": "patch" }
                ]
            }
        ],
        "@semantic-release/changelog",
        [
            "@semantic-release/exec",
            {
                "prepareCmd": ".github/scripts/utils/helpers/deploy/update-version.sh ${nextRelease.version} app/build.gradle.kts"
            }
        ],
        // For better control, disabled to manually handle the git commit for the version bump and changelog update.
        // For more details, see the .github/scripts/utils/helpers/deploy/commit-version-bump.sh script.
        //
        // If you prefer semantic-release to automatically handle the version bump and changelog update commit,
        // re-enable this block by uncommenting it and customizing the commit message as needed.
        //
        // Example configuration for semantic-release to create a commit for the version bump and changelog:
        //[
        //   "@semantic-release/git",
        //   {
        //       "assets": [
        //           "app/build.gradle.kts",
        //           "CHANGELOG.md"
        //       ],
        //       "message": "chore(release): promote release v${nextRelease.version} \n\n${nextRelease.notes}"
        //   }
        //,
        [
            "semantic-release-slack-bot",
            {
                "notifyOnSuccess": true,
                "notifyOnFail": true,
                "markdownReleaseNotes": true,
                "packageName": process.env.SEMANTIC_RELEASE_PACKAGE
            }
        ],
        [
            "@semantic-release/github",
            {
                "assets": ["CHANGELOG.md"],
                "successComment": false,
                "failComment": false,
                "failTitle": false,
                "labels": false
            }
        ]
    ]
};
