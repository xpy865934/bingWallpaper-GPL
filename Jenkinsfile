// 所有的脚本命令灰放在pipeline中
pipeline {
    // 指定任务在哪个集群节点中执行
    agent any
    // 制定jdk
    tools {
       jdk "jdk17"
    }
    // 声明环境变量
    environment {
        PROJECT_GIT_URL='http://127.0.0.1/bingWallpaper'
        ACTIVE_PROFILE='test'
        REMOTE_DIRECTORY='bing_wallpaper'
    }
    stages {
        stage('拉取git仓库代码') {
            steps {
            }
        }
        stage('通过maven构建项目') {
            steps {
            }
        }
        stage('发送后端文件到目标服务器') {
            steps {
            }
        }
        stage('构建前端文件') {
            steps {
                sh '''
                cd ruoyi-ui/
                yarn install
                yarn run build:${ACTIVE_PROFILE}'''
            }
        }
        stage('发送前端文件到目标服务器') {
            steps {
            }
        }
    }
}
