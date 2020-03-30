def engineRoot = ""

def setRoot(root){
    engineRoot = root
}

def root(){
    return engineRoot
}

def pack(ue4_dir,project,platform,config,output_dir){
    echo "Settings:\nProject: ${project}\nPlatform: ${platform}\nConfig: ${config}\nOutput directory: ${output_dir}"
    bat label: 'Exec Package', script: "CALL \"${ue4_dir}Engine\\Build\\BatchFiles\\RunUAT.bat\" BuildCookRun -project=\"${project}\" -noP4 -Distribution -TargetPlatform=${platform} -Platform=${platform} -ClientConfig=${config} -ServerConfig=${config} -Cook -allmaps -Build -Stage -Pak -Archive -archivedirectory=\"${output_dir}\" -Rocket -Prereqs -Package"
}

def build(ue4_dir,project,project_name,platform,config,output_dir){
    echo "Settings:\nProject: ${project}\nPlatform: ${platform}\nConfig: ${config}\nOutput directory: ${output_dir}"
    bat label: 'UnrealBuildTool', script: "CALL \"${ue4_dir}Engine\\Binaries\\DotNET\\UnrealBuildTool.exe\" -projectfiles -project=\"${project}\" -game -rocket -progress -2019 -Platforms=${platform} PrecompileForTargets = PrecompileTargetsType.Any;"
    bat label: 'Build', script: "CALL \"${ue4_dir}Engine\\Build\\BatchFiles\\Build.bat\" ${project_name}Editor ${platform} ${config} \"${project}\""
}

def listTests(project,platform,config = "Development"){
echo "Settings:\nProject: ${project}\nPlatform: ${platform}\nConfig: ${config}\n"
echo "Ensuring ShaderCompileWorker is built before building project Editor modules..."
bat label: 'ShaderCompileWorker', script: "CALL \"${ue4_dir}Engine\\Build\\BatchFiles\\Build.bat\" ShaderCompileWorker ${platform} ${config}"
echo "Ensure the Editor version of the game has been build..."
bat label: 'Build', script: "CALL \"${ue4_dir}Engine\\Build\\BatchFiles\\Build.bat\" ${project_name}Editor ${platform} ${config} \"${project}\""
echo "Retrieving automation test list..."
def output = bat label: 'list all tests', returnStdout: true, script:"CALL \"${engineRoot}\\Engine\\Binaries\\${platform}\\UE4Editor-Cmd.exe\" \"${project}\" -game -buildmachine -stdout -fullstdoutlogoutput -forcelogflush -unattended -nopause -nullrhi -nosplash -ExecCmds=\"automation List;quit\""
return process(output)
}


def runAllTests(project,platform = "Win64",config = "Development"){
echo "Settings:\nProject: ${project}\nPlatform: ${platform}\nConfig: ${config}\n"
echo "Ensuring ShaderCompileWorker is built before building project Editor modules..."
bat label: 'ShaderCompileWorker', script: "CALL \"${ue4_dir}Engine\\Build\\BatchFiles\\Build.bat\" ShaderCompileWorker ${platform} ${config}"
echo "Ensure the Editor version of the game has been build..."
bat label: 'Build', script: "CALL \"${ue4_dir}Engine\\Build\\BatchFiles\\Build.bat\" ${project_name}Editor ${platform} ${config} \"${project}\""
echo "Run all tests..."
bat label: 'run all tests', script:"CALL \"${engineRoot}\\Engine\\Binaries\\${platform}\\UE4Editor-Cmd.exe\" \"${project}\" -game -buildmachine -stdout -fullstdoutlogoutput -forcelogflush -unattended -nopause -nullrhi -nosplash -ExecCmds=\"automation RunAll;quit\""
}

//[UE4CLI] EXECUTE COMMAND: "C:\ue4\UE_4.23.1_Management\Engine\Binaries\Win64\UE4Editor-Cmd.exe" "C:\jenkins\build latest\Kari.uproject" -game -buildmachine -stdout -fullstdoutlogoutput -forcelogflush -unattended -nopause -nosplash -nullrhi -ExecCmds="automation RunTests Now TEST_CheckBlessing;quit"

def runTests(project,tests,platform = "Win64",config = "Development"){
def testStr = ""
def testsSplit = tests.split(",")
if(testsSplit.length == 0){
    testStr = tests
}else{
    testStr = testsSplit.join("+")
}
echo "Settings:\nProject: ${project}\nPlatform: ${platform}\nConfig: ${config}\n"
echo "Ensuring ShaderCompileWorker is built before building project Editor modules..."
bat label: 'ShaderCompileWorker', script: "CALL \"${ue4_dir}Engine\\Build\\BatchFiles\\Build.bat\" ShaderCompileWorker ${platform} ${config}"
echo "Ensure the Editor version of the game has been build..."
bat label: 'Build', script: "CALL \"${ue4_dir}Engine\\Build\\BatchFiles\\Build.bat\" ${project_name}Editor ${platform} ${config} \"${project}\""
echo "Run tests..."
bat label: 'run tests', script:"CALL \"${engineRoot}\\Engine\\Binaries\\${platform}\\UE4Editor-Cmd.exe\" \"${project}\" -game -buildmachine -stdout -fullstdoutlogoutput -forcelogflush -unattended -nopause -nullrhi -nosplash -ExecCmds=\"automation RunTests Now ${testStr};quit\""
}

/*
Filters:
Engine
Smoke
Stress
Perf
Product
*/
def runFilteredTests(project,filter,platform = "Win64",config = "Development"){

switch(filter){
    case "Engine":
    case "engine":
    case "smoke":
    case "Smoke":
    case "stress":
    case "Stress":
    case "Perf":
    case "perf":
    case "product":
    case "Product":
        echo "Valid Filter!";
    break;
    default:
    error "runFilteredTests() The filter is not Valid! Valid: Engine Smoke Stress Perf Product"
    break;
}


echo "Settings:\nProject: ${project}\nPlatform: ${platform}\nConfig: ${config}\n"
echo "Ensuring ShaderCompileWorker is built before building project Editor modules..."
bat label: 'ShaderCompileWorker', script: "CALL \"${ue4_dir}Engine\\Build\\BatchFiles\\Build.bat\" ShaderCompileWorker ${platform} ${config}"
echo "Ensure the Editor version of the game has been build..."
bat label: 'Build', script: "CALL \"${ue4_dir}Engine\\Build\\BatchFiles\\Build.bat\" ${project_name}Editor ${platform} ${config} \"${project}\""
echo "Run test with filter ${filter}.." 
bat label: 'run filtered tests', script:"CALL \"${engineRoot}\\Engine\\Binaries\\${platform}\\UE4Editor-Cmd.exe\" \"${project}\" -game -buildmachine -stdout -fullstdoutlogoutput -forcelogflush -unattended -nopause -nullrhi -nosplash -ExecCmds=\"automation RunFilter ${filter};quit\""

}

def process(input){
    echo "Tests:"
    def logLine = input.split("\n")
    def tests = []
    logLine.each {
        line ->
        def logData = line.split("Display:")
       if(logData.length == 2){
           tests.add(logData[1])
       }
    }
    return tests;
}