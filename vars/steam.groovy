def depotManifest(depotNumber,contentRoot,localPath="*",depotPath=".",recursive="1",exclude="*.pdb"){
    def depot_manifest = libraryResource 'de/simonrenger/depot_build.vdf.tpl'
    depot_manifest = depot_manifest.replace('[STEAM_DEPOT_ID]',depotNumber)
    depot_manifest = depot_manifest.replace('[CONTENT_ROOT]',contentRoot)
    depot_manifest = depot_manifest.replace('[STEAM_LOCAL_PATH]',localPath)
    depot_manifest = depot_manifest.replace('[STEAM_DEPOT_PATH]',depotPath)
    depot_manifest = depot_manifest.replace('[STEAM_DEPOT_RECURSIVE]',recursive)
    depot_manifest = depot_manifest.replace('[STEAM_DEPOT_EXCLUDE]',exclude)
    writeFile file: "depot_build_${depotNumber}.vdf", text: depot_manifest
    return "depot_build_${depotNumber}.vdf"
}

def appManifest(appId,depotNumber,contentroot,steamBranch,isPreview="0",outputdir="output"){
    def app_manifest = libraryResource 'de/simonrenger/app_build.vdf.tpl'
    app_manifest = app_manifest.replace('[STEAM_APP_ID]',appId)
    app_manifest = app_manifest.replace('[JOB_BASE_NAME]',JOB_NAME)
    app_manifest = app_manifest.replace('[BUILD_NUMBER]',BUILD_NUMBER)
    app_manifest = app_manifest.replace('[STEAM_IS_PREVIEW_BUILD]',isPreview)
    app_manifest = app_manifest.replace('[STEAM_BRANCH]',steamBranch)
    app_manifest = app_manifest.replace('[OUTPUT_DIR]',outputdir)
    app_manifest = app_manifest.replace('[CONTENT_ROOT]',contentroot)
    app_manifest = app_manifest.replace('[STEAM_DEPOT_NUMBER]',depotNumber)
    writeFile file: "app_build_${appId}.vdf", text: app_manifest
    return "app_build_${appId}.vdf"
}

//downloads steamcmd
def setup(sourceDir = "..\\",installDir = "..\\steamcmd"){
    
    sourceDir = "${env.workspace}\\${sourceDir}"
    installDir ="${env.workspace}\\${installDir}"
    if(!fileExists("${sourceDir}steamcmd.zip")){
        powershell label: 'Download SteamCMD', script: "Invoke-WebRequest http://media.steampowered.com/installer/steamcmd.zip -O '${sourceDir}steamcmd.zip'"
        powershell label: 'Unzip SteamCMD', script: "Expand-Archive -LiteralPath '${sourceDir}steamcmd.zip' -DestinationPath '${installDir}'"
    }else{
        echo "SteamCMD is set up and does not need to be downloaded!"
    }
}

def deployIf(credentials,appManifest){
    try{
        deploy(credentials,appManifest)
    }catch(err){
        echo "Request User Input"
        def INPUT = input message: 'Steam Guard Code:', ok: 'Submit',
                            parameters: [
                            string(name: 'STEAM_GUARD', defaultValue: 'Mr Jenkins', description: 'Please provide your steam Guard code')]
        deploy(credentials,appManifest,INPUT)
    }
}


def deploy(credentials,appManifest,steamGuard = null){
    withCredentials([usernamePassword(credentialsId: credentials, passwordVariable: 'STEAMCMD_PASSWORD', usernameVariable: 'STEAMCMD_USERNAME')]) {
        if(steamGuard == null){
            bat label: 'steam deployment without SteamGuard', script: "\"${STEAM_BUILDER}\" +login \"${STEAMCMD_USERNAME}\" \"${STEAMCMD_PASSWORD}\" +run_app_build_http \"${appManifest}\" +quit"
        }else{
             bat label: 'steam deployment with SteamGuard', script: "\"${STEAM_BUILDER}\" +login \"${STEAMCMD_USERNAME}\" \"${STEAMCMD_PASSWORD}\" \"${steamGuard}\"  +run_app_build_http \"${appManifest}\" +quit"
        }
    }
}