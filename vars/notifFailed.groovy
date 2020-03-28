def call(
CHANGE_URL,
WEBHOOK
){
    discord.send(
             discord.createMessage(":x: Build #${env.BUILD_NUMBER} - Last Perforce Revision: ${env.P4_CHANGELIST} Failed Building","failed",[
                [name:"Last Perforce Revision",value:"${env.P4_CHANGELIST}",inline:true]
            ],"${CHANGE_URL}${env.P4_CHANGELIST}","**Status: Failed**\nBuild URL: ${env.BUILD_URL}\nBuild Log URL: ${env.BUILD_URL}console/ \nBuild Parsed Log URL: ${env.BUILD_URL}parsed_console/ \nChange Url: ${CHANGE_URL}${env.P4_CHANGELIST}")
            ,WEBHOOK)
}