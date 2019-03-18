pipeline {
    agent any

    tools { 
        //These have to be configured by a Jenkins administrator
        maven 'Maven 3.6.0' 
    }

    stages {
        stage ('Build develop') {
            when { branch 'develop' }
            steps {
                sh 'mvn clean install'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage ('Build branch') {
            when { not { branch 'develop' } }
            steps {
                // Not on the develop branch, so build it, but do not install it.
                sh 'mvn clean verify'
            }
        }
    }
}
