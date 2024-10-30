package com.eryansky.common.utils.encode;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.AlgorithmParameters;
import java.security.Key;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Arrays;

import static java.util.Objects.isNull;

/**
 * Sm4 国密算法
 * 支持 ECB和CBC两种模式
 *
 */
public final class Sm4Utils {
 

 
    private static final String ENCODING = "UTF-8";
    public static final String ALGORITHM_NAME = "SM4";
    // 加密算法/分组加密模式/分组填充方式
    // PKCS5Padding-以8个字节为一组进行分组加密
    // 定义分组加密模式使用：PKCS5Padding
    public static final String ALGORITHM_NAME_ECB_PADDING = "SM4/ECB/PKCS5Padding";
    // 加密算法/分组加密模式/分组填充方式
    // PKCS5Padding-以8个字节为一组进行分组加密
    // 定义分组加密模式使用：PKCS5Padding
    public static final String ALGORITHM_NAME_CBC_PADDING = "SM4/CBC/PKCS5Padding";

    // 128-32位16进制；256-64位16进制
    public static final int DEFAULT_KEY_SIZE = 128;

    private static final BouncyCastleProvider PROVIDER = new BouncyCastleProvider();

    static {
        if (isNull(Security.getProvider(BouncyCastleProvider.PROVIDER_NAME))) {
            Security.addProvider(PROVIDER);
        }
    }

    /**
     * 自动生成密钥
     *
     * @return
     * @explain
     */
    public static byte[] generateKey() throws Exception {
        return generateKey(DEFAULT_KEY_SIZE);
    }


    /**
     * 自动生成密钥
     * @return
     * @throws Exception
     */
    public static String generateHexKeyString() throws Exception {
        return Hex.toHexString(generateKey());
    }

    /**
     * @param keySize
     * @return
     * @throws Exception
     * @explain
     */
    public static byte[] generateKey(int keySize) throws Exception {
        KeyGenerator kg = KeyGenerator.getInstance(ALGORITHM_NAME, BouncyCastleProvider.PROVIDER_NAME);
        kg.init(keySize, new SecureRandom());
        return kg.generateKey().getEncoded();
    }


    /**
     * 生成ECB暗号
     *
     * @param algorithmName 算法名称
     * @param mode          模式
     * @param key
     * @return
     * @throws Exception
     * @explain ECB模式（电子密码本模式：Electronic codebook）
     */
    private static Cipher generateEcbCipher(String algorithmName, int mode, byte[] key) throws Exception {
        Cipher cipher = Cipher.getInstance(algorithmName, BouncyCastleProvider.PROVIDER_NAME);
        Key sm4Key = new SecretKeySpec(key, ALGORITHM_NAME);
        cipher.init(mode, sm4Key);
        return cipher;
    }
 
    /**
     * sm4加密
     *
     * @param hexKey   16进制密钥（忽略大小写）
     * @param paramStr 待加密字符串
     * @return 返回16进制的加密字符串
     * @explain 加密模式：ECB
     * 密文长度不固定，会随着被加密字符串长度的变化而变化
     */
    public static String encryptEcb(String hexKey, String paramStr) {
        try {
            // 16进制字符串-->byte[]
            byte[] keyData = Hex.decode(hexKey);
            // String-->byte[]
            byte[] srcData = paramStr.getBytes(ENCODING);
            // 加密后的数组
            byte[] cipherArray = encryptEcbPadding(keyData, srcData);
            // byte[]-->hexString
            String cipherText = Hex.toHexString(cipherArray);
            return cipherText;
        } catch (Exception e) {
            return paramStr;
        }
    }
 
    /**
     * 加密模式之Ecb
     *
     * @param key
     * @param data
     * @return
     * @throws Exception
     * @explain
     */
    public static byte[] encryptEcbPadding(byte[] key, byte[] data) throws Exception {
        Cipher cipher = generateEcbCipher(ALGORITHM_NAME_ECB_PADDING, Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(data);
    }
 
    /**
     * sm4解密
     *
     * @param hexKey     16进制密钥
     * @param cipherText 16进制的加密字符串（忽略大小写）
     * @return 解密后的字符串
     * @throws Exception
     * @explain 解密模式：采用ECB
     */
    public static String decryptEcb(String hexKey, String cipherText) {
        // 用于接收解密后的字符串
        String decryptStr = "";
        // hexString-->byte[]
        byte[] keyData = Hex.decode(hexKey);
        // hexString-->byte[]
        byte[] cipherData = Hex.decode(cipherText);
        // 解密
        byte[] srcData = new byte[0];
        try {
            srcData = decryptEcbPadding(keyData, cipherData);
            // byte[]-->String
            decryptStr = new String(srcData, ENCODING);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return decryptStr;
    }
 
    /**
     * 解密
     *
     * @param key
     * @param cipherText
     * @return
     * @throws Exception
     * @explain
     */
    public static byte[] decryptEcbPadding(byte[] key, byte[] cipherText) throws Exception {
        Cipher cipher = generateEcbCipher(ALGORITHM_NAME_ECB_PADDING, Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(cipherText);
    }
 
    /**
     * 校验加密前后的字符串是否为同一数据
     *
     * @param hexKey     16进制密钥（忽略大小写）
     * @param cipherText 16进制加密后的字符串
     * @param paramStr   加密前的字符串
     * @return 是否为同一数据
     * @throws Exception
     * @explain
     */
    public static boolean verifyEcb(String hexKey, String cipherText, String paramStr) throws Exception {
        // 用于接收校验结果
        boolean flag = false;
        // hexString-->byte[]
        byte[] keyData = Hex.decode(hexKey);
        // 将16进制字符串转换成数组
        byte[] cipherData = Hex.decode(cipherText);
        // 解密
        byte[] decryptData = decryptEcbPadding(keyData, cipherData);
        // 将原字符串转换成byte[]
        byte[] srcData = paramStr.getBytes(ENCODING);
        // 判断2个数组是否一致
        flag = Arrays.equals(decryptData, srcData);
        return flag;
    }

//    CBC模式
    /**
     * sm4加密(CBC)
     *
     * @param hexKey   16进制密钥（忽略大小写）
     * @param paramStr 待加密字符串
     * @return 返回16进制的加密字符串
     * @throws Exception
     * @explain 加密模式：CBC
     */
    public static String encrypt(String hexKey, String paramStr) throws Exception {
        String result = "";
        // 16进制字符串-->byte[]
        byte[] keyData = Hex.decode(hexKey);
        // String-->byte[]
        byte[] srcData = paramStr.getBytes(ENCODING);
        // 加密后的数组
        byte[] cipherArray = encryptCbcPadding(keyData, srcData);

        // byte[]-->hexString
        result = Hex.toHexString(cipherArray);
        return result;
    }

    /**
     * 加密模式之CBC
     *
     * @param key
     * @param data
     * @return
     * @throws Exception
     * @explain
     */
    public static byte[] encryptCbcPadding(byte[] key, byte[] data) throws Exception {
        Cipher cipher = generateCbcCipher(ALGORITHM_NAME_CBC_PADDING, Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(data);
    }

    /**
     * 加密模式之CBC
     * @param algorithmName
     * @param mode
     * @param key
     * @return
     * @throws Exception
     */
    private static Cipher generateCbcCipher(String algorithmName, int mode, byte[] key) throws Exception {
        Cipher cipher = Cipher.getInstance(algorithmName, BouncyCastleProvider.PROVIDER_NAME);
        Key sm4Key = new SecretKeySpec(key, ALGORITHM_NAME);
        cipher.init(mode, sm4Key, generateIV());
        return cipher;
    }

    /**
     * 生成iv
     * @return
     * @throws Exception
     */
    public static AlgorithmParameters generateIV() throws Exception {
        //iv 为一个 16 字节的数组，这里采用和 iOS 端一样的构造方法，数据全为0
        byte[] iv = new byte[16];
        Arrays.fill(iv, (byte) 0x00);
        AlgorithmParameters params = AlgorithmParameters.getInstance(ALGORITHM_NAME);
        params.init(new IvParameterSpec(iv));
        return params;
    }

    /**
     * sm4解密(CBC)
     *
     * @param hexKey 16进制密钥
     * @param text   16进制的加密字符串（忽略大小写）
     * @return 解密后的字符串
     * @throws Exception
     * @explain 解密模式：采用CBC
     */
    public static String decrypt(String hexKey, String text) throws Exception {
        // 用于接收解密后的字符串
        String result = "";
        // hexString-->byte[]
        byte[] keyData = Hex.decode(hexKey);
        // hexString-->byte[]
        byte[] resultData = Hex.decode(text);
        // 解密
        byte[] srcData = decryptCbcPadding(keyData, resultData);
        // byte[]-->String
        result = new String(srcData, ENCODING);
        return result;
    }

    /**
     * 解密
     *
     * @param key
     * @param cipherText
     * @return
     * @throws Exception
     * @explain
     */
    public static byte[] decryptCbcPadding(byte[] key, byte[] cipherText) throws Exception {
        Cipher cipher = generateCbcCipher(ALGORITHM_NAME_CBC_PADDING, Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(cipherText);
    }


    public static void main(String[] args) {
        try {
            String paramStr = "11a87789527470081563a709175ec839ce0bc6060bad733dbe03b10e7eed86b88f5b1588b20b10c1b48266dd9b10a5eb4c6048313640d0bab690d874a8db3047635def28cab94510e086872786c5ee053413cad58c2bdf69babc50e20a63bf90280b313eadff245ed017c79a8cef325b91168ecb5e075384a351c1f5bcd55c40e93f96b7fbde58ff063f927048655f315256e6e6e4390a629c848ae7d4503d10a4fcdd0f96d9a4cd32ddd67ee4d5fd2f42e7d4ea835a4abce7d6288938e23077b70407dc8f9529652e482636347296cb92d4182d91044430b7486df0dc46d7bcc6b61bfc561d072c2b47f1045263a0bcc057c7847f5c19924c7c03c2e938666a218345f6f0944306a5aab1eb04d85f7b459d8382fc0eecebca82186ed6e6d3f37d78858f552921456e2dc78c1a5a5bf5292580c049716f8fdb363d595afb50d833319de2319a7004bb16cee9172d8daa2552bfa3c7b0b87df326897372820488951b3dc77dd2aad085e48c23ec95fbb2c708b9d9cf61817adaea87c7f0d45756d3ddda66e5390c9ca2a7144cf021c9eec46e964e99235d6512fbebd303fb72f79b55f1a49fee1a7253fbf5b03e391ed453ce25d5088dc1d12a50b9df836dd95a8c16ead62d385b71396419500b6c225a1f52dbfa2e3b7eb4e79f488b044315de42f2d169d4047fba4e3bbd2ceb52ad63bc40022b329b83224a1dbd6b4c1b60dc85da675d06a151f8dff6edd74a83afaa19392a7d89c1f1a456229747b8efe82c9b9dcb3bc11eaa31950cf26a77f8f4abe85277883de78c706bd8fdda7c66f6b7901c5c031fc2f6d04e601e5684a13979d2f6978eecf3188359b0ff54864e24f09a3975d5cc46bf8d414adbefd95b9016a9376ff4fab09e7847239621fc0bd6f0023d2bc7369fd7af11925a605929a1769ceacec8a1b576f5e2fb4fd13e9af2f82ea49f35e90f66dc5d32c8ba4e594334955c44a7b6f5f38a1bdbe10f1c787f932c89f7010d0bc222c1b3b64dcb51f0fcc94c617c0e4d5d7fa306bc545e032c5b5dd18a84606452cdcf5d3c795dd3755bb5f62c012f0b52d40eda2dc21b15120e8762d26a8e030a24fe0afc4798e23b6112bb4b0a0f5f696ced552c0be50ef928baa0782e50381bf5b9f18a78d47936160a2dbf0b090905c62487815a60f787a3d2e0188587f77e0a4370767019e50d693b0cca9c874f974233ee9b7f024953adec70c2cd9063047e3dd588ae4c9be2ac5684fbd81cace503baf5ae65a7f2de88ece9429dbc91cd4ef4564c73073dbcf44fc733c7c7c0c8f88e6d1e0eeffe2c65e238d4c917deb9a16ba0b9d59b324e6e0083351b464ac16e74d26e54ff13e8f545b0a41bfb963f53ca91ca662a4140141237c34cbf16b1478934da678e27b849010b7301220eefa688135b162cd2529646e8a78118fe7ec90232c954a23b33a6c64c1d087137eabf06b41948460271e981cd1b5ec33cb5815029a7767cb19e5d49f8d0f7ac994feb5e96efc0d1979ab18c50589f4694532d011f75097bff17ab2cbb41efec31639237043e8fe6a6ed054e378c217c42902e146da972245a0a01acc5d0ec061364f086477333fc2743ec6e3c2d885ff4807e47b75874bd8676da79124280a7d6876bd9fd47f7e4d1ce3bcdbbdc8c93412e1b0b13d06cabc43e653fb3626ae717ec1124d725485d006bc0d51e152717e8235dc60f496352f1bd28426ec213b413df446f72563af572dfc5d80111fc31b68bae36920be14ab888f5ebfdde256245a05df32566a1092c51c40d3218b8ddb4f91898774a1fe53d1181";
            String eKey = "66c11520443b21735753f4ba122d946b23fccbae74dd871a0d5908546e80f209ad662a65a6147ac36ab924bef776e52aadd1bee2005bb7d436b2e15d5db7c9d7";
            String key = RSAUtils.decryptHexString(eKey);
            System.out.println(key);
            System.out.println(decrypt(key,paramStr));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}