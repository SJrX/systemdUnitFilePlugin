/**
 * This pipeline controls things pertaining to automating release branch stuff (e.g., fixing files, and altering GitHub).
 */

def buildDate = new Date().format("yyMMdd", TimeZone.getTimeZone('UTC'))

def buildPodDefinition(workerPodImage, ciUtilsEnabled, kanikoEnabled) {
  // language=yaml
  yaml = """
kind: Pod
spec:
  hostAliases:
  # Custom set env var
  - ip: "${env.JENKINS_IP_ADDRESS}"
    hostnames:
    # Custom set env var
    - "${env.JENKINS_HOSTNAME}"
  volumes:
  - name: github-ssh-host-key
    configMap:
      name: github-ssh-host-key
  - name: jenkins-docker-cfg
    projected:
      sources:
      - secret:
          name: regcred
          items:
            - key: .dockerconfigjson
              path: config.json
  containers:
"""

  if (kanikoEnabled) {
    yaml += """
  - name: kaniko
    image: gcr.io/kaniko-project/executor:v1.8.1-debug
    imagePullPolicy: Always
    command:
    - sleep
    args:
    - 99d
    resources:
    requests:
      memory: "3172Mi"
    volumeMounts:
    - name: jenkins-docker-cfg
      mountPath: /kaniko/.docker
    - name: github-ssh-host-key
      mountPath:  /github-ssh-host-key   
    securityContext:
     # Tried adding a new user but it didn't work maybe I need to look a bit more into it (or set USER and GROUP)
     runAsUser: 0 
"""
  }

  if (workerPodImage != null) {
    yaml += """
  - name: worker-pod
    image: "$workerPodImage"
    imagePullPolicy: Always
    command:
    - sleep
    args:
    - 99d
    resources:
      requests:
        memory: "2048Mi"
    volumeMounts:
    - name: github-ssh-host-key
      mountPath:  /github-ssh-host-key   
    securityContext:
      runAsUser: 1000 # default UID of jenkins user in agent image
"""
  }

  if (ciUtilsEnabled) {
    yaml += """
  - name: ci-utils
    image: ${env.DOCKER_REGISTRY_PREFIX}/ci-utils:latest
    imagePullPolicy: Always
    command:
    - sleep
    args:
    - 99d
    resources:
      requests:
        memory: "128Mi"
      limits:
        memory: "128Mi"
    volumeMounts:
    - name: jenkins-docker-cfg
      mountPath: /kaniko/.docker
    - name: github-ssh-host-key
      mountPath:  /github-ssh-host-key
    securityContext:
      runAsUser: 1000 # default UID of jenkins user in agent image
"""
  }

  return yaml
}

pipeline {
  options {
    // This is important because we are checking out git code at the same time and pushing it back up.
    disableConcurrentBuilds()
    timestamps()
    quietPeriod(1)
    //skipDefaultCheckout()
  }
  agent none

  stages {
    stage('Create Build Environment') {
      stages {
//        stage('Determine Image Hashes') {
//          agent {
//            kubernetes {
//              //cloud 'kubernetes'
//              defaultContainer 'ci-utils'
//
//              // language=yaml
//              yaml buildPodDefinition(null, true, false)
//              //workspaceVolume hostPathWorkspaceVolume('/opt/jenkins/workspace')
//            }
//          }
//          steps {
//            script {
//              ubuntuUnitsHash = sh(script: """
//                  sha256sum ./systemd-build/Ubuntu-Units.Dockerfile ./systemd-build/ubuntu-units.sh | sha256sum | sed -E "s/\\s.+\$/-${buildDate}/g"
//            """, returnStdout: true
//              ).trim()
//              withEnv(["IMAGE=sjrx/systemd-plugin-ubuntu-image", "TAG=${ubuntuUnitsHash}"]) {
//                ubuntuUnitsBuildNeeded = sh(script: '''
//                /check-if-image-exists
//                ''', returnStatus: true)
//              }
//              buildEnvironmentHash = sh(script: '''
//                  sha256sum ./ci/Build-Environment.Dockerfile | sha256sum | sed -E "s/\\s.+$//g"
//            ''', returnStdout: true
//              ).trim()
//              withEnv(["IMAGE=sjrx/systemd-plugin-build-environment", "TAG=${buildEnvironmentHash}"]) {
//                buildEnvironmentBuildNeeded = sh(script: '''
//                /check-if-image-exists
//                ''', returnStatus: true)
//              }
//              systemdBuilderHash = sh(script: '''
//                  sha256sum ./systemd-build/Systemd-Builder.Dockerfile ./systemd-build/systemd-build.sh | sha256sum | sed -E "s/\\s.+$//g"
//            ''', returnStdout: true
//              ).trim()
//              withEnv(["IMAGE=sjrx/systemd-plugin-systemd-builder-image", "TAG=${systemdBuilderHash}"]) {
//                systemdBuilderBuildNeeded = sh(script: '''
//                /check-if-image-exists
//                ''', returnStatus: true)
//              }
//            }
//          }
//        }
//        stage('Build Needed Images') {
//          parallel {
//            stage('Conditionally Build and Push Ubuntu Units') {
//              when {
//                anyOf {
//                  equals expected: 1, actual: ubuntuUnitsBuildNeeded
//                }
//              }
//              stages {
//                stage('Build and Push Ubuntu Units') {
//                  agent {
//                    kubernetes {
//                      //cloud 'kubernetes'
//                      defaultContainer 'kaniko'
//
//                      // language=yaml
//                      yaml buildPodDefinition(null, false, true)
//                      //workspaceVolume hostPathWorkspaceVolume('/opt/jenkins/workspace')
//                    }
//                  }
//                  steps {
//                    container('kaniko') {
//                      sh """
//                        /kaniko/executor --force -f `pwd`/systemd-build/Ubuntu-Units.Dockerfile -c `pwd`/systemd-build/ --cache=true --compressed-caching=false --destination=${env.DOCKER_REGISTRY_PREFIX}/systemd-plugin-ubuntu-image:$ubuntuUnitsHash
//                      """
//                    }
//                  }
//                }
//              }
//            }
//            stage('Conditionally Build and Push Systemd Builder') {
//              when {
//                anyOf {
//                  equals expected: 1, actual: systemdBuilderBuildNeeded
//                }
//              }
//              // Nest the actual stage inside another stages block to prevent agent deployment when not needed
//              stages {
//                stage("Build and Push Systemd Builder") {
//                  agent {
//                    kubernetes {
//                      //cloud 'kubernetes'
//                      defaultContainer 'kaniko'
//
//                      // language=yaml
//                      yaml buildPodDefinition(null, false, true)
//                      //workspaceVolume hostPathWorkspaceVolume('/opt/jenkins/workspace')
//                    }
//                  }
//                  steps {
//                    container('kaniko') {
//                      sh """
//                    /kaniko/executor --force -f `pwd`/systemd-build/Systemd-Builder.Dockerfile -c `pwd`/systemd-build/ --cache=true --compressed-caching=false --destination=${env.DOCKER_REGISTRY_PREFIX}/systemd-plugin-systemd-builder-image:$systemdBuilderHash
//                      """
//                    }
//                  }
//                }
//              }
//            }
//            stage('Conditionally Build and Push Build Environment') {
//              when {
//                anyOf {
//                  equals expected: 1, actual: buildEnvironmentBuildNeeded
//                }
//              }
//              stages {
//                stage("Build and Push Build Environment") {
//
//                  agent {
//                    kubernetes {
//                      //cloud 'kubernetes'
//                      defaultContainer 'kaniko'
//
//                      // language=yaml
//                      yaml buildPodDefinition(null, false, true)
//                      //workspaceVolume hostPathWorkspaceVolume('/opt/jenkins/workspace')
//                    }
//                  }
//
//                  steps {
//                    container('kaniko') {
//                      sh """
//                      /kaniko/executor --force -f `pwd`/ci/Build-Environment.Dockerfile -c `pwd`/systemd-build/ --cache=false --compressed-caching=false --destination=${env.DOCKER_REGISTRY_PREFIX}/systemd-plugin-build-environment:$buildEnvironmentHash
//                    """
//                    }
//                  }
//                }
//              }
//            }
//          }
//        }
//        stage("Assemble Metadata") {
//          parallel {
//            stage('Assemble Ubuntu Units') {
//              agent {
//                kubernetes {
//                  //cloud 'kubernetes'
//                  defaultContainer 'worker-pod'
//
//                  // language=yaml
//                  yaml buildPodDefinition("${env.DOCKER_REGISTRY_PREFIX}/systemd-plugin-ubuntu-image:$ubuntuUnitsHash", false, false)
//                  //workspaceVolume hostPathWorkspaceVolume('/opt/jenkins/workspace')
//                }
//              }
//              steps {
//                sh("""
//                   mkdir -p ./systemd-build/build
//
//                   cp /ubuntu-units.txt ./systemd-build/build/ubuntu-units.txt
//                   """)
//                stash includes: 'systemd-build/build/**', name: 'ubuntu-units', allowEmpty: true
//                }
//            }
//            stage('Assemble systemd metadata') {
//              agent {
//                kubernetes {
//                  //cloud 'kubernetes'
//                  defaultContainer 'worker-pod'
//
//                  // language=yaml
//                  yaml buildPodDefinition("${env.DOCKER_REGISTRY_PREFIX}/systemd-plugin-systemd-builder-image:$systemdBuilderHash", false,false)
//                  //workspaceVolume hostPathWorkspaceVolume('/opt/jenkins/workspace')
//                }
//              }
//              steps {
//
//                  sh("""
//                      mkdir -p ./systemd-build/build
//                      cp /opt/systemd-source/systemd/load-fragment-gperf.gperf ./systemd-build/build
//                      cp -R /opt/systemd-source/systemd/man ./systemd-build/build
//                    """)
//                stash includes: 'systemd-build/build/**', name: 'systemd-build-build', allowEmpty: false
//              }
//            }
//          }
//        }
        stage("Build") {
          agent {
            kubernetes {
              //cloud 'kubernetes'
              defaultContainer 'worker-pod'

              // language=yaml
              yaml buildPodDefinition("${env.DOCKER_REGISTRY_PREFIX}/systemd-plugin-build-environment:bb76be5adbff362a15efe3fbbe1c083ce07cd1b99d7549e7d9429bcbdfc755b3", false,false)
              //workspaceVolume hostPathWorkspaceVolume('/opt/jenkins/workspace')
            }
          }
          steps {
            //unstash 'systemd-build-build'
            //unstash 'ubuntu-units'
            withCredentials([sshUserPrivateKey(credentialsId: 'ci-ssh-key', keyFileVariable: 'KEYFILE')]) {
                sh("""
                  #./gradlew --build-cache clean build buildPlugin --scan
                  exit 0
                  """)
                script {
                  if (env.BRANCH_NAME ==~ /^([0-9][0-9][0-9].x|PR-176)$/) {
                    sh("""
                    echo "Tagging"
                    mkdir -p ~/.ssh/
                    #Delete me
                    mkdir -p ./build/distributions/
                    touch build/distributions/systemdUnitFilePlugin-223.230108.113.zip

                    VERSION=\$(ls -1 ./build/distributions/*.zip | sort | tail -n 1 | sed -E "s/.+-(.+).zip\$/\\1/g")
                    cp /github-ssh-host-key/* ~/.ssh/
                    export GIT_SSH_COMMAND="ssh -i \$KEYFILE"
                    git config --global user.email "jenkins@sjrx.net"
                    git config --global user.name "Jenkins CI System"
  
                    git status
  
                    echo "Current Version \$VERSION"
  
                    git tag "v\${VERSION}"
                    git push -f "v\${VERSION}"
  
  
                    printenv
  
                    exit 0;
"""
                    )
                  } else {
                    sh("""
                    echo "No tagging"
""")                  }
                  }
//                  if [[ \$BRANCH_NAME ~=  ]]; then

//            archiveArtifacts artifacts: 'build/distributions/*.zip'
//           archiveArtifacts artifacts: 'build/reports/**'
            }
          }
        }
      }
    }
  }
  post {
    failure {
      mail body: "<br>Project: ${env.JOB_NAME} <br>Build: <a href=\"${env.BUILD_URL}\">#${env.BUILD_NUMBER}</a> Failed", charset: 'UTF-8', mimeType: 'text/html',
           subject: "ERROR CI: Project name -> ${env.JOB_NAME}", to: "${env.FAILED_BUILD_NOTIFICATION_EMAIL}"

    }
    fixed {
      mail body: "<b>Example</b><br>Project: ${env.JOB_NAME} <br>Build: <a href=\"${env.BUILD_URL}\">${env.BUILD_NUMBER}</a> Failed\"", charset: 'UTF-8', mimeType: 'text/html',
           subject: "OK CI: Project name -> ${env.JOB_NAME}", to: "${env.FAILED_BUILD_NOTIFICATION_EMAIL}"
    }
  }
}
