import groovy.json.JsonOutput
import hudson.model.*

def token_ = ""
def url_ = ""

def setUrl(url){
    url_ = url
}
def setToken(token){
    token_ = token
}

def update(id,status,resolution,handler = null){

def body = [
    id: id,
    status: [
        name: status
    ],
    resolution: [
        name: resolution
    ]
]

    if(status == "assigned" && handler != null){
        body.handler = [
            name: handler
        ]
    }

    def bodyJson = JsonOutput.toJson(body).replace('"','""')
    bat label: '', script: """curl --location --request PATCH \"${url_}/api/rest/issues/${id}\"^
    --header \"Authorization: ${token_}\"^
    --header \"Content-Type: application/json\" ^
    --data-raw \"${bodyJson}\""""
}