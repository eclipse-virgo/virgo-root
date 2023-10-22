pipeline {
  agent {
    kubernetes {
      label 'virgo-agent-pod'
      yaml """
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: gradle
    image: gradle:5.6.4-jdk8
    command:
    - cat
    tty: true

    env:
    - name: GRADLE_USER_HOME
      value: "/tmp/gradle"

    resources:
      limits:
        memory: "2Gi"
        cpu: "1"
      requests:
        memory: "2Gi"
        cpu: "1"
"""
    }
  }
  stages {
    stage('Build') {
      steps {
        container('gradle') {
          sh 'gradle build'
        }
      }
    }
  }
}
