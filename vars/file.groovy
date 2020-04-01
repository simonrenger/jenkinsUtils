// why? This function allowes for checking outside of the workspace while `fileExists` only within the boundaries of the workspace
def dirExists(folder){
    def result = powershell(label: 'check if folder exsits', returnStdout: true, script: "Test-Path '${folder}'")
    if(result.replaceAll("\\s","").equals("True")){
        return true
    }
    return false
}