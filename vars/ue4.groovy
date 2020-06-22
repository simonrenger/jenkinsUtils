import groovy.json.JsonOutput
import groovy.json.JsonSlurper

def engineRoot = ""

def setRoot(root){
    engineRoot = root
}

def root(){
    return engineRoot
}

def pack(ue4_dir,project,platform,config,output_dir,extra_args=null){
    if(extra_args == null){
        extra_args = ""
    }else{
        echo "Executes with extra args: ${extra_args}"
    }
    echo "Settings:\nProject: ${project}\nPlatform: ${platform}\nConfig: ${config}\nOutput directory: ${output_dir}"
    bat label: 'Exec Package', script: "CALL \"${ue4_dir}Engine\\Build\\BatchFiles\\RunUAT.bat\" BuildCookRun -project=\"${project}\" -noP4 -Distribution -TargetPlatform=${platform} -Platform=${platform} -ClientConfig=${config} -ServerConfig=${config} -Cook -allmaps -Build -Stage -Pak -Archive -archivedirectory=\"${output_dir}\" -Rocket -Prereqs -Package ${extra_args}"
}

def cook(ue4_dir,project,platform,config,extra_args=null){
    if(extra_args == null){
        extra_args = ""
    }else{
        echo "Executes with extra args: ${extra_args}"
    }
    echo "Settings:\nProject: ${project}\nPlatform: ${platform}\nConfig: ${config}\nOutput directory: ${output_dir}"
    bat label: 'Exec Package', script: "CALL \"${ue4_dir}Engine\\Build\\BatchFiles\\RunUAT.bat\" BuildCookRun -project=\"${project}\" -noP4 -Distribution -TargetPlatform=${platform} -Platform=${platform} -ClientConfig=${config} -ServerConfig=${config} -Cook -allmaps -skipstage ${extra_args}"
}

def build(ue4_dir,project,project_name,platform,config,output_dir,logDir = "${env.WORKSPACE}\\ue4_pack_log_${env.BUILD_NUMBER}.log"){
    echo "Settings:\nProject: ${project}\nPlatform: ${platform}\nConfig: ${config}\nOutput directory: ${output_dir}"
    bat label: 'UnrealBuildTool', script: "CALL \"${ue4_dir}Engine\\Binaries\\DotNET\\UnrealBuildTool.exe\" -projectfiles -project=\"${project}\" -game -rocket -progress -2019 -log=\"${logDir}\" -Platforms=${platform} PrecompileForTargets = PrecompileTargetsType.Any;"
    if(config == "Development"){
        bat label: 'Build Engine', script: "CALL \"${ue4_dir}Engine\\Build\\BatchFiles\\Build.bat\" ${project_name}Editor ${platform} ${config} \"${project}\" -log=\"${logDir}\""
    }else{
        echo "Do not build Engine in ${config}. We build only in Development."
    }
}

def createProjectVar(project_name,path){
    return "${path}\\${project_name}.uproject"
}

def listTests(project_name,project_path,platform,config = "Development"){
def project = createProjectVar(project_name,project_path)
//create project
echo "Settings:\nProject: ${project}\nPlatform: ${platform}\nConfig: ${config}\n"
echo "Ensuring ShaderCompileWorker is built before building project Editor modules..."
bat label: 'ShaderCompileWorker', script: "CALL \"${ue4_dir}Engine\\Build\\BatchFiles\\Build.bat\" ShaderCompileWorker ${platform} ${config}"
echo "Ensure the Editor version of the game has been build..."
bat label: 'Build', script: "CALL \"${ue4_dir}Engine\\Build\\BatchFiles\\Build.bat\" ${project_name}Editor ${platform} ${config} \"${project}\""
echo "Retrieving automation test list..."
def output = bat label: 'list all tests', returnStdout: true, script:"CALL \"${engineRoot}\\Engine\\Binaries\\${platform}\\UE4Editor-Cmd.exe\" \"${project}\" -game -buildmachine -stdout -fullstdoutlogoutput -forcelogflush -unattended -nopause  -nosound  -nullrhi -nosplash -ExecCmds=\"automation List;quit\""
return process(output)
}


def runAllTests(project_name,project_path,platform = "Win64",config = "Development",extra_args = null){
def project = createProjectVar(project_name,project_path)
echo "Settings:\nProject: ${project}\nPlatform: ${platform}\nConfig: ${config}\n"
echo "Ensuring ShaderCompileWorker is built before building project Editor modules..."
bat label: 'ShaderCompileWorker', script: "CALL \"${ue4_dir}Engine\\Build\\BatchFiles\\Build.bat\" ShaderCompileWorker ${platform} ${config}"
echo "Ensure the Editor version of the game has been build..."
bat label: 'Build', script: "CALL \"${ue4_dir}Engine\\Build\\BatchFiles\\Build.bat\" ${project_name}Editor ${platform} ${config} \"${project}\""
echo "Run all tests..."
//bat label: 'run all tests', script:"CALL \"${engineRoot}\\Engine\\Binaries\\${platform}\\UE4Editor-Cmd.exe\" \"${project}\" -buildmachine -stdout -fullstdoutlogoutput -forcelogflush -unattended  -nosound  -nopause -nullrhi -nosplash -ExecCmds=\"automation RunAll;quit\""
runEditor("run filtered tests",project,platform,"${extra_args} -ExecCmds=\"automation RunAll;quit\"")
}

def runTestFile(project_name,project_path,testFilePath,platform = "Win64",config = "Development",extra_args = null){
    // load file
    def content = readFile file: testFilePath
    def jsonSlurper = new JsonSlurper()
    def testsJson = jsonSlurper.parseText(content)
    def tests = []
    testsJson['tests'].each{ value -> 
      tests += value+"," 
    }
    runTests(project_name,project_path,tests,platform,config,extra_args)
}

def runTests(project_name,project_path,tests,platform = "Win64",config = "Development",extra_args = null){
def project = createProjectVar(project_name,project_path)
def testStr = ""
def testsSplit = tests.split(",")
if(testsSplit.length == 0){
    testStr = tests
}else{
    testStr = testsSplit.join("+")
}
if(extra_args == null){
    extra_args = ""
}else{
    echo "Extra arguments: ${extra_args}"
}
echo "Settings:\nProject: ${project}\nPlatform: ${platform}\nConfig: ${config}\n"
echo "Ensuring ShaderCompileWorker is built before building project Editor modules..."
bat label: 'ShaderCompileWorker', script: "CALL \"${ue4_dir}Engine\\Build\\BatchFiles\\Build.bat\" ShaderCompileWorker ${platform} ${config}"
echo "Ensure the Editor version of the game has been build..."
bat label: 'Build', script: "CALL \"${ue4_dir}Engine\\Build\\BatchFiles\\Build.bat\" ${project_name}Editor ${platform} ${config} \"${project}\""
echo "Run tests..."

//bat label: 'run tests', script:"CALL \"${engineRoot}\\Engine\\Binaries\\${platform}\\UE4Editor-Cmd.exe\" \"${project}\" -buildmachine -stdout -fullstdoutlogoutput -forcelogflush -unattended -nosound -nopause -nosplash -nullrhi ${extra_args} -ExecCmds=\"automation RunTests Now ${testStr};quit\""
runEditor("run filtered tests",project,platform,"${extra_args} -ExecCmds=\"automation RunTests Now ${testStr};quit\"")
}

/*
Filters:
Engine
Smoke
Stress
Perf
Product
*/
def runFilteredTests(project_name,project_path,filter,platform = "Win64",config = "Development"){
def project = createProjectVar(project_name,project_path)

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
//bat label: 'run filtered tests', script:"CALL \"${engineRoot}\\Engine\\Binaries\\${platform}\\UE4Editor-Cmd.exe\" \"${project}\" -buildmachine -stdout -fullstdoutlogoutput -forcelogflush -unattended -nopause  -nosound  -nullrhi -nosplash -ExecCmds=\"automation RunFilter ${filter};quit\""
runEditor("run filtered tests",project,platform,"-ExecCmds=\"automation RunFilter ${filter};quit\"")
}

//https://docs.unrealengine.com/en-US/Engine/Basics/Redirectors/index.html
//TODO: implement function
def fixupRedirectors (){
    echo "fixupRedirectors"
}

//FIXME: is not really working as expected
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


def runEditor(label,project,platform,args){ 
bat label: "runEditor: ${label}", script:"CALL \"${engineRoot}\\Engine\\Binaries\\${platform}\\UE4Editor-Cmd.exe\" \"${project}\" -buildmachine -stdout -fullstdoutlogoutput -forcelogflush -unattended -nopause  -nosound  -nullrhi -nosplash ${args}"   
}