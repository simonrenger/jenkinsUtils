"DepotBuildConfig"
{
    // Set your assigned depot ID here
    "DepotID" "{STEAM_DEPOT_ID}"

	// Set a root for all content.
	// All relative paths specified below (LocalPath in FileMapping entries, and FileExclusion paths)
	// will be resolved relative to this root.
	// If you don't define ContentRoot, then it will be assumed to be
	// the location of this script file, which probably isn't what you want
    "ContentRoot"	"{CONTENT_ROOT}"

	// include all files recursivley
  "FileMapping"
  {
  	// This can be a full path, or a path relative to ContentRoot
    "LocalPath" "{STEAM_LOCAL_PATH}"
    
    // This is a path relative to the install folder of your game
    "DepotPath" "{STEAM_DEPOT_PATH}"
    
    // If LocalPath contains wildcards, setting this means that all
    // matching files within subdirectories of LocalPath will also
    // be included.
    "recursive" "{STEAM_DEPOT_RECURSIVE}"
  }

	// but exclude all symbol files  
	// This can be a full path, or a path relative to ContentRoot
  "FileExclusion" "{STEAM_DEPOT_EXCLUDE}"
}
