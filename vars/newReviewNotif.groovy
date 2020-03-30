def call(
reviewId,
REVIEW_URL,
WEBHOOK,
description = "",
reviewStatusParam = "New/Updated"
){
    discord.send(
             discord.createMessage("${reviewStatusParam} Review: ${reviewId}","new",[
                [name:"Perforce review",value:"${reviewId}",inline:true],
                [name:"Jenkins Build",value:"${env.BUILD_NUMBER}",inline:true]
            ],"${REVIEW_URL}${reviewId}","**${reviewStatusParam} Review**\nBuild URL: ${env.BUILD_URL}\nReview Url: ${REVIEW_URL}${reviewId}\nReview Description:\n${description}")
            ,WEBHOOK)
}