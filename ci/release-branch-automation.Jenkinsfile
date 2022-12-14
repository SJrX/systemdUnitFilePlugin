/**
 * This pipeline controls things pertaining to automating release branch stuff (e.g., fixing files, and altering GitHub).
 */
pipeline {
  options {
    // This is important because we are checking out git code at the same time and pushing it back up.
    disableConcurrentBuilds()
    timestamps()
    quietPeriod(1)
    skipDefaultCheckout()
  }
  agent {
    kubernetes {
      //cloud 'kubernetes'
      defaultContainer 'ci-utils'

      // language=yaml
      yaml """
        kind: Pod
        spec:
          hostAliases:
          # Custom set env var
          - ip: "${env.JENKINS_IP_ADDRESS}"
            hostnames:
            # Custom set env var
            - "${env.JENKINS_HOSTNAME}"
          containers:
          - name: ci-utils
            image: "${env.DOCKER_REGISTRY_PREFIX}/ci-utils:latest"
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
              - name: github-ssh-host-key
                mountPath:  /github-ssh-host-key
            
            securityContext:
              # Tried adding a new user but it didn't work maybe I need to look a bit more into it (or set USER and GROUP)
              runAsUser: 0 
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
            """
      workspaceVolume hostPathWorkspaceVolume('/opt/jenkins/workspace')
    }
  }
  stages {
    stage('Update Major Version') {
      steps {
        script {
          currentBuild.description = "Update Major Version to $env.BRANCH_NAME"
          container('ci-utils') {
            withCredentials([sshUserPrivateKey(credentialsId: 'ci-ssh-key', keyFileVariable: 'KEYFILE')]) {

              sh(script:
                   """#!/usr/bin/env bash

                  mkdir -p /root/.ssh/

                  cp /github-ssh-host-key/* /root/.ssh/
                  export GIT_SSH_COMMAND="ssh -i \$KEYFILE"
                  # ssh -i \$KEYFILE -T git@github.com
                  git clone git@github.com:SJrX/systemdUnitFilePlugin.git || true
                  git config --global user.email "jenkins@sjrx.net"
                  git config --global user.name "Jenkins CI System"
                  git -C systemdUnitFilePlugin fetch
                  git -C systemdUnitFilePlugin checkout \$BRANCH_NAME
                  git -C systemdUnitFilePlugin reset --hard origin/\$BRANCH_NAME
                  
                  

                  echo "#Warning this file is AUTO-generated and overridden" > systemdUnitFilePlugin/gradle.properties

                  echo \$BRANCH_NAME | sed -E "s/([0-9][0-9])([0-9]).x/intellijVersion=20\\1.\\2/" >> systemdUnitFilePlugin/gradle.properties
                  echo \$BRANCH_NAME | sed -E "s/([0-9][0-9])([0-9]).x/sinceVersion=\\1\\2.0/" >> systemdUnitFilePlugin/gradle.properties
                  echo \$BRANCH_NAME | sed -E "s/([0-9][0-9])([0-9]).x/untilVersion=270.0/" >> systemdUnitFilePlugin/gradle.properties
                  echo \$BRANCH_NAME | sed -E "s/([0-9][0-9])([0-9]).x/pluginMajorVersion=\\1\\2/" >> systemdUnitFilePlugin/gradle.properties

                  git -C systemdUnitFilePlugin add gradle.properties
                  cat systemdUnitFilePlugin/gradle.properties

                  if [[ `git -C systemdUnitFilePlugin status --porcelain` ]]; then
                    # Changes
                    git -C systemdUnitFilePlugin diff
                    git -C systemdUnitFilePlugin commit -m "Update gradle.properties for version \$BRANCH_NAME (Run ${env.BUILD_NUMBER})"
                    git -C systemdUnitFilePlugin push origin -f
                  else
                    # No changes
                    echo "No changes"
                  fi
            """)
            }
          }
        }
      }
    }
  }
}
