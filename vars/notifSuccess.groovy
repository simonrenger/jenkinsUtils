def call(
CHANGE_URL,
WEBHOOK
){
    discord.send(
        discord.createMessage(":white_check_mark: Build #${env.BUILD_NUMBER} - Last Perforce Revision: ${env.P4_CHANGELIST} Passed Building","ok",[
                    [name:"Last Perforce Revision",value:"${env.P4_CHANGELIST}",inline:true]
                ],"${CHANGE_URL}${env.P4_CHANGELIST}","Build URL: ${env.BUILD_URL}\nChange Url: ${CHANGE_URL}${env.P4_CHANGELIST}")
                ,WEBHOOK)
}