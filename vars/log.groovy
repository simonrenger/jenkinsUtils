def setup(rulefile = 'msbuildparserules.txt', rules = null){

if(rules == null){
rules = '''
# Divide into sections based on project compile start
start /BUILD COMMAND/
start /COOK COMMAND/
# Compiler Error
error /(?i)error [A-Z]+[0-9]+:/
error /(?i)Error:/
# Compiler Warning
warning /(?i)warning [A-Z]+[0-9]+:/
warning /(?i)Warning:/
warning /Couldn't find/
'''
}

if (!fileExists(rulefile) || rules != null) {
    writeFile file: rulefile, text: rules
}
}

def warning(message){
    echo "Warning: ${message}"
}

def error(message){
    echo "Error: ${message}"
}

def info(message){
    echo "Info: ${message}"
}

def parse(projectRulePath = 'msbuildparserules.txt',parsingRulesPath = 'msbuildparserules.txt',showGraphs = true){
    logParser(projectRulePath:projectRulePath, parsingRulesPath: parsingRulesPath , showGraphs: showGraphs, useProjectRule: true)
}

def this(){
    env.GLOBAL_STAGE_NAME = env.STAGE_NAME
}

def lastStage(){
    return env.GLOBAL_STAGE_NAME
}