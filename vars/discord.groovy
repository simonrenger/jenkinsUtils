import groovy.json.JsonOutput
import hudson.model.*

def createMessage(title,status,fields,url,content = null){

def color = 16711680

if ( status == "ok"){
    color = 65280
}else if(status == "new"){
    color = "359360"
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

def mention(message,filter,filterFile="discord_filter.json",tag=""){
    def content = readFile encoding: 'UTF-8', file: filterFile
    echo content
    def jsonSlurper = new JsonSlurper()
    def filterObject = jsonSlurper.parseText(content)
    filterObject[filter].each{ key, value -> 
    message = message.replace("${tag}${key}","<@${value}>")
    }
    return  message
}

def mentionGroup(message,filterFile="discord_filter.json"){
    return mention(message,"discord_groups",filterFile,"#")
}

def mentionUser(message,filterFile="discord_filter.json"){
    return mention(message,"discord_users",filterFile)
}

