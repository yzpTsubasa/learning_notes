package org.devops 

import org.yaml.snakeyaml.Yaml

def getChangeString() {
    MAX_MSG_LEN = 100
    echo "Gathering SCM changes......"
    return currentBuild.changeSets.collect{
        def i = 1
        return it.items.collect{
            "${i++}. ${it.msg.take(MAX_MSG_LEN).replaceAll("[\r\n]+", "")} by ${it.author.getFullName()} at ${it.getCommitId()}"
        }.join("\n")
    }
}

def sendStart2DingTalk() {
    if (params.HG_QUIET) {
        return;
    }
    // dingtalk(
    //     robot: 'automator',
    //     type: 'ACTION_CARD',
    //     title: "${currentBuild.fullDisplayName} 开始",
    //     text: [
    //         "- **任务** [${currentBuild.fullDisplayName}](${BUILD_URL}) ",
    //         "- **状态** 开始",
    //         // "- **备注** ${env.HG_BUILD_DESC ? env.HG_BUILD_DESC : '无'}",
    //         "- **发起** ${currentBuild.getBuildCauses()[0].userName ? currentBuild.getBuildCauses()[0].userName : currentBuild.getBuildCauses()[0].shortDescription.minus("Started by ").replace("timer", "定时器").replace("an SCM change", "SCM轮询")}",
    //         "- **记录**",
    //         "***",
    //     ] + getChangeString()
    // )
}

def sendResult2DingTalk() {
    if (params.HG_QUIET) {
        return;
    }
    env.result_color = currentBuild.result == 'SUCCESS' ? '#52c41a' : currentBuild.result == 'FAILURE' ? '#f5222d' : '#ff9f00'
    env.result = currentBuild.result == 'SUCCESS' ? '成功' : currentBuild.result == 'FAILURE' ? '失败' : '取消'
    env.description = currentBuild.description
    env.durationString = currentBuild.durationString.minus(" and counting")
    def atUsers = getAtUsers()
    dingtalk(
        robot: 'automator',
        type: 'ACTION_CARD',
        title: "${currentBuild.fullDisplayName} ${result}",
        at: atUsers,
        atAll: false,
        text: [
            "- **任务** [${currentBuild.fullDisplayName}](${BUILD_URL}) ",
            "- **状态** <font color=${result_color}>${result}</font>",
            // "- **备注** ${env.HG_BUILD_DESC ? env.HG_BUILD_DESC : '无'}",
            "- **发起** ${currentBuild.getBuildCauses()[0].userName ? currentBuild.getBuildCauses()[0].userName : currentBuild.getBuildCauses()[0].shortDescription.minus("Started by ").replace("timer", "定时器").replace("an SCM change", "SCM轮询")}",
            "- **用时** ${durationString}",
            "- **记录**",
            "***",
        ] + getChangeString() + (
            currentBuild.result == 'FAILURE' ? [
                "***",
                "- **<font color=${result_color}>失败日志</font>**",
                getTailLogString(),
            ] : []
        )
    )
}

// 获取当前版本号
def getLastChangedRev() {
    def out = bat([returnStdout: true, script: '@echo off && svn info'])
    def yaml = new Yaml()
    def map = yaml.load(out)
    return map['Last Changed Rev']
}

// 新的发布流程
def pubToWeb() {
    bat([label: '发布', returnStdout: false, script: """cd /d "E:/projects/publish"
    git checkout -- * 
    git pull
    if "%chkdst%" == "true" (
    npx hgbuild walk ${HG_PUB_RES} ${HG_PUB_TYPE} --noUserOp --chkdst
    ) else (
    npx hgbuild walk ${HG_PUB_RES} ${HG_PUB_TYPE} --noUserOp
    )"""])
}

// 新的发布流程 _10_common 发布入口
def pubToWebCommon() {
    // _10_common 发布入口
    bat([label: '发布', returnStdout: false, script: """cd /d "E:/projects/publish"
    git checkout -- * 
    git pull
    if "%chkdst%" == "true" (
        hgbuild run _10_common --prg_dir ${pwd()} --upload_filter ${params.upload_filter} --toolTag ${params.toolTag} --cfg_dir ${params.cfg_dir} --noUserOp --chkdst
    ) else (
        hgbuild run _10_common --prg_dir ${pwd()} --upload_filter ${params.upload_filter} --toolTag ${params.toolTag} --cfg_dir ${params.cfg_dir} --noUserOp
    )"""])
}

// 新的发布流程 - 集成版本
def pubToWebIntegrated() {
    lock(resource: "${HG_PUB_RES}") {
        // 检出
        checkoutSVN(params.HG_REPOSITORY_SRC)
        // 发送通知
        sendStart2DingTalk_PubWeb()
        // 发布
        pubToWeb()
    }
}

// 新的发布流程 - 集成版本
def pubToWebIntegratedCommonOld() {
    lock(resource: "${cfg_dir}") {
        dir("project") {
            // 检出
            checkoutSVN(params.HG_REPOSITORY_SRC)
            // 发送通知
            sendStart2DingTalk_PubWeb()
        }
        // 发布
        dir("publish") {
            checkout([$class: 'GitSCM', branches: [[name: '*/yzp']], extensions: [], userRemoteConfigs: [[url: 'http://192.168.1.205:3000/fangjie/publish.git']]])
            bat """
npm i
"""
            bat([label: '发布', returnStdout: false, script: """
if "%chkdst%" == "true" (
    hgbuild run _11_common_old --prg_dir ${WORKSPACE}/project --upload_filter ${params.upload_filter} --toolTag ${params.toolTag} --cfg_dir ${params.cfg_dir} --noUserOp --chkdst
) else (
    hgbuild run _11_common_old --prg_dir ${WORKSPACE}/project --upload_filter ${params.upload_filter} --toolTag ${params.toolTag} --cfg_dir ${params.cfg_dir} --noUserOp
)"""])
        }
    }
}

// 新的发布流程 - 集成版本
def pubToWebIntegratedCommon() {
    lock(resource: "${cfg_dir}") {
        dir("project") {
            // 检出
            checkoutSVN(params.HG_REPOSITORY_SRC)
            // 发送通知
            sendStart2DingTalk_PubWeb()
        }
        // 发布
        dir("publish") {
            checkout([$class: 'GitSCM', branches: [[name: '*/yzp']], extensions: [], userRemoteConfigs: [[url: 'http://192.168.1.205:3000/fangjie/publish.git']]])
            bat """
npm i
"""
            bat([label: '发布', returnStdout: false, script: """
if "%chkdst%" == "true" (
    hgbuild run _10_common --prg_dir ${WORKSPACE}/project --upload_filter ${params.upload_filter} --toolTag ${params.toolTag} --cfg_dir ${params.cfg_dir} --noUserOp --chkdst
) else (
    hgbuild run _10_common --prg_dir ${WORKSPACE}/project --upload_filter ${params.upload_filter} --toolTag ${params.toolTag} --cfg_dir ${params.cfg_dir} --noUserOp
)"""])
        }
    }
}

// pubToWeb构建开始
def sendStart2DingTalk_PubWeb() {
    if (params.HG_QUIET) {
        return;
    }
    dingtalk(
        robot: 'automator',
        type: 'ACTION_CARD',
        title: "${currentBuild.fullDisplayName} 开始",
        // at: getAtUsers(),
        // atAll: false,
        text: [
            "# **[${currentBuild.fullDisplayName}](${BUILD_URL})**",
            "***",
            "- **状态** 开始",
            // "- **备注** ${env.HG_BUILD_DESC ? env.HG_BUILD_DESC : '无'}",
            "- **发起** ${currentBuild.getBuildCauses()[0].userName ? currentBuild.getBuildCauses()[0].userName : currentBuild.getBuildCauses()[0].shortDescription.minus("Started by ").replace("timer", "定时器").replace("an SCM change", "SCM轮询")}",
            "- **仓库**",
            params.HG_REPOSITORY_SRC ? (params.HG_REPOSITORY_SRC - ~/.*\//) : "Unknown",
            "- **记录**",
            "***",
        ] + getChangeString()
    )
}

// 获取要@的用户
def getAtUsers() {
    def AT_USERS_STR = params.AT_USERS != null ? params.AT_USERS : "";
    def AT_USERS = AT_USERS_STR.tokenize(",");
    // 添加构建者(需要允许指定的API)
    def builderMobile = currentBuild.getBuildCauses()[0].userId ? hudson.model.User.getById(currentBuild.getBuildCauses()[0].userId, false).getProperty(io.jenkins.plugins.DingTalkUserProperty.class).getMobile() : "";
    if (builderMobile) {
        AT_USERS.add(builderMobile)
    }
    // 失败时，@提交者
    if (currentBuild.result == 'FAILURE') {
        AT_USERS += getCommitUserMobiles()
    }
    // 去重
    AT_USERS.unique()
    print AT_USERS
    return AT_USERS
}

// 获取当前提交者的手机号
def getCommitUserMobiles() {
    def mobiles = (currentBuild.changeSets.collect{
        it.items.collect{
            hudson.model.User.getById(it.author.getId(), false).getProperty(io.jenkins.plugins.DingTalkUserProperty.class).getMobile()
        }.findAll {
            it
        }
    }).flatten();
    return mobiles ? mobiles : []
}

// pubToWeb构建结束
def sendResult2DingTalk_PubWeb() {
    if (params.HG_QUIET) {
        return;
    }
    env.result_color = currentBuild.result == 'SUCCESS' ? '#52c41a' : currentBuild.result == 'FAILURE' ? '#f5222d' : '#ff9f00'
    env.result = currentBuild.result == 'SUCCESS' ? '成功' : currentBuild.result == 'FAILURE' ? '失败' : '取消'
    env.description = currentBuild.description
    env.durationString = currentBuild.durationString.minus(" and counting")
    def pubWebVersion = getPubWebVersion()
    dingtalk(
        robot: 'automator',
        type: 'ACTION_CARD',
        title: "${currentBuild.fullDisplayName} ${result}",
        at: getAtUsers(),
        atAll: false,
        text: [
            "# **[${currentBuild.fullDisplayName}](${BUILD_URL})**",
            "***",
            "- **状态** <font color=${result_color}>${result}</font>",
            pubWebVersion ? "- **资源版本** <font color=${result_color}>${pubWebVersion}</font>" : "",
            // "- **备注** ${env.HG_BUILD_DESC ? env.HG_BUILD_DESC : '无'}",
            "- **发起** ${currentBuild.getBuildCauses()[0].userName ? currentBuild.getBuildCauses()[0].userName : currentBuild.getBuildCauses()[0].shortDescription.minus("Started by ").replace("timer", "定时器").replace("an SCM change", "SCM轮询")}",
            "- **用时** ${durationString}",
            "- **仓库**",
            params.HG_REPOSITORY_SRC ? (params.HG_REPOSITORY_SRC - ~/.*\//) : "Unknown",
            "- **记录**",
            "***",
        ] + getChangeString() + (
            currentBuild.result == 'FAILURE' ? [
                "***",
                "- **<font color=${result_color}>失败日志</font>**",
                getTailLogString(),
            ] : []
        )
    )
}

// 取回已翻译的内容 API版本
def retrieveTranslationAPI() {
    lock(resource: "conversion_api") {
        dir('automator') {
            checkout changelog: false, poll: false, scm: [$class: 'GitSCM', branches: [[name: 'master']], extensions: [], userRemoteConfigs: [[url: 'https://e.coding.net/tsubasaohzora/playground/automator.git']]]
            bat 'npm i'
        }
        dir('project/resource/assets/cfgjson') {
            checkoutComplexSVN([scm: [$class: 'SubversionSCM', additionalCredentials: [], excludedCommitMessages: '', excludedRegions: '', excludedRevprop: '', excludedUsers: '', filterChangelog: true, ignoreDirPropChanges: false, includedRegions: '''.*/resource/assets/cfgjson/\\w+\\.json
    .*/resource/assets/cfgjson/base/\\w+\\.json
    .*/resource/js/common\\.js''', locations: [[cancelProcessOnExternalsFail: true, credentialsId: 'dfb8344e-2d0c-4750-8154-9503745a01f9', depthOption: 'infinity', ignoreExternalsOption: true, local: '.', remote: "$SCM_URL/resource/assets/cfgjson"]], quietOperation: true, workspaceUpdater: [$class: 'UpdateUpdater']]])
        }
        dir('project/resource/js') {
            checkoutComplexSVN([scm: [$class: 'SubversionSCM', additionalCredentials: [], excludedCommitMessages: '', excludedRegions: '', excludedRevprop: '', excludedUsers: '', filterChangelog: true, ignoreDirPropChanges: false, includedRegions: '''.*/resource/assets/cfgjson/\\w+\\.json
    .*/resource/assets/cfgjson/base/\\w+\\.json
    .*/resource/js/common\\.js''', locations: [[cancelProcessOnExternalsFail: true, credentialsId: 'dfb8344e-2d0c-4750-8154-9503745a01f9', depthOption: 'infinity', ignoreExternalsOption: true, local: '.', remote: "$SCM_URL/resource/js"]], quietOperation: true, workspaceUpdater: [$class: 'UpdateUpdater']]])
        }   
        dir('translation') {
            checkoutComplexSVN changelog: false, poll: false, scm: [$class: 'SubversionSCM', additionalCredentials: [], excludedCommitMessages: '', excludedRegions: '', excludedRevprop: '', excludedUsers: '', filterChangelog: false, ignoreDirPropChanges: false, includedRegions: '', locations: [[cancelProcessOnExternalsFail: true, credentialsId: 'dfb8344e-2d0c-4750-8154-9503745a01f9', depthOption: 'infinity', ignoreExternalsOption: true, local: '.', remote: 'https://svn100.hotgamehl.com/svn/Html5/trunk/dldl_WX/translation_keyvalue']], quietOperation: true, workspaceUpdater: [$class: 'UpdateUpdater']]
        }
        dir('convert2src') {
            bat 'node %WORKSPACE%/automator/main.js %WORKSPACE%/automator/cfg/dldl/conversion_retrieve@api.yml --FULL_AUTOMATIC 1 --QUITE_MODE 1 --projectFolder %WORKSPACE%/project --gitFolder %WORKSPACE%/i18n_cp_seirei --conversionWorkspaceFolder %WORKSPACE%/conversion --translationFolder %WORKSPACE%/translation --zipUrl "%ZIP_URL%"'
        }
    }
}

// 生成翻译KV表_API
def generateTranslationKV_API() {
    lock(resource: "conversion_api") {
        dir('automator') {
            checkout changelog: false, poll: false, scm: [$class: 'GitSCM', branches: [[name: 'master']], extensions: [], userRemoteConfigs: [[url: 'https://e.coding.net/tsubasaohzora/playground/automator.git']]]
            bat 'npm i'
        }
        dir('translation') {
            checkoutComplexSVN changelog: false, poll: false, scm: [$class: 'SubversionSCM', additionalCredentials: [], excludedCommitMessages: '', excludedRegions: '', excludedRevprop: '', excludedUsers: '', filterChangelog: false, ignoreDirPropChanges: false, includedRegions: '', locations: [[cancelProcessOnExternalsFail: true, credentialsId: 'dfb8344e-2d0c-4750-8154-9503745a01f9', depthOption: 'infinity', ignoreExternalsOption: true, local: '.', remote: 'https://svn100.hotgamehl.com/svn/Html5/trunk/dldl_WX/translation_keyvalue']], quietOperation: true, workspaceUpdater: [$class: 'UpdateUpdater']]
        }
        dir('project/resource/assets/cfgjson') {
            checkoutComplexSVN([$class: 'SubversionSCM', additionalCredentials: [], excludedCommitMessages: '', excludedRegions: '', excludedRevprop: '', excludedUsers: '', filterChangelog: true, ignoreDirPropChanges: false, includedRegions: '''.*/resource/assets/cfgjson/\\w+\\.json
    .*/resource/assets/cfgjson/base/\\w+\\.json
    .*/resource/js/common\\.js''', locations: [[cancelProcessOnExternalsFail: true, credentialsId: 'dfb8344e-2d0c-4750-8154-9503745a01f9', depthOption: 'infinity', ignoreExternalsOption: true, local: '.', remote: "$SCM_URL/resource/assets/cfgjson"]], quietOperation: true, workspaceUpdater: [$class: 'UpdateUpdater']])
        }
        dir('project/resource/js') {
            checkoutComplexSVN([$class: 'SubversionSCM', additionalCredentials: [], excludedCommitMessages: '', excludedRegions: '', excludedRevprop: '', excludedUsers: '', filterChangelog: true, ignoreDirPropChanges: false, includedRegions: '''.*/resource/assets/cfgjson/\\w+\\.json
    .*/resource/assets/cfgjson/base/\\w+\\.json
    .*/resource/js/common\\.js''', locations: [[cancelProcessOnExternalsFail: true, credentialsId: 'dfb8344e-2d0c-4750-8154-9503745a01f9', depthOption: 'infinity', ignoreExternalsOption: true, local: '.', remote: "$SCM_URL/resource/js"]], quietOperation: true, workspaceUpdater: [$class: 'UpdateUpdater']])
        }                
        dir('convert2src') {
            bat 'node %WORKSPACE%/automator/main.js %WORKSPACE%/automator/cfg/dldl/conversion_to_src@api.yml --FULL_AUTOMATIC 1 --QUITE_MODE 1 --projectFolder %WORKSPACE%/project --conversionWorkspaceFolder %WORKSPACE%/conversion --translationFolder %WORKSPACE%/translation'
        }
    }
}

def mergeSVN() {
    dir('automator') {
            checkout changelog: false, poll: false, scm: [$class: 'GitSCM', branches: [[name: 'master']], extensions: [], userRemoteConfigs: [[url: 'https://e.coding.net/tsubasaohzora/playground/automator.git']]]
            bat 'npm i'
    }
    dir('project') {
        checkoutSVN(params.HG_REPOSITORY_SRC)
    }
    bat 'node %WORKSPACE%/automator/main.js %WORKSPACE%/automator/cfg/dldl/svn_merge.yml --FULL_AUTOMATIC 1 --QUITE_MODE 1 --dst %WORKSPACE%/project --src %MREGE_SRC% --revisions "%MERGE_REVISIONS%"'
    if (params.BUILD_NEXT_JOB && params.NEXT_JOB) {
        build wait: false, job: params.NEXT_JOB
    }
}

def sendResult2Emailext (){
    if (params.HG_QUIET) {
        return;
    }

    env.result_color = currentBuild.result == 'SUCCESS' ? '#52c41a' : currentBuild.result == 'FAILURE' ? '#f5222d' : '#ff9f00'
    env.result = currentBuild.result == 'SUCCESS' ? '成功' : currentBuild.result == 'FAILURE' ? '失败' : '取消'
    env.description = currentBuild.description
    env.durationString = currentBuild.durationString.minus(" and counting")
    emailext (
        subject: "[jenkins auto Pipeline] ${currentBuild.fullDisplayName} ${result}",
        to:"${MAIL_TO}",
        body: """
        <body>
            <table width='95%' cellpadding='0' cellspacing='0'>
                <tr>
                    <td>
                        <h2>构建结果:<span color='#0000FF'>${currentBuild.currentResult}</span></h2>
                    </td>
                </tr>
                <tr>
                  <td>
                    <ul>
                      <li>项目名称&nbsp;：&nbsp;${currentBuild.fullDisplayName}</li>
                      <li>发起人&nbsp;：&nbsp;${currentBuild.getBuildCauses()[0].userName ? currentBuild.getBuildCauses()[0].userName : currentBuild.getBuildCauses()[0].shortDescription.minus("Started by ").replace("timer", "定时器").replace("an SCM change", "SCM轮询")}</li>
                      <li>状态&nbsp;：&nbsp;<font color=${result_color}>${result}</font></li>
                      <li>备注&nbsp;：&nbsp;${env.HG_BUILD_DESC ? env.HG_BUILD_DESC : '无'}</li>
                      <li>用时&nbsp;：&nbsp;${durationString}</li>
                    </ul>
                  </td>
                </tr>
                <!-- 构建信息 -->
                <tr>
                  <td><br/>
                    <b>
                      <font color="#0B610B">构建信息</font>
                    </b>
                    <hr size="2" width="100%" align="center" />
                  </td>
                </tr>
                <tr>
                    <td>
                        <ul>
                            <li>构建日志：&nbsp;<a href="${BUILD_URL}">${BUILD_URL}</a></li>
                        </ul>
                    </td>
                </tr>
            </table>
        </body>
        """
    )
}

// 从控制台查找资源版本号
def getPubWebVersion() {
    def consoleText = httpRequest quiet: true, url: "${BUILD_URL}consoleText", wrapAsMultipart: false
    def result = ((consoleText.content =~ /"autoIn":\["(\d+)"\]/))
    if (result.find()) {
        return result[0][1]
    }
    result = ((consoleText.content =~ /build web base v(\d+) begin/))
    if (result.find()) {
        return result[0][1]
    }
    return null
}

// 上传资源到FTP上
def ftpUploadSource() {
    dir("source") {
        checkout([$class: 'SubversionSCM', additionalCredentials: [], excludedCommitMessages: '', excludedRegions: "", excludedRevprop: '', excludedUsers: '', filterChangelog: true, ignoreDirPropChanges: false, includedRegions: ".*/${LOCAL_FILE}", locations: [[cancelProcessOnExternalsFail: true, credentialsId: 'dfb8344e-2d0c-4750-8154-9503745a01f9', depthOption: 'infinity', ignoreExternalsOption: true, local: '.', remote: "$SCM_URL"]], quietOperation: true, workspaceUpdater: [$class: 'UpdateUpdater']])
    }
    dir('automator') {
            checkout changelog: false, poll: false, scm: [$class: 'GitSCM', branches: [[name: 'master']], extensions: [], userRemoteConfigs: [[url: 'https://e.coding.net/tsubasaohzora/playground/automator.git']]]
            bat 'npm i'
    }
    dir("ftp") {
        bat "node %WORKSPACE%/automator/main.js %WORKSPACE%/automator/cfg/dldl/ftp_upload.yml --FULL_AUTOMATIC 1 --QUITE_MODE 1 --remote_file %REMOTE_FILE% --local_file ${params.LOCAL_FILE.tokenize(",").collect{env.WORKSPACE + "/source/" + it}.join(",")}"
    }
}

// 使用本地环境的 svn 检出, 不需要 svn upgrade
def checkoutSVN(scmUrl) {
    if (fileExists('.svn')) {
        // 检查状态
        def status = bat returnStdout: true, script: '@echo off && svn status'
        print status
        if (status && (status =~ /^.{2}L/).find()) {
            print 'Workspace is already locked'
            bat "svn cleanup"
        } else {
            print "Workspace is not locked"
        }
        // 还原
        bat returnStdout: true, script: '@echo off && svn revert -R .'
    } else {
        // 拉取 SVN
        bat(script: "svn checkout ${scmUrl} . --quiet")
    }
    // pollSCM
    checkout([$class: 'SubversionSCM', additionalCredentials: [], excludedCommitMessages: '', excludedRegions: '', excludedRevprop: '', excludedUsers: '', filterChangelog: true, ignoreDirPropChanges: false, includedRegions: '', locations: [[cancelProcessOnExternalsFail: true, credentialsId: 'dfb8344e-2d0c-4750-8154-9503745a01f9', depthOption: 'infinity', ignoreExternalsOption: true, local: '.', remote: "${scmUrl}"]], quietOperation: true, workspaceUpdater: [$class: 'UpdateUpdater']])
}

// 使用本地环境的 svn 检出, 不需要 svn upgrade
def checkoutComplexSVN(scm) {
    if (fileExists('.svn')) {
        // 检查状态
        def status = bat returnStdout: true, script: '@echo off && svn status'
        print status
        if (status && (status =~ /^.{2}L/).find()) {
            print 'Workspace is already locked'
            bat "svn cleanup"
        } else {
            print "Workspace is not locked"
        }
        // 还原
        bat returnStdout: true, script: '@echo off && svn revert -R .'
    } else {
        // 拉取 SVN
        def scmUrl = scm.scm ? scm.scm.locations[0].remote : scm.locations[0].remote
        bat(script: "svn checkout ${scmUrl} . --quiet")
    }
    // pollSCM
    checkout(scm)
}

def pub200AutomaticNewIntegrated() {
    // 编译
    dir("project") {
        checkoutSVN(params.HG_REPOSITORY_SRC)
        if (needCompile()) {
            bat([label: '发布200', returnStdout: false, script: "node scripts --hgt _200 --noUserOp"])
        }
    }
    // 创建out目录resource链接
    dir("project/out") {
        bat """if not exist "resource" (
mklink /j "resource" "../resource"
)
"""
    }
}

def pub200AutomaticOldIntegrated() {
    // 编译
    dir("project") {
        checkoutSVN(params.HG_REPOSITORY_SRC)
        if (needCompile()) {
            // 有环境才执行排序
            if (fileExists('./tools/main.exe') && fileExists('./tools/cfg/generate_sorted_ts.yml')) {
                bat([label: '更新manifest', returnStdout: false, script: '"./tools/main.exe" "./tools/cfg/generate_sorted_ts.yml" --QUIET_MODE'])
            }
            def pub_200_out_bat = ""
            // 编译代码的备选批处理文件
            def pub_200_out_bat_alternatives = [
                "pub_200_out.bat",
            ]
            for (alternative in pub_200_out_bat_alternatives) {
                if (fileExists(alternative)) {
                    pub_200_out_bat = alternative
                    break
                }
            }
            bat([label: '编译代码', returnStdout: false, script: pub_200_out_bat])
            bat([label: 'SVN提交', returnStdout: false, script: "svn commit -m \"out [${getLastChangedRev()}]\" out/main.min.* manifest.json src/base/WND_ID_CFG.ts ui_ctrl out/index.html"])
        }
    }
    // 创建out目录resource链接
    dir("project/out") {
        bat """if not exist "resource" (
mklink /j "resource" "../resource"
)
"""
    }
}

// 获取末尾的几条日志
def getTailLogString(size = 50) {
    // def consoleTextUrl = "http://192.168.1.205:8080/job/pipeline_dldl_h5_en_translation_ob_dev/20/consoleText"
    def consoleTextUrl = "${BUILD_URL}consoleText"
    def consoleText = httpRequest quiet: true, url: consoleTextUrl, wrapAsMultipart: false 
    def result = consoleText.content.tokenize("\n").findAll {
        !((it =~ /\[Pipeline\]/).find())
    }.collect {
        (it - ~/^\[\d+\-\d+\-\d+T\d+\:\d+\:\d+\.\d+Z\] */).replaceAll("\\[\\d+m", "")
    }
    def max_size = result.size()
    return result[(Math.min(max_size, size) * -1)..-1].join("\n")
}



def getRevisions() {
    return currentBuild.changeSets.collect{
        return it.items.collect{
            "${it.getCommitId()}"
        }.join(",")
    }.join("")
}

// 发送翻译KV表_API
def generateSendTranslationKV_API() {
    lock(resource: "conversion_api") {
        dir('automator') {
            checkout changelog: false, poll: false, scm: [$class: 'GitSCM', branches: [[name: 'master']], extensions: [], userRemoteConfigs: [[url: 'https://e.coding.net/tsubasaohzora/playground/automator.git']]]
            bat 'npm i'
        }
        dir('project/resource/assets/cfgjson') {
            checkoutComplexSVN(changelog: false, poll: false, scm: [$class: 'SubversionSCM', additionalCredentials: [], excludedCommitMessages: '', excludedRegions: '', excludedRevprop: '', excludedUsers: '', filterChangelog: true, ignoreDirPropChanges: false, includedRegions: '''.*/resource/assets/cfgjson/\\w+\\.json
    .*/resource/assets/cfgjson/base/\\w+\\.json
    .*/resource/js/common\\.js''', locations: [[cancelProcessOnExternalsFail: true, credentialsId: 'dfb8344e-2d0c-4750-8154-9503745a01f9', depthOption: 'infinity', ignoreExternalsOption: true, local: '.', remote: "$SCM_URL/resource/assets/cfgjson"]], quietOperation: true, workspaceUpdater: [$class: 'UpdateUpdater']])
        }
        dir('project/resource/js') {
            checkoutComplexSVN(changelog: false, poll: false, scm: [$class: 'SubversionSCM', additionalCredentials: [], excludedCommitMessages: '', excludedRegions: '', excludedRevprop: '', excludedUsers: '', filterChangelog: true, ignoreDirPropChanges: false, includedRegions: '''.*/resource/assets/cfgjson/\\w+\\.json
    .*/resource/assets/cfgjson/base/\\w+\\.json
    .*/resource/js/common\\.js''', locations: [[cancelProcessOnExternalsFail: true, credentialsId: 'dfb8344e-2d0c-4750-8154-9503745a01f9', depthOption: 'infinity', ignoreExternalsOption: true, local: '.', remote: "$SCM_URL/resource/js"]], quietOperation: true, workspaceUpdater: [$class: 'UpdateUpdater']])
        }
        // .*/${PROJECT_NAME}/${PROJECT_VER}/cn/.*
        def common_js = readFile encoding: 'utf-8', file: 'project/resource/js/common.js'
        def localeCfg = ((common_js =~ /HG_GLOBAL\.LOCALIZATION_CFG \= ([\s\S]*?\});/)[0][1])
        // print localeCfg
        def projectName = ((localeCfg =~ /projectName\: "(.*?)"/)[0][1])
        print projectName
        def projectVer = ((localeCfg =~ /projectVer\: "(.*?)"/)[0][1])
        print projectVer
        dir('translation') {
            checkoutComplexSVN([$class: 'SubversionSCM', additionalCredentials: [], excludedCommitMessages: '', excludedRegions: '', excludedRevprop: '', excludedUsers: '', filterChangelog: false, ignoreDirPropChanges: false, includedRegions: ".*/${projectName}/${projectVer}/cn/.*", locations: [[cancelProcessOnExternalsFail: true, credentialsId: 'dfb8344e-2d0c-4750-8154-9503745a01f9', depthOption: 'infinity', ignoreExternalsOption: true, local: '.', remote: 'https://svn100.hotgamehl.com/svn/Html5/trunk/dldl_WX/translation_keyvalue']], quietOperation: true, workspaceUpdater: [$class: 'UpdateUpdater']])
        }                
        dir('convert2src') {
            env.REVISIONS = env.REVISIONS ? env.REVISIONS : getRevisions()
            print "env.REVISIONS " + env.REVISIONS
            bat 'node %WORKSPACE%/automator/main.js %WORKSPACE%/automator/cfg/dldl/conversion_to_send@api.yml --FULL_AUTOMATIC 1 --QUITE_MODE 1 --projectFolder %WORKSPACE%/project --projectName %PROJECT_NAME% --conversionWorkspaceFolder %WORKSPACE%/conversion --translationFolder %WORKSPACE%/translation --revisions "%REVISIONS%" --revision_beg "%REVISION_BEG%" --revision_end "%REVISION_END%"'
        }
    }
}

// 判断是有需要编译
def needCompile() {
    return hasCode2Compile() || params.FORCE_COMPILE
}

// 判断是否有代码需要编译
def hasCode2Compile() {
    return currentBuild.changeSets.any {
        return it.items.any {
            return it.getAffectedFiles().any {
                def path = it.getPath()
                // print path
                return (path =~ /([\\\/]|^)(src|src_base|src_ext|dep_libs)[\\\/].*\.(ts|js)$/).find() && path != "src\\base\\WND_ID_CFG.ts"
            }
        }
    }   
}