def unity_ = ""

def setRoot(root){
    unity_ = unity_
}

// Build a release version of a Unity project using the projects C# build script.
def build(projectPath,executeMethod,executeMethodParams = [], logFile = "log{$env.JOB_BASE_NAME}${env.BUILD_NUMBER}.txt"){
    def params = ""
    if(executeMethodParams.length > 0){
        executeMethodParams.each{
            -> value
            params = "${params} \"${value}\""
        }
    }
    bat label: 'build unity', script: "CALL \"${unity_}\"  -batchmode -quit -projectPath \"${projectPath}\" -logFile \"${logFile}\" -executeMethod ${executeMethod} ${params}"
}