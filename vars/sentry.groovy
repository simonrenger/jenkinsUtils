// .\sentry-cli-Windows-x86_64.exe --url http://sentry.zsinfo.nl:9000/ --auth-token 64c90a3f9d3a493d848933447b10bbe3490b5be041ae4799a1ac20133bac852d upload-dif -o management -p managment ./Kari

def upload(
    url,
    token,
    org,
    project,
    folder){

def sourceDir = "${env.WORKSPACE}\\sentry\\"

if(!fileExists("${sourceDir}sentry-cli-Windows-x86_64.exe")){
     download("https://github.com/getsentry/sentry-cli/releases/download/1.53.0/sentry-cli-Windows-x86_64.exe","${sourceDir}sentry-cli-Windows-x86_64.exe")
 }else{
     echo "Sentry CLI is set up and does not need to be downloaded!"
}


bat label: 'run sentry-cli', script: "${sourceDir}sentry-cli-Windows-x86_64.exe --url ${url} --auth-token ${token} upload-dif -o ${org} -p ${project} ${folder}"
}