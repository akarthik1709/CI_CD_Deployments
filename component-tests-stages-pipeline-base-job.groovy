import groovy.json.JsonSlurper;

def causesVarToClearJSONFiles = currentBuild.getBuildCauses()

def alllPipelineResultsFileName;
def apiConfigPipelineResultsFile;
def apiCompatPipelineResultsFile;
def pgtapPipelineResultsFile;
def protractorPipelineResultsFile;
def protractorProvisionPipelineResultsFile;
def allPipelineResultsFile;
def CDN_IP_ADDRESS;

def root_build_number = "${env.BUILD_NUMBER}"
println "The build number for environment ${env.BUILD_NUMBER}"


def branch_name_pipeline
def PRODUCTION_PIPELINEES = []
PRODUCTION_PIPELINEES.add("${PRODUCTION_PIPELINE}")

pulse_pipeline_root_folder = "Pulse-Pipeline-Tests"

echo "BRANCH NAME ${BRANCH_NAME}"

branch_name_pipeline = "${BRANCH_NAME}"

echo "Branch Pipeline is ${branch_name_pipeline}"
if ("${PRODUCTION_PIPELINE}" == "develop") {
    println "The pulse pipeline root folder"+ pulse_pipeline_root_folder;

    alllPipelineResultsFileName = "all-pipeline-latest.json";
    apiConfigPipelineResultsFile = env.JENKINS_HOME + "/userContent/pipelineRunData/api-config-latest.json";
    apiCompatPipelineResultsFile = env.JENKINS_HOME + "/userContent/pipelineRunData/api-compat-latest.json";
    pgtapPipelineResultsFile = env.JENKINS_HOME + "/userContent/pipelineRunData/pgtap-latest.json";
    protractorPipelineResultsFile = env.JENKINS_HOME + "/userContent/pipelineRunData/protractor-latest.json";
    protractorProvisionPipelineResultsFile = env.JENKINS_HOME + "/userContent/pipelineRunData/protractor-provision-test-latest.json";
    allPipelineResultsFile = env.JENKINS_HOME + "/userContent/pipelineRunData/" + alllPipelineResultsFileName;
} else if ("${PRODUCTION_PIPELINE}" == "development" ) {
    
    pulse_pipeline_root_folder = "Pulse-Nightly-Pipeline-Clone-Development"
    println "The pulse pipeline root folder" + pulse_pipeline_root_folder;

    alllPipelineResultsFileName = "all-pipeline-latest.json";
    apiConfigPipelineResultsFile = env.JENKINS_HOME + "/userContent/pipelineRunData/pulse-scripts-development-dir/api-config-latest.json";
    apiCompatPipelineResultsFile = env.JENKINS_HOME + "/userContent/pipelineRunData/pulse-scripts-development-dir/api-compat-latest.json";
    pgtapPipelineResultsFile = env.JENKINS_HOME + "/userContent/pipelineRunData/pulse-scripts-development-dir/pgtap-latest.json";
    protractorPipelineResultsFile = env.JENKINS_HOME + "/userContent/pipelineRunData/pulse-scripts-development-dir/protractor-latest.json";
    protractorProvisionPipelineResultsFile = env.JENKINS_HOME + "/userContent/pipelineRunData/pulse-scripts-development-dir/protractor-provision-test-latest.json";
    allPipelineResultsFile = env.JENKINS_HOME + "/userContent/pipelineRunData/pulse-scripts-development-dir/" + alllPipelineResultsFileName;

    
} else if ("${PRODUCTION_PIPELINE}" == "release" ) {
    node {
    sh (
            script: "mkdir " + env.JENKINS_HOME + "/userContent/pipelineRunData/release-${root_build_number}" ,
                returnStdout: true
            ).trim()
    }
    alllPipelineResultsFileName = "all-pipeline-latest.json";
    apiConfigPipelineResultsFile = env.JENKINS_HOME + "/userContent/pipelineRunData/release-${root_build_number}/api-config-latest.json";
    apiCompatPipelineResultsFile = env.JENKINS_HOME + "/userContent/pipelineRunData/release-${root_build_number}/api-compat-latest.json";
    pgtapPipelineResultsFile = env.JENKINS_HOME + "/userContent/pipelineRunData/release-${root_build_number}/pgtap-latest.json";
    protractorPipelineResultsFile = env.JENKINS_HOME + "/userContent/pipelineRunData/release-${root_build_number}/protractor-latest.json";
    protractorProvisionPipelineResultsFile = env.JENKINS_HOME + "/userContent/pipelineRunData/release-${root_build_number}/protractor-provision-test-latest.json";
    allPipelineResultsFile = env.JENKINS_HOME + "/userContent/pipelineRunData/release-${root_build_number}/" + alllPipelineResultsFileName;
}

common_infrastructure_folder = "Common-Infrastructure-Jobs"
api_compat_tests_folder = "API-Compat-Tests"
api_config_tests_folder = "API-Config-Tests"
pgtap_jobs_folder = "Pgtap-Tests"
protractor_jobs_folder = "UI-Tests"
sanity_tests_folder = "Sanity-Tests"
unit_tests_folder = "Unit-Tests"

//@NonCPS
//def getBuildUser() {
//        return currentBuild.rawBuild.getCause(Cause.UserIdCause).getUserId()
//    }

//println "THE USER STARTED THE JOB"+getBuildUser();

def causesBuildUser = currentBuild.getBuildCauses();
def userStarted;
def epicName = "${EPIC_NAME}"
println "THE CAUSES ARE USER 1 "+ causesBuildUser[0].userId;

if (causesBuildUser[0].userId == ""){
    userStarted = "akarthik"
}
else {
    userStarted = causesBuildUser[0].userId;
}

def source_dir
node {
    repo_to_use = "<Repo URL>";
    

    if ( "${PRODUCTION_PIPELINE}" == "release" ){
        source_dir = env.JENKINS_HOME+"/<BaseFolder>/TestPipelineFolderRelease"
    }
    else if ( "${PRODUCTION_PIPELINE}" == "develop" ){
        source_dir = env.JENKINS_HOME+"/<BaseFolder>/TestPipelineFolder"
    }
    else if ( "${PRODUCTION_PIPELINE}" == "development" ){
        source_dir = env.JENKINS_HOME+"/<BaseFolder>/TestPipelineFolderDevel"
    }

    println "SOURCE ${source_dir}"
    branch_name = "develop"
    def file = new File(source_dir)
    if (file.exists()) {
        sh "rm -rf ${source_dir}"
    }
    sh "mkdir -p ${source_dir}"
    sh "git clone -b ${branch_name} ${repo_to_use} ${source_dir}";
}

pulse_pipeline_json_script_path = "${source_dir}/testtool/pulse-pipeline-scripts"
pulse_pipeline_json_script_path_default = env.JENKINS_HOME+ "/userContent/pipelineRunData/pulse-scripts-development-dir"

CDN_IP_ADDRESS = "<IP>";
def tasks = [:]
def allPipelineResults = readJSON text: '{}';
def baseUrl = "${env.JENKINS_URL}"
def sanityTestJobs = [
    "gaBuild" : "${pulse_pipeline_root_folder}/${sanity_tests_folder}/Pulse-CI-BuildStatus-GA",
    "gaEc2": "${pulse_pipeline_root_folder}/${sanity_tests_folder}/ec2-Pulse-CI-Healthy",
    "gxBuild" : "${pulse_pipeline_root_folder}/${sanity_tests_folder}/Pulse-CI-BuildStatus-GX",
    "gxEc2": "${pulse_pipeline_root_folder}/${sanity_tests_folder}/ec2-Pulse-CI-Healthy",
    //"prn": "Velo-NMS-develop-sanity"
];


def unitTestJobs = [
        "nmsCluster": "(root)/nms_cluster",
        "pulseFe": "(root)/Pulse_FE",
        "nodeServices": "(root)/nodeservices",
        "clusterComponents": "cluster_components/Default",
        "clusterCore": "cluster_core/Default",
        "statsDdeShared": "stats_dde_shared/Default",
        "updateMgr": "update_mgr/Default",
        "configBe": "configuration_be/Default",
        "nmsLib": "nms_lib/Default",
        "nmsCore": "nms_core_libs/Default",
        "nmsIcd": "nms_icd_api/Default"
    ];


node {
    def nms_dev_tools_config_be_branch
    if ( "${PRODUCTION_PIPELINE}" == "release") {
        nms_dev_tools_config_be_branch = "${BRANCH_NAME}"
    }
    else{
        nms_dev_tools_config_be_branch = "develop"
    }
    def version_url_map = "curl -s ${CDN_IP_ADDRESS}/files/vm_build_rpm/build_status/k8s/kubernetes_status_check_result.txt | tail -n 1"

    def build_name1 = sh ( script: version_url_map, returnStdout: true).trim()
    println build_name1

    def items2 = build_name1.tokenize( ':' )
    def nms_type_version = items2[0]
    def test_suite1 = items2[1]
    println nms_type_version

    println test_suite1
    def items1 = test_suite1.tokenize( '(' )

    def k8_stability = items1[0]
    println "The stability is given by=============="+k8_stability

    def k8_stability_string = k8_stability.toString();

    def ami_id_value = items1[1]
    def ami_id = ami_id_value.tokenize(':')

    println ami_id

    def final_ami_id = ami_id[0]
    println final_ami_id 
    
    def unitTestBuildNumber;
    def unitTestStatus

    allPipelineResults["sanityTests"] = readJSON text: '{}';
    allPipelineResults["unitTests"] = readJSON text: '{}';

    stage ("Restore the JSON pipeline scripts"){
        sh "cp ${pulse_pipeline_json_script_path}/protractor-provision-test-latest.json ${protractorProvisionPipelineResultsFile}"
        sh "cp ${pulse_pipeline_json_script_path}/protractor-latest.json ${protractorPipelineResultsFile}"
        sh "cp ${pulse_pipeline_json_script_path}/pgtap-latest.json ${pgtapPipelineResultsFile}"
        sh "cp ${pulse_pipeline_json_script_path}/api-compat-latest.json ${apiCompatPipelineResultsFile}"
        sh "cp ${pulse_pipeline_json_script_path}/api-config-latest.json ${apiConfigPipelineResultsFile}"
        sh "cp ${pulse_pipeline_json_script_path}/all-pipeline-latest.json ${allPipelineResultsFile}"

        println "The JSON pipeline job scripts have been restored..."
    }
    if ("${PRODUCTION_PIPELINE}" == "develop"){
         stage ("Fetch Sanity Test Results"){ 
        sanityTestJobs.each {
            println Jenkins.instance.getItemByFullName("${it.value}").getUrl()
            def sanityTestBuildNumber = Jenkins.instance.getItemByFullName("${it.value}").lastBuild.number
            def sanityTestStatus = Jenkins.instance.getItemByFullName("${it.value}").lastBuild.result
            println sanityTestBuildNumber;
            println "Result is : " + sanityTestStatus;
            if( sanityTestStatus as String != "SUCCESS"){
                println "Setting sanityTestsSuccess to False";
                sanityTestsSuccess = false;
            }
            allPipelineResults["sanityTests"]["${it.key}" + "JobNumber"] = sanityTestBuildNumber;
            allPipelineResults["sanityTests"]["${it.key}" + "JobStatus"] = sanityTestStatus as String;
            def dsurl = baseUrl + Jenkins.instance.getItemByFullName("${it.value}").getUrl()  + sanityTestBuildNumber + "/testReport/api/json";
            //def dsurl =  baseUrl + "job/" + "${it.value}/" + sanityTestBuildNumber + "/testReport/api/json";
    		script {
                try {
                    def response = httpRequest dsurl
                    def json = new JsonSlurper().parseText(response.content)
                    json.remove('suites')
                    println json;
    				allPipelineResults["sanityTests"]["${it.key}" + "JobResult"] = json;

                } catch (err) {
                    echo err.getMessage()
                    echo "Error detected, but we will continue with other sanity test jobs."
                }
    		}
        }
    }
    }
    stage ("Fetch Unit Test Results") {
        def jobName = "${pulse_pipeline_root_folder}/${unit_tests_folder}/Pulse-ci-cd-unit-tests"
        build job: "${jobName}", propagate: false
        unitTestJobs.each {
            unitTestBuildNumber = Jenkins.instance.getItemByFullName("${pulse_pipeline_root_folder}/${unit_tests_folder}/unit-tests").lastBuild.number
            println unitTestBuildNumber;
            allPipelineResults["unitTests"]["${it.key}" + "JobNumber"] = unitTestBuildNumber;
            def dsjoburl =  baseUrl + "job/${pulse_pipeline_root_folder}/job/${unit_tests_folder}/job/unit-tests/" + unitTestBuildNumber + "/testReport/" + "${it.value}";
            def dsurl = dsjoburl + "/api/json";
            script {
                try {
                    def response = httpRequest dsurl
                    def json = new JsonSlurper().parseText(response.content)
                    json.remove('child')
                    allPipelineResults["unitTests"]["${it.key}" + "JobResult"] = json;
                    allPipelineResults["unitTests"]["${it.key}" + "JobUrl"] = dsjoburl;
                    if(json.failCount == 0) {
                        allPipelineResults["unitTests"]["${it.key}" + "JobStatus"] = "SUCCESS";
                    } else if (json.failCount > 0) {
                        allPipelineResults["unitTests"]["${it.key}" + "JobStatus"] = "UNSTABLE";
                    } else {
                        allPipelineResults["unitTests"]["${it.key}" + "JobStatus"] = "FAILURE";
                    }
                    println allPipelineResults["unitTests"]["${it.key}" + "JobStatus"];
                } catch (err) {
                    echo err.getMessage()
                    allPipelineResults["unitTests"]["${it.key}" + "JobResult"] = "NA";
                    allPipelineResults["unitTests"]["${it.key}" + "JobStatus"] = "FAILURE";
                    
                    echo "Error detected, but we will continue for other unit test jobs."
                    }
                }
            }
        }

    
    //platform_map.each {
    //    def new_platform_map = it;
    //    println("first level item: " + new_platform_map);
    //    new_platform_map.each {
    

//def versions_present_develop = versions_present_develop_arr.join(",")
//def versions_present_release = versions_present_release_arr.join(",")


    platforms_present = "${PLATFORMS_PRESENT}".tokenize(",")
    
    platform_map_apiscenarios = [:]
    
    def versions_present
    def release_array = []
    def develop_array = []
    
    println "THE MAP IS "+  platform_map_apiscenarios["${PRODUCTION_PIPELINE}"]
    for (plt in platforms_present){
        if ("${BRANCH_NAME}".contains("release") ==  true){
            release_array.add(plt+"-rel-20-10")
            release_array.add(plt+"-rel-20-20")
            release_array.add(plt+"-rel-30-10")
            release_array.add(plt+"-rel-30-20")

            platform_map_apiscenarios["${PRODUCTION_PIPELINE}"] = release_array 

        }
        else{
            develop_array.add(plt+"-30-10")
            develop_array.add(plt+"-30-20")

            platform_map_apiscenarios["${PRODUCTION_PIPELINE}"] = develop_array

        }
    }
    versions_present = platform_map_apiscenarios["${PRODUCTION_PIPELINE}"].join(",")
    println "THE VERSION IS"+ versions_present
    def versions_present_str = versions_present.toString()

    sanityTestsSuccess = true
    tasks["task_1"] = {
        
        println "The sanity Tests Value is : " + sanityTestsSuccess;
        stage ("CI-API-Scenarios") {
            allPipelineResults['compatApiPipelineInfo'] = readJSON text: '{}';
            if(sanityTestsSuccess == true){
                if ( k8_stability_string.contains("Stable") == true ) {
                println "========================================================"
                println "K8s Status is STABLE hence proceeding with the creation....."
                println "========================================================"
                build job: '../API-Compat-Tests/ec2-Pulse-pipeline-parallel-CI-API-scenarios',
                    parameters: [
                        string(name: 'NUM_ITERATIONS', value: '1'),
                        string(name: 'RMT_PKG_URL', value: '<RMT_PKG_URL>'),
                        string(name: 'PLATFORMS', value: "${versions_present}"),
                        string(name: 'BRANCH_UNDER_TEST', value: 'develop'),
                        booleanParam(name: 'RUN_SERIALLY', value: false),
                        text(name: 'CUSTOM_RPMS_TO_INSTALL', value: ''),
                        booleanParam(name: 'ALWAYS_DELETE_EC2_VM', value: true),
                        string(name: 'PRODUCTION_PIPELINE', value: "${PRODUCTION_PIPELINE}"),
                        string(name: 'USER_STARTED', value: "${userStarted}"),
                        string(name: 'EPIC_NAME', value: "${epicName}"),
                        string(name: 'COMPONENT_NAME', value: 'no-branch'),
                        string(name: 'K8_AMI_ID', value: "${final_ami_id}"),
                        string(name: 'BRANCH_NAME', value: "${BRANCH_NAME}"),
                        string(name: 'BUILD_NUM', value: "${root_build_number}")
                    ], propagate: false   
            def apiCompatPipelineResults = readJSON file: apiCompatPipelineResultsFile;
            println apiCompatPipelineResults;
            allPipelineResults['compatApiPipelineInfo'] = apiCompatPipelineResults;
            }
            else {
                    println "=================================================="
                    println "Exiting the API scenarios pipeline.....Since K8 status is not stable"
                    println "=================================================="
                    //System.exit(-1);
                }

        } 
        }
    }

    tasks["task_2"] = {
        
        stage ("CI-Config-WS") {
            allPipelineResults['configApiPipelineInfo'] = readJSON text: '{}';
            if(sanityTestsSuccess == true) {
                network_ver = [:]
                plt_ver = [:]
                version_url_map = [:]
                if ("${PRODUCTION_PIPELINE}".contains("release") ==  true){
                    network_ver["${PRODUCTION_PIPELINE}"] = "2.0.0.0"
                    plt_ver["${PRODUCTION_PIPELINE}"] =  "ga-rel,gx-rel"
                    plt_ver["BRANCH_NAME"] = "${branch_name_pipeline}"
                }
                else{
                    if ("${PRODUCTION_PIPELINE}" == "development"){
                        plt_ver["BRANCH_NAME"] = "${BRANCH_NAME_NMS_DEVTOOLS_CFGBUILDER_REPO}"
                    }
                    else{
                        plt_ver["BRANCH_NAME"] = "develop"
                    }
                    network_ver["${PRODUCTION_PIPELINE}"] = "1.6.1.0"
                    plt_ver["${PRODUCTION_PIPELINE}"] = "${PLATFORMS_PRESENT}"
                }
                
                build job: '../API-Config-Tests/ec2-Pulse-pipeline-CI-Config-WS',
                    parameters: [
                        string(name: 'NUM_ITERATIONS', value: '1'),
                        string(name: 'PLATFORMS', value: plt_ver["${PRODUCTION_PIPELINE}"]),
                        text(name: 'CUSTOM_RPMS_TO_INSTALL', value: ''),
                        booleanParam(name: 'ALWAYS_DELETE_EC2_VM', value: true),
                        string(name: 'PRODUCTION_PIPELINE', value: "${PRODUCTION_PIPELINE}"),
                        string(name: 'USER_STARTED', value: "${userStarted}"),
                        string(name: 'EPIC_NAME', value: "${epicName}"),
                        string(name: 'NETWORKVER', value: network_ver["${PRODUCTION_PIPELINE}"]),
                        string(name: 'BRANCH_NAME', value: plt_ver["BRANCH_NAME"]),
                        string(name: 'BUILD_NUM', value: "${root_build_number}")
                    ], propagate: false
            def apiConfigPipelineResults = readJSON file: apiConfigPipelineResultsFile;
            println apiConfigPipelineResults;
            allPipelineResults['configApiPipelineInfo'] = apiConfigPipelineResults;
            }
        }
        
    }

    
    tasks["task_3"] = {
        stage ("CI-PGTAP") {
            pgtap_map = [:]
            def branch_plt_array = []
            for (plt in platforms_present){
                if ("${BRANCH_NAME}".contains("release") ==  true){
                    def string_branch = "release-" + plt
                    branch_plt_array.add(string_branch)
                    pgtap_map["BRANCH_NAME"] = "${branch_name_pipeline}"
                }
                else{
                    def string_branch = "develop-" + plt
                    branch_plt_array.add(string_branch)
                    pgtap_map["BRANCH_NAME"] = "develop"
                     if ("${PRODUCTION_PIPELINE}" == "development"){
                        pgtap_map["BRANCH_NAME"] = "${BRANCH_NAME_NMS_DEVTOOLS_CFGBUILDER_REPO}"
                    } 
                }
                }
            pgtap_map["PLATFORMS"] = branch_plt_array.join(",")

            allPipelineResults['pgtapPipelineInfo'] = readJSON text: '{}';
            if(sanityTestsSuccess == true) {
                
                        build job: '../Pgtap-Tests/ec2-Pulse-pipeline-pgtap-tests',
                        parameters: [
                            string(name: 'PLATFORMS', value: pgtap_map["PLATFORMS"]),
                            string(name: 'BRANCH_UNDER_TEST', value: pgtap_map["BRANCH_NAME"]),
                            booleanParam(name: 'ALWAYS_DELETE_EC2_VM', value: true),
                            string(name: 'COMPONENT_NAME', value: 'Configuration_BE'),
                            string(name: 'BASE_BUILD_TYPE', value:  pgtap_map["BRANCH_NAME"]),
                            string(name: 'PRODUCTION_PIPELINE', value: "${PRODUCTION_PIPELINE}"),
                            string(name: 'USER_STARTED', value: "${userStarted}"),
                            string(name: 'EPIC_NAME', value: "${epicName}"),
                            string(name: 'BASE_BUILD_NUMBER', value: 'latest'),
                            string(name: 'BUILD_NUM', value: "${root_build_number}"),
                            string(name: 'BRANCH_NAME', value: pgtap_map["BRANCH_NAME"])
                        ], propagate: false
                    def pgtapPipelineResults = readJSON file: pgtapPipelineResultsFile;
                    println pgtapPipelineResultsFile;
                    allPipelineResults['pgtapPipelineInfo'] = pgtapPipelineResults;
            }
        }
    }
    
    
	tasks["task_4"] = {
        
		stage ("CI-PROTRACTOR"){    
            allPipelineResults['protractorPipelineInfo'] = readJSON text: '{}';
            protractor_map = [:]
            tests_arry = []
            for (plt in platforms_present){
                platforms = plt.toUpperCase()
                if ("${BRANCH_NAME}".contains("release") ==  true){
                    def string_tests_to_run = "$platforms-rel:allfetests_regressionv2:saucelabs-Pulse-CI-develop-FE-good-$platforms-Herndon:ec2-ip:no:sanityfe_testsv2:saucelabs-Pulse-CI-develop-FE-sanity-$platforms-Herndon:smoke_tests:saucelabs-Pulse-CI-develop-FE-sanity-$platforms-Herndon"
                    tests_arry.add(string_tests_to_run)
                    protractor_map["BRANCH_NAME"] = "${branch_name_pipeline}"
                }
                else{
                    def string_tests_to_run = "$platforms:allfetests_regressionv2:saucelabs-Pulse-CI-develop-FE-good-$platforms-Herndon:ec2-ip:no:sanityfe_testsv2:saucelabs-Pulse-CI-develop-FE-sanity-$platforms-Herndon:smoke_tests:saucelabs-Pulse-CI-develop-FE-sanity-$platforms-Herndon"
                    tests_arry.add(string_tests_to_run)
                    protractor_map["BRANCH_NAME"] = "develop"
                    if ("${BRANCH_NAME}" == "development"){
                        protractor_map["BRANCH_NAME"] = "${BRANCH_NAME_NMS_DEVTOOLS_CFGBUILDER_REPO}"
                    }
                }
            }
            protractor_map["TESTS_TO_RUN"] = tests_arry
            def tests_to_run_array = protractor_map["TESTS_TO_RUN"].join("\n")

            if(sanityTestsSuccess == true){
                if ( k8_stability_string.contains("Stable") == true ) {
                println "========================================================"
                println "K8s Status is STABLE hence proceeding with the creation....."
                println "========================================================"

                
                build job: '../Protractor-Tests/saucelabs-Pulse-pipeline-CI-develop-FE',
                    parameters: [
                        string(name: 'SELENIUM_GRID_URL',
                        value: 'http://10.250.42.87:4444/wd/hub'),
                        string(name: 'NMS_SERVER_BRANCH', value: 'develop'),
                        booleanParam(name: 'USE_SAUCELABS', value: false),
                        string(name: 'NMS_DEV_TOOLS_BRANCH', value: 'develop'),
                        string(name: 'SAUCE_TUNNEL', value: 'idirect_pulse_ci_tunnel_protractor'),
                        string(name: 'SAUCE_USERNAME', value: 'idirect_pulse_ci'),
                        string(name: 'SAUCE_ACCESS_KEY', value: 'c513a3f1-2b80-4c86-a95e-da0e5861dd43'),
                        string(name: 'SC_TUNNEL_CODE', value: '/jenkins-disk/home-jenkins/jenkins/work/nms_dev_tools'),
                        text(name: 'TESTS_TO_RUN', value: "${tests_to_run_array}"),
                        string(name: 'NUM_ITERATIONS', value: '1'),
                        booleanParam(name: 'ALWAYS_DELETE_EC2_VM', value: true),
                        string(name: 'PRODUCTION_PIPELINE', value: "${PRODUCTION_PIPELINE}"),
                        string(name: 'USER_STARTED', value: "${userStarted}"),
                        string(name: 'EPIC_NAME', value: "${epicName}"),
                        string(name: 'K8_AMI_ID', value: "${final_ami_id}"),
                        string(name: 'BRANCH_NAME', value: protractor_map["BRANCH_NAME"]),
                        string(name: 'BUILD_NUM', value: "${root_build_number}")
                        ], propagate: false
                    
                def protractorPipelineResults = readJSON file: protractorPipelineResultsFile;
                println protractorPipelineResults;
                allPipelineResults['protractorPipelineInfo'] = protractorPipelineResults;
                
                }
                else {
                    println "=================================================="
                    println "Exiting the protractor pipeline.....Since K8 status is not stable"
                    println "=================================================="
                    //System.exit(-1);
            
                }
            }
    }
}
    tasks["task_5"] = {
        
            stage ("CI-PROTRACTOR-PROVISION-TESTS"){
            allPipelineResults['protractorConfigTestsPipelineInfo'] = readJSON text: '{}';
            protractor_provision_map = [:]
        def tests_to_run = []
            for (plt in platforms_present){
                platforms = plt.toUpperCase()
                if ("${BRANCH_NAME}".contains("release") ==  true){
                    def string_tests_to_run = "$platforms-rel:regression_provisioning_tests:saucelabs-Pulse-CI-config-tests-FE-good-$platforms-Herndon:ec2-ip:no"
                    tests_to_run.add(string_tests_to_run)
                    protractor_provision_map["BRANCH_NAME"] = "${branch_name_pipeline}"
                }
                else{
                    def string_tests_to_run = "$platforms:regression_provisioning_tests:saucelabs-Pulse-CI-config-tests-FE-good-$platforms-Herndon:ec2-ip:no"
                    tests_to_run.add(string_tests_to_run)
                    protractor_provision_map["BRANCH_NAME"] = "develop"
                    if ("${BRANCH_NAME}" == "development"){
                        protractor_provision_map["BRANCH_NAME"] = "development"
                    }
                }
            }
            protractor_provision_map["TESTS_TO_RUN"] =  tests_to_run
            def string_test_value_string = tests_to_run.join("\n")
                
                if(sanityTestsSuccess == true){
                    build job: '../Protractor-Tests/saucelabs-Pulse-pipeline-CI-config-tests-FE',
                        parameters: [
                                string(name: 'SELENIUM_GRID_URL',
                                value: 'http://10.250.42.87:4444/wd/hub'),
                                string(name: 'NMS_SERVER_BRANCH', value: 'develop'),
                                booleanParam(name: 'USE_SAUCELABS', value: true),
                                string(name: 'NMS_DEV_TOOLS_BRANCH', value: protractor_provision_map["BRANCH_NAME"]),
                                string(name: 'SAUCE_TUNNEL', value: 'idirect_pulse_ci_tunnel'),
                                string(name: 'SAUCE_USERNAME', value: 'idirect_pulse_ci'),
                                string(name: 'SAUCE_ACCESS_KEY', value: 'c513a3f1-2b80-4c86-a95e-da0e5861dd43'),
                                string(name: 'SC_TUNNEL_CODE', value: '/jenkins-disk/home-jenkins/jenkins/work/nms_dev_tools'),
                                text(name: 'TESTS_TO_RUN', value: "${string_test_value_string}"),
                                string(name: 'NUM_ITERATIONS', value: '1'),
                                booleanParam(name: 'ALWAYS_DELETE_EC2_VM', value: true),
                                string(name: 'PRODUCTION_PIPELINE', value: "${PRODUCTION_PIPELINE}"),
                                string(name: 'USER_STARTED', value: "${userStarted}"),
                                string(name: 'EPIC_NAME', value: "${epicName}"),
                                string(name: 'BRANCH_NAME', value: protractor_provision_map["BRANCH_NAME"]),
                                string(name: 'BUILD_NUM', value: "${root_build_number}")

                            ], propagate: false
                    def protractorConfigTestsPipelineResults = readJSON file: protractorProvisionPipelineResultsFile;
                    println protractorConfigTestsPipelineResults;
                    allPipelineResults['protractorConfigTestsPipelineInfo'] = protractorConfigTestsPipelineResults;
                }
            }
            
        }
    
    parallel tasks

    
    for (plt in platforms_present){
        def platform = plt + '_' + "${PRODUCTION_PIPELINE}"
        println "PLATFORM"+platform
        _release_url = "<URL>"+ platform+ "/latest/_version.txt"
        println "RELEASE====="+_release_url
        //version_url_map[ platform ] = _release_url
        //def response = httpRequest _release_url

        def file_update_string = "nmsBuildVersion" + plt 
        println "FILE====="+file_update_string
        release_url_shell = "curl -s "+ _release_url
        allPipelineResults[file_update_string] = sh (script: release_url_shell, returnStdout: true).trim()
    }
        
    def causes = currentBuild.getBuildCauses()
    if ("${PRODUCTION_PIPELINE}" != "release" && causes[0].shortDescription.equals("Started by timer")){
        writeJSON(file: allPipelineResultsFile, json: allPipelineResults, pretty: 4)
        writeJSON(file: alllPipelineResultsFileName, json: allPipelineResults, pretty: 4)
        archiveArtifacts artifacts: alllPipelineResultsFileName
    }
    else{
        writeJSON(file: allPipelineResultsFile, json: allPipelineResults, pretty: 4)
        writeJSON(file: alllPipelineResultsFileName, json: allPipelineResults, pretty: 4)
        archiveArtifacts artifacts: alllPipelineResultsFileName
    }
    
    if (sanityTestsSuccess == false) {
        println "============================================================="
        println "The sanity tests did not run hence not running the pipeline.."
        println "============================================================="
        sh (script: "exit 1", returnStdout: true).trim()
    }
}