def unity_ = ""

def setRoot(root){
    unity_ = unity_
}

// Build a release version of the VRTower Unity project using the projects C# build script.
def build(projectPath,executeMethod,level,output, logFile = "log{$env.JOB_BASE_NAME}${env.BUILD_NUMBER}.txt"){
    bat label: 'build unity', script: "CALL \"${unity_}\"  -batchmode -quit -projectPath \"${projectPath}\" -logFile \"${logFile}\" -executeMethod ${executeMethod} \"${level}\" \"${output}\""
}