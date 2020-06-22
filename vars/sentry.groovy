// .\sentry-cli-Windows-x86_64.exe --url http://sentry.zsinfo.nl:9000/ --auth-token 64c90a3f9d3a493d848933447b10bbe3490b5be041ae4799a1ac20133bac852d upload-dif -o management -p managment ./Kari

def sentry__ = null

def setUp(
    sourceDir = "..\\",
    installDir = "..\\sentry",
    org = null,
    project = null,
    url = null,
    token = null
){

    sentry__ = [
    token:null,
    url:null,
    org:null,
    project:null,
    app_path:null
    ]

    if(url != null)
    {
      sentry__.url = url  
    }else{
        sentry__.url = env.SENTRY_URL
    }

    if(token != null){
        sentry__.token = token;
    }else{
        sentry__.token = env.SENTRY_TOKEN
    }

    if(org != null){
        sentry__.org = org;
    }else{
        sentry__.org = env.SENTRY_ORG
    }

    if(project != null){
        sentry__.project = project;
    }else{
        sentry__.project = env.SENTRY_PROJECT
    }
    // download if not exists:
    sourceDir = "${env.workspace}\\${sourceDir}"
    if(!fileExists("${sourceDir}sentry-cli-Windows-x86_64.exe")){
        download("https://github.com/getsentry/sentry-cli/releases/download/1.53.0/sentry-cli-Windows-x86_64.exe","${sourceDir}sentry-cli-Windows-x86_64.exe")
    }else{
        echo "Sentry CLI is set up and does not need to be downloaded!"
    }
    sentry__.app_path = "${sourceDir}sentry-cli-Windows-x86_64.exe"
}

def upload(folder){
bat label: 'run sentry-cli', script: "${sentry__.app_path} --url ${sentry__.url} --auth-token ${sentry__.token} upload-dif -o ${sentry__.org} -p ${sentry__.project} ${folder}"
}