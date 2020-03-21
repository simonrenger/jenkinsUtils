def call(
REVIEW_URL,
WEBHOOK,
description = ""
){
    discord.send(
             discord.createMessage(":x: Build #${env.BUILD_NUMBER} - Review: ${env.P4_REVIEW} Failed Building","failed",[
                [name:"Perforce Revision",value:"${env.P4_CHANGELIST}",inline:true],
                [name:"Perforce Review",value:"${env.P4_REVIEW}",inline:true]
            ],"${REVIEW_URL}${env.P4_REVIEW}","Build URL: ${env.BUILD_URL}\nBuild Log URL: ${env.BUILD_URL}console/ \nBuild Parsed Log URL: ${env.BUILD_URL}parsed_console/ \nReview Url: ${REVIEW_URL}${env.P4_REVIEW}Review Description:\n${description}")
            ,WEBHOOK)
}