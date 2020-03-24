"appbuild"
{
	// Set the app ID that this script will upload.
	"appid" "[STEAM_APP_ID]"

	// The description for this build.
	// The description is only visible to you in the 'Your Builds' section of the App Admin panel.
	// This can be changed at any time after uploading a build on the 'Your Builds' page.
	"desc" "[JOB_BASE_NAME} (Build {BUILD_NUMBER})"

	// Enable/Disable whether this a preview build.
	// It's highly recommended that you use preview builds while doing the initially setting up SteamPipe to
	// ensure that the depot manifest contains the correct files.
	"preview" "[STEAM_IS_PREVIEW_BUILD]"

	// File path of the local content server if it's enabled.
	"local" ""

	// Branch name to automatically set live after successful build, none if empty.
	// Note that the 'default' branch can not be set live automatically. That must be done through the App Admin panel.
	"setlive" "[STEAM_BRANCH]"


	// The following paths can be absolute or relative to location of the script.

	// This directory will be the location for build logs, chunk cache, and intermediate output.
	// The cache stored within this causes future SteamPipe uploads to complete quicker by using diffing.
	"buildoutput" "[OUTPUT_DIR]"

	// The root of the content folder.
	"contentroot" "[CONTENT_ROOT]"

	// The list of depots included in this build.
	"depots"
	{
		"[STEAM_DEPOT_NUMBER]" "depot_build_[STEAM_DEPOT_NUMBER].vdf"
	}
}
