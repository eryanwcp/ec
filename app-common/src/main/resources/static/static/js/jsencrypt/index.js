// esbuild index.js --bundle --minify --outfile=encrypt.all.min.js
import CryptoJS from './crypto-js.min.js';
import JSEncrypt from './jsencrypt.min.js';
import sm4 from './sm4.js';
import './base64.js';
import './encrypt.min.js';

// 显式挂载到全局 window 对象
window.CryptoJS = CryptoJS;
window.JSEncrypt = JSEncrypt;
window.sm4 = sm4;