var NativeBridgeProxy = window.NativeBridgeProxy = (function(){
    function NativeBridgeProxy() {
        /** @type {ListenerInfo[]} */
        this.listeners = [];  
        this.callbackMap = {};
    }
    /** 
     * 发送消息到Native
     */
    NativeBridgeProxy.prototype.send = function(key, data, callback, isCallback) {
        this.callbackMap[key] = callback;
        var obj = {
            key: key,
            type: isCallback ? InvokeType.Callback : InvokeType.Normal,
            data: data
        };
        this.sendToNative(obj);
    };
    /** 
     * 接收来自Native的消息
     */
    NativeBridgeProxy.prototype.recv = function(key, data, isCallback) {
        this.resolve(key, data);
        
        if (isCallback) {
            var callback = this.callbackMap[key];
            callback && callback(data);
        } else {
            NotifyMgr.send(key, data);
        }
    };
    /**
     * 监听来自Native的消息
     */
    NativeBridgeProxy.prototype.on = function(key, callback, thisArg) {
        for (var i = this.listeners.length - 1; i >= 0; --i) {
            var listenerInfo = this.listeners[i];
            if (listenerInfo.key == key && listenerInfo.callback == callback && listenerInfo.thisArg == thisArg) {
                return;
            }
        }
        this.listeners.push(new ListenerInfo(key, callback, thisArg));
    };
    /**
     * 取消监听来自Native的消息
     */
    NativeBridgeProxy.prototype.off = function(key, callback, thisArg) {
        for (var i = this.listeners.length - 1; i >= 0; --i) {
            var listenerInfo = this.listeners[i];
            if ((listenerInfo.key == key || !key)
                 && (listenerInfo.callback == callback || !callback)
                 && (listenerInfo.thisArg == thisArg || !thisArg)
                ) {
                this.listeners.splice(i, 1);
            }
        }
    };

    NativeBridgeProxy.prototype.resolve = function(key, data) {
        for (var i = this.listeners.length - 1; i >= 0; --i) {
            var listenerInfo = this.listeners[i];
            listenerInfo.resolve(key, data);
        }
    };
    NativeBridgeProxy.prototype.sendToNative = function(obj) {
        var rawString = JSON.stringify(obj);
        var encodedRawString = encodeURIComponent(rawString);
        
        var deviceType = GameData.DeviceProxy.deviceType;
        GameTool.warn("[JS => Native] " + JSON.stringify(rawString), deviceType);
        try {
            if (deviceType & DeviceType.Browser) {
                if (deviceType & DeviceType.Android) {
                    JSBridge.recvFromJS(encodedRawString);
                } else if (deviceType & DeviceType.IOS) {
                    window.webkit.messageHandlers.JSBridge.postMessage(encodedRawString);
                }
            } else if (deviceType & DeviceType.Native) {
                if (deviceType & DeviceType.Android) {
                    // JSBridge.recvFromJS(encodedRawString);
                    jsb.reflection.callStaticMethod("org/cocos2dx/javascript/JSBridge", "recvFromJS", "(Ljava/lang/String;)V", encodedRawString);
                } else if (deviceType & DeviceType.IOS) {
                    // window.webkit.messageHandlers.JSBridge.postMessage(encodedRawString);
                    jsb.reflection.callStaticMethod("JSBridge", "recvFromJS:", encodedRawString);
                }
            }
        } catch (error) {
            GameTool.warn("JSBridge isn't ready!", error);
        }
    };
    NativeBridgeProxy.prototype.recvFromNative = function(rawString) {
        var decodedRawString = decodeURIComponent(rawString);
        var rawData = JSON.parse(decodedRawString);
        var key = rawData.key;
        var type = rawData.type;
        var data = rawData.data;
        GameTool.warn("[Native => JS] " + decodedRawString);
        this.recv(key, data, type == InvokeType.Callback);
    };

        

    return NativeBridgeProxy;
})();

var InvokeType = {
    Normal: "normal",
    Callback: "callback",
};