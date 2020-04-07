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
        REVIEW_URL = "https://swarm.company.tld:8443/reviews/"
    }
   stages {
        stage('p4 sync'){
            steps{
                script{
                    p4c.pull(P4USER,P4CLIENT)
                }
                }
            }
        stage('ue4 build'){
            steps{
                 script{
                    ue4.build(UE4_DIR,PROJECT,PROJECT_NAME,PLATFORM,CONFIG,OUTPUT_DIR)
                 }
            }
        }
        stage('ue4 package'){
            steps{
                 script{
                     //cookes and builds the ue4 project
                    ue4.pack(UE4_DIR,PROJECT,PLATFORM,CONFIG,OUTPUT_DIR)
                 }
            }
        }
        stage('single test'){
            steps{
                 script{
                  ue4.setRoot(UE4_DIR)
                  ue4.runTests(PROJECT_NAME,"${env.WORKSPACE}","TEST_HEALTH")
                 }
            }
                }
        stage('multiple tests'){
                steps{
                    script{
                    ue4.runTests(PROJECT_NAME,"${env.WORKSPACE}","TEST_HEALTH,TEST_DAMAGE")
                    }
                }
            }
        stage('smoke tests'){
                steps{
                    script{
                    ue4.runFilteredTests(PROJECT_NAME,"${env.WORKSPACE}","smoke",PLATFORM)
                    }
                }
            }
        stage('engine tests'){
                steps{
                    script{
                    ue4.runFilteredTests(PROJECT_NAME,"${env.WORKSPACE}","engine",PLATFORM)
                    }
                }
            }
        stage('stress tests'){
                steps{
                    script{
                    ue4.runFilteredTests(PROJECT_NAME,"${env.WORKSPACE}","stress",PLATFORM)
                    }
                }
            }
        stage('pref tests'){
                steps{
                    script{
                    ue4.runFilteredTests(PROJECT_NAME,"${env.WORKSPACE}","pref",PLATFORM)
                    }
                }
            }
        stage('all tests'){
                steps{
                    script{
                    ue4.runAllTests(PROJECT_NAME,"${env.WORKSPACE}",PLATFORM)
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
                    def description = p4c.getCurrentChangelistDescription(P4USER,P4CLIENT,P4MAPPING)
                    notifReviewSuccess REVIEW_URL, WEBHOOK, description
                    try{
                    bat label: 'inform swarm', script: "curl \"${pass}\"" // makes sure that swarm shows the build is good
                    }catch(err){
                        echo "no pass parameter was given!"
                    }
            }
        }
        failure {
            script{
                    def description = p4c.getCurrentReviewDescription(P4USER,P4CLIENT,P4MAPPING)
                    notifReviewFailed REVIEW_URL, WEBHOOK, description
                    try{
                        bat label: 'inform swarm', script: "curl \"${fail}\""  // makes sure that swarm shows the build is bad
                    }catch(err){
                        echo "no fail param was given!"
                    }
            }
        }
    }
}
