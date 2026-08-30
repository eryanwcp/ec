/**
 * RSA 加解密工具类 (基于 JSEncrypt)
 */
window.RSAUtils = {
    /**
     * 公钥加密 (对应后端的公钥加密/前端请求密钥加密)
     * @param {string} plainText 待加密明文
     * @param {string} publicKey Base64 格式的公钥
     * @returns {string|boolean} Base64 加密字符串
     */
    encrypt(plainText, publicKey) {
        if (!plainText || !publicKey) return plainText;
        try {
            // 直接使用页面先引入的全局 JSEncrypt 对象
            const encryptor = new JSEncrypt();
            encryptor.setPublicKey(publicKey);
            return encryptor.encrypt(plainText);
        } catch (e) {
            console.error('RSA Encryption Error:', e);
            return false;
        }
    },

    /**
     * 私钥解密 (前端需要私钥解密的场景)
     * @param {string} cipherText Base64 密文
     * @param {string} privateKey Base64 格式的私钥
     * @returns {string|boolean} 解密后的明文
     */
    decrypt(cipherText, privateKey) {
        if (!cipherText || !privateKey) return cipherText;
        try {
            const decryptor = new JSEncrypt();
            decryptor.setPrivateKey(privateKey);
            return decryptor.decrypt(cipherText);
        } catch (e) {
            console.error('RSA Decryption Error:', e);
            return false;
        }
    }
};

/**
 * 国密 SM4 加解密工具类 (基于全局 sm4)
 */
window.Sm4Utils = {
    getZeroIV() {
        return [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
    },
    encrypt(hexKey, paramStr) {
        if (!hexKey || !paramStr) return paramStr;
        try {
            return sm4.encrypt(paramStr, hexKey, { mode: 'cbc', iv: this.getZeroIV() });
        } catch (e) {
            console.error('SM4 Encryption Error:', e);
            return paramStr;
        }
    },
    decrypt(hexKey, text) {
        if (!hexKey || !text) return text;
        try {
            return sm4.decrypt(text, hexKey, { mode: 'cbc', iv: this.getZeroIV() });
        } catch (e) {
            console.error('SM4 Decryption Error:', e);
            return text;
        }
    }
};

/**
 * AES 加解密工具类 (基于全局 CryptoJS)
 */
window.Cryptos = {
    aesECBEncrypt(input, base64Key) {
        if (!input || !base64Key) return input;
        try {
            const key = CryptoJS.enc.Base64.parse(base64Key);
            const encrypted = CryptoJS.AES.encrypt(input, key, { mode: CryptoJS.mode.ECB, padding: CryptoJS.pad.Pkcs7 });
            return encrypted.toString();
        } catch (e) {
            console.error('AES Encryption Error:', e);
            return input;
        }
    },
    aesECBDecrypt(base64Data, base64Key) {
        if (!base64Data || !base64Key) return base64Data;
        try {
            const key = CryptoJS.enc.Base64.parse(base64Key);
            const decrypted = CryptoJS.AES.decrypt(base64Data, key, { mode: CryptoJS.mode.ECB, padding: CryptoJS.pad.Pkcs7 });
            return decrypted.toString(CryptoJS.enc.Utf8);
        } catch (e) {
            console.error('AES Decryption Error:', e);
            return base64Data;
        }
    }
};