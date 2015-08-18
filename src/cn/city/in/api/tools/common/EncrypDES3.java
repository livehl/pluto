package cn.city.in.api.tools.common;

import java.security.InvalidKeyException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;

import org.bouncycastle.util.encoders.Base64;

public class EncrypDES3 {
	private static final String transformation = "DESede";
	// Cipher负责完成加密或解密工作
	private static Cipher c = getCipher();

	public static String createKey() throws Exception {
		KeyGenerator kg = KeyGenerator.getInstance(transformation);
		kg.init(168);
		SecretKey key = kg.generateKey();
		byte[] byteKey = key.getEncoded();
		return new String(Base64.encode(byteKey));
	}

	/**
	 * 对字符串解密
	 * 
	 * @param buff
	 * @return
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public static byte[] Decryptor(byte[] buff, SecretKey key)
			throws InvalidKeyException, IllegalBlockSizeException,
			BadPaddingException {
		// 根据密钥，对Cipher对象进行初始化，DECRYPT_MODE表示解密模式
		c.init(Cipher.DECRYPT_MODE, key);
		byte[] cipherByte = c.doFinal(buff);
		return cipherByte;
	}

	/**
	 * 对字符串解密
	 * 
	 * @param buff
	 *            the buff
	 * @param key
	 *            the key
	 * @return the byte[]
	 * @throws Exception
	 *             the exception
	 * @author 黄林
	 */
	public static byte[] Decryptor(byte[] buff, String key) throws Exception {
		byte[] keyBytes = Base64.decode(key.getBytes());
		DESedeKeySpec dks = new DESedeKeySpec(keyBytes);
		SecretKeyFactory keyFactory = SecretKeyFactory
				.getInstance(transformation);
		SecretKey secretKey = keyFactory.generateSecret(dks);
		return Decryptor(buff, secretKey);
	}
	// public static void main(String...args)throws Exception
	// {
	// System.out.println(createKey());
	// String key="NOUWbbNtAR8Vm0nmGVK1v9yRN5QVrhAv";
	// System.out.println("密钥:");
	// System.out.println(key);
	// String value="http://192.168.1.40/private_api/oauth/bind";
	// System.out.println(value);
	// byte[] data=Encrytor(value, key);
	// System.out.println(new String(Base64.encode(data)));
	// data=Base64.decode("459tNeTVd+gRCjjb+OAG6JDrA8NP8dJO+3xTln789J8ctw+MKqwCxyY+MXmCMsGqEhq4i9XA3LYb2C+E0TfA0g==");
	// System.out.println(new String(Base64.encode(data)));
	// String out=new String (Decryptor(data, key));
	// System.out.println(out);
	//
	// }

	/**
	 * 对字符串加密
	 * 
	 * @param str
	 * @return
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public static byte[] Encrytor(String str, SecretKey key)
			throws InvalidKeyException, IllegalBlockSizeException,
			BadPaddingException {
		// 根据密钥，对Cipher对象进行初始化，ENCRYPT_MODE表示加密模式
		c.init(Cipher.ENCRYPT_MODE, key);
		byte[] src = str.getBytes();
		byte[] cipherByte = c.doFinal(src);
		return cipherByte;
	}

	/**
	 * 对字符串加密
	 * 
	 * @param str
	 *            the str
	 * @param key
	 *            the key
	 * @return the byte[]
	 * @throws Exception
	 *             the exception
	 * @author 黄林
	 */
	public static byte[] Encrytor(String str, String key) throws Exception {
		byte[] keyBytes = Base64.decode(key.getBytes());
		DESedeKeySpec dks = new DESedeKeySpec(keyBytes);
		SecretKeyFactory keyFactory = SecretKeyFactory
				.getInstance(transformation);
		SecretKey secretKey = keyFactory.generateSecret(dks);
		// SecretKey deskey = new SecretKeySpec(str.getBytes(), transformation);
		return Encrytor(str, secretKey);
	}

	private static Cipher getCipher() {
		try {
			return Cipher.getInstance(transformation);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
