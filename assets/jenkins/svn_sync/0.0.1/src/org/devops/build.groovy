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
                (showIndex ? "${i++}. " : "") + "${it.msg.take(MAX_MSG_LEN).replaceAll('[\r\n]+', '')}" + (showDetail ? " by ${it.author.getFullName()} at ${it.getCommitId()}" : "")
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
        type: 'ACTION_CARD',
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
    switch(currentBuild.result) {
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
        type: 'ACTION_CARD',
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
    generatePatchFile()
    resolveResult()
    env.description = currentBuild.description
    env.durationString = currentBuild.durationString.minus(' and counting')
    // 失败时，@提交者
    def atUsers = getAtUsers(currentBuild.result == 'FAILURE')
    dingtalk(
        robot: getDingTalkRobot(),
        type: 'ACTION_CARD',
        title: "${currentBuild.fullDisplayName} ${result}",
        at: atUsers,
        atAll: false,
        text: [
            "- 任务 [${currentBuild.fullDisplayName}](${BUILD_URL}) ",
            "- 状态 <font color=${result_color}>${result}</font>",
            "- 发起 ${getRootBuildTriggerDesc()}",
            "- 时刻 ${new Date().format('yyyy-MM-dd(E)HH:mm:ss', TimeZone.getTimeZone('Asia/Shanghai')) - '星期'}",
            "- 用时 ${durationString}",
            "- [记录](${env.HG_PATCH_FILE})",
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
        type: 'ACTION_CARD',
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

def generatePatchFile() {
    if (!env.HG_REPOSITORY_SRC) {
        return;
    }
    def revisions = getRevisions()
    if (revisions) {
        def patches = ""
        revisions.tokenize(",").each {
            def revision = it
            def patch = bat returnStdout: true, script: "@echo off && svn diff ${HG_REPOSITORY_SRC} -c${revision}"
            patches += patch + "\n"
        }
        def filename = "patches/out/r${revisions}.patch";
        def filepath = "http://192.168.1.205:8686/view/${WORKSPACE.replaceAll('\\\\', '/')}/${filename}"
        fileOperations([fileCreateOperation(fileContent: patches, fileName: filename)])
        env.HG_PATCH_FILE = filepath
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
        checkoutSVN(params.HG_REPOSITORY_SRC)
        // 发送通知
        sendStart2DingTalk_PubWeb()
        // 设置环境变量 prg_dir 给 hgbuild 使用
        env.prg_dir = pwd()
    }
    checkoutPublish()
    cleanupHGPubToolsDist()
    lock(resource: 'pub2web') {
        dir('publish') {
            // 发布
            bat([label: '发布', returnStdout: false, script: """
if "%chkdst%" == "true" (
npx hgbuild walk ${HG_PUB_RES} ${HG_PUB_TYPE} --noUserOp --noProjectUpdate --chkdst
) else (
npx hgbuild walk ${HG_PUB_RES} ${HG_PUB_TYPE} --noUserOp --noProjectUpdate
)"""])
        }
    }
    }

// 新的发布流程 - 集成版本
def pubToWebIntegratedCommonOld() {
    // lock(resource: "${cfg_dir}") {
    dir('project') {
        // 检出
        checkoutSVN(params.HG_REPOSITORY_SRC)
        // 发送通知
        sendStart2DingTalk_PubWeb()
    }
    checkoutPublish()
    cleanupHGPubToolsDist()
    lock(resource: 'pub2web') {
        // 发布
        dir('publish') {
            bat([label: '发布', returnStdout: false, script: """
if "%chkdst%" == "true" (
    hgbuild run _11_common_old --prg_dir ${WORKSPACE}/project --upload_filter ${params.upload_filter} --toolTag ${params.toolTag} --cfg_dir ${params.cfg_dir} --hgVerTag ${params.hgVerTag ? params.hgVerTag : "hgvc_ver"} --noUserOp --noProjectUpdate --chkdst
) else (
    hgbuild run _11_common_old --prg_dir ${WORKSPACE}/project --upload_filter ${params.upload_filter} --toolTag ${params.toolTag} --cfg_dir ${params.cfg_dir} --hgVerTag ${params.hgVerTag ? params.hgVerTag : "hgvc_ver"} --noUserOp --noProjectUpdate
)"""])
        }
    }
    }

// 新的发布流程 - 集成版本
def pubToWebIntegratedCommon() {
    // lock(resource: "${cfg_dir}") {
    dir('project') {
        // 检出
        checkoutSVN(params.HG_REPOSITORY_SRC)
        // 发送通知
        sendStart2DingTalk_PubWeb()
    }
    // 发布
    checkoutPublish()
    cleanupHGPubToolsDist()
    lock(resource: 'pub2web') {
        dir('publish') {
            bat([label: '发布', returnStdout: false, script: """
if "%chkdst%" == "true" (
    hgbuild run _10_common --prg_dir ${WORKSPACE}/project --upload_filter ${params.upload_filter} --toolTag ${params.toolTag} --cfg_dir ${params.cfg_dir}  --hgVerTag ${params.hgVerTag ? params.hgVerTag : "hgvc_ver"} --noUserOp --noProjectUpdate --chkdst
) else (
    hgbuild run _10_common --prg_dir ${WORKSPACE}/project --upload_filter ${params.upload_filter} --toolTag ${params.toolTag} --cfg_dir ${params.cfg_dir}  --hgVerTag ${params.hgVerTag ? params.hgVerTag : "hgvc_ver"} --noUserOp --noProjectUpdate
)"""])
        }
    }
    }

// pubToWeb构建开始
def sendStart2DingTalk_PubWeb() {
    if (params.HG_QUIET) {
        return
    }
    dingtalk(
        robot: getDingTalkRobot(),
        type: 'ACTION_CARD',
        title: "${currentBuild.fullDisplayName} 开始",
        // at: getAtUsers(),
        // atAll: false,
        text: [
            "# **[${currentBuild.fullDisplayName}](${BUILD_URL})**",
            '***',
            '- 状态 开始',
            "- 发起 ${getRootBuildTriggerDesc()}",
            "- 时刻 ${new Date().format('yyyy-MM-dd(E)HH:mm:ss', TimeZone.getTimeZone('Asia/Shanghai')) - '星期'}",
            '- 仓库',
            params.HG_REPOSITORY_SRC ? (params.HG_REPOSITORY_SRC - ~/.*\//) : 'Unknown',
            '- logo ' + (hasLogo2Refresh() ? '<font color=#ff9f00>已修改</font>' : '未修改'),
            '- 记录',
            '***',
        ] + getChangeString()
    )
}

// 获取要@的用户
def getAtUsers(includeCommitUser = false) {
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
    env.description = currentBuild.description
    env.durationString = currentBuild.durationString.minus(' and counting')
    dingtalk(
        robot: getDingTalkRobot(),
        type: 'ACTION_CARD',
        title: "${currentBuild.fullDisplayName} ${result}",
        at: getAtUsers(),
        atAll: false,
        text: [
            "# **[${currentBuild.fullDisplayName}](${BUILD_URL})**",
            '***',
            "- 状态 <font color=${result_color}>${result}</font>",
            "- 资源版本 <font color=${result_color}>${pubWebVersion ? pubWebVersion : 'Unknown'}</font>",
            "- 发起 ${getRootBuildTriggerDesc()}",
            "- 时刻 ${new Date().format('yyyy-MM-dd(E)HH:mm:ss', TimeZone.getTimeZone('Asia/Shanghai')) - '星期'}",
            "- 用时 ${durationString}",
            '- 仓库',
            params.HG_REPOSITORY_SRC ? (params.HG_REPOSITORY_SRC - ~/.*\//) : 'Unknown',
            '- logo ' + (hasLogo2Refresh() ? '<font color=#ff9f00>已修改</font>' : '未修改'),
            '- 记录',
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

def sendStart2DingTalk_PubMinigame() {
    if (params.HG_QUIET) {
        return
    }
    dingtalk(
        robot: getDingTalkRobot(),
        type: 'ACTION_CARD',
        title: "${currentBuild.fullDisplayName} 开始",
        // at: getAtUsers(),
        // atAll: false,
        text: [
            "# **[${currentBuild.fullDisplayName}](${BUILD_URL})**",
            '***',
            '- 状态 开始',
            "- 发起 ${getRootBuildTriggerDesc()}",
            "- <font color=${env.ENABLE_PUBLISH_STATIC_RESOURCE == "true" ? "#1890ff" : "#888888"}>静态资源${env.ENABLE_PUBLISH_STATIC_RESOURCE == "true" ? "" : "不"}更新</font>",
            "- <font color=${env.ENABLE_MINIGAME_UPLOAD == "true" ? "#1890ff" : "#888888"}>游戏包${env.ENABLE_MINIGAME_UPLOAD == "true" ? "" : "不"}更新</font>",
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
        type: 'ACTION_CARD',
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
            minigameToggleOperation ? "- 小游戏配置 <font color=#1890ff>${minigameToggleOperation}</font>" : "",
            "- 生效时间 <font color=#1890ff>${getDateByStep().format('yyyy-MM-dd(E)HH:mm:ss', TimeZone.getTimeZone('Asia/Shanghai')) - '星期'}</font>",
            "- <font color=${env.ENABLE_PUBLISH_STATIC_RESOURCE == "true" ? "#000000" : "#aaaaaa"}>静态资源${env.ENABLE_PUBLISH_STATIC_RESOURCE == "true" ? "" : "不"}更新</font>",
            "- <font color=${env.ENABLE_MINIGAME_UPLOAD == "true" ? "#000000" : "#aaaaaa"}>游戏包${env.ENABLE_MINIGAME_UPLOAD == "true" ? "" : "不"}更新</font>",
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
        )).findAll{ it }
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
        type: 'ACTION_CARD',
        title: "${currentBuild.fullDisplayName} ${result}",
        at: getAtUsers(),
        atAll: false,
        text: [
            "# **[${currentBuild.fullDisplayName}](${BUILD_URL})**",
            '***',
            "- 状态 <font color=${result_color}>${result}</font>",
            "- 时刻 ${new Date().format('yyyy-MM-dd(E)HH:mm:ss', TimeZone.getTimeZone('Asia/Shanghai')) - '星期'}",
            "- 用时 ${durationString}",
        ] + (
            currentBuild.result == 'FAILURE' ? [
                '***',
                "- <font color=${result_color}>失败日志</font>",
                getTailLogString(10),
            ] : []
        )
    )
}

// 取回已翻译的内容 API版本
def retrieveTranslationAPI() {
    lock(resource: 'conversion_api') {
        checkoutAutomator()
        dir('project/resource/assets/cfgjson') {
            checkoutComplexSVN([scm: [$class: 'SubversionSCM', additionalCredentials: [], excludedCommitMessages: '', excludedRegions: '', excludedRevprop: '', excludedUsers: '', filterChangelog: true, ignoreDirPropChanges: false, includedRegions: '''.*/resource/assets/cfgjson/\\w+\\.json
    .*/resource/assets/cfgjson/base/\\w+\\.json
    .*/resource/js/common\\.js''', locations: [[cancelProcessOnExternalsFail: true, credentialsId: getCredentialsId(), depthOption: 'infinity', ignoreExternalsOption: true, local: '.', remote: "$SCM_URL/resource/assets/cfgjson"]], quietOperation: true, workspaceUpdater: [$class: 'UpdateUpdater']]])
        }
        dir('project/resource/js') {
            checkoutComplexSVN([scm: [$class: 'SubversionSCM', additionalCredentials: [], excludedCommitMessages: '', excludedRegions: '', excludedRevprop: '', excludedUsers: '', filterChangelog: true, ignoreDirPropChanges: false, includedRegions: '''.*/resource/assets/cfgjson/\\w+\\.json
    .*/resource/assets/cfgjson/base/\\w+\\.json
    .*/resource/js/common\\.js''', locations: [[cancelProcessOnExternalsFail: true, credentialsId: getCredentialsId(), depthOption: 'infinity', ignoreExternalsOption: true, local: '.', remote: "$SCM_URL/resource/js"]], quietOperation: true, workspaceUpdater: [$class: 'UpdateUpdater']]])
        }
        dir('translation') {
            checkoutComplexSVN changelog: false, poll: false, scm: [$class: 'SubversionSCM', additionalCredentials: [], excludedCommitMessages: '', excludedRegions: '', excludedRevprop: '', excludedUsers: '', filterChangelog: false, ignoreDirPropChanges: false, includedRegions: '', locations: [[cancelProcessOnExternalsFail: true, credentialsId: getCredentialsId(), depthOption: 'infinity', ignoreExternalsOption: true, local: '.', remote: 'https://svn100.hotgamehl.com/svn/Html5/trunk/dldl_WX/translation_keyvalue']], quietOperation: true, workspaceUpdater: [$class: 'UpdateUpdater']]
        }
        dir('convert2src') {
            retry(1) {
                bat '%WORKSPACE%/automator/automator %WORKSPACE%/automator/cfg/dldl/conversion_retrieve@api.yml --FULL_AUTOMATIC 1 --projectFolder %WORKSPACE%/project --gitFolder %WORKSPACE%/i18n_cp_seirei --conversionWorkspaceFolder %WORKSPACE%/conversion --translationFolder %WORKSPACE%/translation --zipUrl "%ZIP_URL%"'
            }
        }
    }
    // 自动构建发布任务
    if (params.BUILD_NEXT_JOB && params.NEXT_JOB) {
        build wait: false, job: params.NEXT_JOB, parameters: [extendedChoice(name: 'HG_REPOSITORY_SRC', value: params.SCM_URL)]
    }
}

// 生成翻译KV表_API
def generateTranslationKV_API() {
    lock(resource: 'conversion_api') {
        checkoutAutomator()
        dir('translation') {
            checkoutComplexSVN changelog: false, poll: false, scm: [$class: 'SubversionSCM', additionalCredentials: [], excludedCommitMessages: '', excludedRegions: '', excludedRevprop: '', excludedUsers: '', filterChangelog: false, ignoreDirPropChanges: false, includedRegions: '', locations: [[cancelProcessOnExternalsFail: true, credentialsId: getCredentialsId(), depthOption: 'infinity', ignoreExternalsOption: true, local: '.', remote: 'https://svn100.hotgamehl.com/svn/Html5/trunk/dldl_WX/translation_keyvalue']], quietOperation: true, workspaceUpdater: [$class: 'UpdateUpdater']]
        }
        dir('project/resource/assets/cfgjson') {
            checkoutComplexSVN([$class: 'SubversionSCM', additionalCredentials: [], excludedCommitMessages: '', excludedRegions: '', excludedRevprop: '', excludedUsers: '', filterChangelog: true, ignoreDirPropChanges: false, includedRegions: '''.*/resource/assets/cfgjson/\\w+\\.json
    .*/resource/assets/cfgjson/base/\\w+\\.json
    .*/resource/js/common\\.js''', locations: [[cancelProcessOnExternalsFail: true, credentialsId: getCredentialsId(), depthOption: 'infinity', ignoreExternalsOption: true, local: '.', remote: "$SCM_URL/resource/assets/cfgjson"]], quietOperation: true, workspaceUpdater: [$class: 'UpdateUpdater']])
        }
        dir('project/resource/js') {
            checkoutComplexSVN([$class: 'SubversionSCM', additionalCredentials: [], excludedCommitMessages: '', excludedRegions: '', excludedRevprop: '', excludedUsers: '', filterChangelog: true, ignoreDirPropChanges: false, includedRegions: '''.*/resource/assets/cfgjson/\\w+\\.json
    .*/resource/assets/cfgjson/base/\\w+\\.json
    .*/resource/js/common\\.js''', locations: [[cancelProcessOnExternalsFail: true, credentialsId: getCredentialsId(), depthOption: 'infinity', ignoreExternalsOption: true, local: '.', remote: "$SCM_URL/resource/js"]], quietOperation: true, workspaceUpdater: [$class: 'UpdateUpdater']])
        }
        dir('convert2src') {
            bat '%WORKSPACE%/automator/automator %WORKSPACE%/automator/cfg/dldl/conversion_to_src@api.yml --FULL_AUTOMATIC 1 --projectFolder %WORKSPACE%/project --conversionWorkspaceFolder %WORKSPACE%/conversion --translationFolder %WORKSPACE%/translation'
        }
    }
}

def mergeSVN() {
    checkoutAutomator()
    dir('project') {
        checkoutSVN(params.HG_REPOSITORY_SRC)
    }
    bat '%WORKSPACE%/automator/automator %WORKSPACE%/automator/cfg/dldl/svn_merge.yml --FULL_AUTOMATIC 1 --dst %WORKSPACE%/project --src %MREGE_SRC% --revisions "%MERGE_REVISIONS%"'
    if (params.BUILD_NEXT_JOB && params.NEXT_JOB) {
        build wait: false, job: params.NEXT_JOB
    }
}

def sendResult2Emailext () {
    if (params.HG_QUIET && currentBuild.result == 'SUCCESS') {
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
                      <li>发起人&nbsp;：&nbsp;${currentBuild.getBuildCauses()[0].userName ? currentBuild.getBuildCauses()[0].userName : currentBuild.getBuildCauses()[0].shortDescription.minus('Started by ').replace('timer', '定时器').replace('an SCM change', 'SCM轮询')}</li>
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
        // 检查状态
        def status = bat returnStdout: true, script: '@echo off && svn status'
        print status
        if (status && (status =~ /^.{2}L/).find()) {
            print 'Workspace is already locked'
            bat 'svn cleanup'
        } else {
            print 'Workspace is not locked'
    }
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
    getSVNInfo()
}

// 使用本地环境的 svn 检出, 不需要 svn upgrade
def checkoutComplexSVN(scm) {
    if (fileExists('.svn')) {
        // 检查状态
        def status = bat returnStdout: true, script: '@echo off && svn status'
        print status
        if (status && (status =~ /^.{2}L/).find()) {
            print 'Workspace is already locked'
            bat 'svn cleanup'
        } else {
            print 'Workspace is not locked'
    }
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
    getSVNInfo()
}

def checkoutGit(url, branch = "master") {
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
        checkout changelog: false, poll: false, scm: [$class: 'GitSCM', branches: [[name: branch]], extensions: [[$class: 'SubmoduleOption', disableSubmodules: false, parentCredentials: false, recursiveSubmodules: true, reference: '', trackingSubmodules: false]], userRemoteConfigs: [[url: url]]]
    }
    bat "git checkout -- *" // 先还原
    bat "git pull ${git_remote} ${branch} --recurse-submodules"
    bat "git submodule update"
}

def pub200AutomaticIntegrated() {
    checkoutAutomator()
    dir('project') {
        // 检出代码
        // checkoutSVN(params.HG_REPOSITORY_SRC)
        checkoutComplexSVN(changelog: true, poll: true, scm: [$class: 'SubversionSCM', additionalCredentials: [], excludedCommitMessages: '', excludedRegions: '''.*/out/.*''', excludedRevprop: '', excludedUsers: '', filterChangelog: true, ignoreDirPropChanges: false, includedRegions: '', locations: [[cancelProcessOnExternalsFail: true, credentialsId: getCredentialsId(), depthOption: 'infinity', ignoreExternalsOption: true, local: '.', remote: "$HG_REPOSITORY_SRC"]], quietOperation: true, workspaceUpdater: [$class: 'UpdateUpdater']])


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
            if (pub_200_out_bat) {// 执行manifest排序
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
                if(!(compileLog =~ /${params.HG_VALIDATE_SUCCESS_KEYWORD}/).find()) {
                    print "success keyword \"${HG_VALIDATE_SUCCESS_KEYWORD}\" not found"
                    error "validateDev failed"
                }
            }
            if (params.HG_VALIDATE_FAILURE_KEYWORD) {
                if((compileLog =~ /${params.HG_VALIDATE_FAILURE_KEYWORD}/).find()) {
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

def checkoutAutomator() {
    dir('automator') {
        try {
            checkoutGit("http://192.168.1.205:3000/yzp/automator_artifact.git")
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

// 发送翻译KV表_API
def generateSendTranslationKV_API() {
    lock(resource: 'conversion_api') {
        checkoutAutomator()
        dir('project/resource/assets/cfgjson') {
            checkoutComplexSVN(changelog: false, poll: false, scm: [$class: 'SubversionSCM', additionalCredentials: [], excludedCommitMessages: '', excludedRegions: '', excludedRevprop: '', excludedUsers: '', filterChangelog: true, ignoreDirPropChanges: false, includedRegions: '''.*/resource/assets/cfgjson/\\w+\\.json
    .*/resource/assets/cfgjson/base/\\w+\\.json
    .*/resource/js/common\\.js''', locations: [[cancelProcessOnExternalsFail: true, credentialsId: getCredentialsId(), depthOption: 'infinity', ignoreExternalsOption: true, local: '.', remote: "$SCM_URL/resource/assets/cfgjson"]], quietOperation: true, workspaceUpdater: [$class: 'UpdateUpdater']])
        }
        dir('project/resource/js') {
            checkoutComplexSVN(changelog: false, poll: false, scm: [$class: 'SubversionSCM', additionalCredentials: [], excludedCommitMessages: '', excludedRegions: '', excludedRevprop: '', excludedUsers: '', filterChangelog: true, ignoreDirPropChanges: false, includedRegions: '''.*/resource/assets/cfgjson/\\w+\\.json
    .*/resource/assets/cfgjson/base/\\w+\\.json
    .*/resource/js/common\\.js''', locations: [[cancelProcessOnExternalsFail: true, credentialsId: getCredentialsId(), depthOption: 'infinity', ignoreExternalsOption: true, local: '.', remote: "$SCM_URL/resource/js"]], quietOperation: true, workspaceUpdater: [$class: 'UpdateUpdater']])
        }
        // .*/${PROJECT_NAME}/${PROJECT_VER}/cn/.*
        def common_js = readFile encoding: 'utf-8', file: 'project/resource/js/common.js'
        def localeCfg = ((common_js =~ /HG_GLOBAL\.LOCALIZATION_CFG \= ([\s\S]*?\});/)[0][1])
        // print localeCfg
        def projectName = ((localeCfg =~ /projectName\: "(.*?)"/)[0][1])
        print projectName
        def projectVer = ((localeCfg =~ /projectVer\: "(.*?)"/)[0][1])
        print projectVer
        def dst_locale = ((localeCfg =~ /dst_locale\: "(.*?)"/)[0][1])
        print dst_locale
        dir('translation') {
            checkoutComplexSVN([$class: 'SubversionSCM', additionalCredentials: [], excludedCommitMessages: '', excludedRegions: '', excludedRevprop: '', excludedUsers: '', filterChangelog: false, ignoreDirPropChanges: false, includedRegions: ".*/${projectName}/${projectVer}/${dst_locale}/cn/.*", locations: [[cancelProcessOnExternalsFail: true, credentialsId: getCredentialsId(), depthOption: 'infinity', ignoreExternalsOption: true, local: '.', remote: 'https://svn100.hotgamehl.com/svn/Html5/trunk/dldl_WX/translation_keyvalue']], quietOperation: true, workspaceUpdater: [$class: 'UpdateUpdater']])
        }
        dir('convert2src') {
            retry(1) {
                env.REVISIONS = env.REVISIONS ? env.REVISIONS : getRevisions()
                print 'env.REVISIONS ' + env.REVISIONS
                bat '%WORKSPACE%/automator/automator %WORKSPACE%/automator/cfg/dldl/conversion_to_send@api.yml --FULL_AUTOMATIC 1 --projectFolder %WORKSPACE%/project --projectName %PROJECT_NAME% --conversionWorkspaceFolder %WORKSPACE%/conversion --translationFolder %WORKSPACE%/translation --revisions "%REVISIONS%" --revision_beg "%REVISION_BEG%" --revision_end "%REVISION_END%"'
            }
        }
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
            return it.getAffectedFiles().any {
                def path = it.getPath()
                // print path
                return ((path =~ /([\\\/]|^)(src|src_base|src_ext|dep_libs|index)[\\\/].*\.(ts|js)$/).find() && path != "src\\base\\WND_ID_CFG.ts") || FILES_TRIGGER_COMPILE.any { path == it }
            }
        }
    }
}

// 判断是否有logo需要刷新
def hasLogo2Refresh() {
    return currentBuild.changeSets.any {
        return it.items.any {
            return it.getAffectedFiles().any {
                def path = it.getPath()
                // print path
                return (path =~ /(\\|^)resource\\loading\\res\\logo.*\.png$/).find()
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

// 获取最上游构建的发起人手机
def getRootBuildMobile() {
    def userId = getRootBuildUserId()
    if (!userId || userId.getClass().name == "net.sf.json.JSONNull") {
        return ""
    } else {
        return hudson.model.User.getById(userId, false).getProperty(io.jenkins.plugins.DingTalkUserProperty.class).getMobile()
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