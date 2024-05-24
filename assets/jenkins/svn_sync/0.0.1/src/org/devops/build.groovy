/* groovylint-disable DuplicateListLiteral, DuplicateMapLiteral, DuplicateNumberLiteral, DuplicateStringLiteral, ExplicitCallToMinusMethod, ImplicitClosureParameter, LineLength, MethodParameterTypeRequired, MethodReturnTypeRequired, NoDef, SpaceAfterMethodCallName, SpaceAfterMethodDeclarationName, TernaryCouldBeElvis, UnnecessaryDotClass, UnnecessaryGString, UnnecessaryGetter, UseCollectMany, UseCollectNested, VariableName, VariableTypeRequired */
package org.devops

import org.yaml.snakeyaml.Yaml

def getChangeString(showIndex = true, showDetail = true) {
    MAX_MSG_LEN = 500
    // echo 'Gathering SCM changes......'
    def MAX_ITEMS = 20 // 限制记录条数上限为20条
    def isExceeded = false // 是否超过上限条数
    def numItem = 0
    def totalChanges = 0
    def logs = currentBuild.changeSets.collect {
        def i = 1
        it.items.findAll {
            totalChanges++
            if (isExceeded) {
                return false;
            }
            def ret = !((it.msg.take(MAX_MSG_LEN) =~ /^(auto )?out \[\d+\]/).find())
            if (!ret) {
                return false
            }
            numItem++
            if (numItem == MAX_ITEMS) {
                isExceeded = true
            }
            return true
        }.collect {
            return it.collect {
                (showIndex ? "${i++}. " : "") + "${it.msg.take(MAX_MSG_LEN).replaceAll('[\r\n]+', '')}" + (showDetail ? " by ${it.author.getFullName()} at ${new Date(it.getTimestamp()).format('HH:mm:ss', TimeZone.getTimeZone('Asia/Shanghai'))}" : "")
            }.join('\n')
        }.join('\n')
    }
    if (totalChanges) {
        logs.add(0, "*共${totalChanges}条*")
    } else {
        logs.add(0, "*无*")
    }
    return logs
}

def sendStart2DingTalk() {
    if (params.HG_QUIET) {
        return
    }
    dingtalk(
        robot: getDingTalkRobot(),
        type: 'MARKDOWN',
        title: "${currentBuild.fullDisplayName} 开始",
        text: [
            "- 任务 [${currentBuild.fullDisplayName}](${BUILD_URL}) ",
            '- 状态 开始',
            "- 发起 ${getRootBuildTriggerDesc()}",
            "- 时刻 ${new Date().format('yyyy-MM-dd(E)HH:mm:ss', TimeZone.getTimeZone('Asia/Shanghai')) - '星期'}",
            '- 记录',
            '***',
        ] + getChangeString()
    )
}

def resolveResult() {
    switch (currentBuild.result) {
        case "SUCCESS":
            env.result_color = '#52c41a';
            env.result = '成功';
            break;
        case "ABORTED":
            env.result_color = '#333333';
            env.result = '取消';
            break;
        case "FAILURE":
            env.result_color = '#f5222d'
            env.result = '失败';
            break
        case "UNSTABLE":
            env.result_color = '#ff9f00'
            env.result = '不稳定';
            break;
        default:
            env.result_color = '#000000';
            env.result = currentBuild.result;
            break;
    }
}

def sendResult2DingTalkTest() {
    if (params.HG_QUIET) {
        return
    }
    resolveResult()
    env.description = currentBuild.description
    env.durationString = currentBuild.durationString.minus(' and counting')
    def atUsers = getAtUsers()
    dingtalk(
        robot: getDingTalkRobot(),
        type: 'MARKDOWN',
        title: "${currentBuild.fullDisplayName} ${result}",
        at: atUsers,
        atAll: false,
        text: [
            "- 任务 [${currentBuild.fullDisplayName}](${BUILD_URL}) ",
            "- 状态 <font color=${result_color}>${result}</font>",
            "1. 发起 ${getRootBuildTriggerDesc()}",
            "2. 时刻 ${new Date().format('yyyy-MM-dd(E)HH:mm:ss', TimeZone.getTimeZone('Asia/Shanghai')) - '星期'}",
            "3. 用时 ${durationString}",
            '4. 记录',
            '***',
        ] + getChangeString() + (
            currentBuild.result == 'FAILURE' ? [
                '***',
                "- <font color=${result_color}>失败日志</font>",
                getTailLogString(),
            ] : []
        )
    )
}

def sendResult2DingTalk() {
    if (params.HG_QUIET) {
        return
    }
    // generatePatchFile()
    resolveResult()
    env.description = currentBuild.description
    env.durationString = currentBuild.durationString.minus(' and counting')
    // 失败时，@提交者
    def atUsers = getAtUsers(currentBuild.result == 'FAILURE', currentBuild.result != 'FAILURE')
    dingtalk(
        robot: getDingTalkRobot(),
        type: 'MARKDOWN',
        title: "${currentBuild.fullDisplayName} ${result}",
        at: atUsers,
        atAll: false,
        text: [
            "- 任务 [${currentBuild.fullDisplayName}](${BUILD_URL}) ",
            "- 状态 <font color=${result_color}>${result}</font>",
            "- 发起 ${getRootBuildTriggerDesc()}",
            "- 时刻 ${new Date().format('yyyy-MM-dd(E)HH:mm:ss', TimeZone.getTimeZone('Asia/Shanghai')) - '星期'}",
            "- 用时 ${durationString}",
            // "- [记录](${env.HG_PATCH_FILE})",
            "- 记录",
            '***',
        ] + getChangeString() + (
            currentBuild.result == 'FAILURE' ? [
                '***',
                "- <font color=${result_color}>失败日志</font>",
                getTailLogString(),
            ] : []
        )
    )
}

def sendResult2DingTalkSimple() {
    if (params.HG_QUIET) {
        return
    }
    resolveResult()
    env.description = currentBuild.description
    env.durationString = currentBuild.durationString.minus(' and counting')
    // 失败时，@提交者
    def atUsers = getAtUsers(currentBuild.result == 'FAILURE')
    dingtalk(
        robot: getDingTalkRobot(),
        type: 'MARKDOWN',
        title: "${currentBuild.fullDisplayName} ${result}",
        at: atUsers,
        atAll: false,
        text: [
            "- 任务 [${currentBuild.fullDisplayName}](${BUILD_URL}) ",
            "- 状态 <font color=${result_color}>${result}</font>",
            "- 时刻 ${new Date().format('yyyy-MM-dd(E)HH:mm:ss', TimeZone.getTimeZone('Asia/Shanghai')) - '星期'}",
        ]
    )
}

def generatePatchFile(include = "") {
    if (!env.HG_REPOSITORY_SRC) {
        return;
    }
    def revisions = getRevisions()
    if (revisions) {
        def patches = ""
        withCredentials([usernamePassword(credentialsId: getCredentialsId(), passwordVariable: 'HG_CREDENTIAL_PASSWORD', usernameVariable: 'HG_CREDENTIAL_USERNAME')]) {
            revisions.tokenize(",").each {
                def revision = it
                def patch = bat returnStdout: true, script: "@echo off && svn diff ${HG_REPOSITORY_SRC} -c${revision} ${include} --username %HG_CREDENTIAL_USERNAME% --password %HG_CREDENTIAL_PASSWORD%"
                if (patch) {
                    patches += patch + "\n"
                }
            }
        }
        if (patches) {
            def filename = "patches/out/r${revisions}.patch";
            def filepath = "http://192.168.1.205:8686/view/${WORKSPACE.replaceAll('\\\\', '/')}/${filename}"
            fileOperations([fileCreateOperation(fileContent: patches, fileName: filename)])
            env.HG_PATCH_FILE = filepath
        }
    }
}

// 获取当前版本号
def getLastChangedRev() {
    def out = bat([returnStdout: true, script: '@echo off && svn info'])
    def yaml = new Yaml()
    def map = yaml.load(out)
    return map['Last Changed Rev']
}

def pubToWebIntegrated() {
    // lock(resource: "${HG_PUB_RES}") {
    dir('project') {
        // 检出
        if (!params.USE_LOCAL_REPOSITORY) {
            checkoutSVN(params.HG_REPOSITORY_SRC)
        }
        getSVNInfo()
        // 发送通知
        sendStart2DingTalk_PubWeb()
        // 设置环境变量 prg_dir 给 hgbuild 使用
        env.prg_dir = pwd()
        // 设置环境变量 prg_dir 给 automator 使用
        env.WORKSPACE_FOLDER = pwd()
    }
    checkoutPublish()
    cleanupHGPubToolsDist()
    checkoutAutomator()
    lock(resource: 'pub2web') {
        webUpgradeSdkInfo()
        dir('publish') {
            // 发布
            bat([label: '发布', returnStdout: false, script: """
if "%chkdst%" == "true" (
npx hgbuild walk ${HG_PUB_RES} ${HG_PUB_TYPE} --noUserOp --noProjectUpdate --chkdst
) else (
npx hgbuild walk ${HG_PUB_RES} ${HG_PUB_TYPE} --noUserOp --noProjectUpdate
)"""])
        }
        webSyncIndex()
    }
    }

// 新的发布流程 - 集成版本
def pubToWebIntegratedCommonOld() {
    // lock(resource: "${cfg_dir}") {
    dir('project') {
        // 检出
        if (!params.USE_LOCAL_REPOSITORY) {
            checkoutSVN(params.HG_REPOSITORY_SRC)
        }
        getSVNInfo()
        // 发送通知
        sendStart2DingTalk_PubWeb()
        // 设置环境变量 prg_dir 给 automator 使用
        env.WORKSPACE_FOLDER = pwd()
    }
    checkoutPublish()
    cleanupHGPubToolsDist()
    checkoutAutomator()
    lock(resource: 'pub2web') {
        webUpgradeSdkInfo()
        // 发布
        dir('publish') {
            bat([label: '发布', returnStdout: false, script: """
if "%chkdst%" == "true" (
    hgbuild run _11_common_old --prg_dir ${WORKSPACE}/project --upload_filter ${params.upload_filter} --toolTag ${params.toolTag} --cfg_dir ${params.cfg_dir} --hgVerTag ${params.hgVerTag ? params.hgVerTag : "hgvc_ver"} --noUserOp --noProjectUpdate --chkdst
) else (
    hgbuild run _11_common_old --prg_dir ${WORKSPACE}/project --upload_filter ${params.upload_filter} --toolTag ${params.toolTag} --cfg_dir ${params.cfg_dir} --hgVerTag ${params.hgVerTag ? params.hgVerTag : "hgvc_ver"} --noUserOp --noProjectUpdate
)"""])
        }
        webSyncIndex()
    }
    }

// 新的发布流程 - 集成版本
def pubToWebIntegratedCommon() {
    // lock(resource: "${cfg_dir}") {
    dir('project') {
        // 检出
        if (!params.USE_LOCAL_REPOSITORY) {
            checkoutSVN(params.HG_REPOSITORY_SRC)
        }
        getSVNInfo()
        // 发送通知
        sendStart2DingTalk_PubWeb()
        // 设置环境变量 prg_dir 给 automator 使用
        env.WORKSPACE_FOLDER = pwd()
    }
    // 发布
    checkoutPublish()
    cleanupHGPubToolsDist()
    checkoutAutomator()
    lock(resource: 'pub2web') {
        webUpgradeSdkInfo()
        dir('publish') {
            bat([label: '发布', returnStdout: false, script: """
if "%chkdst%" == "true" (
    hgbuild run _10_common --prg_dir ${WORKSPACE}/project --upload_filter ${params.upload_filter} --toolTag ${params.toolTag} --cfg_dir ${params.cfg_dir}  --hgVerTag ${params.hgVerTag ? params.hgVerTag : "hgvc_ver"} --noUserOp --noProjectUpdate --chkdst
) else (
    hgbuild run _10_common --prg_dir ${WORKSPACE}/project --upload_filter ${params.upload_filter} --toolTag ${params.toolTag} --cfg_dir ${params.cfg_dir}  --hgVerTag ${params.hgVerTag ? params.hgVerTag : "hgvc_ver"} --noUserOp --noProjectUpdate
)"""])
        }
        webSyncIndex()
    }
    }

// pubToWeb构建开始
def sendStart2DingTalk_PubWeb() {
    if (params.HG_QUIET) {
        return
    }
    dingtalk(
        robot: getDingTalkRobot(),
        type: 'MARKDOWN',
        title: "${currentBuild.fullDisplayName} 开始",
        // at: getAtUsers(),
        // atAll: false,
        text: (
            [
                "# **[${currentBuild.fullDisplayName}](${BUILD_URL})**",
                '***',
                '- 状态 开始',
                "- 发起 ${getRootBuildTriggerDesc()}",
                "- 时刻 ${new Date().format('yyyy-MM-dd(E)HH:mm:ss', TimeZone.getTimeZone('Asia/Shanghai')) - '星期'}",
                '- 仓库',
                params.HG_REPOSITORY_SRC ? (params.HG_REPOSITORY_SRC - ~/.*\//) : 'Unknown',
                (hasLogo2Refresh() ? '- logo <font color=#ff9f00>已修改</font>' : ''),
                (hasIndexJS2Refresh() ? '- index <font color=#ff9f00>已修改</font>' : ''),
                '- 记录',
                '***',
            ] + getChangeString()
        ).findAll { it }
    )
}

// 获取要@的用户
def getAtUsers(includeCommitUser = false, includeLogUser = false) {
    def AT_USERS_STR = params.AT_USERS != null ? params.AT_USERS : ''
    def AT_USERS = AT_USERS_STR.tokenize(',')
    // 添加构建者(需要允许指定的API)
    def builderMobile = getRootBuildMobile()
    if (builderMobile) {
        AT_USERS.add(builderMobile)
    }
    if (includeCommitUser) {
        AT_USERS += getCommitUserMobiles()
    }
    if (includeLogUser) {
        AT_USERS += getLogUserMobiles()
    }
    // 添加环境变量中配置的 AT_USERS@${JOB_NAME}
    def JOB_AT_USERS = env["AT_USERS@${JOB_NAME}"]
    if (JOB_AT_USERS) {
        AT_USERS += JOB_AT_USERS.tokenize(',').collect {
            def user = hudson.model.User.getById(it, false)
            if (user) {
                return user.getProperty(io.jenkins.plugins.DingTalkUserProperty.class).getMobile()
            } else {
                return ""
            }
        }
    }
    // 去重
    AT_USERS.unique()
    // print AT_USERS
    // 过滤空手机号
    return AT_USERS.findAll { it }
}

// 获取当前提交者的手机号
def getCommitUserMobiles() {
    def mobiles = (currentBuild.changeSets.collect {
        it.items.collect {
            hudson.model.User.getById(it.author.getId(), false).getProperty(io.jenkins.plugins.DingTalkUserProperty.class).getMobile()
        }
    }).flatten();
    return mobiles ? mobiles : []
}

// 获取日志中@的用户手机号
def getLogUserMobiles() {
    def mobiles = (currentBuild.changeSets.collect {
        it.items.collect {
            def result = ((it.msg =~ /@(\S*)/))
            if (result.find()) {
                def userName = result[0][1]
                def user = hudson.model.User.get(userName, false)
                if (user) {
                    return user.getProperty(io.jenkins.plugins.DingTalkUserProperty.class).getMobile()
                }
            }
            return ""
        }
    }).flatten();
    return mobiles ? mobiles : []
}

// 获取当前提交者的邮箱地址
def getCommitUserEmails() {
    def emails = (currentBuild.changeSets.collect {
        it.items.collect {
            hudson.model.User.getById(it.author.getId(), false).getProperty(hudson.tasks.Mailer.UserProperty).getAddress()
        }
    }).flatten();
    return emails ? emails : []
}

// 前一次构建是否成功
def isPreviousBuildSuccess() {
    return currentBuild.previousBuild?.result == null || currentBuild.previousBuild.result == 'SUCCESS'
}

// 如果上一次构建没有成功，则把当前构建状态设置为不稳定
def setCurrentBuildUnstableIfNecessary() {
    if (!isPreviousBuildSuccess()) {
        currentBuild.result = 'UNSTABLE'
    }
}


// 获取当前提交者的名字
def getCommitUsernames() {
    def usernames = (currentBuild.changeSets.collect {
        it.items.collect {
            it.author.getFullName()
        }.findAll {
            it
        }
    }).flatten();
    return usernames ? usernames.unique() : []
}

// pubToWeb构建结束
def sendResult2DingTalk_PubWeb() {
    // addBuildDescripion ("${new Date().format('yyyy-MM-dd(E)HH:mm:ss', TimeZone.getTimeZone('Asia/Shanghai')) - '星期'}")
    def pubWebVersion = getPubWebVersion()
    if (env.SVN_LAST_CHANGED_REV) {
        addBuildDescripion ("r" + (env.SVN_LAST_CHANGED_REV))
    }
    if (pubWebVersion) {
        addBuildDescripion ("v" + pubWebVersion)
    }
    if (params.HG_REPOSITORY_SRC) {
        addBuildDescripion ((params.HG_REPOSITORY_SRC - ~/.*\//))
    }
    addBuildDescripion (getRootBuildTriggerDesc())
    if (params.HG_QUIET) {
        return
    }
    resolveResult()
    generatePatchFile("resource/assets/cfgjson")
    env.description = currentBuild.description
    env.durationString = currentBuild.durationString.minus(' and counting')
    dingtalk(
        robot: getDingTalkRobot(),
        type: 'MARKDOWN',
        title: "${currentBuild.fullDisplayName} ${result}",
        at: getAtUsers(),
        atAll: false,
        text: (
            [
                "# **[${currentBuild.fullDisplayName}](${BUILD_URL})**",
                '***',
                "- 状态 <font color=${result_color}>${result}</font>",
                "- 资源版本 <font color=${result_color}>${pubWebVersion ? pubWebVersion : 'Unknown'}</font>",
                "- 发起 ${getRootBuildTriggerDesc()}",
                "- 时刻 ${new Date().format('yyyy-MM-dd(E)HH:mm:ss', TimeZone.getTimeZone('Asia/Shanghai')) - '星期'}",
                "- 用时 ${durationString}",
                '- 仓库',
                params.HG_REPOSITORY_SRC ? (params.HG_REPOSITORY_SRC - ~/.*\//) : 'Unknown',
                (hasLogo2Refresh() ? '- logo <font color=#ff9f00>已修改</font>' : ''),
                (hasIndexJS2Refresh() ? '- index <font color=#ff9f00>已修改</font>' : ''),
                '- 记录',
                env.HG_PATCH_FILE ? "[点击查看cfgjson修改](${env.HG_PATCH_FILE})" : "",
                '***',
            ] 
            + getChangeString() 
            + ( currentBuild.result == 'FAILURE' ? [ '***', "- <font color=${result_color}>失败日志</font>", getTailLogString(), ] : [])
        ).findAll { it }
    )
}

def sendStart2DingTalk_PubMinigame() {
    if (params.HG_QUIET) {
        return
    }
    dingtalk(
        robot: getDingTalkRobot(),
        type: 'MARKDOWN',
        title: "${currentBuild.fullDisplayName} 开始",
        // at: getAtUsers(),
        // atAll: false,
        text: [
            "# **[${currentBuild.fullDisplayName}](${BUILD_URL})**",
            '***',
            '- 状态 开始',
            "- 发起 ${getRootBuildTriggerDesc()}",
            "- <font color=${env.ENABLE_PUBLISH_STATIC_RESOURCE == "true" ? "#52c41a" : "#888888"}>静态资源${env.ENABLE_PUBLISH_STATIC_RESOURCE == "true" ? "" : "不"}更新</font>",
            "- <font color=${env.ENABLE_MINIGAME_UPLOAD == "true" ? "#52c41a" : "#888888"}>游戏包${env.ENABLE_MINIGAME_UPLOAD == "true" ? "" : "不"}更新</font>",
            "- 时刻 ${new Date().format('yyyy-MM-dd(E)HH:mm:ss', TimeZone.getTimeZone('Asia/Shanghai')) - '星期'}",
            '- 仓库',
            params.HG_REPOSITORY_SRC ? (params.HG_REPOSITORY_SRC - ~/.*\//) : 'Unknown',
            '- 记录',
            '***',
        ] + getChangeString()
    )
}

def sendResult2DingTalk_PubMinigame() {
    // addBuildDescripion ("${new Date().format('yyyy-MM-dd(E)HH:mm:ss', TimeZone.getTimeZone('Asia/Shanghai')) - '星期'}")
    def minigameVersion = getMinigameVersion()
    if (minigameVersion) {
        addBuildDescripion (minigameVersion)
    }
    def minigameOutputURL = getMinigameOutputURL();
    def minigameToggleOperation = getMiniGameToggleOperation()
    if (minigameToggleOperation) {
        addBuildDescripion (minigameToggleOperation)
    }
    def pubWebVersion = getPubWebVersion()
    if (env.SVN_LAST_CHANGED_REV) {
        addBuildDescripion ("r" + (env.SVN_LAST_CHANGED_REV))
    }
    if (pubWebVersion) {
        addBuildDescripion ("v" + pubWebVersion)
    }
    if (params.HG_REPOSITORY_SRC) {
        addBuildDescripion ((params.HG_REPOSITORY_SRC - ~/.*\//))
    }
    addBuildDescripion (getRootBuildTriggerDesc())
    if (params.HG_QUIET) {
        return
    }
    resolveResult()
    env.description = currentBuild.description
    env.durationString = currentBuild.durationString.minus(' and counting')
    dingtalk(
        robot: getDingTalkRobot(),
        type: 'MARKDOWN',
        title: "${currentBuild.fullDisplayName} ${result}",
        at: getAtUsers(),
        atAll: false,
        text: ([
            "# **[${currentBuild.fullDisplayName}](${BUILD_URL})**",
            '***',
            "- 状态 <font color=${result_color}>${result}</font>",
            "- 发起 ${getRootBuildTriggerDesc()}",
            pubWebVersion ? "- 资源版本 <font color=${result_color}>${pubWebVersion}</font>" : "",
            "- 小游戏版本 <font color=${result_color}>${minigameVersion ? minigameVersion : 'Unknown'}</font>",
            minigameOutputURL ? "- [下载游戏包](${minigameOutputURL})" : "",
            minigameToggleOperation ? "- 小游戏配置 <font color=#52c41a>${minigameToggleOperation}</font>" : "",
            "- 生效时间 <font color=#52c41a>${getDateByStep().format('yyyy-MM-dd(E)HH:mm:ss', TimeZone.getTimeZone('Asia/Shanghai')) - '星期'}</font>",
            "- <font color=${env.ENABLE_PUBLISH_STATIC_RESOURCE == "true" ? "#52c41a" : "#888888"}>静态资源${env.ENABLE_PUBLISH_STATIC_RESOURCE == "true" ? "" : "不"}更新</font>",
            "- <font color=${env.ENABLE_MINIGAME_UPLOAD == "true" ? "#52c41a" : "#888888"}>游戏包${env.ENABLE_MINIGAME_UPLOAD == "true" ? "" : "不"}更新</font>",
            "- 用时 ${durationString}",
            params.HG_REPOSITORY_SRC ? ('- 仓库 ' + (params.HG_REPOSITORY_SRC - ~/.*\//)) : "",
            '- 记录',
            '***',
        ] + getChangeString() + (
            currentBuild.result == 'FAILURE' ? [
                '***',
                "- <font color=${result_color}>失败日志</font>",
                getTailLogString(),
            ] : []
        )).findAll { it }
    )
}

// 通用构建通知
def sendCommonResult2DingTalk() {
    if (params.HG_QUIET) {
        return
    }
    resolveResult()
    env.durationString = currentBuild.durationString.minus(' and counting')
    dingtalk(
        robot: getDingTalkRobot(),
        type: 'MARKDOWN',
        title: "${currentBuild.fullDisplayName} ${result}",
        at: getAtUsers(),
        atAll: false,
        text: [
            "# **[${currentBuild.fullDisplayName}](${BUILD_URL})**",
            '***',
            "- 状态 <font color=${result_color}>${result}</font>",
            "- 时刻 ${new Date().format('yyyy-MM-dd(E)HH:mm:ss', TimeZone.getTimeZone('Asia/Shanghai')) - '星期'}",
            "- 用时 ${durationString}",
            '***',
        ] + (
            env.EXTRA_DINGTALK_NOTIFICATIONS ? env.EXTRA_DINGTALK_NOTIFICATIONS.tokenize(",") : []
        ) + (
            currentBuild.result == 'FAILURE' ? [
                '***',
                "- <font color=${result_color}>失败日志</font>",
                getTailLogString(10),
            ] : []
        )
    )
}


def mergeSVN() {
    checkoutAutomator()
    dir('project') {
        checkoutSVN(params.HG_REPOSITORY_SRC)
        getSVNInfo()
    }
    // 获取凭证
    withCredentials([usernamePassword(credentialsId: getCredentialsId(), passwordVariable: 'HG_CREDENTIAL_PASSWORD', usernameVariable: 'HG_CREDENTIAL_USERNAME')]) {
        env.SVN_CREDENTIAL = "--username ${HG_CREDENTIAL_USERNAME} --password ${HG_CREDENTIAL_PASSWORD}"
    }
    bat '%WORKSPACE%/automator/automator LSB0eXBlOiBzdG9yYWdlX29wdGlvbmFsDQogIGRhdGE6DQogICAgcmV2aXNpb25zOiAiMCINCiAgICBzcmM6ICIiDQogICAgZHN0OiAiIg0KDQotIHR5cGU6IGVudl9vcHRpb25hbA0KICBkYXRhOg0KICAgIFNWTl9DUkVERU5USUFMOg0KLSB0eXBlOiBhc3NlcnQNCiAgZGF0YToNCiAgICBrZXk6DQogICAgICAtIFNWTl9DUkVERU5USUFMDQoNCi0gdHlwZTogdHJ1bmNhdGVfYWxsDQogIGRhdGE6DQogICAga2V5OiBTVk5fQ1JFREVOVElBTA0KICAgIHBhdHRlcm46IFwtXC0oXFMrKSAoXFMrKQ0KICAgIHN0b3JlX2tleTogY3JlZGVudGlhbF9saXN0DQoNCi0gdHlwZTogbGlzdDJkaWN0DQogIGRhdGE6DQogICAga2V5OiBjcmVkZW50aWFsX2xpc3QNCiAgICBzdG9yZV9rZXk6IGNyZWRlbnRpYWxfb2JqDQoNCi0gdHlwZTogc2hlbGwNCiAgdGl0bGU6IOi/mOWOnw0KICBkYXRhOg0KICAgIGNtZDogc3ZuIHJldmVydCAtUiAuIDw8U1ZOX0NSRURFTlRJQUw+Pg0KICAgIGN3ZDogPDxkc3Q+PiAgICANCi0gdHlwZTogc2hlbGwNCiAgdGl0bGU6IOabtOaWsA0KICBkYXRhOg0KICAgIGNtZDogc3ZuIHVwZGF0ZSA8PFNWTl9DUkVERU5USUFMPj4NCiAgICBjd2Q6IDw8ZHN0Pj4NCg0KLSB0eXBlOiBldmFsDQogIGRhdGE6DQogICAgY29kZTogJDw8cmV2aXNpb25zPj4gPT0gMA0KICAgIHN0b3JlX2tleTogbWVyZ2VfYWxsX3JldmlzaW9ucw0KLSB0eXBlOiBwcmludA0KICBkYXRhOg0KICAgIGtleToNCiAgICAgIC0gbWVyZ2VfYWxsX3JldmlzaW9ucw0KDQotIHR5cGU6IHNoZWxsDQogIGlmX3RydWU6IG1lcmdlX2FsbF9yZXZpc2lvbnMNCiAgZGF0YToNCiAgICBjbWQ6IHN2biBtZXJnZWluZm8gLS1zaG93LXJldnMgZWxpZ2libGUgPDxzcmM+PiA8PFNWTl9DUkVERU5USUFMPj4NCiAgICBjd2Q6IDw8ZHN0Pj4NCiAgICBjYXB0dXJlX3N0ZG91dDogdHJ1ZQ0KICAgIHN0b3JlX2tleTogcmV2aXNpb25zDQogICAgc3RvcmVfcHJpbnQ6IHRydWUNCg0KLSB0eXBlOiBldmFsDQogIGRhdGE6DQogICAgY29kZTogPg0KICAgICAgKCIiICsgJDw8cmV2aXNpb25zPj4pLnNwbGl0KC9bXjAtOVwtXSsvKS5tYXAodiA9PiB7DQogICAgICAgICAgbGV0IGFyciA9IHYuc3BsaXQoIi0iKTsNCiAgICAgICAgICBpZiAoYXJyLmxlbmd0aCA9PSAyKSB7DQogICAgICAgICAgICAgIGxldCByZXQgPSBbXTsNCiAgICAgICAgICAgICAgZm9yIChsZXQgaSA9ICthcnJbMF07IGkgPD0gK2FyclsxXTsgKytpKSB7DQogICAgICAgICAgICAgICAgICByZXQucHVzaCgiIiArIGkpOw0KICAgICAgICAgICAgICB9DQogICAgICAgICAgICAgIHJldHVybiByZXQ7DQogICAgICAgICAgfQ0KICAgICAgICAgIHJldHVybiB2Ow0KICAgICAgfSkuZmxhdCgpLmZpbHRlcih2ID0+IHYudHJpbSgpKS5zb3J0KChhLCBiKSA9PiBhIC0gYikNCiAgICBzdG9yZV9rZXk6IHJldmlzaW9ucw0KICAgIHN0b3JlX3ByaW50OiB0cnVlDQoNCi0gdHlwZTogcHJpbnQNCiAgaWZfZmFsc2U6IHJldmlzaW9ucy5sZW5ndGgNCiAgZGF0YToNCiAgICBjb250ZW50OiDmsqHmnInpnIDopoHlkIjlubbnmoTniYjmnKwNCiAgICBsZXZlbDogNA0KLSB0eXBlOiBjb250aW51ZQ0KICBpZl9mYWxzZTogcmV2aXNpb25zLmxlbmd0aA0KICAgIA0KLSB0eXBlOiBzaGVsbA0KICBkYXRhOg0KICAgIGNtZDogc3ZuIGluZm8gPDxzcmM+PiA8PFNWTl9DUkVERU5USUFMPj4NCiAgICBjYXB0dXJlX3N0ZG91dDogdHJ1ZQ0KICAgIHN0b3JlX2tleTogc3JjX3N2bl9pbmZvDQogICAgcGFyc2VfdHlwZTogeWFtbA0KDQotIHR5cGU6IHNoZWxsDQogIGRhdGE6DQogICAgY21kOiBzdm4gaW5mbyA8PGRzdD4+IDw8U1ZOX0NSRURFTlRJQUw+Pg0KICAgIGNhcHR1cmVfc3Rkb3V0OiB0cnVlDQogICAgc3RvcmVfa2V5OiBkc3Rfc3ZuX2luZm8NCiAgICBwYXJzZV90eXBlOiB5YW1sDQoNCi0gdHlwZTogc2hlbGwNCiAgZGF0YToNCiAgICBjbWQ6IHN2biBsb2cgLWM8PHJldmlzaW9ucz4+IDw8c3JjPj4gLS14bWwgPDxTVk5fQ1JFREVOVElBTD4+DQogICAgY2FwdHVyZV9zdGRvdXQ6IHRydWUNCiAgICBzdG9yZV9rZXk6IHN2bl9sb2dzDQogICAgcGFyc2VfdHlwZTogeG1sDQogICAgZW5jb2Rpbmc6IHV0ZjgNCi0gdHlwZTogZXZhbA0KICBkYXRhOg0KICAgIGNvZGU6IEFycmF5LnByb3RvdHlwZS5tYXAuY2FsbCgkPDxzdm5fbG9ncz4+LmdldEVsZW1lbnRzQnlUYWdOYW1lKCJsb2dlbnRyeSIpLCAodikgPT4gdi5hdHRyaWJ1dGVzLmdldE5hbWVkSXRlbSgncmV2aXNpb24nKS5ub2RlVmFsdWUpDQogICAgc3RvcmVfa2V5OiBtZXJnZWRfcmV2aXNpb25zDQogICAgc3RvcmVfcHJpbnQ6IHRydWUNCi0gdHlwZTogZXZhbA0KICBkYXRhOg0KICAgIGNvZGU6IEFycmF5LnByb3RvdHlwZS5tYXAuY2FsbCgkPDxzdm5fbG9ncz4+LmdldEVsZW1lbnRzQnlUYWdOYW1lKCJtc2ciKSwgKHYpID0+IHYudGV4dENvbnRlbnQpDQogICAgc3RvcmVfa2V5OiBtZXJnZWRfbXNncw0KICAgIHN0b3JlX3ByaW50OiB0cnVlDQotIHR5cGU6IGV2YWwNCiAgZGF0YToNCiAgICBjb2RlOiBBcnJheS5wcm90b3R5cGUubWFwLmNhbGwoJDw8c3ZuX2xvZ3M+Pi5nZXRFbGVtZW50c0J5VGFnTmFtZSgiYXV0aG9yIiksICh2KSA9PiB2LnRleHRDb250ZW50KQ0KICAgIHN0b3JlX2tleTogbWVyZ2VkX2F1dGhvcnMNCiAgICBzdG9yZV9wcmludDogdHJ1ZQ0KDQotIHR5cGU6IGV2YWwNCiAgZGF0YToNCiAgICBjb2RlOiA+DQogICAgICAoKCkgPT4gew0KICAgICAgICBsZXQgcmV0ID0gIiI7DQogICAgICAgIHJldCArPSBg5ZCI5bm25LqG5L+u5pS554mI5pys5Y+3PDxtZXJnZWRfcmV2aXNpb25zfHN0cmluZ2lmeSgic2VxdWVuY2UiKT4+5LuOJHskPDxzcmNfc3ZuX2luZm8+PlsiUmVsYXRpdmUgVVJMIl0uc3Vic3RyKDIpfTpcclxuYDsNCiAgICAgICAgcmV0ICs9ICQ8PG1lcmdlZF9tc2dzPj4ubWFwKCh2LCBpKSA9PiB7DQogICAgICAgICAgcmV0dXJuIGAke3Z9IGJ5ICR7JDw8bWVyZ2VkX2F1dGhvcnM+PltpXX1gDQogICAgICAgIH0pLmpvaW4oIlxyXG4iKTsNCiAgICAgICAgcmV0dXJuIHJldDsNCiAgICAgIH0pKCkNCiAgICBzdG9yZV9rZXk6IG1lcmdlZF9zdW1tYXJ5DQogICAgc3RvcmVfcHJpbnQ6IHRydWUNCg0KLSB0eXBlOiBzaGVsbA0KICB0aXRsZTog5ZCI5bm25oyH5a6a54mI5pysDQogIGRhdGE6DQogICAgY21kOiBzdm4gbWVyZ2UgLWM8PHJldmlzaW9ucz4+IDw8c3JjPj4gPDxTVk5fQ1JFREVOVElBTD4+DQogICAgY3dkOiA8PGRzdD4+DQogICAgDQotIHR5cGU6IHN2bl9jb21taXQNCiAgZGF0YToNCiAgICBjd2Q6IDw8ZHN0Pj4NCiAgICBtc2c6IDw8bWVyZ2VkX3N1bW1hcnk+Pg0KICAgIGNyZWRlbnRpYWw6IDw8Y3JlZGVudGlhbF9vYmo+Pg0K --FULL_AUTOMATIC 1 --dst %WORKSPACE%/project --src %MREGE_SRC% --revisions "%MERGE_REVISIONS%"'
    if (params.BUILD_NEXT_JOB && params.NEXT_JOB) {
        build wait: false, job: params.NEXT_JOB
    }
}

def sendResult2Emailext () {

    if(!params.MAIL_TO){
        return;
    }

    if (params.ALERT_MAIL_ONLY && currentBuild.result == 'SUCCESS') {
        return
    }

    resolveResult()
    env.description = currentBuild.description
    env.durationString = currentBuild.durationString.minus(' and counting')
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
                      <li>发起人&nbsp;：&nbsp;${getRootBuildTriggerDesc()}</li>
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
                            ${ currentBuild.result == 'FAILURE' ? '<li>构建失败原因：&nbsp;' + getTailLogString() + '</li>' : ''}
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
    def consoleTextUrl = "${JENKINS_HOME}/jobs/${JOB_NAME}/builds/${BUILD_NUMBER}/log"
    def consoleText = readFile encoding: 'utf8', file:consoleTextUrl
    def result = ((consoleText =~ /"autoIn":\["(\d+)"\]/))
    if (result.find()) {
        return result[0][1]
    }
    result = ((consoleText =~ /build web base v(\d+) begin/))
    if (result.find()) {
        return result[0][1]
    }
    result = ((consoleText =~ /bin\-release\\web\\v(\d+)/))
    if (result.find()) {
        return result[0][1]
    }
    return null
}

def getMinigameVersion() {
    def consoleTextUrl = "${JENKINS_HOME}/jobs/${JOB_NAME}/builds/${BUILD_NUMBER}/log"
    def consoleText = readFile encoding: 'utf8', file:consoleTextUrl
    def result = ((consoleText =~ /"MiniGameVersion: (.*)"/))
    if (result.find()) {
        return result[0][1]
    }
    return null;
}

def getMiniGameToggleOperation() {
    def consoleTextUrl = "${JENKINS_HOME}/jobs/${JOB_NAME}/builds/${BUILD_NUMBER}/log"
    def consoleText = readFile encoding: 'utf8', file:consoleTextUrl
    def result = ((consoleText =~ /"MiniGameToggleOperation: (.*)"/))
    if (result.find()) {
        return result[0][1]
    }
    return null;
}

def getMinigameOutput() {
    def consoleTextUrl = "${JENKINS_HOME}/jobs/${JOB_NAME}/builds/${BUILD_NUMBER}/log"
    def consoleText = readFile encoding: 'utf8', file:consoleTextUrl
      // def consoleText = "\"MiniGameOutput: E:/projects/dldl_WX/dldl_bt_oppogame/oppo_quickgame/dist/com.rsdzz.net.nearme.gamecenter.signed.rpk\""
    def result = ((consoleText =~ /"MiniGameOutput: (.*)"/))
    if (result.find()) {
        return result[0][1]
    }
    return null
}

def getMinigameOutputURL() {
    def output = getMinigameOutput()
    if (!output) return null
    // http://192.168.1.205:8686/public_tool_grab_resource_item_img_seirei_jp/out/%E5%9B%BE%E6%A0%87.zip
    def root= new File("${WORKSPACE}")
    def full= new File(output)

    // Print the relative path of 'full' in relation to 'root'
    // Notice that the full path is passed as a parameter to the root.
    def relPath= root.toURI().relativize(full.toURI()).toString()
    return "${JOB_URL.replaceAll("\\:\\d+/job", ":8686")}${relPath}"
}

// 上传资源到FTP上
def ftpUploadSource() {
    dir('source') {
        checkout([$class: 'SubversionSCM', additionalCredentials: [], excludedCommitMessages: '', excludedRegions: '', excludedRevprop: '', excludedUsers: '', filterChangelog: true, ignoreDirPropChanges: false, includedRegions: ".*/${LOCAL_FILE}", locations: [[cancelProcessOnExternalsFail: true, credentialsId: getCredentialsId(), depthOption: 'infinity', ignoreExternalsOption: true, local: '.', remote: "$SCM_URL"]], quietOperation: true, workspaceUpdater: [$class: 'UpdateUpdater']])
    }
    checkoutAutomator()
    dir('ftp') {
        bat "%WORKSPACE%/automator/automator %WORKSPACE%/automator/cfg/dldl/ftp_upload.yml --FULL_AUTOMATIC 1 --remote_file %REMOTE_FILE% --local_file ${ params.LOCAL_FILE.tokenize(',').collect { env.WORKSPACE + '/source/' + it }.join(',')}"
    }
}

def getSVNInfo() {
    // 获取凭证
    withCredentials([usernamePassword(credentialsId: getCredentialsId(), passwordVariable: 'HG_CREDENTIAL_PASSWORD', usernameVariable: 'HG_CREDENTIAL_USERNAME')]) {
        svn_info = bat returnStdout: true, script: "svn info --username %HG_CREDENTIAL_USERNAME% --password %HG_CREDENTIAL_PASSWORD%"
        svn_last_changed_rev = ((svn_info =~ /Last Changed Rev\: (\d+)/)[0][1])
        env.SVN_LAST_CHANGED_REV = svn_last_changed_rev
    }
}

// 使用本地环境的 svn 检出, 不需要 svn upgrade
def checkoutSVN(scmUrl, poll = true, changelog = true, quiet = true, local = ".", includedRegions = "", excludedRegions = "") {
    if (fileExists('.svn')) {
        bat 'svn cleanup'
        // 还原
        bat returnStdout: true, script: '@echo off && svn revert -R .'
    } else {
        // 获取凭证
        withCredentials([usernamePassword(credentialsId: getCredentialsId(), passwordVariable: 'HG_CREDENTIAL_PASSWORD', usernameVariable: 'HG_CREDENTIAL_USERNAME')]) {
            // 拉取 SVN
            bat(script: "svn checkout ${scmUrl} . --quiet --username %HG_CREDENTIAL_USERNAME% --password %HG_CREDENTIAL_PASSWORD%")
        }
    }
    // pollSCM
    checkout changelog: changelog, poll: poll, scm: [$class: 'SubversionSCM', additionalCredentials: [], excludedCommitMessages: '', excludedRegions: excludedRegions, excludedRevprop: '', excludedUsers: '', filterChangelog: true, ignoreDirPropChanges: false, includedRegions: includedRegions, locations: [[cancelProcessOnExternalsFail: true, credentialsId: getCredentialsId(), depthOption: 'infinity', ignoreExternalsOption: true, local: local, remote: "${scmUrl}"]], quietOperation: quiet, workspaceUpdater: [$class: 'UpdateUpdater']]
}

// 使用本地环境的 svn 检出, 不需要 svn upgrade
def checkoutComplexSVN(scm) {
    if (fileExists('.svn')) {
        bat 'svn cleanup'
        // 还原
        bat returnStdout: true, script: '@echo off && svn revert -R .'
    } else {
        def scmUrl = scm.scm ? scm.scm.locations[0].remote : scm.locations[0].remote
        // 获取凭证
        withCredentials([usernamePassword(credentialsId: getCredentialsId(), passwordVariable: 'HG_CREDENTIAL_PASSWORD', usernameVariable: 'HG_CREDENTIAL_USERNAME')]) {
            // 拉取 SVN
            bat(script: "svn checkout ${scmUrl} . --quiet --username %HG_CREDENTIAL_USERNAME% --password %HG_CREDENTIAL_PASSWORD%")
        }
}
    // pollSCM
    checkout(scm)
}

def checkoutGit(url, branch = "master", remote_submodule = false) {
    def git_remote = "origin"
    def git_remote_url = ""
    def git_branch = ""
    if (fileExists('.git')) {
        // git_remote = bat([returnStdout: true, script: '@echo off && git remote']).trim()
        git_remote_url = bat([returnStdout: true, script: "@echo off && git remote get-url ${git_remote}"]).trim()
        git_branch = bat([returnStdout: true, script: "@echo off && git branch --show-current"]).trim()
    }
    // print git_remote
    // print git_remote_url
    // print git_branch
    // print git_remote_url == url
    // print git_branch == branch
    def changed = git_remote_url != url || git_branch != branch
    if (changed) {
        print("prev=${git_remote_url} - ${git_branch} curr=${url} - ${branch}")
        // git changelog: false, poll: false, url: url, branch: branch
        deleteDir()
        bat "git clone -b ${branch} ${url} ."
    }
    bat "git checkout -- *" // 先还原
    bat "git pull ${git_remote} ${branch}"
    if (remote_submodule) {
        bat "git submodule update --init --recursive --remote"
    } else {
        bat "git submodule update --init --recursive"
    }
}

def pub200AutomaticIntegrated() {
    checkoutAutomator()
    dir('project') {
        // 检出代码
        // checkoutSVN(params.HG_REPOSITORY_SRC)
        checkoutComplexSVN(changelog: true, poll: true, scm: [$class: 'SubversionSCM', additionalCredentials: [], excludedCommitMessages: '', excludedRegions: '''.*/out/.*''', excludedRevprop: '', excludedUsers: '', filterChangelog: true, ignoreDirPropChanges: false, includedRegions: '', locations: [[cancelProcessOnExternalsFail: true, credentialsId: getCredentialsId(), depthOption: 'infinity', ignoreExternalsOption: true, local: '.', remote: "$HG_REPOSITORY_SRC"]], quietOperation: true, workspaceUpdater: [$class: 'UpdateUpdater']])
        getSVNInfo()


        if (params.HG_MONITOR_SKIN_ID) {
            bat([label: '皮肤控件ID检测', returnStdout: false, script: "%WORKSPACE%/automator/automator %WORKSPACE%/automator/cfg/dldl/monitor_resource_modification.yml --FULL_AUTOMATIC --workspaceFolder %WORKSPACE%/project --revisions \"${getRevisions()}\" --jenkins ${JENKINS_URL} --webhook https://oapi.dingtalk.com/robot/send?access_token=d49fdc03b05ac8d52da7ad4167b94823a2c77225bb93d943440a0340db5dd313"])
        }
        if (params.HG_MONITOR_SKIN_GROUPNAME) {
            bat([label: '皮肤组名检测', returnStdout: false, script: "%WORKSPACE%/automator/automator %WORKSPACE%/automator/cfg/dldl/check_skin_notify.yml --FULL_AUTOMATIC --workspaceFolder %WORKSPACE%/project --webhook https://oapi.dingtalk.com/robot/send?access_token=d49fdc03b05ac8d52da7ad4167b94823a2c77225bb93d943440a0340db5dd313"])
        }
        if (params.HG_MONITOR_IMAGE) {
            bat([label: '图片资源检测', returnStdout: false, script: "%WORKSPACE%/automator/automator %WORKSPACE%/automator/cfg/dldl/check_image_notify.yml --FULL_AUTOMATIC --workspaceFolder %WORKSPACE%/project --webhook https://oapi.dingtalk.com/robot/send?access_token=d49fdc03b05ac8d52da7ad4167b94823a2c77225bb93d943440a0340db5dd313"])
        }
        // addBuildDescripion ("${new Date().format('yyyy-MM-dd(E)HH:mm:ss', TimeZone.getTimeZone('Asia/Shanghai')) - '星期'}")
        // if (env.SVN_LAST_CHANGED_REV) {
        //     addBuildDescripion ("r" + env.SVN_LAST_CHANGED_REV)
        // }
        // addBuildDescripion (getCommitUsernames().join(","))
        addBuildDescripion (getChangeString(false).join(","))
        // 编译
        if (needCompile()) {
            // addInfoBadge text: '触发编译'
            addBuildDescripion ("编译")
            def pub_200_out_bat = ''
            // 编译代码的备选批处理文件
            def pub_200_out_bat_alternatives = [
                'pub_200_out.bat',
            ]
            for (alternative in pub_200_out_bat_alternatives) {
                if (fileExists(alternative)) {
                    pub_200_out_bat = alternative
                    break
                }
            }
            if (pub_200_out_bat) { // 执行manifest排序
                bat([label: '更新manifest', returnStdout: false, script: '%WORKSPACE%/automator/automator %WORKSPACE%/automator/cfg/dldl/generate_sorted_ts.yml --FULL_AUTOMATIC --workspaceFolder %WORKSPACE%/project'])
                bat([label: '编译代码', returnStdout: false, script: pub_200_out_bat])
                // 获取凭证
                withCredentials([usernamePassword(credentialsId: getCredentialsId(), passwordVariable: 'HG_CREDENTIAL_PASSWORD', usernameVariable: 'HG_CREDENTIAL_USERNAME')]) {
                    // 提交 SVN
                    bat([label: 'SVN提交', returnStdout: false, script: "svn commit -m \"out [${getLastChangedRev()}]\" --username %HG_CREDENTIAL_USERNAME% --password %HG_CREDENTIAL_PASSWORD% out/main.min.* manifest.json src/base/WND_ID_CFG.ts ui_ctrl out/index.html"])
                }
            } else {
                bat([label: '发布200', returnStdout: false, script: 'node scripts --hgt _200_loc --noUserOp --noProjectUpdate'])
                // 获取凭证
                withCredentials([usernamePassword(credentialsId: getCredentialsId(), passwordVariable: 'HG_CREDENTIAL_PASSWORD', usernameVariable: 'HG_CREDENTIAL_USERNAME')]) {
                    // 提交 SVN
                    bat([label: 'SVN提交', returnStdout: false, script: "svn commit -m \"out [${getLastChangedRev()}]\" --username %HG_CREDENTIAL_USERNAME% --password %HG_CREDENTIAL_PASSWORD% out manifest.json"])
                }
            }
        } else {
            setCurrentBuildUnstableIfNecessary()
        }
    }
    // 创建out目录resource链接
    dir('project/out') {
        bat '''if not exist "resource" (
mklink /j "resource" "../resource"
)
'''
    }
}

def addBuildDescripion(str) {
    if (!str) return
    buildDescription ((currentBuild.description ? currentBuild.description + " | " : "") + str)
}

def validateDev() {
    dir('project') {
        // 检出代码
        checkoutComplexSVN(changelog: true, poll: true, scm: [$class: 'SubversionSCM', additionalCredentials: [], excludedCommitMessages: '', excludedRegions: '', excludedRevprop: '', excludedUsers: '', filterChangelog: true, ignoreDirPropChanges: false, includedRegions: '''.*/src/.*\\w+\\.ts''', locations: [[cancelProcessOnExternalsFail: true, credentialsId: getCredentialsId(), depthOption: 'infinity', ignoreExternalsOption: true, local: '.', remote: "$HG_REPOSITORY_SRC"]], quietOperation: true, workspaceUpdater: [$class: 'UpdateUpdater']])
        getSVNInfo()
        // addBuildDescripion ("${new Date().format('yyyy-MM-dd(E)HH:mm:ss', TimeZone.getTimeZone('Asia/Shanghai')) - '星期'}")
        // if (env.SVN_LAST_CHANGED_REV) {
        //     addBuildDescripion ("r" + env.SVN_LAST_CHANGED_REV)
        // }
        // addBuildDescripion (getCommitUsernames().join(","))
        addBuildDescripion (getChangeString(false).join(","))
        // 编译
        if (needCompile()) {
            // addInfoBadge text: '触发编译'
            addBuildDescripion ("编译")
            // bat([label: '校验', returnStdout: false, script: params.HG_VALIDATE_SCRIPT])
            compileLog = bat([label: '校验', returnStdout: true, script: params.HG_VALIDATE_SCRIPT])
            print compileLog
            if (params.HG_VALIDATE_SUCCESS_KEYWORD) {
                if (!(compileLog =~ /${params.HG_VALIDATE_SUCCESS_KEYWORD}/).find()) {
                    print "success keyword \"${HG_VALIDATE_SUCCESS_KEYWORD}\" not found"
                    error "validateDev failed"
            }
        }
            if (params.HG_VALIDATE_FAILURE_KEYWORD) {
                if ((compileLog =~ /${params.HG_VALIDATE_FAILURE_KEYWORD}/).find()) {
                    print "failure keyword \"${HG_VALIDATE_FAILURE_KEYWORD}\" found"
                    error "validateDev failed"
            }
    }
}
}
}

// 获取末尾的几条日志
def getTailLogString(size = 50) {
    def consoleTextUrl = "${JENKINS_HOME}/jobs/${JOB_NAME}/builds/${BUILD_NUMBER}/log"
    def consoleText = readFile encoding: 'utf8', file:consoleTextUrl
    def result = consoleText.tokenize('\n').findAll {
        !((it =~ /\[Pipeline\]/).find())
    }.collect {
        (it - ~/^\[\d+\-\d+\-\d+T\d+\:\d+\:\d+\.\d+Z\] */).replaceAll("\\[\\d+m", '')
    }
    def max_size = result.size()
    return result[(Math.min(max_size, size) * -1)..-1].join('\n')
}

def getRevisions() {
    return currentBuild.changeSets.collect {
        return it.items.collect {
            "${it.getCommitId()}"
        }.join(',')
    }.join('')
}

def getCredentialsId() {
    return env.HG_CREDENTIALS_ID ? env.HG_CREDENTIALS_ID : "dfb8344e-2d0c-4750-8154-9503745a01f9"
}

def getDingTalkRobot() {
    return env.HG_DINGTALK_ROBOT ? env.HG_DINGTALK_ROBOT : "automator"
}

def checkoutAutomator(remote_submodule = true) {
    dir('automator') {
        try {
            checkoutGit("http://192.168.1.205:3000/yzp/automator_artifact.git", "master", remote_submodule)
        } catch (Exception e) {
            print(e)
            currentBuild.result = 'UNSTABLE'
        }
    }
}

def checkoutPublish() {
    dir('publish') {
        try {
            checkoutGit("http://192.168.1.205:3000/fangjie/publish.git")
            bat '''
npm i
'''
        } catch (Exception e) {
            print(e)
            currentBuild.result = 'UNSTABLE'
        }
    }
}

def cleanupHGPubToolsDist() {
    lock(resource: 'pub2web') {
        // 检查状态
        def status = bat returnStdout: true, script: '@echo off && svn status %DLDL_PUB_TOOLS_DIR%'
        if (status && (status =~ /^.{2}L/).find()) {
            print 'HGPubToolsDist is already locked'
            bat 'svn cleanup %DLDL_PUB_TOOLS_DIR%'
        } else {
            print 'HGPubToolsDist is not locked'
    }
        bat 'svn up %DLDL_PUB_TOOLS_DIR%'
}
}

// 判断是有需要编译
def needCompile() {
    return hasCode2Compile() || params.FORCE_COMPILE
}

// 判断是否有代码需要编译
def hasCode2Compile() {
    def FILES_TRIGGER_COMPILE = (params.FILES_TRIGGER_COMPILE ? params.FILES_TRIGGER_COMPILE : "").tokenize(",")
    return currentBuild.changeSets.any {
        return it.items.any {
            return it.getAffectedPaths().any {
                def path = it
                // print path
                return ((path =~ /(\/|^)(src|src_base|src_ext|dep_libs|index)\/.*\.(ts|js)$/).find() && path != "src/base/WND_ID_CFG.ts") || FILES_TRIGGER_COMPILE.any { path == it }
            }
        }
    }
}

// 判断是否有logo需要刷新
def hasLogo2Refresh() {
    return currentBuild.changeSets.any {
        return it.items.any {
            return it.getAffectedPaths().any {
                def path = it
                // print path
                return (path =~ /(\/|^)resource\/loading\/res\/logo.*\.png$/).find()
            }
        }
    }
}
// 判断是否有index.js/base.js/seasundclogger_2.0.4.js需要刷新
def hasIndexJS2Refresh() {
    return currentBuild.changeSets.any {
        return it.items.any {
            return it.getAffectedPaths().any {
                def path = it
                // print path
                return (path =~ /(\/|^)resource\/js\/(index|base|seasundclogger_2.0.4)\.js$/).find()
            }
        }
    }
}

// 获取最上游构建的发起描述
def getRootBuildTriggerDesc() {
    def build = getRootBuild(currentBuild)
    def desc = build.getBuildCauses()[0] && (build.getBuildCauses()[0].userName ? build.getBuildCauses()[0].userName : build.getBuildCauses()[0].shortDescription.minus('Started by ').replace('timer', '定时器').replace('an SCM change', 'SCM轮询'))
    if (build.getAbsoluteUrl() != currentBuild.getAbsoluteUrl()) {
        desc += "[${build.getFullDisplayName()}](${build.getAbsoluteUrl()})"
    }
    return desc
}

// 获取最上游构建的发起人id
def getRootBuildUserId() {
    def build = getRootBuild(currentBuild)
    return build.getBuildCauses()[0] && build.getBuildCauses()[0].userId
}

// 判断是否为手动触发
def isManualTrigger() {
    return !!getRootBuildUserId();
}

// 获取最上游构建的发起人手机
def getRootBuildMobile() {
    def userId = getRootBuildUserId()
    if (!userId || userId.getClass().name == "net.sf.json.JSONNull") {
        return ""
    } else {
        return hudson.model.User.getById(userId, false).getProperty(io.jenkins.plugins.DingTalkUserProperty.class).getMobile()
    }
}

// 获取最上游构建的发起人邮箱地址
def getRootBuildEmail() {
    def userId = getRootBuildUserId()
    if (!userId || userId.getClass().name == "net.sf.json.JSONNull") {
        return ""
    } else {
        return hudson.model.User.getById(userId, false).getProperty(hudson.tasks.Mailer.UserProperty).getAddress()
    }
}

// 获取最上游构建
def getRootBuild(build) {
    if (build.upstreamBuilds.size() == 0) {
        return build
    } else {
        return getRootBuild(build.upstreamBuilds[0]);
    }
}

def getDateByStep(step = 3e5) {
    def timestamp = System.currentTimeMillis()
    def takeEffectTimestamp = ((((timestamp / step) as int) + 1) * step) as long
    def takeEffectTime = new Date()
    takeEffectTime.setTime(takeEffectTimestamp)
    return takeEffectTime;
}

// 判断是有需要提升大版本（手动勾选或者index相关的内容更新）
def needUpgradeIndexVersion() {
    return params.PUB_CFG && (params.ENABLE_UPGRADE_INDEX_VERSION || hasLogo2Refresh() || hasIndexJS2Refresh())
}

// 提升大版本号，需要额外配置参数如： PUB_CFG={ "root": "md2", "cdn_root": "md2", "cdn_local": "cdn/local/moli", "cdn_sync": "cdn/start_cos_sync.bat", "version": "1000", "index_tag": "6497" }
def webUpgradeSdkInfo() {
    if (!needUpgradeIndexVersion()) {
        return
    }
    bat '%WORKSPACE%/automator/automator %WORKSPACE%/automator/cfg/release/web/web_upgrade_sdk_info.yml --FULL_AUTOMATIC 1'
}

// 同步大版本 index 文件
def webSyncIndex() {
    if (!needUpgradeIndexVersion()) {
        return
    }
    bat '%WORKSPACE%/automator/automator %WORKSPACE%/automator/cfg/release/web/web_sync_index.yml --FULL_AUTOMATIC 1'
}

def dowloadFont(url, ver, langs) {
    if(!url || !ver){
        return;
    }

    for(lan in langs){
        def dir = "./$lan/"
        def folder = new File(dir)
        if(!folder.exists()){
            folder.mkdirs()
        }
        fileOperations([fileDownloadOperation(
        proxyHost: '127.0.0.1'
        , proxyPort: '10811'
        , targetFileName: 'Font.ttf'
        , targetLocation: dir
        , url: "https://${url}/${ver}/g123/i18n/${lan}/fonts/fonts.ttf"
        , password: ''
        , userName: '')]);
    }

    bat([label: 'SVN新增', returnStdout: false, script: "svn add ./ --force"])
    withCredentials([usernamePassword(credentialsId: getCredentialsId(), passwordVariable: 'HG_CREDENTIAL_PASSWORD', usernameVariable: 'HG_CREDENTIAL_USERNAME')]) {
        // 提交 SVN
        bat([label: 'SVN提交', returnStdout: false, script: "svn commit -m \"[0] ttf Update\" --username %HG_CREDENTIAL_USERNAME% --password %HG_CREDENTIAL_PASSWORD%"])
    }
   
}