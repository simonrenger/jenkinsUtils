
def getChangelistDescription(id,credential,client,view_mapping){
    def p4s = p4(credential: credential, workspace: manualSpec(charset: 'none', cleanup: false, name: client, pinHost: false, spec: clientSpec(allwrite: false, backup: true, changeView: '', clobber: false, compress: false, line: 'LOCAL', locked: false, modtime: false, rmdir: false, serverID: '', streamName: '', type: 'WRITABLE', view: view_mapping)))
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

def comment(review,user,ticket,swarm_url,comment){
    bat label: 'send comment request', script: "curl -u \"${user}:${ticket}\" -X POST -H \"Content-Type: application/x-www-form-urlencoded\" -d \"topic=reviews/${review}&body=${comment}\" \"${swarm_url}/api/v9/comments\""
}

def upVote(user,ticket,swarm_url,review){
    bat label: '', script: "curl -u \"${user}:${ticket}\" -X POST \"${swarm_url}/reviews/${review}/vote/up\" "
}

def upDown(user,ticket,swarm_url,review){
    bat label: '', script: "curl -u \"${user}:${ticket}\" -X POST \"${swarm_url}/reviews/${review}/vote/down\" "
}

def swamUrl(){
    def p4s = p4(credential: env.P4USER, workspace: manualSpec(charset: 'none', cleanup: false, name: env.P4CLIENT, pinHost: false, spec: clientSpec(allwrite: true, backup: true, changeView: '', clobber: false, compress: false, line: 'LOCAL', locked: false, modtime: false, rmdir: false, serverID: '', streamName: '', type: 'WRITABLE', view: env.P4MAPPING)))
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
