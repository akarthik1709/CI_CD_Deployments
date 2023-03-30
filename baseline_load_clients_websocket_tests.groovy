import groovy.json.JsonSlurper;
import java.text.SimpleDateFormat;

def causesVarToClearJSONFiles = currentBuild.getBuildCauses()

def baseUrl = "${env.JENKINS_URL}"

def credentials_csv = "${CREDENTIALS_FILE}"

println "THE CREDENTIALS${CREDENTIALS_FILE}"

def tasks = [:]

@NonCPS
def sleep_function(sleep_time_value) {
    echo "Sleep for ${sleep_time_value} mins...."
    def sleep_time_int  = sleep_time_value as int
    sleep(sleep_time_int)
}

@NonCPS
def sleep_sessiontime(sleep_time_value) {
    echo "Sleep for ${sleep_time_value} mins...."
    sleep (${SESSION_TIME_HOURS} * 60 * 60)
}

def stages_executed_for_baseline_scripts(sleep_time_value,time_sleep_function=true){
    tasks["task_1"] = {
        stage ("Run the Sockio Baseline Load"){

                build job: 'Baseline_Load_Client_Sockio',
                    parameters: [
                        string(name: 'NUM_USERS', value: "${NUM_USERS}"),
                        string(name: 'HOST', value: "${HOST}"),
                        string(name: 'TERM_LIMIT', value: "${TERM_LIMIT}"),
                        string(name: 'API_VER', value: "${API_VER}"),
                        string(name: 'TEST_DURATION', value: "${TEST_DURATION}"),
                        string(name: 'CREDENTIALS_CSV', value: "${CREDENTIALS_FILE}"),
                        string(name: 'TERM_NAME_PREFIX', value: "${TERM_NAME_PREFIX}"),
                        string(name: 'USERGROUP_OPTIONS', value: "${USERGROUP_OPTIONS}"),
                        string(name: 'USERNAME', value: "${Username}"),
                        string(name: 'PASSWORD', value: "${Password}")
                    ], propagate: false
        

        if ("${RUN_IN_SEQUENCE}" == "true"){
            sleep_function(sleep_time_value)
        }
        }
    }
    tasks["task_2"] = {
        stage ("Run the Pubsub Baseline Load"){
            // This is the build job which triggers the build script
                build job: 'Baseline_Load_Client_Pubsub',
                    parameters: [
                        string(name: 'NUM_USERS', value: "${NUM_USERS}"),
                        string(name: 'HOST', value: "${HOST}"),
                        string(name: 'TERM_LIMIT', value: "${TERM_LIMIT}"),
                        string(name: 'API_VER', value: "${API_VER}"),
                        string(name: 'TEST_DURATION', value: "${TEST_DURATION}"),
                        string(name: 'CREDENTIALS_CSV', value: "${CREDENTIALS_FILE}"),
                        string(name: 'TERM_NAME_PREFIX', value: "${TERM_NAME_PREFIX}"),
                        string(name: 'USERGROUP_OPTIONS', value: "${USERGROUP_OPTIONS}"),
                        string(name: 'USERNAME', value: "${Username}"),
                        string(name: 'PASSWORD', value: "${Password}")
                    ], propagate: false
        
        if ("${RUN_IN_SEQUENCE}" == "true"){
            sleep_function(sleep_time_value)
        }
        }
    }
    tasks["task_3"] = {
        stage ("Run the Websocket Baseline Load"){
            // This is the build job which triggers the build script
                build job: 'Baseline_Load_Client_Websocket',
                    parameters: [
                        string(name: 'NUM_USERS', value: "${NUM_USERS}"),
                        string(name: 'HOST', value: "${HOST}"),
                        string(name: 'TERM_LIMIT', value: "${TERM_LIMIT}"),
                        string(name: 'API_VER', value: "${API_VER}"),
                        string(name: 'TEST_DURATION', value: "${TEST_DURATION}"),
                        string(name: 'CREDENTIALS_CSV', value: "${CREDENTIALS_FILE}"),
                        string(name: 'TERM_NAME_PREFIX', value: "${TERM_NAME_PREFIX}"),
                        string(name: 'USERGROUP_OPTIONS', value: "${USERGROUP_OPTIONS}"),
                        string(name: 'USERNAME', value: "${Username}"),
                        string(name: 'PASSWORD', value: "${Password}")
                    ], propagate: false
        }
        }
}
node {
    
    def time_sleep_function = false
    def sleep_time_value = "${SLEEP_TIME_BEFORE_ADDING_JOBS}"
    println "sleep"+sleep_time_value
    println "sequence${RUN_IN_SEQUENCE}"

    if ( "${RUN_IN_SEQUENCE}" == "true" ){
        stages_executed_for_baseline_scripts(sleep_time_value)
    }
    else
        {
        stages_executed_for_baseline_scripts(sleep_time_value)
        parallel tasks
    }
}

    