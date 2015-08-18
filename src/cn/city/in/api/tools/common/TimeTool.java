package cn.city.in.api.tools.common;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 功能:时间操作工具类
 * 
 * @author 黄林 2012-1-29
 * @version
 */
public class TimeTool extends NumberTool {

	private static Log log = LogFactory.getLog(TimeTool.class);
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 5589769629239551202L;

	/** The time mark map. */
	private static SoftHashMap<Integer, Long> timeMarkMap = new SoftHashMap <Integer, Long>();

	/** The last add. */
	private static AtomicInteger lastAdd = new AtomicInteger(0);

	/**
	 * 功能:添加时间标记 创建者： 黄林 2011-10-18.
	 * 
	 * @return integer
	 */
	public static Integer addTimeMark() {
		int add = lastAdd.addAndGet(1);
		synchronized (timeMarkMap) {
			timeMarkMap.put(add, System.nanoTime());
		}
		return add;
	}

	/**
	 * Description: 比较两个日期的大小 <br>
	 * Implement: <br>
	 * 1、… <br>
	 * 2、… <br>
	 * [参数列表，说明每个参数用途].
	 * 
	 * @param dateString1
	 *            the date string1
	 * @param dateString2
	 *            the date string2
	 * @return int 两日期相等返回0,dateString1早于dateString2返回小于0的值,
	 *         dateString1晚于dateString2返回大于0的值
	 * @exception/throws [违例类型] [违例说明]
	 * @see [类、类#方法、类#成员]
	 */
	public static int compareToShortDate(String dateString1, String dateString2) {
		Date date1 = getDateByFormatDateStr(dateString1);
		Date date2 = getDateByFormatDateStr(dateString2);
		return date1.compareTo(date2);
	}

	/**
	 * 比较两个日期的天差距 date1 > date2
	 * 
	 * @param field
	 *            the field
	 * @param date1
	 *            the date1
	 * @param date2
	 *            the date2
	 * @return the int
	 * @author 黄林
	 */
	public static int compareWithDate(Date date1, Date date2) {
		Calendar cal1=Calendar.getInstance();
		cal1.setTime(date1);
		Calendar cal2=Calendar.getInstance();
		cal2.setTime(date2);
		int diff=cal1.get(Calendar.YEAR)-cal2.get(Calendar.YEAR);
		if (diff!=0){
			long nd = 1000 * 24 * 60 * 60;// 一天的毫秒数
			long daydiff = (date1.getTime() / nd) - (date2.getTime() / nd);
			return (int) daydiff;
		}
		if (diff==1){
			return cal1.get(Calendar.DAY_OF_YEAR)-cal2.get(Calendar.DAY_OF_YEAR)+365;
		}
		if (diff==-1){
			return cal1.get(Calendar.DAY_OF_YEAR)-cal2.get(Calendar.DAY_OF_YEAR)-365;
		}
		return cal1.get(Calendar.DAY_OF_YEAR)-cal2.get(Calendar.DAY_OF_YEAR);
	}
	
	/**
	 * 判断是否非同一天
	 *
	 * @param date1 the date1
	 * @param date2 the date2
	 * @return true, if is diff date
	 */
	public static boolean isDiffDate(Date date1,Date date2)
	{
		return Math.abs(date1.getTime()-date2.getTime())>1000*60*60*24||date1.getDate()!=date2.getDate();
	}
	
//	/**
//	 * 判断是否同一天,用指定的小时分割天
//	 *
//	 * @param date1 the date1
//	 * @param date2 the date2
//	 * @param hour the hour
//	 * @return true, if is diff date
//	 */
//	public static boolean isDiffDate(Date date1,Date date2,int hour){
//		if(hour>=24||hour<=0){
//			return isDiffDate(date1,date2);
//		}else{
//			if(Math.abs(date1.getTime()-date2.getTime())>1000*60*60*24){
//				return true;
//			}else if(date1.getDate()!=date2.getDate()&&date1.getHours()>=hour&&date2.getHours()<hour||date1.getDate()!=date2.getDate()&&date2.getHours()>=hour&&date1.getHours()<hour||
//					date1.getDate()==date2.getDate()&&date1.getHours()>=hour&&date2.getHours()>=hour||date1.getDate()==date2.getDate()&&date1.getHours()<=hour&&date2.getHours()<=hour){
//				return false;
//			}else{
//				return true;
//			}
//		}
//	}
	
//	/**
//	 * 判断是否为同一天
//	 *
//	 * @param date1 the date1
//	 * @param date2 the date2
//	 * @param hour the hour
//	 * @return true, if is same date
//	 */
//	public static boolean isSameDate(Date date1,Date date2,int hour){
//		return !isDiffDate(date1, date2, hour);
//	}
	
	/**
	 *	是否同一天
	 * @param date1 the date1
	 * @param date2 the date2
	 * @param hour the hour
	 * @return true, if is same date other
	 */
	public static boolean isSameDateOther(Date date1,Date date2 ,int hour){
		hour=hour>24?24:hour<0?0:hour;
		int add=24-hour;
		Calendar cal1=Calendar.getInstance();
		cal1.setTime(date1);
		cal1.add(Calendar.HOUR, add);
		date1=cal1.getTime();
		
		Calendar cal2=Calendar.getInstance();
		cal2.setTime(date2);
		cal2.add(Calendar.HOUR, add);
		date2=cal2.getTime();
		return isSameDate(date1, date2);
	}
	
	/**
	 * 是否间隔天
	 *
	 * @param date1 the date1
	 * @param date2 the date2
	 * @param hour the hour
	 * @return true, if is diff date other
	 */
	public static boolean isDiffDateOther(Date date1,Date date2 ,int hour){
		return !isSameDateOther(date1,date2,hour);
	}

	/**
	 * 以当前时间创建Timestamp
	 * 
	 * @return the timestamp
	 */
	public static Timestamp createTimestamp() {
		return new Timestamp(System.currentTimeMillis());
	}

	/**
	 * 以指定时间为基准,获取表达式时间.
	 * 
	 * @param date
	 *            the date
	 * @param strExp
	 *            the str exp
	 * @return the after date
	 */
	public static Date getAfterDate(Date date, String strExp) {
		String exp = strExp.substring(strExp.length() - 1);
		int add = Integer.valueOf(strExp.substring(0, strExp.length() - 1));
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		if (exp.trim().equals("s")) {
			cal.add(Calendar.SECOND, add);
		} else if (exp.trim().equals("m")) {
			cal.add(Calendar.MINUTE, add);
		} else if (exp.trim().equals("H")) {
			cal.add(Calendar.HOUR, add);
		} else if (exp.trim().equals("d")) {
			cal.add(Calendar.DAY_OF_MONTH, add);
		} else if (exp.trim().equals("M")) {
			cal.add(Calendar.MONTH, add);
		} else if (exp.trim().equals("y")) {
			cal.add(Calendar.YEAR, add);
		}
		return cal.getTime();

	}

	/**
	 * 以当前时间为基准,获取表达式时间.
	 * 
	 * @param strExp
	 *            the str exp
	 * @return the after date
	 */
	public static Date getAfterDate(String strExp) {
		return getAfterDate(new Date(), strExp);

	}

	/**
	 * 获取中文星期.
	 * 
	 * @param date
	 *            the date
	 * @return the week str
	 */
	public static String getChinaWeekStr(Date date) {
		String str = "";
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		int day = cal.get(Calendar.DAY_OF_WEEK);
		if (day == Calendar.SUNDAY) {
			str = "星期日";
		} else if (day == Calendar.MONDAY) {
			str = "星期一";
		} else if (day == Calendar.TUESDAY) {
			str = "星期二";
		} else if (day == Calendar.WEDNESDAY) {
			str = "星期三";
		} else if (day == Calendar.THURSDAY) {
			str = "星期四";
		} else if (day == Calendar.FRIDAY) {
			str = "星期五";
		} else if (day == Calendar.SATURDAY) {
			str = "星期六";
		}
		return str;
	}

	/**
	 * Description: 通过"yyyy-MM-dd"字符串获得时间<br>
	 * .
	 * 
	 * @param dateString
	 *            the date string
	 * @return Date (异常返回null)
	 */
	public static Date getDateByFormatDateStr(String dateString) {
		if (dateString == null || "".equals(dateString)) {
			return null;
		}

		Date date = null;
		try {
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			date = df.parse(dateString);
		} catch (Exception e) {
			date = null;
		}
		return date;
	}

	/**
	 * Description: 通过"yyyy-MM-dd HH:mm:ss"字符串获得时间<br>
	 * .
	 * 
	 * @param dateString
	 *            the date string
	 * @return Date (异常返回null)
	 */
	public static Date getDateByFormatStr(String dateString) {
		return getDateByFormatStr(dateString, "yyyy-MM-dd HH:mm:ss");
	}

	/**
	 * Description:通过制定的格式化方法获得date <br>
	 * .
	 * 
	 * @param dateString
	 *            日期的字符表现
	 * @param format
	 *            格式化的字符串 eg："yyyy-MM-dd HH:mm:ss"
	 * @return Date (异常返回null)
	 */
	public static Date getDateByFormatStr(String dateString, String format) {
		Date date = null;
		try {
			SimpleDateFormat df = new SimpleDateFormat(format);
			date = df.parse(dateString);
		} catch (Exception e) {
			date = null;
		}

		return date;
	}

	/**
	 * 获取时间字段所对应的数据(例如，分钟m、小时h)
	 * 
	 * @param date
	 *            the date
	 * @param filed
	 *            the filed
	 * @return the date filed
	 */
	public static int getDateFiled(Date date, String filed) {
		if (null == filed || null == date) {
			return -1;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		if (filed.equals("s")) {
			return cal.get(Calendar.SECOND);
		}
		if (filed.equals("m")) {
			return cal.get(Calendar.MINUTE);
		}
		if (filed.equals("h")) {
			return cal.get(Calendar.HOUR_OF_DAY);
		}
		if (filed.equals("d")) {
			return cal.get(Calendar.DAY_OF_MONTH);
		}
		if (filed.equals("M")) {
			return cal.get(Calendar.MONTH);
		}
		if (filed.equals("y")) {
			return cal.get(Calendar.YEAR);
		}
		return -1;
	}

	/**
	 * 获取当前时间字段所对应的数据(例如，分钟m、小时h)
	 * 
	 * @param filed
	 *            the filed
	 * @return the date filed
	 */
	public static int getDateFiled(String filed) {
		return getDateFiled(new Date(), filed);
	}

	/**
	 * Gets the date.
	 * 
	 * @param date
	 *            the date
	 * @return the date
	 */
	public static String getFormatStringByDate(Date date) {
		if (null == date) {
			return null;
		}
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return df.format(date);
	}

	/**
	 * Description: 获得当前的yyyy-MM-dd HH:mm:ss<br>
	 * .
	 * 
	 * @return String
	 */
	public static String getFormatStringByNow() {
		return getFormatStringByDate(new Date());
	}

	/**
	 * Description: 根据指定的格式获得当前的时间字符形式<br>
	 * .
	 * 
	 * @param format
	 *            the format
	 * @return String
	 */
	public static String getFormatStringByNow(String format) {
		return getStringByDateAndFormat(new Date(), format);
	}

	/**
	 * 获取当月最大天数
	 * 
	 * @param cal
	 *            the cal
	 * @return the max day for month
	 */
	public static int getMaxDayForMonth(Calendar cal) {
		Calendar calClone = (Calendar) cal.clone();
		calClone.roll(Calendar.MONTH, true);
		calClone.set(Calendar.DAY_OF_MONTH, 1);
		calClone.add(Calendar.DAY_OF_MONTH, -1);
		return calClone.get(Calendar.DAY_OF_MONTH);
	}

	/**
	 * 获取当月最大天数
	 * 
	 * @param date
	 *            the date
	 * @return the max day for month
	 */
	public static int getMaxDayForMonth(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return getMaxDayForMonth(cal);
	}

	/**
	 * 取13位时间戳的6-10位.
	 * 
	 * @return the shot time stamp
	 */
	public static Integer getShotTimeStamp() {
		return Integer.valueOf(((Long) System.currentTimeMillis()).toString()
				.substring(6, 10));
	}

	/**
	 * 获取一段时间以前 单位小时.
	 * 
	 * @param hour
	 *            the hour
	 * @return the sometimes ago
	 */
	public static String getSometimesAgo(Integer hour) {
		Date date = new Date();
		long flag = 1000 * 60 * 60;
		long seconds = date.getTime() - (hour * flag);
		date.setTime(seconds);
		return StringTool.formateDateWithStamp(date);
	}

	/**
	 * Description: 通过制定的格式化形式获得时间的字符串<br>
	 * .
	 * 
	 * @param date
	 *            需要格式化的时间
	 * @param format
	 *            格式化类型 eg:yyyy-MM-dd HH:mm:ss
	 * @return String 格式化后的字符串(异常返回空)
	 */
	public static String getStringByDateAndFormat(Date date, String format) {
		String dateString = "";
		if (date == null) {
			return dateString;
		}
		try {
			SimpleDateFormat df = new SimpleDateFormat(format);
			dateString = df.format(date);
		} catch (Exception e) {
			e.printStackTrace();
			dateString = "";
		}
		return dateString;
	}

	/**
	 * Description: 通过制定的格式化形式获得时间的字符串<br>
	 * .
	 * 
	 * @param strDate
	 *            the str date
	 * @param format
	 *            格式化类型 eg:yyyy-MM-dd HH:mm:ss
	 * @return String 格式化后的字符串(异常返回空)
	 */
	public static String getStringByStringDateAndFormat(String strDate,
			String format) {
		String dateString = "";
		if (strDate == null) {
			return dateString;
		}
		Date date = getDateByFormatStr(strDate);
		return getStringByDateAndFormat(date, format);
	}

	/**
	 * targetTime必须大于nowTime 获取targetTime 与nowTime的时间差 格式化为 今天00:00:00 类型 只判断到明天
	 * 
	 * @author Johnny Yang
	 * @return
	 */
	public static String getTimeDiffDateFormat(long targetTime, long nowTime) {
		String message = "";
		Date date1 = new Date(targetTime);
		Date date2 = new Date(nowTime);
		int dayDiff = compareWithDate(date1, date2);
		SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
		if (dayDiff == 0) {
			message = "今天 " + df.format(date1);
		} else if (dayDiff == 1) {
			message = "明天" + df.format(date1);
		} else if (dayDiff == 2) {
			message = "后天" + df.format(date1);
		} else if (dayDiff == -1) {
			message = "昨天 " + df.format(date1);
		} else if (dayDiff == -2) {
			message = "前天 " + df.format(date1);
		} else if (dayDiff == -3) {
			message = "大前天 " + df.format(date1);
		} else {
			SimpleDateFormat df1 = new SimpleDateFormat("MM/dd HH:mm:ss");
			message = df1.format(date1);
		}
		return message;
	}

	/**
	 * 获取标记时的毫秒数.
	 * 
	 * @param mark
	 *            the mark
	 * @return the time mark
	 */
	public static Long getTimeMark(Integer mark) {
		return timeMarkMap.get(mark);
	}

	/**
	 * 获取时间标记,并移除时间标记.
	 * 
	 * @param mark
	 *            the mark
	 * @return the time mark and remove
	 */
	public static Long getTimeMarkAndRemove(Integer mark) {
		Long timeMark = getTimeMark(mark);
		removeTimeMark(mark);
		return timeMark;

	}

	/**
	 * 获取时间标记与当前差值.
	 * 
	 * @param mark
	 *            the mark
	 * @return the time mark diff
	 */
	public static Long getTimeMarkDiff(Integer mark) {
		Long getMark = getTimeMark(mark);
		if (null == getMark) {
			System.out.println("mark is null!" + mark);
			return 0L;
		} else {
			return (System.nanoTime() - getMark) / 1000 / 1000;
		}
	}

	/**
	 * 获取时间标记与当前差值,并移除时间标记.
	 * 
	 * @param mark
	 *            the mark
	 * @return the time mark diff and remove
	 */
	public static Long getTimeMarkDiffAndRemove(Integer mark) {
		Long diff = getTimeMarkDiff(mark);
		removeTimeMark(mark);
		return diff;
	}

	/**
	 * Description: 通过制定的格式化形式获得24小时前的时间的字符串<br>
	 * .
	 * 
	 * @param format
	 *            格式化类型 eg:yyyyMMdd
	 * @return String 格式化后的字符串(异常返回空)
	 */
	public static String getYesterdayDate(String format) {
		long time = System.currentTimeMillis();

		time = time - 24 * 60 * 60 * 1000;

		Date d = new Date(time);
		return getStringByDateAndFormat(d, format);
	}

	/**
	 * 判断当前时间是否在0点到7点之间
	 * 
	 * @return
	 */
	public static boolean isBetween0to7() {
		String hour = getFormatStringByNow("HH");
		Integer hFlag = Integer.parseInt(hour);
		if (hFlag >= 7) {
			return false;
		}
		return true;
	}

	/**
	 * 功能:判断是否过期 创建者： 黄林 2011-7-4.
	 * 
	 * @param date
	 *            the date
	 * @return int 1是过期了，0是没过期
	 */
	public static int isExpired(Date date) {
		Date nowDate = new Date();
		long ex = nowDate.getTime() - date.getTime();
		if (ex > 0) {
			return 1;
		} else {
			return 0;
		}

	}

	/**
	 * Description: 判断两个日期是否相同 <br>
	 * Implement: <br>
	 * 1、… <br>
	 * 2、… <br>
	 * [参数列表，说明每个参数用途].
	 * 
	 * @param date1
	 *            the date1
	 * @param date2
	 *            the date2
	 * @return boolean
	 * @exception/throws [违例类型] [违例说明]
	 * @see [类、类#方法、类#成员]
	 */
	public static boolean isSameDate(Date date1, Date date2) {
		if (date1 == null || date2 == null) {
			return false;
		}

		Calendar cal1 = Calendar.getInstance();
		Calendar cal2 = Calendar.getInstance();

		cal1.setTime(date1);
		cal2.setTime(date2);

		return (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR))
				&& (cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH))
				&& (cal1.get(Calendar.DAY_OF_MONTH) == cal2
						.get(Calendar.DAY_OF_MONTH));
	}

	/**
	 * 功能:判断cron表达式是否与当前时间匹配 创建者： 黄林 2012-1-18. 性能:四核 40000/s
	 * 
	 * @param cron
	 *            cron 表达式
	 * @param time
	 *            时间
	 * @return 是否匹配
	 * @throws ParseException
	 *             cron解析失败
	 * @throws NullPointerException
	 *             表达式或时间空指针
	 */
	public static boolean matchCron(String cron) throws ParseException,
			NullPointerException {
		return matchCron(cron, new Date());
	}

	/**
	 * 功能:判断cron表达式是否与指定时间匹配 创建者： 黄林 2012-1-18. 性能:四核 40000/s
	 * 
	 * @param cron
	 *            cron 表达式
	 * @param time
	 *            时间
	 * @return 是否匹配
	 * @throws ParseException
	 *             cron解析失败
	 * @throws NullPointerException
	 *             表达式或时间空指针
	 */
	public static boolean matchCron(String cron, Date time)
			throws ParseException, NullPointerException {
		// 验证表达式是否合法
		if (null == cron || cron.length() == 0) {
			throw new NullPointerException("cron expression is null");
		}
		if (null == time) {
			throw new NullPointerException("time is null");
		}
		String[] timeArea = cron.split(" ");
		if (timeArea.length < 6 || timeArea.length > 7) {
			throw new ParseException(
					"cron expression leng too long or too short", 0);
		}
		for (int i = 0; i < timeArea.length; i++) {
			ArrayList<String> lists = ListTool.toList("0", "1", "2", "3", "4",
					"5", "6", "7", "8", "9", ",", "-", "*", "/");
			if (i == 3) {
				lists.add("?");
				lists.add("L");
				lists.add("W");
				// lists.add("C");
			} else if (i == 5) {
				lists.add("?");
				lists.add("L");
				// lists.add("C");
				lists.add("#");
				timeArea[i] = timeArea[i].replace("MON", "1");
				timeArea[i] = timeArea[i].replace("TUE", "2");
				timeArea[i] = timeArea[i].replace("WEN", "3");
				timeArea[i] = timeArea[i].replace("THU", "4");
				timeArea[i] = timeArea[i].replace("FRI", "5");
				timeArea[i] = timeArea[i].replace("SAT", "6");
				timeArea[i] = timeArea[i].replace("SUN", "7");
			} else if (i == 4) {
				timeArea[i] = timeArea[i].replace("JAN", "1");
				timeArea[i] = timeArea[i].replace("FEB", "2");
				timeArea[i] = timeArea[i].replace("MAR", "3");
				timeArea[i] = timeArea[i].replace("APR", "4");
				timeArea[i] = timeArea[i].replace("MAY", "5");
				timeArea[i] = timeArea[i].replace("JUN", "6");
				timeArea[i] = timeArea[i].replace("JUL", "7");
				timeArea[i] = timeArea[i].replace("AUG", "8");
				timeArea[i] = timeArea[i].replace("SEP", "9");
				timeArea[i] = timeArea[i].replace("OCT", "10");
				timeArea[i] = timeArea[i].replace("NOV", "11");
				timeArea[i] = timeArea[i].replace("DEC", "12");
			}
			char[] chars = timeArea[i].toCharArray();
			int index = 0;
			for (char c : chars) {
				if (!StringTool.in(String.valueOf(c),
						lists.toArray(new String[0]))) {
					throw new ParseException("cron expression area " + i
							+ " illegal", index);
				}
				index++;
			}
		}
		if (timeArea[3].equals("?") && timeArea[5].equals("?")) {
			throw new ParseException(
					"cron expression ? operator can not be repeated", 3);
		}
		for (int i = 0; i < timeArea.length; i++) {
			String string = timeArea[i];
			if (!MatchCronArea(string, i, time.getTime())) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 功能:匹配core表达式区域 创建者： 黄林 2012-1-29.
	 * 
	 * @param area
	 *            cron表达式子域
	 * @param index
	 *            区域 0-6
	 * @param time
	 *            时间
	 * @return 是否匹配
	 * @throws ParseException
	 *             区域解析异常
	 */
	private static boolean MatchCronArea(String area, int index, long time)
			throws ParseException {
		if (null == area || area.length() == 0) {
			throw new ParseException("cron expression area " + index
					+ " illegal", 0);
		}
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(time);
		// 反斜线--间隔值
		int backslash = -1;
		// 匹配值
		ArrayList<Integer> matchValues = new ArrayList<Integer>();
		/** 是否包含星号 */
		boolean hasstar = false;
		// 拆分反斜线
		if (area.indexOf("/") != -1) {
			try {
				backslash = Integer.valueOf(area.split("/")[1]);
				if (backslash < 1) {
					throw new ParseException("cron expression area " + index
							+ " illegal", 0);
				}
			} catch (Exception e) {
				throw new ParseException("cron expression area " + index
						+ " illegal", 0);
			}
			area = area.split("/")[0];
			if (area.indexOf(",") != -1) {
				throw new ParseException("cron expression area " + index
						+ " / operator and - operator can not simultaneously",
						0);
			}
		}
		// 解析星号
		if (area.indexOf("*") != -1 && !area.equals("*")) {
			// 含有星号并且含有其他字符
			throw new ParseException("cron expression area " + index
					+ " * operator only stand alone ", 0);
		} else if (area.equals("*")) {
			hasstar = true;
		} else if (area.indexOf("?") == -1 && area.indexOf("W") == -1
				&& area.indexOf("L") == -1 && area.indexOf("#") == -1) {
			// 解析,分隔符
			if (area.indexOf(",") != -1) {
				String[] values = area.split(",");
				for (String val : values) {
					// 包含- 区域数字
					if (val.indexOf("-") != -1) {
						String[] valueAreas = val.split("-");
						if (valueAreas.length != 2) {
							throw new ParseException("cron expression area "
									+ index + " illegal", 0);
						} else {
							try {
								int start = Integer.valueOf(valueAreas[0]);
								int end = Integer.valueOf(valueAreas[1]);
								matchValues.addAll(ListTool.toList(NumberTool
										.numtoArray(start, end)));
							} catch (Exception e) {
								throw new ParseException(
										"cron expression area " + index
												+ " value is not number", 0);
							}
						}
					} else {
						// 值
						try {
							matchValues.add(Integer.valueOf(val));
						} catch (Exception e) {
							throw new ParseException("cron expression area "
									+ index + " value is not number", 0);
						}
					}
				}
			} else {
				if (area.indexOf("-") != -1) {
					String[] valueAreas = area.split("-");
					if (valueAreas.length != 2) {
						throw new ParseException("cron expression area "
								+ index + " illegal", 0);
					} else {
						try {
							int start = Integer.valueOf(valueAreas[0]);
							int end = Integer.valueOf(valueAreas[1]);
							matchValues.addAll(ListTool.toList(NumberTool
									.numtoArray(start, end)));
						} catch (Exception e) {
							throw new ParseException("cron expression area "
									+ index + " value is not number", 0);
						}
					}
				} else {
					// 值
					try {
						matchValues.add(Integer.valueOf(area));
					} catch (Exception e) {
						throw new ParseException("cron expression area "
								+ index + " value:" + area + " is not number",
								0);
					}
				}
			}
		}
		Integer timeValue = 0;
		switch (index) {
		case 0:// 秒
			timeValue = cal.get(Calendar.SECOND);
			try {
				return matchValue(backslash, matchValues, hasstar, timeValue,
						61, 0);
			} catch (Exception e) {
				throw new ParseException("cron expression area " + index
						+ " value is not illegal", 0);
			}
		case 1:
			timeValue = cal.get(Calendar.MINUTE);
			try {
				return matchValue(backslash, matchValues, hasstar, timeValue,
						59, 0);
			} catch (Exception e) {
				throw new ParseException("cron expression area " + index
						+ " value is not illegal", 0);
			}
		case 2:
			timeValue = cal.get(Calendar.HOUR_OF_DAY);
			try {
				return matchValue(backslash, matchValues, hasstar, timeValue,
						23, 0);
			} catch (Exception e) {
				throw new ParseException("cron expression area " + index
						+ " value is not illegal", 0);
			}
		case 3:
			if ("?".equals(area)) {
				return true;
			}
			timeValue = cal.get(Calendar.DAY_OF_MONTH);
			if (area.equals("L")) {// 本月最后一天
				return timeValue.equals(getMaxDayForMonth(cal));
			}
			if (area.indexOf("W") != -1) {
				if (area.equals("LW") || area.equals("WL")) {// 本月最后一个工作日
					cal.set(Calendar.DAY_OF_MONTH, getMaxDayForMonth(cal));
					int lastWeek = cal.get(Calendar.DAY_OF_WEEK);
					if (lastWeek == 1) {
						cal.roll(Calendar.DAY_OF_MONTH, false);
						cal.roll(Calendar.DAY_OF_MONTH, false);
					}
					if (lastWeek == 7) {
						cal.roll(Calendar.DAY_OF_MONTH, false);
					}
					return timeValue.intValue() == cal
							.get(Calendar.DAY_OF_MONTH);
				} else {// 最近工作日
					try {
						Integer wvalue = Integer.valueOf(area.substring(0,
								area.length() - 1));
						int maxMonthDay = getMaxDayForMonth(cal);
						cal.set(Calendar.DAY_OF_MONTH, wvalue);
						if (timeValue.intValue() == wvalue.intValue()
								&& cal.get(Calendar.DAY_OF_WEEK) != 1
								&& cal.get(Calendar.DAY_OF_WEEK) != 7) {
							return true;// 当天就是工作日，并且值就是当天
						} else {
							// 当天不是工作日
							if (wvalue == 1
									&& cal.get(Calendar.DAY_OF_WEEK) == 7) {
								return timeValue.intValue() == 3;// 月首
							} else if (wvalue == maxMonthDay
									&& cal.get(Calendar.DAY_OF_WEEK) == 1) {// 月尾
								return timeValue.intValue() == maxMonthDay - 2;
							} else if (cal.get(Calendar.DAY_OF_WEEK) == 7) {
								return timeValue == wvalue - 1;
								// }else if (cal.get(Calendar.DAY_OF_WEEK)==1) {
							} else {
								return timeValue == wvalue + 1;
							}
						}
					} catch (Exception e) {
						log.debug(area, e);
						throw new ParseException("cron expression area "
								+ index + " value is not illegal", 0);
					}
				}
			}
			try {
				return matchValue(backslash, matchValues, hasstar, timeValue,
						31, 1);
			} catch (Exception e) {
				throw new ParseException("cron expression area " + index
						+ " value is not illegal", 0);
			}
		case 4:
			timeValue = cal.get(Calendar.MONTH);
			try {
				return matchValue(backslash, matchValues, hasstar, timeValue,
						12, 1);
			} catch (Exception e) {
				throw new ParseException("cron expression area " + index
						+ " value is not illegal", 0);
			}
		case 5:
			if ("?".equals(area)) {
				return true;
			}
			timeValue = cal.get(Calendar.DAY_OF_WEEK);
			if (area.indexOf("L") != -1) {
				if (area.equals("L")) {// 星期天
					return timeValue.intValue() == 7;
				} else {
					// 这个月的最后一个星期X
					timeValue = cal.get(Calendar.DAY_OF_MONTH);
					Integer wvalue = Integer.valueOf(area.substring(0,
							area.length() - 1));
					if (wvalue < 1 || wvalue > 7) {
						throw new ParseException("cron expression area "
								+ index + " value is not illegal", 0);
					}
					int maxMonthDay = getMaxDayForMonth(cal);
					cal.set(Calendar.DAY_OF_MONTH, maxMonthDay);
					while (cal.get(Calendar.DAY_OF_WEEK) != wvalue.intValue()) {
						cal.roll(Calendar.DAY_OF_MONTH, false);
					}
					return timeValue.intValue() == cal
							.get(Calendar.DAY_OF_MONTH);
				}
			}
			if (area.indexOf("#") != -1) {
				try {
					int weekday = Integer.valueOf(area.split("#")[0]);
					int weeki = Integer.valueOf(area.split("#")[1]);
					return cal.get(Calendar.WEEK_OF_MONTH) == weeki
							&& cal.get(Calendar.DAY_OF_WEEK) == weekday;
				} catch (Exception e) {
					throw new ParseException("cron expression area " + index
							+ " value is not illegal", 0);
				}
			}
			try {
				return matchValue(backslash, matchValues, hasstar, timeValue,
						7, 1);
			} catch (Exception e) {
				throw new ParseException("cron expression area " + index
						+ " value is not illegal", 0);
			}
		case 6:
			timeValue = cal.get(Calendar.YEAR);
			try {
				return matchValue(backslash, matchValues, hasstar, timeValue,
						1970, 2099);
			} catch (Exception e) {
				throw new ParseException("cron expression area " + index
						+ " value is not illegal", 0);
			}
		default:
			break;
		}
		return false;
	}

	/**
	 * 功能:匹配数字 创建者： 黄林 2012-1-29.
	 * 
	 * @param backslash
	 *            反斜线数值(-1表示不存在)
	 * @param matchValues
	 *            匹配的列表，可以为空
	 * @param hasstar
	 *            是否包含星号
	 * @param timeValue
	 *            时间值
	 * @param max
	 *            最大值
	 * @param min
	 *            最小值
	 * @return 是否匹配
	 * @throws ParseException
	 *             匹配列表中含有超出范围的值
	 */
	private static boolean matchValue(int backslash,
			ArrayList<Integer> matchValues, boolean hasstar, Integer timeValue,
			int max, int min) throws ParseException {
		if (hasstar) {
			if (backslash == -1) {
				return true;
			} else if (timeValue % backslash == 0) {
				return true;
			} else {
				return false;
			}
		} else if (backslash == -1) {
			return matchValues.contains(timeValue);
		} else {
			for (Integer integer : matchValues) {
				if (integer.intValue() > max || integer.intValue() < min) {
					throw new ParseException("", 0);
				}
				if (integer.equals(timeValue)) {
					return true;
				}
			}
			return false;
		}
	}

	/**
	 * 功能: 开始日期至结束日期 创建者： 黄林 2011-7-4.
	 * 
	 * @param begin
	 *            the begin
	 * @param end
	 *            the end
	 * @return string
	 */
	public static String parseBeginToEnd(Date begin, Date end) {
		String bg = StringTool.formateDateWithSlash(begin);
		String ed = StringTool.formateDateWithSlash(end);
		return bg + " - " + ed;
	}

	/**
	 * 打印时间标记与当前差值
	 * 
	 * @param mark
	 *            the mark
	 * @author 黄林 Prints the time mark diff.
	 */
	public static void printTimeMarkDiff(Integer mark) {
		printTimeMarkDiff(mark, null);
	}

	/**
	 * 打印时间标记与当前差值
	 * 
	 * @param mark
	 *            the mark
	 * @return the time mark diff and remove
	 */
	public static void printTimeMarkDiff(Integer mark, String info) {
		info = info == null ? "" : info + ":";
		System.out.println(info + getTimeMarkDiff(mark) + "ms");
	}

	/**
	 * 打印时间标记与当前差值,并移除时间标记.
	 * 
	 * @param mark
	 *            the mark
	 * @return the time mark diff and remove
	 */
	public static void printTimeMarkDiffAndRemove(Integer mark) {
		printTimeMarkDiff(mark, "last");
		removeTimeMark(mark);
	}

	/**
	 * 功能:移除时间标记 创建者： 黄林 2011-10-18.
	 * 
	 * @param mark
	 *            the mark
	 */
	public synchronized static void removeTimeMark(Integer mark) {
		timeMarkMap.remove(mark);
		if (lastAdd.get() == Integer.MAX_VALUE) {
			lastAdd.set(Integer.MIN_VALUE);
		}
	}

	/**
	 * 功能:判断是否为一个cron表达式 创建者： 黄林 2012-1-18.
	 * 
	 * @param cron
	 *            cron表达式
	 * @return true,
	 */
	public static boolean validCron(String cron) {
		try {
			matchCron(cron, new Date());
		} catch (Exception e) {
			log.debug(cron, e);
			return false;
		}
		return true;
	}
}
