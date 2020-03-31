def call(
    url,
    filename,
    credentials = null
){
    if(credentials == null){
        powershell label: "Download ${filename}", script: "Invoke-WebRequest ${url} -O '${filename}'"
    }else{
        withCredentials([usernamePassword(credentialsId: credentials, passwordVariable: 'BASIC_AUTH_PASSWORD', usernameVariable: 'BASIC_AUTH_USERNAME' )]) {
        bat label: "Download ${filename} with basic auth", script: "curl --user ${BASIC_AUTH_USERNAME}:${BASIC_AUTH_PASSWORD} ${url} -o ${filename}"
        }
    }
}