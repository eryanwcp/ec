import JSEncrypt from 'jsencrypt'
import {b64tohex, hex2b64} from "jsencrypt/lib/lib/jsbn/base64";

//以下代码测试过 结合后端 实现加解密 没什么问题 引入外部三个包
//安装gm-crypt 出现问题的时候 如果是webpack xxx 控制台执行下面这行
//npm install node-polyfill-webpack-plugin

/**
 * @email 尔演@Eryan0130@163.com
 * @description aes 加解密器
 */
//引入 crypto-js 该包 解决aes加解密
//npm install crypto-js
//https://github.com/brix/crypto-js
import CryptoJS from "crypto-js";
//npm install jsencrypt
//https://github.com/travist/jsencrypt
const jsEncrypt = new JSEncrypt()

/**
 * @description sm4 加解密器
 * @type {SM4}
 */
//npm install gm-crypt
//https://github.com/pecliu/gm-crypt
const SM4 = require("gm-crypt").sm4;

/**
 * @email 尔演@Eryan0130@163.com
 * @description rsa 加解密器
 */
export class RsaHandler{
    publicKey
    privateKey
    /**
     * 初始化 rsa处理实例
     * @param publicKey
     * @param privateKey
     */

    constructor(publicKey,privateKey) {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    setPublicKey = publicKey => {
        this.publicKey = publicKey
    }

    setPrivateKey  = privateKey => {
        this.privateKey = privateKey;
    }
    /**
     * @description rsa加密
     * @author 尔演@Eryan
     * @param content
     * @returns {string}
     */
     encrypt = content => {
        if (content === null || content === ''){
            console.error('待加密数据是空的')
            return content
        }
         jsEncrypt.setPublicKey(this.publicKey)
        return b64tohex(jsEncrypt.encrypt(content))
    }

    /**
     * @description rsa解密
     * @author 尔演@Eryan
     * @param hexStr
     * @returns {*}
     */
     decrypt = hexStr => {
        if (hexStr === null || hexStr === ''){
            console.error('待解密数据是空的')
            return hexStr
        }
         jsEncrypt.setPrivateKey(this.privateKey)
        return jsEncrypt.decrypt(hex2b64(hexStr))
    }
}

export class AesHandler{
    aes_key
    aes_iv

    /**
     * 初始化 aes处理实例
     * @param aesKey
     * @param aesIv
     */
    constructor(aesKey , aesIv) {
        this.aes_key = CryptoJS.enc.Utf8.parse(aesKey)
        this.aes_iv = CryptoJS.enc.Utf8.parse(aesIv)
    }

    setKey = aesKey => {
        this.aes_key = CryptoJS.enc.Utf8.parse(aesKey)
    }
    setIv = aesIv => {
        this.aes_iv = CryptoJS.enc.Utf8.parse(aesIv)
    }
    /**
     * aes加密
     * @param content
     * @returns {WordArray|*}
     */
    encrypt = content => {
        if (content === null || content === ''){
            console.error('待加密数据是空的');
        }
        let srcs = CryptoJS.enc.Utf8.parse(content);
        let encrypted = CryptoJS.AES.encrypt(srcs, this.aes_key, {
                iv: this.aes_iv,
                mode: CryptoJS.mode.CBC,
                padding: CryptoJS.pad.Pkcs7
        });
        // CryptoJS.enc.Utf16
        let base64Str = CryptoJS.enc.Base64.stringify(encrypted.ciphertext);
        return b64tohex(base64Str);
    }
    /**
     *
     * @param hexStr
     * @returns {*}
     */
    decrypt = hexStr => {
        if (hexStr === null || hexStr === ''){
            console.error('待解密数据是空的');
            return hexStr
        }
        let encryptedHexStr = CryptoJS.enc.Hex.parse(hexStr)
        let base64Data = CryptoJS.enc.Base64.stringify(encryptedHexStr)
        let decrypt = CryptoJS.AES.decrypt(base64Data, this.aes_key, {
            iv: this.aes_iv,
            mode: CryptoJS.mode.CBC,
            padding: CryptoJS.pad.Pkcs7
        })
        return decrypt.toString(CryptoJS.enc.Utf8)
    }
}

/**
 * SM4过密加密
 * @type {SM4}
 */

export class SM4Handler{

    SM4Instance

    /**
     * 初始化 不提供set key iv方法
     * @param sm4Key
     * @param sm4Iv
     */
    constructor(sm4Key, sm4Iv) {
        //初始化sm4 加密器
        this.SM4Instance = new SM4({
            key: sm4Key,
            iv: sm4Iv,
            mode: 'cbc',
            cipherType: 'base64'
        })
    }

    /**
     * 后端返回的是 16进制数据
     * @description  加密function
     * @param content 待加密内容
     */
    encrypt = content => {
        if (content === null || content === ''){
            console.error('待加密数据是空的');
            return content
        }
        let encryptBase64Str = this.SM4Instance.encrypt(content);
        return b64tohex(encryptBase64Str)
    }

    decrypt = hexStr => {
        if (hexStr === null || hexStr === ''){
            console.error('待解密数据是空的');
            return hexStr
        }
        return this.SM4Instance.decrypt(hex2b64(hexStr))
    }
}