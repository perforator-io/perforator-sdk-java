#!/bin/sh

command -v yq >/dev/null 2>&1 || { echo >&2 "Script requires 'yq' but it is not available."; exit 1; }
command -v curl >/dev/null 2>&1 || { echo >&2 "Script requires 'curl' but it is not available."; exit 1; }
command -v ssh >/dev/null 2>&1 || { echo >&2 "Script requires 'ssh' but it is not available."; exit 1; }
command -v scp >/dev/null 2>&1 || { echo >&2 "Script requires 'scp' but it is not available."; exit 1; }
command -v ssh-keygen >/dev/null 2>&1 || { echo >&2 "Script requires 'ssh-keygen' but it is not available."; exit 1; }
command -v realpath >/dev/null 2>&1 || { echo >&2 "Script requires 'realpath' but it is not available."; exit 1; }

function log() {
  if [ ! -z "${1}" ]; then 
    echo >&2 "[$(date '+%Y-%m-%d %H:%M:%S,000')][][INFO] - ${1}"
  fi
}

function getConfigProperty () {
  if [ ! -z "${!1}" ]; then echo ${!1};
  elif [ ! -z "${2}" ] && [ -f "${2}" ] && [ ! -z "${3}" ]; then cat $2 | yq $3;
  else return 1; fi
}

function getCloudRunnerType () {
  if [ -z "${1}" ]; then echo 'C2';
  elif [ "$1" -ge 8000 ]; then echo 'C72';
  elif [ "$1" -ge 4000 ]; then echo 'C36';
  elif [ "$1" -ge 2000 ]; then echo 'C16';
  elif [ "$1" -ge 1000 ]; then echo 'C8';
  elif [ "$1" -ge 500 ]; then echo 'C4';
  else echo 'C2'; fi
}

function getSshPublicKey () {
  local default_ssh_key_type="$1"
  local default_ssh_dir="$2"
  local default_private_key_location="$default_ssh_dir/id_${default_ssh_key_type}"
  local default_public_key_location="$default_ssh_dir/id_${default_ssh_key_type}.pub"
  
  if [ ! -d "$default_ssh_dir" ]; then 
    mkdir $default_ssh_dir
  fi
  
  if [ -f "$default_public_key_location" ]; then 
    cat $default_public_key_location
  else 
    ssh-keygen -t $default_ssh_key_type -f "$default_private_key_location" -q -N ""
    cat $default_public_key_location
  fi
}

function getAccessToken () {
  if [ ! -z "${1}" ] && [ ! -z "${2}" ] && [ ! -z "${3}" ]; then 
    local content_type='Content-Type: application/json'
    local http_method='POST'
    local request_url="${1}/oauth/token"
    local request_body="{ \"grant_type\":\"client_credentials\",\"client_id\":\"${2}\",\"client_secret\":\"${3}\" }"
    local response=`curl -H "$content_type" -X "$http_method" -d "$request_body" $request_url 2>/dev/null`
    echo $response | yq '.access_token'
  else 
    return 1
  fi
}

function createNewExecution () {
  if [ ! -z "${1}" ] && [ ! -z "${2}" ] && [ ! -z "${3}" ]; then 
    local auth_header="Authorization: Bearer $2"
    local content_type='Content-Type: application/json'
    local http_method='POST'
    local request_url="${1}/v1/projects/${3}/executions"
    local execution_notes='<p><span class=\"text-gray-700\">Cloud-native execution with the dedicated runner.</span></p>'
    local request_body="{ \"notes\":\"$execution_notes\" }"
    local response=`curl -H "$content_type" -H "$auth_header" -X "$http_method" -d "$request_body" $request_url 2>/dev/null`
    echo $response | yq '.uuid'
  else 
    return 1
  fi
}

function createNewCloudRunner () {
  if [ ! -z "${1}" ] && [ ! -z "${2}" ] && [ ! -z "${3}" ] && [ ! -z "${4}" ] && [ ! -z "${5}" ] && [ ! -z "${6}" ]; then 
    local auth_header="Authorization: Bearer $2"
    local content_type='Content-Type: application/json'
    local http_method='POST'
    local request_url="${1}/v1/projects/${3}/executions/${4}/cloud_runners"
    local request_body="{ \"hardwareType\":\"${5}\",\"sshPublicKey\":\"${6}\"}"
    local response=`curl -H "$content_type" -H "$auth_header" -X "$http_method" -d "$request_body" $request_url 2>/dev/null`
    echo $response | yq '.uuid'
  else 
    return 1
  fi
}

function getCloudRunnerDetails () {
  if [ ! -z "${1}" ] && [ ! -z "${2}" ] && [ ! -z "${3}" ] && [ ! -z "${4}" ] && [ ! -z "${5}" ]; then 
    local auth_header="Authorization: Bearer $2"
    local content_type='Content-Type: application/json'
    local http_method='GET'
    local request_url="${1}/v1/projects/${3}/executions/${4}/cloud_runners/${5}"
    local response=`curl -H "$content_type" -H "$auth_header" -X "$http_method" $request_url 2>/dev/null`
    echo $response
  else 
    return 1
  fi
}

function getCloudRunnerUser () {
  if [ ! -z "${1}" ] && [ ! -z "${2}" ] && [ ! -z "${3}" ] && [ ! -z "${4}" ] && [ ! -z "${5}" ]; then 
    local details=`getCloudRunnerDetails "$1" "$2" "$3" "$4" "$5"`
    echo $details | yq '.sshUser'
  else 
    return 1
  fi
}

function getCloudRunnerHost () {
  if [ ! -z "${1}" ] && [ ! -z "${2}" ] && [ ! -z "${3}" ] && [ ! -z "${4}" ] && [ ! -z "${5}" ]; then 
    local details=`getCloudRunnerDetails "$1" "$2" "$3" "$4" "$5"`
    echo $details | yq '.sshHostName'
  else 
    return 1
  fi
}

function getCloudRunnerStatus () {
  if [ ! -z "${1}" ] && [ ! -z "${2}" ] && [ ! -z "${3}" ] && [ ! -z "${4}" ] && [ ! -z "${5}" ]; then 
    local details=`getCloudRunnerDetails "$1" "$2" "$3" "$4" "$5"`
    echo $details | yq '.status'
  else 
    return 1
  fi
}

function awaitCloudRunner () {
  if [ ! -z "${1}" ] && [ ! -z "${2}" ] && [ ! -z "${3}" ] && [ ! -z "${4}" ] && [ ! -z "${5}" ] && [ ! -z "${6}" ]; then 
    local expected_status="${6}"
    local current_status=''
    local start_time=`date +%s`
    local max_time=$(($start_time + 3600))
    
    while [ "$current_status" != "$expected_status" ] && [ $max_time -ge $(date +%s) ]; do
      current_status=`getCloudRunnerStatus "$1" "$2" "$3" "$4" "$5"`
      log "Awaiting cloud runner to be ${expected_status}, current status is ${current_status}"
      sleep 1
    done

    echo $current_status
  else 
    return 1
  fi
}

function terminateCloudRunner () {
  if [ ! -z "${1}" ] && [ ! -z "${2}" ] && [ ! -z "${3}" ] && [ ! -z "${4}" ] && [ ! -z "${5}" ]; then 
    local auth_header="Authorization: Bearer $2"
    local content_type='Content-Type: application/json'
    local http_method='POST'
    local request_url="${1}/v1/projects/${3}/executions/${4}/cloud_runners/${5}/terminate"
    local response=`curl -H "$content_type" -H "$auth_header" -X "$http_method" $request_url 2>/dev/null`
    echo $response | yq '.status'
  else 
    return 1
  fi
}

function listBrowserClouds () {
  if [ ! -z "${1}" ] && [ ! -z "${2}" ] && [ ! -z "${3}" ] && [ ! -z "${4}" ]; then 
    local auth_header="Authorization: Bearer $2"
    local content_type='Content-Type: application/json'
    local http_method='GET'
    local request_url="${1}/v1/projects/${3}/executions/${4}/browser_clouds"
    local response=`curl -H "$content_type" -H "$auth_header" -X "$http_method" $request_url 2>/dev/null`
    echo $response | yq '.[].uuid'
  else 
    return 1
  fi
}

function terminateBrowserCloud () {
  if [ ! -z "${1}" ] && [ ! -z "${2}" ] && [ ! -z "${3}" ] && [ ! -z "${4}" ] && [ ! -z "${5}" ]; then 
    local auth_header="Authorization: Bearer $2"
    local content_type='Content-Type: application/json'
    local http_method='POST'
    local request_url="${1}/v1/projects/${3}/executions/${4}/browser_clouds/${5}/terminate"
    local response=`curl -H "$content_type" -H "$auth_header" -X "$http_method" $request_url 2>/dev/null`
    echo $response | yq '.status'
  else 
    return 1
  fi
}

function cleanupResources () {
  if [ ! -z "${1}" ] && [ ! -z "${2}" ] && [ ! -z "${3}" ] && [ ! -z "${4}" ] && [ ! -z "${5}" ]; then 
    local browser_clouds=`listBrowserClouds "$1" "$2" "$3" "$4"`

    for browser_cloud in $browser_clouds 
    do
      log "Terminating browser cloud ${browser_cloud}"
      terminateBrowserCloud "$1" "$2" "$3" "$4" "${browser_cloud}" >/dev/null 2>&1
    done

    log "Terminating cloud runner ${5}"
    terminateCloudRunner "$1" "$2" "$3" "$4" "${5}" >/dev/null 2>&1
  else 
    return 1
  fi
}

function executeLoadGeneratorRemotely () {
  if [ ! -z "${1}" ] && [ ! -z "${2}" ] && [ ! -z "${3}" ] && [ ! -z "${4}" ]; then 
    local current_timezone=$TZ
    if [ -z $current_timezone ]; then
      current_timezone=`date +%Z`
    fi

    local project_dir="${1}"
    local project_absolute_path=`realpath "${project_dir}"`
    local ssh_user="${2}"
    local ssh_host="${3}"
    local ssh_key="${4}"
    local ssh_dir="~/load_generator"
    local ssh_script="source ~/.profile; TZ=${current_timezone} ${ssh_dir}/cloud-full-run.sh"
    local ssh_options="-oStrictHostKeyChecking=no"
    
    log "Uploading files from ${project_absolute_path} to ${ssh_user}@${ssh_host}:${ssh_dir}"
    ssh -i "${ssh_key}" "$ssh_options" "${ssh_user}@${ssh_host}" mkdir ${ssh_dir}
    scp -i "${ssh_key}" "$ssh_options" -r -O ${project_absolute_path}/* "${ssh_user}@${ssh_host}:${ssh_dir}"

    log "Starting remote load generator via ${ssh_user}@${ssh_host}"
    ssh -i "${ssh_key}" -t "$ssh_options" "${ssh_user}@${ssh_host}" "${ssh_script}"
  else 
    return 1
  fi
}

project_path=`dirname -- "$0"`
config_path="$project_path/config.yml"
api_base_path="https://api.perforator.io"
default_ssh_key_type="ed25519"
default_ssh_dir="$project_path/.ssh"
default_ssh_key_location="$project_path/.ssh/id_$default_ssh_key_type"

ssh_public_key=`getSshPublicKey $default_ssh_key_type $default_ssh_dir`
if [ -z "${ssh_public_key}" ] || [ "${ssh_public_key}" = 'null' ]; then echo >&2 "Can't determine ssh_public_key"; exit 1; fi

api_client_id=`getConfigProperty 'LOADGENERATOR_APICLIENTID' $config_path '.loadGenerator.apiClientId'`
if [ -z "${api_client_id}" ] || [ "${api_client_id}" = 'null' ]; then echo >&2 "Can't determine api_client_id"; exit 1; fi

api_client_secret=`getConfigProperty 'LOADGENERATOR_APICLIENTSECRET' $config_path '.loadGenerator.apiClientSecret'`
if [ -z "${api_client_secret}" ] || [ "${api_client_secret}" = 'null' ]; then echo >&2 "Can't determine api_client_secret"; exit 1; fi

project_key=`getConfigProperty 'LOADGENERATOR_PROJECTKEY' $config_path '.loadGenerator.projectKey'`
if [ -z "${project_key}" ] || [ "${project_key}" = 'null' ]; then echo >&2 "Can't determine project_key"; exit 1; fi

full_concurrency=`cat $config_path| yq '.suites.*.concurrency'| awk '{ sum += $1 } END { print sum }'`
if [ -z "${full_concurrency}" ] || [ "${full_concurrency}" = '0' ]; then echo >&2 "Can't determine full_concurrency"; exit 1; fi

cloud_runner_type=`getCloudRunnerType "$full_concurrency"`
if [ -z "${cloud_runner_type}" ] || [ "${cloud_runner_type}" = 'null' ]; then echo >&2 "Can't determine cloud_runner_type"; exit 1; fi

access_token=`getAccessToken "$api_base_path" "$api_client_id" "$api_client_secret"`
if [ -z "${access_token}" ] || [ "${access_token}" = 'null' ]; then echo >&2 "Can't determine access_token"; exit 1; fi

execution_key=`createNewExecution "$api_base_path" "$access_token" "$project_key"`
if [ -z "${execution_key}" ] || [ "${execution_key}" = 'null' ]; then echo >&2 "Can't determine execution_key"; exit 1; fi

log "Requesting new cloud runner, type = $cloud_runner_type"
cloud_runner_key=`createNewCloudRunner "$api_base_path" "$access_token" "$project_key" "$execution_key" "$cloud_runner_type" "$ssh_public_key"`
if [ -z "${cloud_runner_key}" ] || [ "${cloud_runner_key}" = 'null' ]; then echo >&2 "Can't determine cloud_runner_key"; exit 1; fi

trap "{ cleanupResources $api_base_path $access_token $project_key $execution_key $cloud_runner_key ; }" INT TERM

cloud_runner_status=`awaitCloudRunner "$api_base_path" "$access_token" "$project_key" "$execution_key" "$cloud_runner_key" "OPERATIONAL"`
if [ -z "${cloud_runner_status}" ] || [ "${cloud_runner_status}" = 'null' ] || [ "${cloud_runner_status}" != 'OPERATIONAL' ]; then echo >&2 "Cloud runner is not operational"; exit 1; fi

cloud_runner_ssh_user=`getCloudRunnerUser "$api_base_path" "$access_token" "$project_key" "$execution_key" "$cloud_runner_key"`
if [ -z "${cloud_runner_ssh_user}" ] || [ "${cloud_runner_ssh_user}" = 'null' ]; then echo >&2 "Can't determine cloud_runner_ssh_user"; exit 1; fi

cloud_runner_ssh_host=`getCloudRunnerHost "$api_base_path" "$access_token" "$project_key" "$execution_key" "$cloud_runner_key"`
if [ -z "${cloud_runner_ssh_host}" ] || [ "${cloud_runner_ssh_host}" = 'null' ]; then echo >&2 "Can't determine cloud_runner_ssh_host"; exit 1; fi

log "Cloud Runner is ready, type = $cloud_runner_type, ssh_user = $cloud_runner_ssh_user, ssh_host = $cloud_runner_ssh_host"

executeLoadGeneratorRemotely "$project_path" "$cloud_runner_ssh_user" "$cloud_runner_ssh_host" "$default_ssh_key_location"
cleanupResources $api_base_path $access_token $project_key $execution_key $cloud_runner_key