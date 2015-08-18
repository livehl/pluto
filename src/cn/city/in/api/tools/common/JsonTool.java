package cn.city.in.api.tools.common;

import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;

import cn.city.in.api.common.BaseEntity;

public class JsonTool {

	protected static int maxLevel=4;
	protected static Log log = LogFactory.getLog(JsonTool.class);
	protected static ObjectMapper mapper = new ObjectMapper();
	protected static SoftHashMap<Object, Object> cacheMap=new SoftHashMap<Object, Object>();
	protected static ThreadLocal<Integer> levelCount=new ThreadLocal<Integer>();
	/**
	 * 转换对象为json，包含字段
	 * 
	 * @param bean
	 *            the bean
	 * @param fileds
	 *            the fileds
	 * @return the string
	 * @throws Exception
	 *             the exception
	 */
	public static String beanToJsonWithFiled(Object bean, String... fileds)
			throws Exception {
		return beanToJsonWithFilter(bean, true, false, fileds);
	}

	/**
	 * 转换对象为json，带字段过滤
	 * 
	 * @param bean
	 *            实体
	 * @param isInclued
	 *            过滤器是否为包含过滤
	 * @param fileds
	 *            过滤字段
	 * @return the json
	 * @throws Exception
	 *             the exception
	 */
	public static String beanToJsonWithFilter(Object bean, boolean isInclued,
			boolean resultNull, String... fileds) throws Exception {
		return toJson(beanToMapWithFilter(bean, isInclued, resultNull, fileds));
	}

	/**
	 * 转换对象为json，排除字段
	 * 
	 * @param bean
	 *            the bean
	 * @param fileds
	 *            the fileds
	 * @return the string
	 * @throws Exception
	 *             the exception
	 */
	public static String beanToJsonWithOutFiled(Object bean, String... fileds)
			throws Exception {
		return beanToJsonWithFilter(bean, false, false, fileds);
	}

	/**
	 * 将model中的实体按默认规则转化
	 * 
	 * @param model
	 *            the model
	 * @return the string
	 * @throws Exception
	 *             the exception
	 */
	// public static String toJsonWithDefaultFilter(Map model)throws Exception
	// {
	// for (Object key : model.keySet()) {
	// Object value=model.get(key);
	// if (value instanceof UserDTO) {
	// model.put(key,
	// beanToMapWithFilter(value,true,EntityConstans.DEFAULT_USER_FILTER));
	// }else if (value instanceof BaseEntity) {
	// model.put(key, beanToMapWithFilter(value,false,"page*","cacheLastUse"));
	// }
	// }
	// return toJson(model);
	// }

	/**
	 * 将类按过滤规则封装为map
	 * 
	 * @param bean
	 *            the bean
	 * @param isInclued
	 *            the is inclued
	 * @param resultNull
	 *            the result null
	 * @param fileds
	 *            the fileds
	 * @return the hash map
	 */
	public static HashMap<String, Object> beanToMapWithFilter(Object bean,
			boolean isInclued, boolean resultNull, String... fileds) {
		if (null==levelCount.get()){
			levelCount.set(0);
		}
		if (StringTool.isNull(bean)||levelCount.get()>maxLevel) {
			return null;
		}
		HashMap<String, Method> dealFileds = getFiledMethodWithFilter(bean,
				isInclued, fileds);
		HashMap<String, Object> jsonMap = new HashMap<String, Object>();
		for (Entry<String, Method> filedEntry : dealFileds.entrySet()) {
			try {
				Object result = filedEntry.getValue().invoke(bean);
				if (Tool.isNull(result)) {
					if (resultNull) {
						jsonMap.put(StringTool.camelToUnderline(filedEntry
								.getKey()), null);
					}
				} else {
					if (result instanceof Date) {
						result = ((Date) result).getTime();
					}
					if (result instanceof Boolean) {
						if ((Boolean) result) {
							result = 1;
						} else {
							result = 0;
						}
					}
					if (result instanceof BaseEntity) {
						levelCount.set(levelCount.get()+1);
						result = beanToMapWithFilter(result, isInclued,
								resultNull, fileds);
						levelCount.set(levelCount.get()-1);
					}
					if (result instanceof List) {
						levelCount.set(levelCount.get()+1);
						result = listToMapWithFiledFilter((List) result,
								isInclued, resultNull, fileds);
						levelCount.set(levelCount.get()-1);
					}
					if (Tool.isNotNull(result)||resultNull){
						jsonMap.put(
								StringTool.camelToUnderline(filedEntry.getKey()),
								result);
					}
				}

			} catch (Exception e) {
				log.error("change " + bean.getClass().getName() + ":"
						+ filedEntry.getKey() + " fail", e);
			}
		}
		return jsonMap;
	}

	/**
	 * Bean to map with filter,no return Null.
	 * 
	 * @see #beanToMapWithFilter(Object, boolean,boolean, String...)
	 */
	public static HashMap<String, Object> beanToMapWithFilter(Object bean,
			boolean isInclued, String... fileds) {
		return beanToMapWithFilter(bean, isInclued, false, fileds);
	}

	/**
	 * 实体类列表转map，带过滤器 默认包含规则，不反空值
	 * 
	 * @param bean
	 *            the bean
	 * @param fileds
	 *            the fileds
	 * @return the hash map
	 * @author 黄林
	 */
	public static HashMap<String, Object> beanToMapWithFilter(Object bean,
			String... fileds) {
		return beanToMapWithFilter(bean, true, fileds);
	}

	public static ObjectNode createNewObjectNode() {
		return new ObjectNode(JsonNodeFactory.instance);
	}

	/**
	 * 筛选并获取类的字段
	 * 
	 * @param bean
	 * @param isInclued
	 * @param fileds
	 * @return
	 */
	private static HashMap<String, Method> getFiledMethodWithFilter(
			Object bean, boolean isInclued, String... fileds) {
		if (StringTool.isNull(bean)) {
			return null;
		}
		HashMap<String, Method> map=(HashMap<String, Method>) cacheMap.get(bean.getClass().getName()+Arrays.toString(fileds));
		if(null!=map){
			return map;
		}
		String[] allMethods=(String[]) cacheMap.get(bean.getClass());
		if (null==allMethods){
			allMethods = ReflectTool.getGetMethodNames(bean.getClass());
			cacheMap.put(bean.getClass(), allMethods);
		}
		ArrayList<String> dealMethods = new ArrayList<String>();
		if (isInclued) {
			for (String filed : fileds) {
				dealMethods.addAll(ListTool.toList(StringTool.matches(
						allMethods, filed)));
				dealMethods.addAll(ListTool.toList(StringTool.matches(
						allMethods, "get" + StringTool.upFrist(filed))));
				dealMethods.addAll(ListTool.toList(StringTool.matches(
						allMethods, "is" + StringTool.upFrist(filed))));
			}
		} else {
			dealMethods.addAll(ListTool.toList(allMethods));
			for (String filed : fileds) {
				String[] matchFiled = StringTool.matches(allMethods, filed);
				for (String removeFiled : matchFiled) {
					dealMethods.remove(removeFiled);
				}
			}
		}
		HashMap<String, Method> allMethod = new HashMap<String, Method>();
		for (String method : dealMethods) {
			try {
				String field = method;
				if (field.indexOf("get") != -1) {
					field = field.substring(3);
				}
				allMethod.put(field, bean.getClass().getMethod(method));
			} catch (Exception e) {
				log.debug("get method fail:" + method, e);
			}
		}
		// //方法补偿
		// for (String filed : fileds) {
		// if ((!Tool.isNull(filed))&&Tool.isValid(filed)) {
		// if (!allMethod.containsKey(filed)||null==allMethod.get(filed)) {
		// try {
		// Method method=ReflectTool.getGetMethod(bean.getClass(), "get" +
		// StringTool.upFrist(filed));
		// if (null==method) {
		// method=ReflectTool.getGetMethod(bean.getClass(),filed);
		// }
		// allMethod.put(filed,method);
		// } catch (Exception e) {
		// allMethod.put(filed,
		// ReflectTool.getGetMethod(bean.getClass(),filed));
		// }
		// }
		// }
		// }
		cacheMap.put(bean.getClass().getName()+Arrays.toString(fileds), allMethod);
		return allMethod;
	}

	/**
	 * Json to type.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param json
	 *            the json
	 * @param type
	 *            the type
	 * @return the t
	 * @throws Exception
	 *             the exception
	 */
	public static <T> T jsonToType(String json, Class<T> type) throws Exception {
		return new ObjectMapper().readValue(json, type);
	}

	/**
	 * 将列表转为json，可选是否返回空字段
	 * 
	 * @param list
	 *            the list
	 * @param resultNull
	 *            the result null
	 * @return the array list
	 * @author 黄林
	 */
	public static ArrayList<HashMap<String, Object>> listToMap(
			List<Object> list, boolean resultNull) {
		ArrayList<HashMap<String, Object>> resultList = new ArrayList<HashMap<String, Object>>();
		for (Object listObj : list) {
			resultList.add(objToMap(listObj, resultNull));
		}
		return resultList;
	}

	/**
	 * 实体类列表转map，带过滤器
	 * 
	 * @param list
	 *            the list
	 * @param isInclued
	 *            the is inclued
	 * @param resultNull
	 *            the result null
	 * @param fileds
	 *            the fileds
	 * @return the array list
	 * @throws Exception
	 *             the exception
	 */
	public static ArrayList<HashMap<String, Object>> listToMapWithFiledFilter(
			List<?> list, boolean isInclued, boolean resultNull,
			String... fileds) {
		ArrayList<HashMap<String, Object>> resultList = new ArrayList<HashMap<String, Object>>();
		if (null==levelCount.get()){
			levelCount.set(0);
		}
		if (Tool.isNull(list) || list.size() == 0||levelCount.get()>maxLevel) {
			return resultList;
		}
		if (StringTool.isNull(list.get(0))) {
			return resultList;
		}
		HashMap<String, Method> dealFileds = getFiledMethodWithFilter(
				list.get(0), isInclued, fileds);
		for (Object object : list) {
			HashMap<String, Object> beanMap = new HashMap<String, Object>();
			for (Entry<String, Method> filedEntry : dealFileds.entrySet()) {
				try {
					Object result = filedEntry.getValue().invoke(object);
					if (result == null) {
						if (resultNull) {
							beanMap.put(StringTool.camelToUnderline(filedEntry
									.getKey()), "");
						}
					} else {
						if (result instanceof Date) {
							result = ((Date) result).getTime();
						}
						if (result instanceof Boolean) {
							if ((Boolean) result) {
								result = 1;
							} else {
								result = 0;
							}
						}
						if (result instanceof BaseEntity) {
							levelCount.set(levelCount.get()+1);
							result = beanToMapWithFilter(result, isInclued,
									resultNull, fileds);
							levelCount.set(levelCount.get()-1);
						}
						if (result instanceof List) {
							levelCount.set(levelCount.get()+1);
							result = listToMapWithFiledFilter((List) result,
									isInclued, resultNull, fileds);
							levelCount.set(levelCount.get()-1);
						}
						if (Tool.isNotNull(result)||resultNull){
							beanMap.put(StringTool.camelToUnderline(filedEntry
									.getKey()), result);
						}
					}

				} catch (Exception e) {
					if (null == object) {
						log.debug("object is null", e);
					} else {
						log.debug("change " + object.getClass().getName() + ":"
								+ filedEntry.getKey() + " fail", e);
					}
				}
			}
			resultList.add(beanMap);
		}
		return resultList;
	}

	/**
	 * 实体类列表转map，带过滤器 默认不返回空的参数
	 * 
	 * @param list
	 *            the list
	 * @param isInclued
	 *            the is inclued
	 * @param resultNull
	 *            the result null
	 * @param fileds
	 *            the fileds
	 * @return the array list
	 * @throws Exception
	 *             the exception
	 */
	public static ArrayList<HashMap<String, Object>> listToMapWithFiledFilter(
			List<?> list, boolean isInclued, String... fileds) {
		return listToMapWithFiledFilter(list, isInclued, false, fileds);
	}

	/**
	 * 实体类列表转map，带过滤器 默认不返回空的参数,默认为包含规则
	 * 
	 * @param list
	 *            the list
	 * @param fileds
	 *            the fileds
	 * @return the array list
	 * @author 黄林
	 */
	public static ArrayList<HashMap<String, Object>> listToMapWithFiledFilter(
			List<?> list, String... fileds) {
		return listToMapWithFiledFilter(list, true, fileds);
	}

	/**
	 * 将实体类转为Map，可选是否返回空字段
	 * 
	 * @param bean
	 *            the bean
	 * @param resultNull
	 *            the result null
	 * @return the hash map
	 * @author 黄林
	 */
	public static HashMap<String, Object> objToMap(Object bean,
			boolean resultNull) {
		if (StringTool.isNull(bean)) {
			return null;
		}
		HashMap<String, Object> jsonMap = new HashMap<String, Object>();
		if (bean instanceof List) {
			Object result=listToMap((List) bean, resultNull);
			if (Tool.isNotNull(result)||resultNull)
			{
				jsonMap.put("list",result);
			}
			return jsonMap;
		}
		HashMap<String, Method> dealFileds = getFiledMethodWithFilter(bean,
				true, "*");
		for (Entry<String, Method> filedEntry : dealFileds.entrySet()) {
			try {
				Object result = filedEntry.getValue().invoke(bean);
				if (StringTool.isNull(result)) {
					if (resultNull) {
						jsonMap.put(filedEntry.getKey(), "");
					}
				} else {
					if (result instanceof BaseEntity) {
						result = objToMap(result, resultNull);
					}
					if (result instanceof List) {
						ArrayList<HashMap<String, Object>> resultList = new ArrayList<HashMap<String, Object>>();
						for (Object listObj : (List) result) {
							HashMap<String, Object> resultMap=objToMap(listObj, resultNull);
							if (Tool.isNotNull(resultMap)||resultNull)
							{
								resultList.add(resultMap);
							}
						}
						result = resultList;
					}
					if (Tool.isNotNull(result)||resultNull){
						jsonMap.put(filedEntry.getKey(), result);
					}
				}

			} catch (Exception e) {
				log.debug("change " + bean.getClass().getName() + ":"
						+ filedEntry.getKey() + " fail", e);
			}
		}
		return jsonMap;
	}

	public static ObjectNode readJson(String json) throws Exception {
		return jsonToType(json, ObjectNode.class);
	}

	/**
	 * 转换json
	 * 
	 * @param obj
	 *            the obj
	 * @return the string
	 * @throws Exception
	 *             the exception
	 */
	public static String toJson(Object obj) throws Exception {
		Writer strWriter = new StringWriter();
		mapper.writeValue(strWriter, obj);
		return strWriter.toString();
	}
}
