package cn.city.in.api.tools.common;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.map.LRUMap;

/**
 * @author 字符串相关操作工具类
 */
public class StringTool extends TimeTool {

	protected static Map<String, Pattern> pattenCache = Collections
			.synchronizedMap(new LRUMap(10000));
	
	public static final char[] chars = "0123456789ABCDEF".toCharArray();

	/**
	 * 在字符串头部添加分隔符并合并字符串.
	 * 
	 * @param skipFirst
	 *            是否跳过第一个头
	 * @param spil
	 *            the spil
	 * @param str
	 *            the str
	 * @return the string
	 * @author 黄林
	 */
	public static String appendHeadSpil(boolean skipFirst, String spil,
			String... str) {
		StringBuffer result = new StringBuffer();
		for (String string : str) {
			if ("".equals(string)) {
				continue;
			}
			if (skipFirst) {
				skipFirst = false;
			} else {
				result.append(spil);
			}
			result.append(string);
		}
		return result.toString();
	}

	/**
	 * 在字符串头部添加分隔符并合并字符串,不忽略第一项
	 * 
	 * @param spil
	 *            the spil
	 * @param str
	 *            the str
	 * @return the string
	 */
	public static String appendHeadSpil(String spil, String... str) {
		return appendHeadSpil(false, spil, str);
	}

	/**
	 * 驼峰转下划线
	 * 
	 * @param str
	 *            the str
	 * @return the string
	 */
	public static String camelToUnderline(String str) {
		Pattern p = Pattern.compile("[A-Z]");
		if (str == null || str.equals("")) {
			return "";
		}
		StringBuilder resultSb = new StringBuilder(str);
		Matcher m = p.matcher(str);
		int i = 0;
		while (m.find()) {
			resultSb.replace(m.start() + i, m.end() + i, "_"
					+ m.group().toLowerCase());
			i++;
		}

		if ('_' == resultSb.charAt(0)) {
			resultSb.deleteCharAt(0);
		}
		return resultSb.toString();
	}

	public static Object[] checkNull(Object... objs) {
		ArrayList<Object> noNullStrList = new ArrayList<Object>();
		for (Object obj : objs) {
			if (isNotNull(obj)) {
				noNullStrList.add(obj);
			}
		}
		if (noNullStrList.size() == 0) {
			return new Object[0];
		}
		return noNullStrList.toArray(new Object[0]);
	}

	/**
	 * 功能:检查为空的字符串 创建者： 黄林 2012-2-24.
	 * 
	 * @param strs
	 *            the strs
	 * @return 非空空结果集
	 */
	public static String[] checkNull(String... strs) {
		ArrayList<String> noNullStrList = new ArrayList<String>();
		for (String string : strs) {
			if (isNotNull(string)) {
				noNullStrList.add(string);
			}
		}
		if (noNullStrList.size() == 0) {
			return new String[0];
		}
		return noNullStrList.toArray(new String[noNullStrList.size()]);
	}

	/**
	 * 功能:检查地点名称长度 创建者： 黄林 2011-7-4.
	 * 
	 * @param Placename
	 *            the placename
	 * @return true,
	 */
	public static boolean checkPlaceNameLength(String Placename) {
		if (Placename.length() < 1 || Placename.length() > 15) {
			return false;
		}
		return true;
	}

	/**
	 * 功能:检查用户名 创建者： 黄林 2011-7-4.
	 * 
	 * @param userName
	 *            the user name
	 * @return string
	 */
	public static String checkUserName(String userName) {
		String regex = "([a-z]|[A-Z]|[0-9]|[\\_\\-\\.]|[\\u4e00-\\u9fa5])+";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(userName);
		if (!m.matches()) {
			userName = userName.replaceAll(regex, "");
			return userName;
		}
		return null;
	}

	/**
	 * 功能:检查用户名长度 创建者： 黄林 2011-7-4.
	 * 
	 * @param userName
	 *            the user name
	 * @return true,
	 */
	public static boolean checkUserNameLength(String userName) {
		if (userName.length() < 1 || userName.length() > 15) {
			return false;
		}
		return true;
	}

	/**
	 * 计算字符串src中包含多少个字符c
	 * 
	 * @param src
	 * @param c
	 * @return
	 */
	public static int countChar(String src, char c) {
		if (isNull(src)) {
			return 0;
		}

		int k = 0;
		for (int i = 0; i < src.length(); i++) {
			if (src.charAt(i) == c) {
				k++;
			}
		}

		return k;
	}

	/**
	 * 截断指定长度的字符串
	 * 
	 * @param input
	 *            the input
	 * @return the sumury
	 */
	public static String cutSumury(String input, Integer len) {
		if (input.length() > len) {
			return input.substring(0, len) + "...";
		} else {
			return input;
		}
	}

	/**
	 * URL解码.
	 * 
	 * @param str
	 *            the str
	 * @return string
	 */
	public static String decodeURL(String str) {
		try {
			if (str != null) {
				str = URLDecoder.decode(str, "utf-8");
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
		return str;

	}

	/**
	 * 功能:URL编码 创建者： 黄林 2011-7-4.
	 * 
	 * @param value
	 *            the value
	 * @return string 编码后字符串
	 */
	public static String encodeURL(String value) {
		String encoded = null;
		try {
			encoded = URLEncoder.encode(value, "UTF-8");
		} catch (UnsupportedEncodingException ignore) {
		}
		encoded = encoded == null ? "" : encoded;
		StringBuffer buf = new StringBuffer(encoded.length());
		char focus;
		for (int i = 0; i < encoded.length(); i++) {
			focus = encoded.charAt(i);
			if (focus == '*') {
				buf.append("%2A");
			} else if (focus == '+') {
				buf.append("%20");
			} else if (focus == '%' && (i + 1) < encoded.length()
					&& encoded.charAt(i + 1) == '7'
					&& encoded.charAt(i + 2) == 'E') {
				buf.append('~');
				i += 2;
			} else {
				buf.append(focus);
			}
		}
		return buf.toString();
	}

	/**
	 * 功能:格式化日期 创建者： 黄林 2011-7-4.
	 * 
	 * @param date
	 *            the date
	 * @return string yyyy-MM-dd
	 */
	public static String formateDate(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return sdf.format(date);
	}

	/**
	 * 功能:格式化日期 创建者： 黄林 2011-7-4.
	 * 
	 * @param date
	 *            the date
	 * @return string MM/dd
	 */
	public static String formateDateWithSlash(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd");
		return sdf.format(date);
	}

	/**
	 * 功能:格式化日期 创建者： 黄林 2011-7-4.
	 * 
	 * @param date
	 *            the date
	 * @return string yyyy-MM-dd hh:mm:ss
	 */
	public static String formateDateWithStamp(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		return sdf.format(date);
	}

	/**
	 * 获取字符串数组以","拆分
	 * 
	 * @param str
	 *            the str
	 * @return the array
	 */
	public static String[] getArray(String str) {
		return str.split(",");
	}

	/**
	 * 获取表达式的内容
	 * 
	 * @param context
	 *            内容
	 * @param separate
	 *            表达式分隔
	 * @return the expr
	 * @author 黄林
	 */
	public static String getExpr(String context, String separate) {
		return getExpr(context, separate, separate, null);
	}

	/**
	 * 获取表达式的内容.
	 * 
	 * @param context
	 *            内容
	 * @param startseparate
	 *            表达式分隔开始
	 * @param endSeparate
	 *            表达式分隔结束
	 * @param includeKey
	 *            表达式关键字
	 * @return the expr
	 * @author 黄林
	 */
	public static String getExpr(String context, String startseparate,
			String endSeparate, String includeKey) {
		if (isNull(context) || context.indexOf(startseparate) == -1) {
			return null;
		}
		int start = context.indexOf(startseparate);
		int end = context.indexOf(endSeparate, start + 1);
		String result = context.substring(start, end + 1);
		while (null != includeKey && result.indexOf(includeKey) == -1) {
			start = context.indexOf(startseparate, end + 1);
			if (start == -1) {
				return null;
			}
			end = context.indexOf(endSeparate, start + 1);
			if (end == -1) {
				return null;
			}
			result = context.substring(start, end + 1);
		}
		return result;

	}

	/**
	 * 获取当前时间的间隔
	 * 
	 * @param date
	 *            the date
	 * @return the friendly timestamp
	 */
	public static String getFriendlyTimestamp(Date date) {
		long time = date.getTime();
		String ret = "不知道什么时候";
		long a = new Date().getTime();
		long dis = a - time;
		if (dis < 60000) {
			ret = dis / 1000 + "秒前";
		} else if (dis < (60000 * 60)) {
			ret = dis / 60000 + "分钟前";
		} else if (dis < (60000 * 60 * 24)) {
			ret = dis / (60000 * 60) + "小时前";
		} else {
			ret = formateDate(date);
		}
		return ret;
	}

	/**
	 * 生成luhn码
	 */
	public static char getLuhnCode(String inStr) throws Exception {
		if (inStr == null || inStr.trim().length() == 0
				|| !inStr.matches("\\d+")) {
			throw new IllegalArgumentException("must be number!");
		}
		char[] chs = inStr.trim().toCharArray();
		int luhmSum = 0;
		for (int i = chs.length - 1, j = 0; i >= 0; i--, j++) {
			int k = chs[i] - '0';
			if (j % 2 == 0) {
				k *= 2;
				k = k / 10 + k % 10;
			}
			luhmSum += k;
		}
		return (luhmSum % 10 == 0) ? '0' : (char) ((10 - luhmSum % 10) + '0');
	}

	public static int getNumberForString(String s) {
		return "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
				.indexOf(s);
	}

	/**
	 * 从字符串a-Z中取出顺序对应的字符
	 * 
	 * @param n
	 *            the n
	 * @return the string for number
	 * @author 黄林
	 */
	public static String getStringForNumber(int n) {
		String s = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
		return s.substring(n, n + 1);
	}

	/**
	 * 获取明天.
	 * 
	 * @return the tomorrow
	 */
	@SuppressWarnings("static-access")
	public static String getTomorrow() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Calendar calendar = Calendar.getInstance();
		calendar.roll(calendar.DAY_OF_YEAR, +1);
		return sdf.format(calendar.getTime()) + " 00:00:00";
	}

	/**
	 * 获取指定日期当年的最后一天
	 * 
	 * @param date
	 *            the date
	 * @return the last day
	 */
	@SuppressWarnings("static-access")
	public static String getYearLastDay(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.roll(calendar.DAY_OF_YEAR, -1);
		return sdf.format(calendar.getTime());
	}

	/**
	 * 获取昨天.
	 * 
	 * @return the yesterday
	 */
	@SuppressWarnings("static-access")
	public static String getYesterday() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Calendar calendar = Calendar.getInstance();
		calendar.roll(calendar.DAY_OF_YEAR, -1);
		return sdf.format(calendar.getTime()) + " 23:59:59";

	}

	/**
	 * 功能:判断是否为空字符串,可以传任意个参数 创建者： 黄林 2012-2-23.
	 * 
	 * @param strs
	 *            参数列表
	 * @return true, if is null
	 */
	public static boolean hasNull(String... strs) {
		if (null == strs || strs.length == 0) {
			return true;
		}
		for (String string : strs) {
			if (isNull(string)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 功能:判断一个字符串是否在一个字符串列表中 创建者： 黄林 2012-1-16.
	 * 
	 * @param src
	 *            the src
	 * @param lists
	 *            the lists
	 * @return true,
	 */
	public static boolean in(String src, String... lists) {
		for (String string : lists) {
			if (string.equals(src)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 功能:判断字符串是否是一个数组 (','拆分) 创建者： 黄林 2011-10-14.
	 * 
	 * @param str
	 *            the str
	 * @return true, if is array
	 */
	public static boolean isArray(String str) {
		if ((isNotNull(str)) && str.indexOf(",") != -1) {
			return true;
		}
		return false;
	}

	/**
	 * 功能:检查是否合法邮箱 创建者： 黄林 2011-7-4.
	 * 
	 * @param email
	 *            the email
	 * @return true, if is name adress format
	 */
	public static boolean isNameAdressFormat(String email) {
		boolean isExist = false;
		if (email.length() > 50) {
			return false;
		}
		Pattern p = Pattern.compile("(\\w+.)+@(\\w+.)+[a-z]{2,3}");
		Matcher m = p.matcher(email);
		boolean b = m.matches();
		if (b) {
			isExist = true;
		} else {
		}
		return isExist;
	}

	public static boolean isNotNull(List list) {
		return !isNull(list);
	}

	public static boolean isNotNull(Number obj) {
		return !isNull(obj);
	}

	public static boolean isNotNull(Object obj) {
		return !isNull(obj);
	}

	/**
	 * 检查对象是否不为空
	 * 
	 * @param array
	 *            the array
	 * @return true, if is not null
	 */
	public static boolean isNotNull(Object[] array) {
		return !isNull(array);
	}

	/**
	 * 功能:判断是否为非空字符串 创建者： 黄林 2011-7-4.
	 * 
	 * @param str
	 *            the str
	 * @return true, if is not null
	 */
	public static boolean isNotNull(String str) {
		return !isNull(str);
	}

	public static boolean isNotValid(String str) {
		return !isValid(str);
	}

	/**
	 * 检查对象是否为空
	 * 
	 * @param obj
	 * @return
	 */
	public static boolean isNull(List list) {
		if (list == null || list.size() == 0) {
			return true;
		}
		return false;
	}

	/**
	 * 检查对象是否为空
	 * 
	 * @param obj
	 * @return
	 */
	public static boolean isNull(Number obj) {
		if (obj == null) {
			return true;
		}
		return false;
	}

	/**
	 * 检查对象是否为空
	 * 
	 * @param obj
	 * @return
	 */
	public static boolean isNull(Object obj) {
		if (obj == null) {
			return true;
		}
		if (obj instanceof String) {
			return isNull((String) obj);
		} else if (obj instanceof Number) {
			return isNull((Number) obj);
		} else if (obj instanceof List) {
			return isNull((List) obj);
		} else if (obj instanceof Map) {
			return isNull((Map) obj);
		} else if (obj instanceof Object[]) {
			return isNull((Object[]) obj);
		}
		return false;
	}

	/**
	 * 检查对象是否为空
	 * 
	 * @param obj
	 * @return
	 */
	public static boolean isNull(Object... array) {
		return array == null || array.length == 0;
	}
	/**
	 * 检查对象是否为空
	 * 
	 * @param obj
	 * @return
	 */
	public static boolean isNull(Map map) {
		if (map == null || map.size() == 0) {
			return true;
		}
		return false;
	}

	/**
	 * 功能:判断是否为空字符串 创建者： 黄林 2011-7-4.
	 * 
	 * @param str
	 *            the str
	 * @return true, if is null
	 */
	public static boolean isNull(String str) {
		if (null == str || str.trim().equals("")) {
			return true;
		}
		return false;
	}

	/**
	 * 判断字符串数组是否有空值
	 * 
	 * @param ss
	 * @return
	 */
	public static boolean isNullOrNone(String... ss) {
		if (ss == null || ss.length == 0) {
			return true;
		}

		for (int i = 0; i < ss.length; i++) {
			if (ss[i] == null || "".equals(ss[i].trim())) {
				return true;
			}
		}

		return false;
	}

	/**
	 * 判断字符串是否为数字
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isNumber(String str) {
		if (null == str || "".equals(str)) {
			return false;
		} else {
			return Pattern.matches("^[0-9]+$", str);
		}
	}

	/**
	 * 检查是否为数字或字母
	 * 
	 * @param s
	 * @return
	 */
	public static boolean isNumOrLetter(String s)

	{
		if (null == s || "".equals(s)) {
			return false;
		} else {
			return Pattern.matches("^[A-Za-z0-9]+$", s);
		}
	}

	/**
	 * 检查是否为手机号码
	 * 
	 * @param phone
	 * @return
	 */
	public static boolean isPhoneNum(String phone) {
		if (null == phone || "".equals(phone)) {
			return false;
		} else {
			return Pattern.matches("^(1(([358][0-9])|(47)|[8][01236789]))\\d{8}$", phone);
		}

	}

	/**
	 * 检查非法字符
	 * 
	 * @param s
	 * @return
	 */
	public static boolean isUnlawfulChar(String s) {
		if (null == s || "".equals(s)) {
			return false;
		} else {
			return Pattern
					.matches(
							"^[^`~@#\\$%\\^&\\*\\(\\)=\\!\\+\\\\/\\|<>\\?;\\:\\.'\"\\{\\}\\[\\]??, ]*$",
							s);
		}
	}

	/**
	 * 检查字符串是否合法
	 * 
	 * @param str
	 *            the str
	 * @return true, if is valid
	 */
	public static boolean isValid(String str) {
		return str.matches("\\w+");
	}

	/**
	 * 获得字符串的实际（byte）长度
	 * 
	 * @param s
	 * @return
	 */
	public static int lengthByte(String s) {
		int length = 0;
		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) <= 127) {
				length++;
			} else {
				length = length + 2;
			}
		}
		return length;
	}

	/**
	 * 功能:返回列表中匹配的字符串，匹配正则表达式,转换规则特殊字符，忽略大小写 创建者： 黄林 2012-2-28.
	 * 
	 * @param source
	 *            the source
	 * @param rule
	 *            the rule
	 * @return true,
	 * @see #matche(String, String, boolean, boolean)
	 */
	public static boolean matche(String source, String rule) {
		return matche(source, rule, true, true);
	}

	/**
	 * 功能:返回列表中匹配的字符串，匹配正则表达式 创建者： 黄林 2012-2-28.
	 * 
	 * @param source
	 *            被测试的样本
	 * @param rule
	 *            测试规则
	 * @param igonCase
	 *            是否忽略大小写
	 * @param changeRule
	 *            是否替规则中的换特殊字符
	 * @return true,
	 */
	public static boolean matche(String source, String rule, boolean igonCase,
			boolean changeRule) {
		String realRule = rule;
		if (isNull(realRule)) {
			return false;
		}
		if (igonCase) {
			realRule = realRule.trim().toLowerCase();
		}
		if (changeRule) {
			realRule = realRule.replace("\\", "\\\\");
			realRule = realRule.replace(".", "\\.");
			realRule = realRule.replace("*", ".*");
		}
		Pattern p = null;
		if (pattenCache.containsKey(realRule)) {
			p = pattenCache.get(realRule);
		} else {
			p = Pattern.compile(realRule);
			pattenCache.put(realRule, p);
		}
		Matcher m = null;
		if (igonCase) {
			m = p.matcher(source.trim().toLowerCase());
		} else {
			m = p.matcher(source);
		}
		return m.matches();
	}

	/**
	 * 功能:返回列表中匹配的字符串，匹配正则表达式，忽略大小写,转换规则 创建者： 黄林 2011-10-14.
	 * 
	 * @param sources
	 *            the sources
	 * @param rule
	 *            the rule
	 * @return string[]
	 */
	public static String[] matches(String[] sources, String rule) {
		return matches(sources, rule, true, true);
	}

	/**
	 * 功能:返回列表中匹配的字符串，匹配正则表达式 创建者： 黄林 2011-10-14.
	 * 
	 * @param sources
	 *            the sources
	 * @param rule
	 *            the rule
	 * @param igonCase
	 *            忽略大小写
	 * @param changeRule
	 *            是否转换规则
	 * @return string[]
	 */
	public static String[] matches(String[] sources, String rule,
			boolean igonCase, boolean changeRule) {
		String realRule = rule;
		if (isNull(realRule)) {
			return sources;
		}
		if (igonCase) {
			realRule = realRule.trim().toLowerCase();
		}
		if (changeRule) {
			realRule = realRule.replace("\\", "\\\\");
			realRule = realRule.replace(".", "\\.");
			realRule = realRule.replace("*", ".*");
		}
		ArrayList<String> result = new ArrayList<String>();
		Pattern p = null;
		if (pattenCache.containsKey(realRule)) {
			p = pattenCache.get(realRule);
		} else {
			p = Pattern.compile(realRule);
			pattenCache.put(realRule, p);
		}
		for (String string : sources) {
			Matcher m = null;
			if (igonCase) {
				m = p.matcher(string.trim().toLowerCase());
			} else {
				m = p.matcher(string);
			}
			if (m.matches()) {
				result.add(string);
			}
		}
		return result.toArray(new String[0]);
	}

	/**
	 * md5加密.
	 * 
	 * @param str
	 *            the str
	 * @return string
	 * @throws Exception
	 *             the exception
	 */
	public static String md5(String str) throws Exception {
		MessageDigest md5 = MessageDigest.getInstance("MD5");
		StringBuilder sb = new StringBuilder();
		for (byte b : md5.digest(str.getBytes("utf-8"))) {
			sb.append(str2HexStr(b));
		}
		return sb.toString();
	}
	
	/**
	 * Str to hex str.
	 *
	 * @param b the b
	 * @return the string
	 * @author 黄林
	 */
	public static String str2HexStr(byte b)  
	    {   
	        char[] r=new char[2];
	        int bit = (b & 0x0f0) >> 4;
	        r[0]=chars[bit]; 
	        bit = b & 0x0f;
	        r[1]=chars[bit];
	        String str=new String(r);
	        return str;    
	    } 

	/**
	 * 数字补零大法 inNum传入的数字 length需要补足的位数
	 * 
	 * @author Johnny Yang
	 * @return
	 */
	public static String padingZero(int inNum, int length) {
		String numStr = inNum + "";
		int lenDef = length - numStr.length();
		String reStr = "";
		for (int i = 0; i < lenDef; i++) {
			reStr += "0";
		}
		return reStr + inNum;
	}

	/**
	 * 获得字符串的（byte）字符数组的长度
	 * 
	 * @param str
	 * @return
	 */
	public static int realLength(String str) {
		if (null == str || "".equals(str)) {
			return 0;
		} else {
			return str.getBytes().length;
		}
	}

	/** 首字符大写 */
	public static String upFrist(String str) {
		String frist = str.substring(0, 1);
		frist = frist.toUpperCase();
		return frist + str.substring(1);
	}

}
