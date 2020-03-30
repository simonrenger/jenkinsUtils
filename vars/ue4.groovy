def engineRoot = ""

def setRoot(root){
    engineRoot = root
}

def root(){
    return engineRoot
}

def pack(ue4_dir,project,platform,config,output_dir){
    bat label: 'Exec Package', script: "CALL \"${ue4_dir}Engine\\Build\\BatchFiles\\RunUAT.bat\" BuildCookRun -project=\"${project}\" -noP4 -Distribution -TargetPlatform=${platform} -Platform=${platform} -ClientConfig=${config} -ServerConfig=${config} -Cook -allmaps -Build -Stage -Pak -Archive -archivedirectory=\"${output_dir}\" -Rocket -Prereqs -Package"
}

def build(ue4_dir,project,project_name,platform,config,output_dir){
    bat label: 'UnrealBuildTool', script: "CALL \"${ue4_dir}Engine\\Binaries\\DotNET\\UnrealBuildTool.exe\" -projectfiles -project=\"${project}\" -game -rocket -progress -2019 -Platforms=${platform} PrecompileForTargets = PrecompileTargetsType.Any;"
    bat label: 'Build', script: "CALL \"${ue4_dir}Engine\\Build\\BatchFiles\\Build.bat\" ${project_name}Editor ${platform} ${config} \"${project}\""
}

def listTests(project,platform,config = "Development"){
echo "Ensuring ShaderCompileWorker is built before building project Editor modules..."
bat label: 'ShaderCompileWorker', script: "CALL \"${ue4_dir}Engine\\Build\\BatchFiles\\Build.bat\" ShaderCompileWorker ${platform} ${config}"
echo "Ensure the Editor version of the game has been build..."
bat label: 'Build', script: "CALL \"${ue4_dir}Engine\\Build\\BatchFiles\\Build.bat\" ${project_name}Editor ${platform} ${config} \"${project}\""
echo "Retrieving automation test list..."
def output = output bat label: 'list all tests', returnStdout: true, script:"CALL \"${engineRoot}\\Engine\\Binaries\\${platform}\\UE4Editor-Cmd.exe\" \"${project}\" -game -buildmachine -stdout -fullstdoutlogoutput -forcelogflush -unattended -nopause -nullrhi -nosplash -ExecCmds=\"automation List;quit\""
process(output)
}

def process(input){
    echo "Tests:"
    def array = input.split("\n")
    array.each {
        value ->
        def logData = value.split("Display:")
       if(logData.length == 2){
           echo logData[1]
       }
    }
}