def setup(rulefile = 'msbuildparserules.txt', template = null){

if(template == null){
template = '''
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

if (!fileExists(rulefile)) {
    writeFile file: rulefile, text: template
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