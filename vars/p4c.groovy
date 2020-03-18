
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