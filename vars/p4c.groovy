import groovy.json.JsonSlurper

def getChangelistDescription(id,credential,client,view_mapping){
    def p4s = p4(credential: credential, workspace: manualSpec(charset: 'none', cleanup: false, name: client, pinHost: false, spec: clientSpec(allwrite: true, backup: true, changeView: '', clobber: false, compress: false, line: 'LOCAL', locked: false, modtime: false, rmdir: false, serverID: '', streamName: '', type: 'WRITABLE', view: view_mapping)))
    def list = p4s.run('describe', '-s', '-S', "${id}")
    def desc = ""
    for(def item : list) {
        for (String key : item.keySet()) {
            if(key == "desc"){
		        desc = item.get(key)
		        break
            }
        }
	}
	return desc
}

def ticket(credentials,p4Port){
    def ticket = ""
    withCredentials([usernamePassword(credentialsId: credentials, passwordVariable: 'USERPASS', usernameVariable: 'USER' )]) {
    bat label: '', script: "echo %USERPASS%| p4 -p ${p4Port} -u %USER% trust -y"  
    def status = bat(label: 'request ticket', script: "echo %USERPASS%| p4 -p ${p4Port} -u %USER% login -ap", returnStdout: true)
    echo "status: ${status}"
    def tickets = status.split('\n')
    ticket = tickets[tickets.length-1]
    }
    return ticket
}

def withTicket(credentials,p4Port,Closure body){
    def p4Ticket = ticket(credentials,p4Port)
    body(p4Ticket)
}

def withSwarmUrl(credentials,client,mapping,Closure body){
        withCredentials([usernamePassword(credentialsId: credentials, passwordVariable: 'p4USERPASS', usernameVariable: 'p4USER' )]) {
            def url = swarmUrl(credentials,client,mapping)
            body(url,env.p4User,env.p4USERPASS)
        }
}

// sets up swarm
def withSwarm(credentials,p4Port,client,mapping,Closure body){
    withTicket(credentials,p4Port,
        {
            ticket->
                withSwarmUrl(env.P4USER,env.P4CLIENT,env.P4MAPPING,
                    { 
                        url,user->
                        swarm.setup(user,ticket,url)
                        body(user,ticket,url)
                        swarm.clear();
                    }
                )
        }
    )
}
def comment(review,user,ticket,swarm_url,comment){
    swarm.setup(user,ticket,swarm_url)
    swarm.comment(review,comment)
}

def upVote(review,user,ticket,swarm_url){
    swarm.setup(user,ticket,swarm_url)
    swarm.upVote(review)
}

def downVote(review,user,ticket,swarm_url){
    swarm.setup(user,ticket,swarm_url)
    swarm.downVote(review)
}

def approve(review,user,ticket,swarm_url){
    swarm.setup(user,ticket,swarm_url)
    swarm.approve(review)
}

def needsReview(review,user,ticket,swarm_url){
    swarm.setup(user,ticket,swarm_url)
    swarm.needsReview(review)
}
def needsRevision(review,user,ticket,swarm_url){
    swarm.setup(user,ticket,swarm_url)
    swarm.needsRevision(review)
}
def archive(review,user,ticket,swarm_url){
    swarm.setup(user,ticket,swarm_url)
    swarm.archive(review)
}
def reject(review,user,ticket,swarm_url){
    swarm.setup(user,ticket,swarm_url)
    swarm.reject(review)
}

def updateState(review,user,ticket,swarm_url,state){
    swarm.setup(user,ticket,swarm_url)
    swarm.updateState(review,state)
}


def swarmUrl(credential,client,mapping){
    def p4s = p4(credential: credential, workspace: manualSpec(charset: 'none', cleanup: false, name: client, pinHost: false, spec: clientSpec(allwrite: true, backup: true, changeView: '', clobber: false, compress: false, line: 'LOCAL', locked: false, modtime: false, rmdir: false, serverID: '', streamName: '', type: 'WRITABLE', view: mapping)))
    def prop = p4s.run("property","-l","-n","P4.Swarm.URL")
    for(def item : prop) {
        for (String key : item.keySet()) {
            if(key == "value")
            {
                return item.get(key)
            }
        }
    }
    return ""
}

// just a workaround till the plugin is enabled
def reviewObject(){
    if(params.json != null){
    def jsonSlurper = new JsonSlurper()
    return jsonSlurper.parseText(params.json)
    }
    return [change:null,review:null,pass:null,status:null,fail:null]
}

def getReviewStatus(){
    try{
        return "${status}"
    }catch(err){
        def reviewobj = reviewObject()
        return reviewobj.status;
    }
}

def getReviewPass(){
    try{
        return "${pass}"
    }catch(err){
        def reviewobj = reviewObject()
        return reviewobj.pass;
    }
}

def getReviewFail(){
    try{
        return "${fail}"
    }catch(err){
        def reviewobj = reviewObject()
        return reviewobj.fail;
    }
}


def getChangelist(){
    def reviewobj = reviewObject()
    def changelistId =  reviewobj.change;
    try{
    if(env.P4_CHANGE != null){
    changelistId = "${env.P4_CHANGE}"
    }else{
     changelistId = "${change}"
    }
    return changelistId
    }catch(err){
        return changelistId
    }
}


def getReviewId(){
    def reviewobj = reviewObject()
    def reviewId = reviewobj.review
    try{
    if(env.P4_REVIEW != null){
    reviewId = "${env.P4_REVIEW}"
    }else{
     reviewId = "${review}"
    }
    return reviewId
    }catch(err){
        return reviewId
    }
}


def isReviewUpdate(){
    def url = getReviewPass()
    if(url != null){
        return !url.contains(".v1")
    }
    return false
}

def isCommitted(){
    if(getReviewStatus() == "commited") return true
    return false
}


def getCurrentReviewDescription(credential,client,view_mapping){
    def reviewId = getReviewId()
    echo reviewId
    def desc = getChangelistDescription(reviewId,credential,client,view_mapping)
    //check if the message is restricted
    if(desc.contains("<description: restricted, no permission to view>")){
        if(!isCommitted()){
            desc = "**Review has been submitted in the meanwhile. Without build validation. This might be a cause of a build error. Please do not commit before the build pipeline gives green light.**"
        }else{
            desc = "**Review has been submitted, without build/test validation.**"
        }
    }
    return desc
}

def getCurrentChangelistDescription(credential,client,view_mapping){
    def reviewId = getChangelist()
    return getChangelistDescription(reviewId,credential,client,view_mapping)
}

def pull(credential,workspace_template,format = "jenkins-${JOB_NAME}"){
    p4sync charset: 'none', credential:credential, format: format, populate: forceClean(have: false, parallel: [enable: true, minbytes: '1024', minfiles: '1', threads: '4'], pin: '', quiet: true), source: templateSource(workspace_template)
}