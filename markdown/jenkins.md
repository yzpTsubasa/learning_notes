## jenkins

## 需要发布者手动确认的多阶段任务
``` groovy
// ...
def ret = input message: '准备执行第二阶段?', ok: '继续执行', parameters: [booleanParam(defaultValue: true, description: '安静模式', name: 'HG_QUIET'), string(defaultValue: 'TEST', description: '测试', name: 'TEST')]
print ret
// ...
```

## 无法直接由jenkins执行的任务
> 自启动可能需要特殊权限的任务时，比如 pm2 需要开启 daemon 守护进程，无法在 jenkins 中调用成功，需要设置系统自启动项，如 `pm2 list`，
> 然后在由 jenkins 执行 `pm2 start ...`

## 使用 PowerShell Core Script (powershell) 执行后台任务，版本 7.x
> 默认的 PowerShell Script (pwsh) 使用的可能是旧版本，如 5.x，无法正常执行
``` groovy
pwsh 'Stop-Job -Name http_static_server; Remove-Job -Name http_static_server;'
                pwsh 'Start-Job -Name http_static_server -ScriptBlock { node . %JENKINS_HOME%/workspace --disable_serve_index 0 --valid_pattern "\\\\/(out|public)(\\\\/|$)" --debug 1 --case_sensitive 1 --http_port 9999 }'
```

## macOS 下 jenkins 操作
``` sh
# Install the latest LTS version
brew install jenkins-lts
# Install a specific LTS version
brew install jenkins-lts@YOUR_VERSION
# Start the Jenkins service
brew services start jenkins-lts
# Restart the Jenkins service
brew services restart jenkins-lts
# Update the Jenkins version
brew upgrade jenkins-lts
```

## 性能相关
- 机械硬盘下，IO压力大的情况下，尽量还是单次仅执行一个任务。否则容易出现阻塞，甚至严重的IO异常

## 使用 Pipeline Script from SCM 的问题
> SCM中其他文件的变更，也会触发 pollSCM，暂时不清楚如何屏蔽，只能暂时弃用这个方案。
> 除非单独给每个Jenkinsfile分配一个目录

> 如果使用 pollSCM 会有很大的额外消耗, 尽量还是避免使用

## groovy 中 bat 执行的细节
为了防止 `bat` 被异步执行，可能需要添加参数 `returnStatus` 或者 `returnStdout`

## 判断 svn 是否被locked
```groovy
// 检查状态
def status = bat returnStdout: true, script: '@echo off && svn status'
print status
// 一定要判断 status 是否为空，否则 =~ 正则表达式会报错
if (status) {
    def result = ((status =~ /^.{2}L/))
    if (result.find()) {
        print 'Workspace is already locked'
    } else {
        print "Workspace is not locked"
    }    
}
``` 

## 在 Pipeline 中使用 [URLTrigger](https://plugins.jenkins.io/urltrigger/)
``` groovy
pipeline {
    agent any
    triggers {
        URLTrigger(
            // 计划任务
            cronTabSpec: '*/2 * * * *',
            entries: [
                // URLTrigger
                URLTriggerEntry( 
                    url: "${ZIP_URL}", // 要检查的URL
                    checkLastModificationDate: true, // 检查修改时间
                    timeout: 300, // 超时(秒)
                    // username: 'myuser',
                    // password: 'mypassword',
                    // checkETag: false,
                    // checkStatus: true,
                    // statusCode: 403,
                    // contentTypes: [
                    //     MD5Sum()
                    // ],
                ),
            ]
        )
    }
    stages {
        stage('greeting') {
            steps {
                echo 'Hello World'
            }
        }
    }
    post { 
        always { 
            echo "Result=${currentBuild.result}"
        }
    }
}
```

## 时间格式化
``` groovy
new Date().format("yyyy-MM-dd HH:mm:ss")
new Date().format("yyyy-MM-dd'T'HH:mm:ss.SSS", TimeZone.getTimeZone('Asia/Shanghai'))
```

## 执行 sql 文件
``` groovy
def sqlfile = unstashParam "SQL_FILE"
fileOperations([folderCreateOperation('achieve')])
def dstFile = "achieve/" + new Date().format("yyyy-MM-dd HH-mm-ss") + ".sql"
print dstFile
fileOperations([fileRenameOperation(destination: dstFile, source: sqlfile)])
print readFile(encoding: 'utf-8', file: dstFile)
withEnv(["sqlfile=$dstFile"]) {
bat '''@echo off
mysql -h%HOST% -P%PORT% -u%USER% -p%PASSWORD% -e "source %sqlfile%"
'''
}
```
> 此处使用 `@echo off` 以及 `%HOST%` `%PORT%` `%USER%` `%PASSWORD%` 环境变量，是为了参数安全性

## 禁用并发构建
``` groovy
pipeline {
    agent any
    options {
        disableConcurrentBuilds()
    }
    stages {
        stage('XXX') {
            steps {
                script {
                    print 'XXX'
                }                
            }
        }
        stage('YYY') {
            steps {
                script {
                    print 'YYY'
                }   
            }
        }
    }
    post { 
        always { 
            script {
                print 'POST'
            }  
        }
    }
}
```

## 使用UTF-8编码
> 添加环境变量 变量名：JAVA_TOOL_OPTIONS 变量值： -Dfile.encoding=UTF-8
> 然后重启 Jenkins 服务

> Jenkinsfile(shared library) 代码也需要保存为 UTF-8 格式

## 打印对象尽量使用 println, 而不用 echo

## 捕获 bat 输出
> 要去除包含在输出中的回显内容，需要使用`@echo off`
``` groovy
bat([label: 'svn 信息', returnStdout: true, script: '@echo off && svn info', encoding: 'GBK'])
```

## 一直提示 svn 权限问题，可能是因为本地没有确认过凭证`credential`。只要执行一次拉取，并且永久接受即可

## Apache Tomcat 以服务形式进行后台运行
- 在`tomcat/bin`执行`service.bat install`安装系统服务 (执行 `service.bat uninstall` 可以移除服务)
- 运行`services.msc`，打开`Apache Tomcat`服务
> 后台方式与前台方式的运行环境不同，可能对不同的操作有不同的结果。
> 前台方式限制小。
> 后台方式要正确设置后台方式的`登录`，如使用指定管理员账户`Administrator`

## Apache Tomcat 修改默认首页为 jenkins
把`tomcat/webapps`的`jenkins.war`文件改成`ROOT.war`，并且删除原始的`ROOT`文件夹。
如果已经有`jenkins`文件夹，则把其改名为`ROOT`即可

## Apache Tomcat 中修改端口
在`tomcat/conf/server.xml`配置文件中
- 修改`Server`节点的`port`属性
- 修改`Server/Connector`节点的`port`属性

## Apache Tomcat 中配置 JENKINS_HOME
在`tomcat/conf/context.xml`配置文件的`Context`节点里，添加环境变量配置
``` xml
<Environment name="JENKINS_HOME" type="java.lang.String" value="E:/software/JenkinsTomcat" override="true"/>
```
## 安裝版本配置在默认安装目录下的 `jenkins.xml` 如 `C:\Program Files\Jenkins\jenkins.xml`
``` xml
<env name="JENKINS_HOME" value="E:\Jenkins"/>
```
重新启动即可

## 使用 thinBackup 备份

## 权限管理
### 添加所有项目的只读权限
- 安装插件 `Role-based Authorization Strategy`
- /configureSecurity `授权策略` 选择 `Role-Based Strategy`
- /role-strategy/manage-roles `Global roles` 在 `Role to add` 添加角色如`viewer`，勾选以下权限，保存
  - Overall/Read
  - Job/Read
- /role-strategy/assign-roles `Global roles` 的 `Anonymous` 勾选上面添加的角色 `viewer`，保存
- 已登录用户使用内置组 `authenticated`
### 添加特定项目的只读权限
- ![管理角色](..\assets\jenkins\manage_roles.png)
- ![分配角色](..\assets\jenkins\assign_roles.png)


## 项目名称使用纯英文，否则 pipeline 脚本可能会有问题 

## 使用脚本批量添加用户
``` groovy
import hudson.model.*
import hudson.security.*
import hudson.tasks.Mailer

def nominatedUsers = [
    ["test1", "测试员1", "test1@qq.com"],
    ["test2", "测试员2", "test2@qq.com"],
    ["test3", "测试员3", "test3@qq.com"],
]
nominatedUsers.each{ 
    def userId = it[0]
    def password = it[0]
    def fullName= it[1]
    def email = it[2]
    def user = Jenkins.instance.securityRealm.getUser(userId)
    if (!user) {
        user = Jenkins.instance.securityRealm.createAccount(userId, password)
        println("新用户 ${userId} ${fullName} ${email}, 添加成功!")
    } else {
        println("用户已存在 ${user.getId()} ${user.getFullName()}, 添加失败!")
    }
    user.addProperty(new Mailer.UserProperty(email));
    user.setFullName(fullName)
}
null
```