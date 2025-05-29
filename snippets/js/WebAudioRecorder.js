export const WebAudioRecorderState = {
    Idle: "Idle",
    Requesting: "Requesting",
    Recording: "Recording",
    Stopped: "Stopped",
    Error: "Error",
}

export const CreateRecorder = (options, OnStateChange) => {
    const { desiredSampleRate = 16000, desiredNumberOfChannels = 1, desiredBitsPerSample = 16 } = options || {};
    let mediaStream;
    let audioContext;
    let audioBuffer = [];
    let state = WebAudioRecorderState.Idle;
    let wavBlob;
    let beginTime;
    let endTime;
    const SetState = (newState, msg) => {
        if (state == newState) return;
        state = newState;
        OnStateChange?.(state, msg);
    }
    const Start = () => {
        SetState(WebAudioRecorderState.Requesting)
        // 获取麦克风访问权限
        navigator.mediaDevices.getUserMedia({ audio: true })
            .then(stream => {
                mediaStream = stream;
                // 创建音频上下文
                audioContext = new (window.AudioContext || window.webkitAudioContext)({
                    sampleRate: desiredSampleRate
                });

                // 创建媒体流源节点
                const source = audioContext.createMediaStreamSource(mediaStream);

                // 创建脚本处理节点
                const processor = audioContext.createScriptProcessor(4096, 1, 1);

                // 处理音频数据
                processor.onaudioprocess = (e) => {
                    // 获取单声道数据
                    const channelData = e.inputBuffer.getChannelData(0);

                    // 将Float32转换为Int16
                    const buffer = new Int16Array(channelData.length);
                    for (let i = 0; i < channelData.length; i++) {
                        buffer[i] = convertFloat32ToInt16(channelData[i]);
                    }

                    audioBuffer.push(buffer);
                };

                beginTime = Date.now();
                // 连接节点
                source.connect(processor);
                processor.connect(audioContext.destination);
                audioBuffer = [];
                SetState(WebAudioRecorderState.Recording);
            })
            .catch(error => {
                SetState(WebAudioRecorderState.Error, error.message)
            })
    }
    const Stop = () => {
        // 关闭流
        mediaStream.getTracks().forEach(track => track.stop());

        // 合并音频缓冲区
        const mergedBuffer = mergeBuffers(audioBuffer);

        // 创建WAV文件
        wavBlob = createWavBlob(mergedBuffer, desiredSampleRate, desiredNumberOfChannels, desiredBitsPerSample);
        endTime = Date.now();
        SetState(WebAudioRecorderState.Stopped);
    }

    const GetData = () => {
        return {
            wavBlob,
            duration: endTime - beginTime,
        }
    }

    return {
        Start,
        Stop,
        GetData,
    }
}

// 辅助函数: 将Float32转换为Int16
function convertFloat32ToInt16(buffer) {
    let s = Math.max(-1, Math.min(1, buffer));
    return s < 0 ? s * 0x8000 : s * 0x7FFF;
}

// 辅助函数: 合并多个ArrayBuffer
function mergeBuffers(buffers) {
    let totalLength = 0;
    buffers.forEach(buffer => {
        totalLength += buffer.length;
    });

    const result = new Int16Array(totalLength);
    let offset = 0;
    buffers.forEach(buffer => {
        result.set(buffer, offset);
        offset += buffer.length;
    });

    return result;
}

// 辅助函数: 创建WAV Blob
function createWavBlob(buffer, desiredSampleRate, desiredNumberOfChannels, desiredBitsPerSample) {
    const dataLength = buffer.length * 2; // 每个样本2字节

    // 创建WAV头部
    const header = new ArrayBuffer(44);
    const view = new DataView(header);

    // RIFF标识
    writeString(view, 0, 'RIFF');
    // 文件长度 (数据长度 + 36)
    view.setUint32(4, 36 + dataLength, true);
    // WAVE标识
    writeString(view, 8, 'WAVE');
    // fmt子块
    writeString(view, 12, 'fmt ');
    // fmt块长度 (16)
    view.setUint32(16, 16, true);
    // 格式类型 (1 = PCM)
    view.setUint16(20, 1, true);
    // 声道数
    view.setUint16(22, desiredNumberOfChannels, true);
    // 采样率
    view.setUint32(24, desiredSampleRate, true);
    // 字节率 (采样率 * 块对齐)
    view.setUint32(28, desiredSampleRate * desiredNumberOfChannels * desiredBitsPerSample / 8, true);
    // 块对齐 (声道数 * 位深/8)
    view.setUint16(32, desiredNumberOfChannels * desiredBitsPerSample / 8, true);
    // 位深
    view.setUint16(34, desiredBitsPerSample, true);
    // data子块
    writeString(view, 36, 'data');
    // data块长度
    view.setUint32(40, dataLength, true);

    // 合并头部和PCM数据
    const wavBuffer = new Uint8Array(44 + buffer.length * 2);
    wavBuffer.set(new Uint8Array(header), 0);

    // 将Int16Array转换为Uint8Array并添加到WAV缓冲区
    const dataBytes = new Uint8Array(buffer.buffer);
    wavBuffer.set(dataBytes, 44);

    return new Blob([wavBuffer], { type: 'audio/wav' });
}

// 辅助函数: 写入字符串到DataView
function writeString(view, offset, string) {
    for (let i = 0; i < string.length; i++) {
        view.setUint8(offset + i, string.charCodeAt(i));
    }
}