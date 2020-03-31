// add external lib to get access to some utility stuff
library(identifier: 'jenkinsUtils@master', retriever: modernSCM([$class: 'GitSCMSource', credentialsId: '', remote: 'https://github.com/simonrenger/jenkinsUtils.git']))

pipeline {
    agent {
        //set custom workspace
        node {
            label ""
            customWorkspace "D:\\jenkins\\${env.JOB_NAME}"
        }
    }
   environment {

       // building / packaging
        UE4_DIR = 'C:\\Program Files\\Epic Games\\UE_4.23'
        OUTPUT_DIR = "${env.WORKSPACE}\\Dist"
        CONFIG = 'Development'
        PROJECT_NAME = "MyCoolProject"
        PROJECT = "${env.WORKSPACE}\\MyCoolProject.uproject"
        PLATFORM = 'Win64'

        //perfroce user
        P4USER = 'P4Username'
        P4CLIENT = "jenkins-master-Builder"
        P4MAPPING = "//Projects/MyWorkspace/... //jenkins-master-Builder/..."

        // deployment
        STEAM_BUILDER = "..\\steamcmd\\steamcmd.exe"
        STEAM_APP_ID = "123345"
        STEAM_DEPOT_ID = "1233451"
        STEAM_BRANCH = "development-debug"
        STEAM_CONTENT_DIR_NAME = "WindowsNoEditor"
        STEAM_ACCOUNT = "steamaccount"
        //discord:
        WEBHOOK = "WEEB_HOOK_URL"
        CHANGE_URL = "https://swarm.company.tld:8443/changes/"
    }
    stages {
        stage('steam setup'){
            steps{
                script{
                    steam.setup()
                }
                }
        }
        stage('p4 sync'){
            steps{
                script{
                    p4c.pull(P4USER,P4CLIENT)
                }
                }
            }
        stage('ue4 package'){
            steps{
                 script{
                    ue4.pack(UE4_DIR,PROJECT,PLATFORM,CONFIG,OUTPUT_DIR)
                 }
            }
        }
        stage('deploy'){
            steps{
                script{
                    def contentDir = "${OUTPUT_DIR}\\${STEAM_CONTENT_DIR_NAME}"
                    def appManifest = steam.appManifest(STEAM_APP_ID,STEAM_DEPOT_ID,contentDir,STEAM_BRANCH)
                    steam.depotManifest(STEAM_DEPOT_ID,contentDir)
                    steam.deployif(STEAM_ACCOUNT,"${env.workspace}\\${appManifest}")
                }
            }
        }
    } // end stages
    post { 
        // parse logs and clean up workspace
        always {
            script{
                log.setup()
                log.parse()
            }
           cleanWs()
        }
        success {
            script{
                    notifSuccess CHANGE_URL, WEBHOOK
            }
        }
        failure {
            script{
                    notifFailed CHANGE_URL, WEBHOOK
            }
        }
    }
}

