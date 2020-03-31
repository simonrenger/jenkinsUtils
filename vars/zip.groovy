def packFolder(folder,archivename,format="zip"){
    if(format == "zip"){
        powershell label: "Pack folder with ps", script: "Compress-Archive -Path '${folder}' -DestinationPath '${archivename}.${format}'"
    }else{
        bat label: 'Pack files 7z', script: "7z a -t${format} ${archivename}.${format} -r ${folder}"
    }
}

def unpack(filename,destination,format="zip",force = true){
    if(filename.contains(".7z") && format == "zip"){
        format = "7z"
    }
    switch(format){
        case "zip":
            def forces = ""
            if(force == true){
                forces = "-Force"
            }
            powershell label: "Unzip File", script: "Expand-Archive -LiteralPath '${filename}' ${forces} -DestinationPath '${destination}'"
        break;
        case "bzip2":
        case "wim":
        case "xz":
        case "gzip":
        case "tar":
        case "7z":
            bat label: 'Unzip with 7z',  script: "7z x ${filename} -o${destination} -y -r"
        break;
    }
}

def retrieveAndUnpackArchive(url,filename,destination,format = "zip",credentials = null){
    download url, filename, credentials
    unpack(filename,destination,format,credentials)
    powershell label: "Clean up", script: "rm ${filename}"
    writeFile file: "${filename}.txt", text: "downloaded: ${filename}"
}