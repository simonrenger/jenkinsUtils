# Jenkins Uitils

A collection of useful `groovy` functions for our Unreal Engine 4 Project with Perforce as source control. This package contains utilities for:

- building with ue4 automation tools
- running tests with ue4 automated test tools
- communicating with p4 and swarm
- communicating with mantis
- communicating with discord
- deploy to steam
- parse logs with the [Log Parser Plugin](https://plugins.jenkins.io/log-parser/)

## White list requirements

Please white list the following in your jenkins: *https://jenkins-domain/scriptApproval/*

`method org.jenkinsci.plugins.p4.groovy.P4Groovy run java.lang.String java.lang.String[]`

## collections of global functions:

`newReviewNotif reviewId, REVIEW_URL, WEBHOOK [, description = "",reviewStatusParam = "New/Updated"]`
notifes a discord webhook about a new review. Will indicate if its a new or an updated review. See `p4c.isReviewUpdate()`

`notifReviewFailed REVIEW_URL, WEBHOOK [, description = ""]`
notifes a discord webhook about that a review faild

`notifReviewSuccess REVIEW_URL, WEBHOOK [, description = ""]`
notifes a discord webhook about that a review sucessed

`notifFailed change_URL, WEBHOOK [, description = ""]`
notifes a discord webhook about that a build faild

`notifSuccess change_URL, WEBHOOK [, description = ""]`
notifes a discord webhook about that a build faild

### example:

```groovy
//... more code
    post { 
        always {
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
```

## discord functions

The discord global variables are just utilites they allow to create a proper discord message as well as send utilities

`discord.createMessage(title,status,fields[,url = null,content = null])`
creates a discord message which then can be send. url and content are optional if not provieded they will not be added to the message.

`discord.send(str,hook)`
sends a message to the provieded webhook. Message can be a string should be a Discord confrom JSON string

`discord.sendFile(file,hook)`
sends a file to the provieded webhook

## log functions

Provides an interface to log warning, errors and infos

`log.error(message)`
logs an error

`log.warning(message)`
logs a waring

`log.info(message)`
logs an info

*Example output:* `Warning: Test warning from log.warning`

`log.setup(rulefile = 'msbuildparserules.txt', rules = null)`
In case you wanted to parse the log, the [Log Parser Plugin](https://plugins.jenkins.io/log-parser/) needs a config file. the call `log.setup()` creates such for you. If you do not provide an argument the default name for the file is `rulefile = 'msbuildparserules.txt'`. If no rules are provied it uses the default rules.

*Default rules:*
```
# Divide into sections based on project compile start
start /BUILD COMMAND/
start /COOK COMMAND/
# Compiler Error
error /(?i)error [A-Z]+[0-9]+:/
error /(?i)Error:/
# Compiler Warning
warning /(?i)warning [A-Z]+[0-9]+:/
warning /(?i)Warning:/
warning /Couldn't find/
```

`log.parse(projectRulePath = 'msbuildparserules.txt',parsingRulesPath = 'msbuildparserules.txt',showGraphs = true)`
argument details see [Log Parser Plugin](https://plugins.jenkins.io/log-parser/). **Important:** This function can only be used if there is a `msbuildparserules.txt` (rule file) present or after the `log.setup` has been called.

## mantis functions

Provides currently just a update function to update reports.

`mantis.setUrl(url)`
needs to be called in order to store the url to mantis globally.

`mantis.setToken(token)`
needs to be called at the beginning to set the token.

`mantis.setup(url,token)`
sets the url and the token

`mantis.update(id,status,resolution,handler = null)`
updates a report based on the id with the status (closed etc.) and the resolution (fixed etc.) and optional the handler (who)

*Example:*

```groovy
pipeline {
   agent any

   stages {
      stage('Hello') {
         steps {
             script{
                mantis.setUrl("https://bugs.tld")
                mantis.setToken("TOKEN")
                mantis.update(9,"closed","fixed")
             }
         }
      }
   }
}
```

> *Comment:* Functions to parse context for #[bugID] [status] [resolutions] is planned. This can be used to parse a git or p4 commit message to close a bug after the build / test has been successful. Or leave a comment on mantis if the test failed.

## perforce (p4c) functions

`p4c` stands for perforce client which makes use of the `p4groovy` plugin from jenkins itself. This collection enables you to manage swarm reviews and retrive the discrcibtion

### perforce

`pull(credential,workspace_template,format = "jenkins-${JOB_NAME}")`
calls `p4sync` under the hood with a writable workspace and parallel runners. This will pull down the repositry. If this is a build with review action it will also pull down the correct review and populate P4_REVIEW / P4_CHANGE

`getCurrentChangelistDescription(credential,client,view_mapping) / getCurrentReviewDescription(credential,client,view_mapping)`
Will request via `p4 describe` via `p4groovy` `p4.run("describe","-s","-S","12344")` this returns the desciption of the *current changelist*. The function will call `getReviewId()` internally to get the current review / changelist number.
*Troubleshoot:* Mostlikely if `<description: restricted, no permission to view>` is the result the review (if called on are review) has been commited.
**Important**: the underlaying call will make the client/workspace WIRTABLE.

`getChangelistDescription(id,credential,client,view_mapping)`
Will request via `p4 describe` via `p4groovy` `p4.run("describe","-s","-S","12344")` this returns the desciption. Mostlikely if `<description: restricted, no permission to view>` is the result the review (if called on are review) has been commited.
**Important**: the underlaying call will make the client/workspace WIRTABLE.

`getReviewId()`
returns the current review Id

`getChangelist()`
returns the current changelist based on evaluate if `P4_CHANGE` exists or the variable `change` or `json`

`getReviewStatus()`
returns the current review status based on evaluate if the variable `status` or `json` exists

`getReviewPass()`
returns the current review pass url based on evaluate if the variable `pass` or `json` exists

`getReviewFail()`
returns the current review fail url based on evaluate if the variable `fail` or `json` exists

`isReviewUpdate()` - *workaround*
returns true if the current review is an update or a new commit (false). 
**Important**: only works if the `pass` / `fail` parameter is given!

`isCommitted()`
helper that returns true/false if a changelist has been already commited!

*Internal* `reviewObject()`
returns a json object of the current review build request.

`swarmUrl(credential,client,mapping)`
returns the swarm review of the current client. All parameters are required! (Calls p4)

*Example:*
```groovy
stage('p4 sync'){
    steps{
        script{
            p4c.pull(P4USER,P4CLIENT)
        }
    }
}
```

### swarm

The `swarm_url` param means the actual swarm url not the review url.

`comment(review,user,ticket,swarm_url,comment)`
leaves a comment at the given swarm review.
**Important**: user needs to be a valid user NOT a credential ID from  jenkins. The ticket needs to be valid and for the same user.

`upVote(review,user,ticket,swarm_url)`
adds a up vote to a review
**Important**: user needs to be a valid user NOT a credential ID from  jenkins. The ticket needs to be valid and for the same user.

`downVote(review,user,ticket,swarm_url)`
adds a down vote to a review
**Important**: user needs to be a valid user NOT a credential ID from  jenkins. The ticket needs to be valid and for the same user.

`approve(review,user,ticket,swarm_url)`
approves a review
**Important**: user needs to be a valid user NOT a credential ID from  jenkins. The ticket needs to be valid and for the same user.

`needsReview(review,user,ticket,swarm_url)`
adds the needs review status to a review
**Important**: user needs to be a valid user NOT a credential ID from  jenkins. The ticket needs to be valid and for the same user.

`needsRevision(review,user,ticket,swarm_url)`
adds the needs revision status to a review
**Important**: user needs to be a valid user NOT a credential ID from  jenkins. The ticket needs to be valid and for the same user.

`archive(review,user,ticket,swarm_url)`
archives a review
**Important**: user needs to be a valid user NOT a credential ID from  jenkins. The ticket needs to be valid and for the same user.

`reject(review,user,ticket,swarm_url)`
rejects a review
**Important**: user needs to be a valid user NOT a credential ID from  jenkins. The ticket needs to be valid and for the same user.

`updateState(review,user,ticket,swarm_url,state)`
Updates the status of a review.
**Important**: user needs to be a valid user NOT a credential ID from  jenkins. The ticket needs to be valid and for the same user.

`ticket(credentials,p4Port)`
Requests a ticket for a user (credentials Jenkins)

`withTicket(credentials,p4Port,Closure body)`
Is a stage in which the ticket will be handes as argument to the closur

*Example:* 

```groovy
p4c.withTicket(env.P4USER,'ssl:swarm.url.tld:1234',{
    ticket->
    echo ticket
})
```

`withSwarmUrl(credentials,client,mapping,Closure body)`
In this stage we provide the swarm url to the body as well as the user

*Example:*

```groovy
    p4c.withTicket(env.P4USER,'ssl:swarm.url.tld:1234',
        {
            ticket->
                p4c.withSwarmUrl(env.P4USER,env.P4CLIENT,env.P4MAPPING,
                { 
                    url,user->
                    p4c.comment(144376,user,ticket,url,"Hallo comment via jenkins")
                    p4c.upVote(145299,user,ticket,url)
                    p4c.needsReview(145299,user,ticket,url)
                }
        )
        }
    )
```

## steam functions

`setup(sourceDir = "..\\",installDir = "..\\steamcmd")`
Will download the `steamcmd.exe` and unzips it in the given folder so the rest of the steam global vars can work.
**Important**: This step is required to run `deploy()` and `deployIf()`!
**Note:** Uses powershell instead of bat.


`depotManifest(depotNumber,contentRoot,localPath="*",depotPath=".",recursive="1",exclude="*.pdb")`
creates the depot manifest file `depot_build_[depotNumber].vdf` in the current dir! It returns the name of the file.

`appManifest(appId,depotNumber,contentroot,steamBranch,isPreview="0",outputdir="output")`
creates the depot manifest file `app_build_[appId].vdf` in the current dir! It returns the name of the file.

*Example*
```groovy
        stage('deploy'){
            steps{
                script{
                    def contentDir = "${OUTPUT_DIR}\\${STEAM_CONTENT_DIR_NAME}"
                    def appManifest = steam.appManifest(STEAM_APP_ID,STEAM_DEPOT_ID,contentDir,STEAM_BRANCH)
                    steam.depotManifest(STEAM_DEPOT_ID,contentDir)
                    //...
                }
            }
        }
```
With different dir:
```groovy
        stage('deploy'){
            steps{
                script{
                    dir("scripts"){
                        def contentDir = "${OUTPUT_DIR}\\${STEAM_CONTENT_DIR_NAME}"
                        def appManifest = steam.appManifest(STEAM_APP_ID,STEAM_DEPOT_ID,contentDir,STEAM_BRANCH)
                        steam.depotManifest(STEAM_DEPOT_ID,contentDir)
                    }
                    //...
                }
            }
        }
```


`deploy(credentials,appManifest,steamGuard = null)`
Will call the `steamcmd` to deploy the game. `depotManifest` and `appManifest` should be executed before hand or on SCM. `steamGuard` can be handed to the function if needed. This function will **FAIL** if your mashine is not auth with steam. For convinance use `deployIf`

`deployIf(credentials,appManifest)`
The same as `deploy` just that in case that `deploy()` fails because of the missing steamguard it will ask your via Jenkins for User Input.

### ue4 functions

`setRoot(root)`
sets the engine root directory. 
**Important** There is no check if the engine directory is correct or valid. Might get added.

`root()`
returns the engine root directory

`build(ue4_dir,project,project_name,platform,config,output_dir)`
Will build the engine. The project parameter needs to contain `.uproject`. Might change. The project name is *just* the project name.

`pack(ue4_dir,project,platform,config,output_dir)`
Will cook and package the engine. The project parameter needs to contain `.uproject`. Might change.


`listTests(project,platform[,config = "Development"])`
Will return a list of all tests. Might be buggy and slow.
**Important**: `setRoot` needs to be called before you can use this function!

`runAllTests(project[,platform = "Win64",config = "Development"])`
Will run all tests of the given project. The project parameter needs to contain the `.uproject`.
**Important**: `setRoot` needs to be called before you can use this function!

`runTests(project,tests[,platform = "Win64",config = "Development"])`
Runs one or multiple tests. The tests need to be seperated by a `,`! The project  parameter needs to contain the `.uproject`.
**Important**: `setRoot` needs to be called before you can use this function!

*Example:*
`ue4.runTests(PROJECT,"TEST_CheckDamage")`

or multiple tests

`ue4.runTests(PROJECT,"TEST_CheckDamage,TEST_CheckBlessing")`

`runFilteredTests(project,filter[,platform = "Win64",config = "Development"])`
Runs filtered tests: 
- Engine
- Smoke
- Stress
- Perf
- Product

**Important**: `setRoot` needs to be called before you can use this function!

*Example:*
```groovy
script{
    ue4.runFilteredTests(PROJECT,"smoke",PLATFORM)
}
```





