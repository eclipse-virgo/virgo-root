pipeline {

    agent {
        kubernetes {
            idleMinutes 5
            yamlFile 'build-pod.yaml'
            defaultContainer 'gradle'
        }
    }
    triggers {
        cron('@midnight')
    }
    options {
        timeout(time: 1, unit: 'HOURS')
        ansiColor('xterm')
    }

    stages {
        stage('send telegram message') {
            steps {
                withCredentials([string(credentialsId: 'telegram-bot-token', variable: 'TELEGRAM_BOT_TOKEN')]) {
                    sh "./send-telegram-message.sh \$(git log -1 --pretty=oneline --abbrev-commit)"
                }
            }
        }

        stage('build') {
			steps {
                sh 'gradle -Dci.build=true clean build'
    	    }
        }

        stage ('junit') {
			steps {
		        junit allowEmptyResults: true, testResults: 'build/test-results/test/*.xml', skipPublishingChecks: true
    	    }
        }

        stage ('recordIssues') {
            steps {
                recordIssues tools: [
                    taskScanner(highTags:'FIXME', normalTags:'TODO', includePattern: '**/*.java', excludePattern: 'target/**/*'),
//                     spotBugs(pattern: '**/build/reports/spotbugs/*.xml'),
                ]
            }
        }

		stage ('archive artifacts') {
			when {
				// only green builds
				expression { currentBuild.result == null }
			}
			steps {
				archiveArtifacts 'build/libs/*.jar'
			}
		}

        stage('send success telegram message') {
			when {
				// only green builds
				expression { currentBuild.result == null }
			}
            steps {
                withCredentials([string(credentialsId: 'telegram-bot-token', variable: 'TELEGRAM_BOT_TOKEN')]) {
                    sh "./send-telegram-message.sh success"
                }
            }
        }

        stage('send failure telegram message') {
			when {
				// only green builds
				expression { currentBuild.result != null }
			}
            steps {
                withCredentials([string(credentialsId: 'telegram-bot-token', variable: 'TELEGRAM_BOT_TOKEN')]) {
                    sh "./send-telegram-message.sh failed"
                }
            }
        }
    }
}
