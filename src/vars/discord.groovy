import groovy.json.JsonOutput
import hudson.model.*

def call(Closure body) {
    node {
        body()
    }
}
/*
def createMessage(title,status,fields,url,content = null){

def color = 16711680

if ( status == "ok"){
    color = 65280
}

def  body = [embeds: 
[[
    title: title,
    color: color,
    fields: fields
]
]
]
    if (url != null){
        body.embeds[0].url = url
    }
    if(content !=null){
        body.content = content
    }
return JsonOutput.toJson(body).replace('"','""')
}

def send(str,hook){
    bat label: 'sendDiscord', script: "curl -H \"Content-Type: application/json\" -X POST -d \"${str}\" ${hook}"
}

def sendFile(file,hook){
       bat label: 'sendFileViaDiscord', script: "curl -F \"file=@${file}\" ${hook}" 
}

def halloWorld(){
    echo "Hallo World"
}
*/
