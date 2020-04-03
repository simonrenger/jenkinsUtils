def call(dir,throwError = true){
    if(!dirExists(dir)){
        powershell label: 'create dir', script: "mkdir ${dir}"
    }else{
        echo "Cannot create Dir. It already exists!"
        if(throwError){
            error()
        }
    }
}