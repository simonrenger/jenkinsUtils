def call(dir,name,throwError = true){
    if(!dirExists(dir)){
        echo 
        powershell label: 'create dir', script: "mkdir -Path '${dir}' -Name '${name}'"
    }else{
        echo "Cannot create Dir. It already exists!"
        if(throwError){
            error()
        }
    }
}