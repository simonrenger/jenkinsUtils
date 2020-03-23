def engineRoot = ""

def setRoot(root){
    engineRoot = root
}

def pack(ue4_dir,project,platform,config,output_dir){
    bat label: 'Exec Package', script: "CALL \"${ue4_dir}Engine\\Build\\BatchFiles\\RunUAT.bat\" BuildCookRun -project=\"${project}\" -noP4 -Distribution -TargetPlatform=${platform} -Platform=${platform} -ClientConfig=${config} -ServerConfig=${config} -Cook -allmaps -Build -Stage -Pak -Archive -archivedirectory=\"${output_dir}\" -Rocket -Prereqs -Package"
}

def build(ue4_dir,project,project_name,platform,config,output_dir){
    bat label: 'UnrealBuildTool', script: "CALL \"${ue4_dir}Engine\\Binaries\\DotNET\\UnrealBuildTool.exe\" -projectfiles -project=\"${project}\" -game -rocket -progress -2019 -Platforms=${platform} PrecompileForTargets = PrecompileTargetsType.Any;"
    bat label: 'Build', script: "CALL \"${ue4_dir}Engine\\Build\\BatchFiles\\Build.bat\" ${project_name}Editor ${platform} ${config} \"${project}\""
}

def listTests(platform,project){
    bat label: 'list all tests', script:"CALL \"${engineRoot}Engine\\Engine\\Binaries\\${platform}\\UE4Editor-Cmd.exe\" \"${project}\" -game -buildmachine -stdout -fullstdoutlogoutput -forcelogflush -unattended -nopause -nullrhi -nosplash -ExecCmds=\"automation List;quit\""
}