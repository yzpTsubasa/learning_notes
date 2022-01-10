## javascript&typescript
## express 解决跨域问题
``` js
////// 方案一
app.use(require('cors')())

////// 方案二
app.all('*', function(req, res, next) {
    res.header("Access-Control-Allow-Origin", "*"); // 允许的域名
    res.header("Access-Control-Allow-Headers", "X-Requested-With"); 
    res.header("Access-Control-Allow-Methods", "PUT,POST,GET,DELETE,OPTIONS"); // 允许的方式
    res.header("X-Powerd-By", "3.2.1");
    res.header("Content-Type", "application/json;charset=utf-8");
    next();
});

```
## protobuf.js
``` javascript
// https://github.com/protobufjs/protobuf.js

/**
 * 版本 3.x.x ~ 5.x.x
 * 需要依赖 bytebuffer(包含long) 库
 */

// 加载解析协议内容
// protobuf-light.[.min]js 仅支持 json
dcodeIO.ProtoBuf.loadJson
dcodeIO.ProtoBuf.loadJsonFile
dcodeIO.ProtoBuf.loadProto
dcodeIO.ProtoBuf.loadProtoFile

// 查找指定协议类型
.lookup(...)

// 构建
.build(...)

// 编解码
.encode(...)
.decode(...)

/**
 * 版本 6.x.x ~
 * 需要可执行 eval/Function 的环境（小游戏环境无法使用）
 */

// 加载解析协议内容, 支持 proto/json
protobuf.parse(...)

// ...

...


```

## Base64 <=> ArrayBuffer
``` javascript
function base64ToUint8Array(base64String) {
　　　　const padding = '='.repeat((4 - base64String.length % 4) % 4);
       const base64 = (base64String + padding)
                    .replace(/\-/g, '+')
                    .replace(/_/g, '/');

       const rawData = window.atob(base64);
       const outputArray = new Uint8Array(rawData.length);

       for (let i = 0; i < rawData.length; ++i) {
            outputArray[i] = rawData.charCodeAt(i);
       }
       return outputArray;
}

function arrayBufferToBase64(buffer) {
         var binary = '';
         var bytes = new Uint8Array(buffer);
         var len = bytes.byteLength;
         for (var i = 0; i < len; i++) {
               binary += String.fromCharCode(bytes[i]);
         }
         return window.btoa(binary);
}
```
## 支持时区的Date日期对象
``` javascript
/**
 * 修改Date对象，使得其支持设置时区设置
 * @param {Date & { setTimezone: (timezone: number) => Date }} date 
 * @returns 
 */
function supportTimezone(date) {
    /**
     * 设置时区，东区为正值，西区为负值
     * @param timezone 
     */
     date.setTimezone = function(timezone) {
        this._timezone = timezone;
        this.updateTimezone();
        return this;
    }
    
    date.updateTimezone = function() {
        if (this._timezone == null) return;
        this._timezoneDate = this._timezoneDate || new Date();
        this._timezoneDate.setTime(this.getTime() + (this.getTimezoneOffset() + this._timezone * 60) * 6e4);
    }

    date.getDate = function() {
        if (this._timezone == null)
            return Date.prototype.getDate.call(this);
        return this._timezoneDate.getDate();
    }
    
    date.getDay = function() {
        if (this._timezone == null)
            return Date.prototype.getDay.call(this);
        return this._timezoneDate.getDay();
    }
    
    date.getFullYear = function() {
        if (this._timezone == null)
            return Date.prototype.getFullYear.call(this);
        return this._timezoneDate.getFullYear();
    }
    
    date.getHours = function() {
        if (this._timezone == null)
            return Date.prototype.getHours.call(this);
        return this._timezoneDate.getHours();
    }
    
    date.setDate = function() {
        let ret = Date.prototype.setDate.apply(this, arguments);
        this.updateTimezone();
        return ret;
    }
    
    date.setFullYear = function() {
        let ret = Date.prototype.setFullYear.apply(this, arguments);
        this.updateTimezone();
        return ret;
    }
    
    date.setHours = function() {
        let ret = Date.prototype.setHours.apply(this, arguments);
        this.updateTimezone();
        return ret;
    }
    
    date.setMilliseconds = function() {
        let ret = Date.prototype.setMilliseconds.apply(this, arguments);
        this.updateTimezone();
        return ret;
    }
    
    date.setMinutes = function() {
        let ret = Date.prototype.setMinutes.apply(this, arguments);
        this.updateTimezone();
        return ret;
    }
    
    date.setMonth = function() {
        let ret = Date.prototype.setMonth.apply(this, arguments);
        this.updateTimezone();
        return ret;
    }
    
    date.setSeconds = function() {
        let ret = Date.prototype.setSeconds.apply(this, arguments);
        this.updateTimezone();
        return ret;
    }
    
    date.setTime = function() {
        let ret = Date.prototype.setTime.apply(this, arguments);
        this.updateTimezone();
        return ret;
    }
    
    date.setUTCDate = function() {
        let ret = Date.prototype.setUTCDate.apply(this, arguments);
        this.updateTimezone();
        return ret;
    }
    
    date.setUTCDate = function() {
        let ret = Date.prototype.setUTCDate.apply(this, arguments);
        this.updateTimezone();
        return ret;
    }
    
    date.setUTCFullYear = function() {
        let ret = Date.prototype.setUTCFullYear.apply(this, arguments);
        this.updateTimezone();
        return ret;
    }
    
    date.setUTCHours = function() {
        let ret = Date.prototype.setUTCHours.apply(this, arguments);
        this.updateTimezone();
        return ret;
    }
    
    date.setUTCMilliseconds = function() {
        let ret = Date.prototype.setUTCMilliseconds.apply(this, arguments);
        this.updateTimezone();
        return ret;
    }
    
    date.setUTCMinutes = function() {
        let ret = Date.prototype.setUTCMinutes.apply(this, arguments);
        this.updateTimezone();
        return ret;
    }
    
    date.setUTCMonth = function() {
        let ret = Date.prototype.setUTCMonth.apply(this, arguments);
        this.updateTimezone();
        return ret;
    }
    
    date.setUTCSeconds = function() {
        let ret = Date.prototype.setUTCSeconds.apply(this, arguments);
        this.updateTimezone();
        return ret;
    }
    return date;
}
```
## 代码报错时，会中断当前的调用堆栈(callstack)，比如事件派发器(EventDispatcher)无法完整派发
## 编辑 yarn config
``` bash
# 查看一下当前源
yarn config get registry
# 切换为淘宝源
yarn config set registry https://registry.npm.taobao.org
# 或者切换为自带的
yarn config set registry https://registry.yarnpkg.com
```
## 编辑 npm config
``` bash
npm config delete registry
npm config delete disturl
# 或者 找到淘宝那两行,删除
npm config edit

# 原npm地址
npm config set registry http://registry.npmjs.org 
```
## 生成nodejs可执行文件
``` shell
# 安装指定版本的nodejs 推荐版本，根据 nexe 有编译发行的版本 https://github.com/nexe/nexe/releases?after=v3.3.4
npm i nexe -g
# 生成可执行文件
nexe --resource "./node_modules/**/*" main.js
```
> 当前支持到 [nodejs 14.15.3](https://oss.npmmirror.com/dist/node/v14.15.3/node-v14.15.3-x64.msi)
## 版本号比较
``` javascript
function compareVersion(verA, verB) {
    let versA = verA.split(".");
    let versB = verB.split(".");
    for (let i = 0, len = Math.max(versA.length, versB.length); i < len; ++i) {
        let ret = parseInt(versA[i] || 0) - parseInt(versB[i] || 0);
        if (ret) {
            return ret;
        }
    }
    return 0;
}
```
## 匹配未被引号包围的空格
``` javascript
/ +(?=(?:(?:[^"]*"){2})*[^"]*$)/
```
## 格式化字符串
``` javascript
/**
 * 格式化带管道符的字符串
 * @param {*} value 原始字符串
 * @param {*} args 可供格式化的数据
 * @param {*} funcMap 函数字典
 * @returns 
 */
function format(value, args, funcMap) {
    return value.replace(/\{([\S\s]+?)\}/g, function(s, i) {
        if (i.indexOf("|") == -1) {
            return (args && args[i] == null) ? s : args[i];
        }
        /**
         * 按管道符分割
         * @type {string[]}
         */
        let pipevalues = i.split(/\|(?=(?:(?:[^"]*"){2})*[^"]*$)/); //.map(v => v.trim());
        let fstvalue = pipevalues[0];
        let ret;
        if (args && args[fstvalue] != null) {
            ret = pipevalues.slice(1).reduce((prev,curr)=>{
                /**
                 * 按空格分割
                 * @type {string[]}
                 */
                let argvalues = curr.split(/ +(?=(?:(?:[^"]*"){2})*[^"]*$)/); //.filter(v => v.trim());
                let funcName = argvalues[0];
                if (funcMap && funcMap[funcName]) {
                    argvalues = argvalues.slice(1).map(v=>JSON.parse(v));
                    argvalues.splice(0, 0, prev);
                    return funcMap[funcName].apply(funcMap, argvalues);
                }
                return prev;
            }
            , args[fstvalue]);
        } else {
            ret = s;
        }
        return ret;
    });
}

let funcMap = {
    "+": function(value) {
        let ret = value;
        for (let i = 1, len = arguments.length; i < len; ++i) {
            ret += arguments[i];
        }
        return ret;
    },
    "*": function(value) {
        let ret = value;
        for (let i = 1, len = arguments.length; i < len; ++i) {
            ret *= arguments[i];
        }
        return ret;
    },
    "-": function(value) {
        let ret = value;
        for (let i = 1, len = arguments.length; i < len; ++i) {
            ret -= arguments[i];
        }
        return ret;
    },
    "/": function(value) {
        let ret = value;
        for (let i = 1, len = arguments.length; i < len; ++i) {
            ret /= arguments[i];
        }
        return ret;
    },
    "==": function(value, target, yes = "", no = "") {
        return value == target ? yes : no;
    },
    ">=": function(value, target, yes = "", no = "") {
        return value >= target ? yes : no;
    },
    "<=": function(value, target, yes = "", no = "") {
        return value <= target ? yes : no;
    },
    ">": function(value, target, yes = "", no = "") {
        return value > target ? yes : no;
    },
    "<": function(value, target, yes = "", no = "") {
        return value < target ? yes : no;
    },
    "!=": function(value, target, yes = "", no = "") {
        return value != target ? yes : no;
    },
    "%": function(value, target) {
        return value % target;
    },
    "**": function(value, target) {
        return value ** target;
    },
    "sqrt": function(value) {
        return Math.sqrt(value);
    },
    "fixed": function(value, fractionDigits, digital) {
        if (digital) {
            return +value.toFixed(fractionDigits);
        } else {
            return value.toFixed(fractionDigits);
        }
    },
    "precision": function(value, precision, digital) {
        if (digital) {
            return +value.toPrecision(precision);
        } else {
            return value.toPrecision(precision);
        }
    },
    "abs": function(value) {
        return Math.abs(value);
    },
    "hex": function(value) {
        return value.toString(16);
    },
    "bin": function(value) {
        return value.toString(2);
    },
    "str": function(value, radix) {
        return value.toString(radix);
    },
    "fill": function(value, max, char, right) {
        max = max || 6;
        char = char || "0";
        let ret = value.toString();
        while (ret.length < max) {
            if (right) {
                ret += char;
            } else {
                ret = char + ret;
            }
        }
        return ret;
    },
    "include": function(value, target, yes = "", no = "") {
        return target.indexOf(value) >= 0 ? yes : no;
    },
    "exclude": function(value, target, yes = "", no = "") {
        return target.indexOf(value) == -1 ? yes : no;
    },
};
```
## 覆盖现有函数定义
``` javascript
(function overrideTag(tag, callback) {
    console.log(`overrideTag ${tag}`);
    if (!tag) {
        return;
    }
    let tags = tag.split('.');
    let bak = '.backup.tsubasa';
    let globalTarget;
    if (typeof window !== "undefined") {
        globalTarget = window;
    } else if (typeof global !== "undefined") {
        globalTarget = global;
    }
    let src_parent = globalTarget;
    for (let i = 0, len = tags.length - 1; i < len; ++i) {
        src_parent = src_parent[tags[i]];
    }
    let src_tag = tags[tags.length - 1];
    // 备份
    globalTarget[tag + bak] = globalTarget[tag + bak] || src_parent[src_tag];
    src_parent[src_tag] = function() {
        return callback(tag, globalTarget[tag + bak], src_parent, arguments);
    };
})(
    '',
    function(tag, original, thisArg, args) {
        // console.log.apply(console, Array.prototype.slice.apply(args).concat(tag));
        // 默认行为
        return original.apply(thisArg, args);
    }
)
```
``` javascript
(function overrideTagGetSet(tag) {
    console.log(`overrideTagGetSet ${tag}`);
    if (!tag) {
        return;
    }
    let tags = tag.split('.');
    let bak = '.backup.tsubasa';
    let globalTarget;
    if (typeof window !== "undefined") {
        globalTarget = window;
    } else if (typeof global !== "undefined") {
        globalTarget = global;
    }
    let src_parent = globalTarget;
    for (let i = 0, len = tags.length - 1; i < len; ++i) {
        src_parent = src_parent[tags[i]];
    }
    let src_tag = tags[tags.length - 1];
    // 备份
    let value = src_parent[src_tag];
    Object.defineProperty(src_parent, src_tag, {
        configurable: true,
        get: function() {
            console.log(`Get ${tag}`);
            return value;
        },
        set: function(v) {
            console.log(`Get ${tag} ${v}`);
            v = value;
        },
    });
})(
    ''
)
```
## 忽略新的特性
``` typescript
// @ts-ignore TS2611
```
## 优化JSON显示
``` js
/**
 * JSON字符串化
 * @param {Array|Object} value 对象
 * @param {number} space 空格数，可选。默认值：2
 * @param {string[]?} sortKeys 键排序数据，可选。默认值：null
 */
function stringify(value, space, sortKeys) {
    let result = "";
    space = space === undefined ? 2 : space;
    let depth = arguments[3] === undefined ? 0 : arguments[3];

    /** key: value 是否换行 */
    let wrapKV = false;
    /** 对象属性 是否换行 */
    let wrapAtt = false;
    /** 数组元素 是否换行 */
    let wrapArr = false;
    wrapKV = wrapAtt = wrapArr = space > 0;

    /** 是否可以显示在同一行 */
    let canSameLine = false;
    if (value instanceof Array) {
        canSameLine = value.every(v => typeof v !== "object");
        if (canSameLine) {
            wrapKV = false;
            wrapArr = false;
        }
    } else if (typeof value === "object") {
        canSameLine = true;
        for (let k in value) {
            if (value.hasOwnProperty(k)) {
                if (typeof value[k] === "object") {
                    canSameLine = false;
                    break;
                }
            }
        }
        if (canSameLine) {
            wrapAtt = false;
            wrapKV = false;
        }
    }
    let space1 = "";
    let n = (depth + 1) * space;;
    while (--n >= 0) {
        space1 += " ";
    }
    let space2 = space1.substr(space);

    if (value instanceof Array) {
        result += "[";
        if (wrapKV) {
            result += "\n";
        }
        ++depth;
        if (!wrapArr && wrapKV) {
            result += space1;
        }
        result += value.map(v => {
            return `${wrapArr ? space1 : ""}${stringify(v, space, sortKeys, depth)}`;
        }).join(!wrapArr ? ", " : ",\n");
    } else if (typeof value === "object") {
        result += "{";
        if (wrapKV) {
            result += "\n";
        }
        ++depth;
        let keys = Object.keys(value);
        keys = keys.filter(v => value.hasOwnProperty(v));
        if (sortKeys instanceof Array) {
            let num = sortKeys.length * 2;
            keys.sort((keyA, keyB) => {
                let idxA = sortKeys.indexOf(keyA);
                let idxB = sortKeys.indexOf(keyB);
                idxA = (idxA + num) % num;
                idxB = (idxB + num) % num;
                return idxA - idxB;
            });
        }
        if (!wrapAtt && wrapKV) {
            result += space1;
        }
        result += keys.map(v => {
            return `${wrapAtt ? space1 : ""}"${v}": ${stringify(value[v], space, sortKeys, depth)}`;
        }).join(!wrapAtt ? ", " : ",\n");
    } else if (typeof value === "number" || typeof value === "boolean") {
        result += value;
    } else if (typeof value === "string") {
        result += `"${escapeString(value)}"`;
    }
    if (value instanceof Array) {
        if (wrapKV) {
            result += "\n";
            result += space2;
        }
        result += "]";
    } else if (typeof value === "object") {
        if (wrapKV) {
            result += "\n";
            result += space2;
        }
        result += "}";
    }
    return result;
}

function escapeString(str) {
    return str
        .replace(/\\/g, '\\\\')
        .replace(/\"/g, '\\\"')
        // .replace(/\//g, '\\/')
        // *注: [\b] 是匹配backspace, \b 是匹配边界
        .replace(/[\b]/g, '\\b')
        .replace(/\f/g, '\\f')
        .replace(/\n/g, '\\n')
        .replace(/\r/g, '\\r')
        .replace(/\t/g, '\\t');
};
```
##  格式化
``` js
/** 格式化数值相关 */
function formatByValues(values, fmt, prefix) {
  prefix = prefix || "0000";
  return fmt.replace(/(.)\1{0,}/g, function(matchString, index, self) {
      var key = matchString[0];
      if (key && values.hasOwnProperty(key)) {
          var value = values[key];
          var valueStr = value.toString();
          if (matchString.length == 1) {
              return valueStr;
          } else {
              return (prefix + valueStr).substr(valueStr.length + prefix.length - matchString.length);
          }
      } else {
          return matchString;
      }
  });
}
/**
 * 格式化日期
 * @param {Date} date 
 * @param {string} fmt 
 */
function formatDate(date, fmt) {
    if (fmt == null) {
        fmt = 'YYYY-MM-DD hh:mm:ss SSS';
    }

    var values = {
        "Y": date.getFullYear(),
        "M": date.getMonth() + 1,
        "D": date.getDate(),
        "h": date.getHours(),
        "m": date.getMinutes(),
        "s": date.getSeconds(),
        "S": date.getMilliseconds(),
    };
    return formatByValues(values, fmt);
}

/**
 * 格式化时间戳(毫秒)
 * @param {number} millisec 毫秒
 * @param {string} fmt 
 */
function formatTimestampMillisec(millisec, fmt) {
    /**
     * @type {Date}
     */
    let date = this['__formatDateInMillisec__date'] = this['__formatDateInMillisec__date'] || new Date();
    date.setTime(millisec);
    return formatDate(date);
}

/**
 * 格式化时间戳(秒)
 * @param {number} sec 秒
 * @param {string} fmt 
 */
function formatTimestampSec(sec, fmt) {
    return formatTimestampMillisec(sec * 1000, fmt);
}

/**
 * 格式化时间（秒）
 * @param {number} time 
 * @param {string} fmt 
 */
function formatTimeInSec(time, fmt) {
    if (fmt == null) {
        fmt = 'hh:mm:ss';
    }

    var hours = Math.floor(time / 3600),
        minutes = Math.floor(time / 60) % 60, 
        seconds = Math.floor(time % 60);
    var values = {
        "h": hours,
        "m": minutes,
        "s": seconds,
    };
    return formatByValues(values, fmt);
}

/**
 * 格式化时间（毫秒）
 * @param {number} time 
 * @param {string} fmt 
 */
function formatTimeInMillisec(time, fmt) {
    if (fmt == null) {
        fmt = 'hh:mm:ss SSS';
    }

    var hours = Math.floor(time / 3600000),
        minutes = Math.floor(time / 60000) % 60, 
        seconds = Math.floor(time / 1000) % 60;
        millisec = time % 1000;
    var values = {
        "h": hours,
        "m": minutes,
        "s": seconds,
        "S": millisec,
    };
    return formatByValues(values, fmt);
}
```
##  错误统计示例
``` js
var logTS = Date.now();
var logT = 0;
var errorC = {};
var hgCheckLogT = (function (desc) {
    if (errorC[desc]) {
        return false;
    }
    if (logT + 1 > 60) {
        if (Date.now() - logTS < 3600000) {
            return false;
        }
        else {
            logTS = Date.now();
            logT = 1;
        }
    }
    else {
        ++logT;
    }
    errorC[desc] = true;
    return true;
});
var HGError = (function (e) {
    var desc = gGameInfo.chn + " " + gGameInfo.ver + " ";
    if (!e.filename) {
        desc += "index Obj " + e + " msg: " + e.message + " stack: " + (e.error ? e.error.stack : "null");
        for (var key in e) {
            var val = e[key];
            if (key != "timeStamp" && (typeof val != "function")) {
                if (desc) {
                    desc += " ";
                }
                if (key == "srcElement" && val["src"]) {
                    desc += key + ": " + val + "(" + encodeURIComponent(val["src"]) + ")";
                }
                else {
                    desc += key + ": " + encodeURIComponent(val);
                }
            }
        }
    }
    else {
        desc += "index filename: " + encodeURIComponent(e.filename) + " msg: " + e.message + " stack: " + (e.error ? e.error.stack : "null");
    }
    if (!hgCheckLogT(desc)) {
        return;
    }
    hgHttpPost("https://webxcx.hotgamehl.com/log/log.php", "data=" + desc + "&flag=dldlIndex", null);
});
var erralert = 0;
window.onerror = function (msg, url, line, column, detail) {
    if (gGameInfo.chn) {
        if (erralert++ < 5 && detail)
            alert("出错了，请把此信息截图给开发者\n" + msg + "\n" + detail.stack);
    }
};
window.addEventListener('error', (function (e) {
    if (!e)
        return;
    if (gGameInfo.chn) {
        HGError(e);
    }
    else {
        alert(e && e.message);
    }
    e.preventDefault();
}), true);
```
##  vbs使用参数启动node服务器
``` bash
set ws = createobject("wscript.shell")
ws.Run "node E:\\tsubasa\\node_http_server\\app.js E:\\projects\\DLDL_SVN", 0
```
> Array.prototype.sort 在不同环境（如 Safari, Chrome 等）下的实现方式不同，执行过程中有可能会对原始数组进行修改，因此排序结束前，不应该去访问或者修改原始数组。
##  typescript编译
``` bash
tsc -b .\tsconfigDebug1.json
tsc -w
```
##  正则表达式之名称捕获
``` js
"tsubasa-v100".replace(/(?<name>^.+)-(?<version>v.+$)/g, function (...a) {
    console.log(a)
});
(6) ["tsubasa-v100", "tsubasa", "v100", 0, "tsubasa-v100", {…}]
    0: "tsubasa-v100"
    1: "tsubasa"
    2: "v100"
    3: 0 // 此处为匹配的位置
    4: "tsubasa-v100"
   5: {name: "tsubasa", version: "v100"}
length: 6
__proto__: Array(0)
```
##  Typescript 文件监听 watch 版本
> 高级的版本，会提示错误
```
lib.es2015.promise.d.ts:129:21 - error TS2304: Cannot find name 'Iterable'.
```
> 解决方案一：
也可以在`tsconfig.json`的`compilerOptions`的`lib`中添加：
`"es2015.iterable"`

> 解决方案二:
安装旧版本`typescript`
``` bash
npm install -g typescript@3.4.3
```
##  敏感词管理器
[SensitiveWordMgr.ts](../assets/attachments/SensitiveWordMgr.ts)

##  修改npm源
``` bash
npm config set registry https://registry.npm.taobao.org
```
##  修改yarn镜像地址
``` bash
yarn config set registry https://registry.npm.taobao.org -g
```
##  类定义接口
``` ts
interface IAnyClass<T> {
    new (...args: any[]): T;
}

// 使用举例：
/**
 *
 * @constructor
 */
New<T>(cls: IAnyClass<T>): T;

// 实例化 1
function Ins<T>(cls: {new (...args: any[]): T;}): T {
    return new cls();
}
// 实例化 2
function Ins<T>(cls: IAnyClass<T>): T {
    return new cls();
}
```
##  Native调用（decodeURIComponent）
[NativeBridgeProxy.js](../assets/attachments/NativeBridgeProxy.js)