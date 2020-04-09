import groovy.json.JsonOutput
import groovy.json.JsonSlurper
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

def mention(message,filter,filterFile="discord_filter.json"){
    if(!fileExists(filterFile)){
        error("Could not find ${filterFile}")
        return null;
    }
    def content = readFile file: filterFile
    def jsonSlurper = new JsonSlurper()
    def filterObject = jsonSlurper.parseText(content)
    filterObject[filter].each{ key, value -> 
    message = message.replace("${key}","<@${value}>")
    }
    return  message
}

def mentionGroup(message,filterFile="discord_filter.json"){
    return mention(message,"discord_groups",filterFile)
}

def mentionUser(message,filterFile="discord_filter.json"){
    return mention(message,"discord_users",filterFile)
}

//creates a message from a swarm review object and translates this into a format
def swarmReviewToMessage(reviewObject,autoMention = true){
    def message = ""
    //set up reviwer string:
    def participantsList = swarm.getReviewParticipants(reviewObject)
    def participants = ""
    participantsList.each{
        key,value ->
        participants = "${key} ${participants}"
    }
    def desc = swarm.getReviewDescription(reviewObject)
    if(autoMention == true){
        desc = mentionGroup(desc)
        desc = mentionUser(desc)
        participants = mentionUser(participants)
    }
      return "**Review Details**\n**Review ID**: ${swarm.getReviewId(reviewObject)}\n**Author**: ${swarm.getReviewAuthor(reviewObject)}\r**Rviewer:** ${participants}\n**Description:**\n${desc}"
}
