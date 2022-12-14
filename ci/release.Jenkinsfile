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
      defaultContainer 'build'

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
          - name: build
            image: "${env.DOCKER_REGISTRY_PREFIX}/systemd-plugin-build-environment:467822542980fb55dd01fd916fe729b3f4e0decd"
            imagePullPolicy: Always
            command:
            - sleep
            args:
            - 99d
            resources:
              requests:
                memory: "4096Mi"
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
    stage('Build') {
      steps {
        script {
          currentBuild.description = "Building release"
          container('build') {
            withCredentials([sshUserPrivateKey(credentialsId: 'ci-ssh-key', keyFileVariable: 'KEYFILE')]) {

              sh(script:
                   """
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
                  
                  cd systemdUnitFilePlugin
                  
                  ./gradlew buildPlugin
                  """)
            }
          }
        }
      }
    }
  }
}
