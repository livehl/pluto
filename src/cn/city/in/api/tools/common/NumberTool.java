package cn.city.in.api.tools.common;

import java.text.ParseException;
import java.util.Random;

public class NumberTool {

	/**
	 * 将字符串转为数字数组，以,分割
	 * 
	 * @param str
	 *            the str
	 * @return the integer[]
	 * @author 黄林
	 */
	public static Integer[] arrayValueOfString(String str) {
		return arrayValueOfString(str, ",");
	}

	/**
	 * 将字符串转为数字数组
	 * 
	 * @param str
	 *            the str
	 * @param split
	 *            分隔符
	 * @return the integer[]
	 * @author 黄林
	 */
	public static Integer[] arrayValueOfString(String str, String split) {
		try {
			String strArray[] = str.split(split);
			Integer[] array = new Integer[strArray.length];
			for (int i = 0; i < strArray.length; i++) {
				array[i] = Integer.valueOf(strArray[i]);
			}
			return array;
		} catch (Exception e) {
			return new Integer[0];
		}
	}

	/**
	 * 版本比较
	 * 
	 * @param versiona
	 *            the versiona
	 * @param versionb
	 *            the versionb
	 * @return the int
	 * @author 黄林
	 */
	public static int compare(String s1, String s2) {
		if (s1 == null && s2 == null)
			return 0;
		else if (s1 == null)
			return -1;
		else if (s2 == null)
			return 1;

		String[] arr1 = s1.split("[^a-zA-Z0-9]+"), arr2 = s2
				.split("[^a-zA-Z0-9]+");

		int i1, i2, i3;
		for (int ii = 0, max = Math.min(arr1.length, arr2.length); ii <= max; ii++) {
			if (ii == arr1.length)
				return ii == arr2.length ? 0 : -1;
			else if (ii == arr2.length)
				return 1;

			try {
				i1 = Integer.parseInt(arr1[ii]);
			} catch (Exception x) {
				i1 = Integer.MAX_VALUE;
			}

			try {
				i2 = Integer.parseInt(arr2[ii]);
			} catch (Exception x) {
				i2 = Integer.MAX_VALUE;
			}

			if (i1 != i2) {
				return i1 - i2;
			}

			i3 = arr1[ii].compareTo(arr2[ii]);

			if (i3 != 0)
				return i3;
		}

		return 0;
	}

	/**
	 * 生成随机数 比传入的参数小
	 * 
	 * @author Johnny Yang
	 * @return
	 */
	public static int generateRandomInt(Integer in) {
		if (in == null) {
			in = 99999;
		}
		Random random = new Random();
		int x = random.nextInt(in);
		return x;
	}

	/**
	 * 功能:判断一个数字是否在数字列表中 创建者： 黄林 2012-1-18.
	 * 
	 * @param src
	 *            the src
	 * @param list
	 *            the list
	 * @return true,
	 */
	public static boolean in(Number src, Number... list) {
		for (Number number : list) {
			if (number.equals(src)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isNotValidNumber(String str) {
		return !isValidNumber(str);
	}

	/**
	 * 功能:验证数字是否合法,大于0 创建者： 黄林 2011-9-27.
	 * 
	 * @param number
	 *            the number
	 * @return true, if is valid
	 */
	public static boolean isValid(Number number) {
		return null != number && number.doubleValue() > 0;
	}
	public static boolean isNotValid(Number number) {
		return !isValid(number);
	}
	
	/**
	 * 限制数字避免越界
	 *
	 * @param number the number
	 * @param max the max
	 * @param min the min
	 * @return the int
	 * @author 黄林
	 */
	public static int limit(int number,Integer max,Integer min)
	{
		if (null!=max) {
			number=number>max?max:number;
		}
		if (null!=min){
			number=number<min?min:number;
		}
		return number;
	}

	/**
	 * 验证是否合法数字字符串
	 * 
	 * @param str
	 *            the str
	 * @return true, if is valid number
	 */
	public static boolean isValidNumber(String str) {
		try {
			Double value = Double.valueOf(str);
			return isValid(value);
		} catch (Exception e) {
			return false;
		}
	}

	public static void main(String[] args) {
		System.out.println(compare("2.0.0", "1.2.0"));
	}

	/**
	 * 功能:以起始数字和结尾数字返回自增1的数组 创建者： 黄林 2012-1-29.
	 * 
	 * @param start
	 *            数组起始
	 * @param end
	 *            数组结尾
	 * @return int[] 数组
	 */
	public static Integer[] numtoArray(int start, int end)
			throws ParseException {
		if (end - start < 0) {
			throw new ParseException("start less than end", 0);
		}
		Integer[] arrays = new Integer[end - start + 1];
		for (int i = 0; i <= end - start; i++) {
			arrays[i] = i + start;
		}
		return arrays;
	}

	/**
	 * 返回带指定长度的数字，不足补零
	 * 
	 * @param i
	 *            the i
	 * @param len
	 *            the len
	 * @return the string
	 * @author 黄林
	 */
	public static String numtoStringWithZero(int i, int len) {
		int t = 1;
		for (int j = 1; j < len; j++) {
			t = t * 10;
		}
		String r = i + "";
		while (i / t == 0) {
			r = "0" + r;
			t = t / 10;
		}
		return r;

	}

	/**
	 * 功能:从字符串中获取数字,支持乘法 创建者： 黄林 2011-8-24.
	 * 
	 * @param number
	 *            the number
	 * @return double
	 */
	public static Double valueOf(String number) {
		Double doubleNumber = new Double(1);
		if (number.indexOf("*") != -1) {// 含有乘法
			String[] strs = number.split("\\*");
			for (int i = 0; i < strs.length; i++) {
				doubleNumber = doubleNumber * Double.valueOf(strs[i]);
			}
		} else {
			doubleNumber = Double.valueOf(number);
		}
		return doubleNumber;
	}
}
