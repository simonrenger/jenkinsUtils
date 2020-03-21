def call(
REVIEW_URL,
WEBHOOK,
description = ""
){
    discord.send(
             discord.createMessage("New Review: ${env.P4_REVIEW}","new",[
                [name:"Perforce review",value:"${env.P4_REVIEW}",inline:true],
                [name:"Jenkins Build",value:"${env.BUILD_NUMBER}",inline:true]
            ],"${REVIEW_URL}${env.P4_REVIEW}","Build URL: ${env.BUILD_URL}\nReview Url: ${REVIEW_URL}${env.P4_REVIEW}\nReview Description:\n${description}")
            ,WEBHOOK)
}