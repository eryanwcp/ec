package com.eryansky.fastweixin.message.aes;

@SuppressWarnings("serial")
public class AesException extends Exception {

    public final static int OK = 0;
    public final static int ValidateSignatureError = -40001;
    public final static int PARSE_JSON_ERROR = -40002;
    public final static int ComputeSignatureError = -40003;
    public final static int IllegalAesKey = -40004;
    public final static int ValidateCorpidError = -40005;
    public final static int EncryptAESError = -40006;
    public final static int DecryptAESError = -40007;
    public final static int IllegalBuffer = -40008;
    public final static int ENCODE_BASE64_ERROR = -40009;
    public final static int DECODE_BASE64_ERROR = -40010;
    public final static int GenReturnJsonError = -40011;
    public final static int ParseXmlError = -400021;

    private int code;

    private static String getMessage(int code) {
        switch (code) {
            case ValidateSignatureError:
                return "签名验证错误";
            case PARSE_JSON_ERROR:
                return "json解析失败";
            case ParseXmlError:
                return "xml解析失败";
            case ComputeSignatureError:
                return "sha加密生成签名失败";
            case IllegalAesKey:
                return "SymmetricKey非法";
            case ValidateCorpidError:
                return "corpid校验失败";
            case EncryptAESError:
                return "aes加密失败";
            case DecryptAESError:
                return "aes解密失败";
            case IllegalBuffer:
                return "解密后得到的buffer非法";
            case ENCODE_BASE64_ERROR:
                return "base64加密错误";
            case DECODE_BASE64_ERROR:
                return "base64解密错误";
            case GenReturnJsonError:
                return "josn生成失败";
            default:
                return null; // cannot be
        }
    }

    public int getCode() {
        return code;
    }

    AesException(int code) {
        super(getMessage(code));
        this.code = code;
    }

}
