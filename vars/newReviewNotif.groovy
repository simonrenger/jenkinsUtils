def call(
review,
REVIEW_URL,
WEBHOOK,
description = ""
){
    discord.send(
             discord.createMessage("New/Updated Review: ${review}","new",[
                [name:"Perforce review",value:"${review}",inline:true],
                [name:"Jenkins Build",value:"${env.BUILD_NUMBER}",inline:true]
            ],"${REVIEW_URL}${review}","**New Review or Update**\nBuild URL: ${env.BUILD_URL}\nReview Url: ${REVIEW_URL}${review}\nReview Description:\n${description}")
            ,WEBHOOK)
}