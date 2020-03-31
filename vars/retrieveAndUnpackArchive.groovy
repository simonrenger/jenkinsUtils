def call(url,archivename,destination,format = "zip",credentials = null){
    download url, archivename, credentials
    unpack(archivename,destination,format,credentials)
    powershell label: "Clean up", script: "rm ${archivename}"
    writeFile file: "${archivename}.txt", text: "downloaded: ${archivename}"
}