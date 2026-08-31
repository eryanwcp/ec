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
    },
    /**
     * 公钥加密，返回 Base64 格式密文
     * @param {string} data 待加密明文
     * @param {string} [base64PublicKey] 公钥 (可选，不传使用默认公钥)
     */
    encryptBase64String(data, base64PublicKey) {
        return this.encrypt(data, base64PublicKey);
    },

    /**
     * 私钥解密 Base64 格式密文
     * @param {string} base64Data Base64 密文
     * @param {string} [base64PrivateKey] 私钥 (可选，不传使用默认私钥)
     */
    decryptBase64String(base64Data, base64PrivateKey) {
        return this.decrypt(base64Data, base64PrivateKey);
    },

    /**
     * 公钥加密，返回 Hex（十六进制）格式密文
     * @param {string} plainText 待加密明文
     * @param {string} publicKey Base64 格式的公钥
     * @returns {string|boolean} Hex 加密字符串
     */
    encryptHexString(plainText, publicKey) {
        // 先调用基础 encrypt 方法获取 Base64 格式的密文
        const base64Cipher = this.encrypt(plainText, publicKey);
        if (!base64Cipher) return base64Cipher;

        try {
            // 借助 CryptoJS 将 Base64 转为 Hex
            const wordArray = CryptoJS.enc.Base64.parse(base64Cipher);
            return CryptoJS.enc.Hex.stringify(wordArray);
        } catch (e) {
            console.error('RSA Hex Encryption Error:', e);
            return false;
        }
    },

    /**
     * 私钥解密 Hex（十六进制）格式密文
     * @param {string} hexCipherText Hex 密文
     * @param {string} privateKey Base64 格式的私钥
     * @returns {string|boolean} 解密后的明文
     */
    decryptHexString(hexCipherText, privateKey) {
        if (!hexCipherText || !privateKey) return hexCipherText;

        try {
            // 借助 CryptoJS 将 Hex 转为 Base64，以适应 JSEncrypt
            const wordArray = CryptoJS.enc.Hex.parse(hexCipherText);
            const base64Cipher = CryptoJS.enc.Base64.stringify(wordArray);

            // 调用基础 decrypt 方法进行解密
            return this.decrypt(base64Cipher, privateKey);
        } catch (e) {
            console.error('RSA Hex Decryption Error:', e);
            return false;
        }
    }
};

/**
 * 国密 SM4 加解密工具类 (基于全局 sm4)
 */
window.Sm4Utils = {
    /**
     * 生成 SM4 Hex 编码密钥 (16 字节 / 128 位，输出 32 位 Hex 字符串)
     * 对应 Java: Sm4Utils.generateHexKeyString()
     */
    generateSm4HexKey() {
        return CryptoJS.lib.WordArray.random(16).toString(CryptoJS.enc.Hex);
    },
    getZeroIV() {
        return [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
    },
    encrypt(paramStr,hexKey) {
        if (!hexKey || !paramStr) return paramStr;
        try {
            return sm4.encrypt(paramStr, hexKey, { mode: 'cbc', iv: this.getZeroIV() });
        } catch (e) {
            console.error('SM4 Encryption Error:', e);
            return paramStr;
        }
    },
    decrypt(text,hexKey) {
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
    /**
     * 生成 AES Base64 编码密钥 (默认 16 字节 / 128 位)
     * 对应 Java: Cryptos.getBase64EncodeKey()
     */
    generateAesBase64Key(keyByteSize = 16) {
        return CryptoJS.lib.WordArray.random(keyByteSize).toString(CryptoJS.enc.Base64);
    },
    encrypt(input, base64Key) {
        return this.aesECBEncrypt(input, base64Key);
    },
    decrypt(base64Data, base64Key) {
        return this.aesECBDecrypt(base64Data, base64Key);
    },
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