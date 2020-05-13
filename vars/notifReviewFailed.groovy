def call(
REVIEW_URL,
WEBHOOK,
description = ""
){
    def build_stage = ""
    if(log.lastStage() != null){
        build_stage = "Build Stage: ${log.lastStage()}\n";
    }
    discord.send(
             discord.createMessage(":x: Build #${env.BUILD_NUMBER} - Review: ${env.P4_REVIEW} Failed Building","failed",[
                [name:"Perforce Revision",value:"${env.P4_CHANGELIST}",inline:true],
                [name:"Perforce Review",value:"${env.P4_REVIEW}",inline:true]
            ],"${REVIEW_URL}${env.P4_REVIEW}","**Status: Failed**\n${build_stage}Build URL: ${env.BUILD_URL}\nBuild Log URL: ${env.BUILD_URL}console/ \nBuild Parsed Log URL: ${env.BUILD_URL}parsed_console/ \nReview Url: ${REVIEW_URL}${env.P4_REVIEW}\nReview Description:\n${description}")
            ,WEBHOOK)
}