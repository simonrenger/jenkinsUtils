import groovy.json.JsonSlurper

def swarm_object = null

def setup(user,ticket,url){
    swarm_object = [user:null,ticket:null,url:null]
    swarm_object.user = user
    swarm_object.ticket = ticket
    swarm_object.url = url
}

def clear(){
    swarm_object = [user:null,ticket:null,url:null]
}

def comment(review,comment){
    bat label: 'send comment request', script: "curl -u \"${swarm_object.user}:${swarm_object.ticket}\" -X POST -H \"Content-Type: application/x-www-form-urlencoded\" -d \"topic=reviews/${review}&body=${comment}\" \"${swarm_object.url}/api/v9/comments\""
}

def upVote(review){
    bat label: '', script: "curl -u \"${swarm_object.user}:${swarm_object.ticket}\" -X POST \"${swarm_object.url}/reviews/${review}/vote/up\" "
}

def downVote(review){
    bat label: '', script: "curl -u \"${swarm_object.user}:${swarm_object.ticket}\" -X POST \"${swarm_object.url}/reviews/${review}/vote/down\" "
}

def approve(review){
    updateState(review,"approved")
}

def needsReview(review){
    updateState(review,"needsReview")
}
def needsRevision(review){
    updateState(review,"needsRevision")
}
def archive(review){
    updateState(review,"archived")
}
def reject(review){
    updateState(review,"rejected")
}

def updateState(review,state){
    bat label: 'update state', script: "curl -u \"${swarm_object.user}:${swarm_object.ticket}\" -X PATCH  -H \"Content-Type: application/x-www-form-urlencoded\" -d \"state=${state}\" \"${swarm_object.url}/api/v9/reviews/${review}/state/\""
}

// expensiv and workaround!
@NonCPS
def reviewParser(json){
    try{
    final slurper = new JsonSlurper()
    def object = slurper.parseText(json)
    def temp = [
        reviews: []
    ]
    for(i = 0;i  < object.totalCount; i++ ){
        def review = object.reviews[i]
        def tempReview = [
                id: review.id,
                author: review.author,
                description:review.description,
                participants: []
            ]
        review.participants.each{user,data->
            tempReview.participants.add(user)
        }
        temp.reviews.add(tempReview)
    }
    return temp
    }catch(err){
        error(err.getMessage())
    }
}

def reviewInfo(review){
    def crulResponse = bat( encoding: 'UTF-8', label: 'request review Infos', script: "curl -u \"${swarm_object.user}:${swarm_object.ticket}\" \"${swarm_object.url}/api/v9/reviews?max=2&fields=id,participants,description,author,state,projects&ids\\[\\]=${review}\"" , returnStdout: true)
    def reviewArray = crulResponse.split('\\n')
    def reviewinfo = ""
    //workaround makes an assumption which might be not true ...
    if(reviewArray.length >= 2){
        reviewinfo = reviewArray[2].trim()
    }else{
        echo "Result parsing is incorrect: original string ${crulResponse}"
        error()
    }
    return reviewParser(reviewinfo)
}

def getReviewId(jsonObjectofReview,index = 0){
    return jsonObjectofReview.reviews[index].id
}

def getReviewParticipants(jsonObjectofReview,index = 0){
    return jsonObjectofReview.reviews[index].participants
}

def getReviewAuthor(jsonObjectofReview,index = 0){
    return jsonObjectofReview.reviews[index].author
}

def getReviewDescription(jsonObjectofReview,index = 0){
    return jsonObjectofReview.reviews[index].description
}

def getReviewState(jsonObjectofReview,index = 0){
    return jsonObjectofReview.reviews[index].state
}

def updateTestStatus(isPass = true){
    def url = ""
    if(isPass){
        url = p4c.getReviewPass()
    }else{
        url = p4c.getReviewFail()
    }
    try{
        bat label: 'inform swarm', script: "curl \"${url}\"" // makes sure that swarm shows the build is good
    }catch(err){
        echo "no pass parameter was given! url: ${url}"
    }
}