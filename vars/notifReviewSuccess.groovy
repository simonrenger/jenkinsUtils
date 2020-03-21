def call(
REVIEW_URL,
WEBHOOK,
description = ""
){
    discord.send(
        discord.createMessage(":white_check_mark: Build #${env.BUILD_NUMBER} - Review: ${env.P4_REVIEW} Passed Building","ok",[
                    [name:"Perforce Revision",value:"${env.P4_CHANGELIST}",inline:true],
                    [name:"Perforce Review",value:"${env.P4_REVIEW}",inline:true]
                ],"${REVIEW_URL}${env.P4_REVIEW}","Build URL: ${env.BUILD_URL}\nReview Url: ${REVIEW_URL}${env.P4_REVIEW}\nReview Description:\n${description}")
                ,WEBHOOK)
}