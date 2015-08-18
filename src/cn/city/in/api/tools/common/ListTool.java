package cn.city.in.api.tools.common;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 功能:对LIST排序.
 * 
 * @param <E>列表类型
 * @author 黄林 2011-7-4
 * @version
 */
public class ListTool<E> {
	public static final int FILED_COMPARE_EQUAL = 0;
	public static final int FILED_COMPARE_MORE = 1;
	public static final int FILED_COMPARE_LESS = 2;
	public static final int FILED_COMPARE_NOT_LESS = 3;
	public static final int FILED_COMPARE_NOT_MORE = 4;
	public static final int FILED_COMPARE_NOT_EQUAL = 5;
	public static final int FILED_COMPARE_NULL = 6;
	public static final int FILED_COMPARE_NOT_NULL = 7;

	protected static Log log = LogFactory.getLog(ListTool.class);

	/**
	 * 通用比较算法
	 * 
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return a>b?1:a=b?0:-1
	 * @author 黄林
	 */
	public static int compare(Object a, Object b) {
		int ret = 0;
		if (null == a || null == b) {// 比较值为空
			if (null == a && null != b) {
				if (b instanceof Number) {
					return ((Number) b).intValue();
				} else {
					return b.toString().compareTo("");
				}
			} else if (null != a && null == b) {
				if (a instanceof Number) {
					return ((Number) a).intValue();
				} else {
					return a.toString().compareTo("");
				}
			} else {
				return 0;
			}
		}
		if (a instanceof Number) {// 数字
			Double n1 = ((Number) a).doubleValue();
			Double n2 = ((Number) b).doubleValue();
			ret = n1.compareTo(n2);
		} else if (a instanceof Date)// 时间
		{
			ret = ((Date) a).compareTo((Date) b);
		} else// 字符串
		{
			ret = a.toString().compareTo(b.toString());
		}
		return ret;
	}

	/**
	 * 功能:检查列表中指定字段是与实例相同 创建者： 黄林 2011-9-27.
	 * 
	 * @param <E>
	 *            the element type
	 * @param 列表
	 * @param field
	 *            需要比较的字段
	 * @param isValue
	 *            最后一个参数是否是值
	 * @param bean
	 *            比较对象
	 * @return 结果
	 * @throws Exception
	 *             the exception
	 */
	@SuppressWarnings("unchecked")
	public static <E> boolean contains(List<E> list, String field,
			boolean isValue, Object bean) throws Exception {
		if (null == list || list.size() < 1) {
			return false;
		}
		E eBean = list.get(0);
		Method method = eBean.getClass().getMethod(
				"get" + StringTool.upFrist(field));
		Object r2 = null;
		if (isValue) {
			r2 = bean;
		} else {
			r2 = method.invoke(bean);
		}
		for (E e : list) {
			Object r1 = method.invoke(e);
			if (r2.equals(r1)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 功能:检查列表中指定字段是与实例相同 创建者： 黄林 2011-8-11.
	 * 
	 * @param <E>
	 *            the element type
	 * @param list
	 *            列表
	 * @param field
	 *            需要比较的字段
	 * @param bean
	 *            比较对象
	 * @return 结果
	 * @throws Exception
	 *             the exception
	 */

	public static <E> boolean contains(List<E> list, String field, E bean)
			throws Exception {
		return contains(list, field, false, bean);
	}

	/**
	 * 获取列表分页
	 * 
	 * @param <E>
	 *            the element type
	 * @param list
	 *            the list
	 * @param pageNum
	 *            the page num
	 * @param pageSize
	 *            the page size
	 * @return the list
	 * @author 黄林
	 */
	public static <E> List<E> cutList(List<E> list, int pageNum, int pageSize) {
		if (StringTool.isNull(list)) {
			return null;
		}
		if (list.size() < (pageNum - 1) * pageSize) {
			return new ArrayList<E>();
		}
		if (list.size() < pageNum * pageSize) {
			return list.subList((pageNum - 1) * pageSize, list.size());
		}
		return list.subList((pageNum - 1) * pageSize, pageNum * pageSize);
	}

	/**
	 * 功能:获取列表中指定属性的值相匹配的项 创建者： 黄林 2011-9-27.
	 * 
	 * @param <E>
	 *            the element type
	 * @param list
	 *            列表
	 * @param field
	 *            需要匹配的字段
	 * @param isValue
	 *            最后一个参数是否是值
	 * @param obj
	 *            比较实例
	 * @return array 匹配的项列表
	 * @throws Exception
	 *             the exception
	 */
	public static <E> ArrayList<E> get(List<E> list, String field,
			boolean isValue, Object obj) throws Exception {
		return get(list, field, isValue, obj, 0);
	}

	/**
	 * 获取列表中指定字段与指定值的比较结果相符的元素
	 * 
	 * @param <E>
	 *            the element type
	 * @param list
	 *            the list
	 * @param field
	 *            the field
	 * @param isValue
	 *            the is value
	 * @param obj
	 *            the obj
	 * @param type
	 *            0=,1>,2<,3>=,4<=,5<>,6null,7!null
	 * @return the array list
	 * @throws Exception
	 *             the exception
	 * @author 黄林
	 */
	public static <E> ArrayList<E> get(List<E> list, String field,
			boolean isValue, Object obj, int type) throws Exception {
		if (null == list || list.size() < 1) {
			return null;
		}
		if (type > 7 || type < 0) {
			log.error("type no valid type");
			throw new Exception("type no valid type");
		}
		E eBean = list.get(0);
		Method method = eBean.getClass().getMethod(
				"get" + StringTool.upFrist(field));
		Object r2 = null;
		if (isValue) {
			r2 = obj;
		} else {
			r2 = method.invoke(obj);
		}
		ArrayList<E> listResult = new ArrayList<E>();
		for (E e : list) {
			if (e == null) {
				continue;
			}
			Object r1 = method.invoke(e);
			if (type >= 6) {
				if (type == 6 && r1 == null) {
					listResult.add(e);
				} else if (type == 7 && r1 != null) {
					listResult.add(e);
				}
				continue;
			}
			int cmp = compare(r1, r2);
			if (type == 0) {
				if (cmp == 0) {
					listResult.add(e);
				}
			} else if (type == 1) {
				if (cmp > 0) {
					listResult.add(e);
				}
			} else if (type == 2) {
				if (cmp < 0) {
					listResult.add(e);
				}
			} else if (type == 3) {
				if (cmp >= 0) {
					listResult.add(e);
				}
			} else if (type == 4) {
				if (cmp <= 0) {
					listResult.add(e);
				}
			} else {
				if (cmp != 0) {
					listResult.add(e);
				}
			}
		}
		if (listResult.size() == 0) {
			return null;
		} else {
			return listResult;
		}
	}

	/**
	 * 功能:获取列表中指定属性的值相匹配的项 创建者： 黄林 2011-9-27.
	 * 
	 * @param <E>
	 *            the element type
	 * @param list
	 *            列表
	 * @param field
	 *            需要匹配的字段
	 * @param obj
	 *            比较实例
	 * @return array list
	 * @throws Exception
	 *             the exception
	 */
	public static <E> ArrayList<E> get(List<E> list, String field, E bean)
			throws Exception {
		return get(list, field, false, bean);
	}

	/**
	 * 获取指定字段为空或者非空的元素
	 * 
	 * @param <E>
	 *            the element type
	 * @param list
	 *            the list
	 * @param field
	 *            the field
	 * @param type
	 *            6null,7!null
	 * @return the array list
	 * @throws Exception
	 *             the exception
	 * @author 黄林
	 */
	public static <E> ArrayList<E> get(List<E> list, String field, int type)
			throws Exception {
		if (type > 7 || type < 6) {
			log.error("type no valid type");
			throw new Exception("type no valid type");
		}
		return get(list, field, true, "", type);
	}

	/**
	 * 返回两个列表中，指定字段值相同的外层项
	 * 
	 * @param external
	 *            外层列表
	 * @param internal
	 *            内层列表
	 * @param field
	 *            字段
	 * @return <外层实体,内层匹配的项列表>
	 * @throws Exception
	 *             the exception
	 */
	@SuppressWarnings("unchecked")
	public static ArrayList<HashMap<Object, Object[]>> getEqualBean(
			List external, List internal, String field) throws Exception {
		return getEqualBean(external, internal, field, field);
	}

	/**
	 * 返回两个列表中，指定字段值相同的项
	 * 
	 * @param external
	 *            外层列表
	 * @param internal
	 *            内层列表
	 * @param externalField
	 *            外层字段
	 * @param internalField
	 *            内层字段
	 * @return <外层实体,内层匹配的项列表>
	 * @throws Exception
	 *             the exception
	 */
	@SuppressWarnings("unchecked")
	public static ArrayList<HashMap<Object, Object[]>> getEqualBean(
			List external, List internal, String externalField,
			String internalField) throws Exception {
		if (null == external || null == internal || null == externalField
				|| null == internalField) {
			return null;
		}
		ArrayList<HashMap<Object, Object[]>> resultList = new ArrayList<HashMap<Object, Object[]>>();
		Method externalMethod = null;
		Method internalMethod = null;
		for (Object object : external) {
			if (null == externalMethod) {
				externalMethod = object.getClass().getMethod(
						"get" + StringTool.upFrist(externalField));
			}
			Object r1 = externalMethod.invoke(object);
			ArrayList<Object> internalList = new ArrayList<Object>();
			for (Object object2 : internal) {
				if (null == internalMethod) {
					internalMethod = object2.getClass().getMethod(
							"get" + StringTool.upFrist(internalField));
				}
				Object r2 = internalMethod.invoke(object2);
				if (r1 == r2 || r1.equals(r2)) {
					internalList.add(object2);
				}
			}
			if (internalList.size() > 0) {
				HashMap<Object, Object[]> externalMap = new HashMap<Object, Object[]>();
				externalMap.put(object, internalList.toArray());
				resultList.add(externalMap);
			}
		}
		return resultList;
	}

	/**
	 * 获取列表中指定字段唯一列表
	 * 
	 * @param <E>
	 *            the element type
	 * @param list
	 *            the list
	 * @param field
	 *            需要匹配的字段
	 * @return the unique property
	 * @throws Exception
	 *             the exception
	 */
	public static <E> ArrayList<Object> getUniqueField(List<E> list,
			String field) throws Exception {
		if (null == list || list.size() < 1) {
			return null;
		}
		E eBean = list.get(0);
		Method method = eBean.getClass().getMethod(
				"get" + StringTool.upFrist(field));
		Set<Object> setResult = new HashSet<Object>();
		for (E e : list) {
			setResult.add(method.invoke(e));
		}
		if (setResult.size() == 0) {
			return null;
		} else {
			ArrayList<Object> listResult = new ArrayList<Object>();
			listResult.addAll(setResult);
			return listResult;
		}
	}

	/**
	 * 功能:判断两个列表中是否含有相同的值 创建者： 黄林 2011-9-29.
	 * 
	 * @param <E>
	 *            the element type
	 * @param lista
	 *            the lista
	 * @param listb
	 *            the listb
	 * @param field
	 *            实体类字段
	 * @return result
	 * @throws Exception
	 *             the exception
	 */
	public static <E> boolean hasSameValue(List<E> lista, List<E> listb,
			String field) throws Exception {
		Method method = null;
		for (E e : lista) {
			if (null == method) {
				method = e.getClass().getMethod(
						"get" + StringTool.upFrist(field));
			}
			Object r1 = method.invoke(e);
			for (E e2 : listb) {
				Object r2 = method.invoke(e2);
				if (r1 == r2 || r1.equals(r2)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 功能:移除列表中指定字段与实例匹配的项 创建者： 黄林 2011-8-11.
	 * 
	 * @param <E>
	 *            the element type
	 * @param list
	 *            列表
	 * @param field
	 *            需要匹配的值
	 * @param isValue
	 *            最后一个参数是否是值
	 * @param bean
	 *            匹配对象
	 * @return 移除的数量
	 * @throws Exception
	 *             the exception
	 */
	@SuppressWarnings("unchecked")
	public static <E> Integer remove(List<E> list, String field,
			boolean isValue, Object bean) throws Exception {
		if (null == list || list.size() < 1) {
			return 0;
		}
		E eBean = list.get(0);
		Method method = eBean.getClass().getMethod(
				"get" + StringTool.upFrist(field));
		Object r2 = null;
		if (isValue) {
			r2 = bean;
		} else {
			r2 = method.invoke(bean);
		}
		Integer count = 0;
		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			E e = (E) iterator.next();
			Object r1 = method.invoke(e);
			if (r2.equals(r1)) {
				iterator.remove();
				count++;
			}
		}
		return count;
	}

	/**
	 * 功能:移除列表中指定字段与实例匹配的项 创建者： 黄林 2011-8-11.
	 * 
	 * @param <E>
	 *            the element type
	 * @param list
	 *            列表
	 * @param field
	 *            需要匹配的字段
	 * @param bean
	 *            匹配对象
	 * @return 移除的数量
	 * @throws Exception
	 *             the exception
	 */
	public static <E> Integer remove(List<E> list, String field, E bean)
			throws Exception {
		return remove(list, field, false, bean);
	}

	/**
	 * 功能:移除列表中空对象
	 * 
	 * @param <E>
	 *            the element type
	 * @param list
	 *            the list
	 * @return the integer
	 * @author 黄林
	 */
	public static <E> Integer removeNull(List<E> list) {
		int count = 0;
		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			E e = (E) iterator.next();
			if (e == null) {
				iterator.remove();
			}
		}
		return count;
	}

	/**
	 * 通用排序.(正序,不筛选中文)
	 * 
	 * @param <E>
	 *            the element type
	 * @param list
	 *            列表
	 * @param field
	 *            需要排序的字段
	 */
	public static <E> E[] sort(E[] list, final Integer index) {
		return sort(list, index, false, false);
	}

	/**
	 * 通用排序.(不筛选中文)
	 * 
	 * @param <E>
	 *            the element type
	 * @param list
	 *            列表
	 * @param field
	 *            需要排序的字段
	 * @param isdesc
	 *            是否反序
	 */
	public static <E> E[] sort(E[] list, final Integer index,
			final boolean isdesc) {
		return sort(list, index, isdesc, false);
	}

	/**
	 * 功能:通用排序，应用于排序数组
	 * 
	 * @param <E>
	 *            the element type
	 * @param list
	 *            the list
	 * @param index
	 *            对第几列排序
	 * @param isdesc
	 *            是否倒序
	 * @param chinese
	 *            是否检测中文顺序(效率可能略低)
	 */
	public static <E> E[] sort(E[] list, final Integer index,
			final boolean isdesc, final boolean chinese) {
		List<E> eList = toList(list);
		sort(eList, index, isdesc, chinese);
		return eList.toArray(list);
	}

	/**
	 * 功能:通用排序，应用于排序数组列表 创建者： 黄林 2011-12-21.
	 * 
	 * @see #sort(Object[], Integer, boolean, boolean)
	 */
	@SuppressWarnings("unchecked")
	public static <E> void sort(List<E> list, final Integer index,
			final boolean isdesc, final boolean chinese) {
		Collections.sort(list, new Comparator() {
			@Override
			public int compare(Object a, Object b) {
				int ret = 0;
				E r1 = ((E[]) a)[index];
				E r2 = ((E[]) b)[index];
				if (null == r1 || null == r2) {// 比较值为空
					if (null == r1 && null != r2) {
						if (r2 instanceof Number) {
							return ((Number) r2).intValue();
						} else {
							return r2.toString().compareTo("");
						}
					} else if (null != r1 && null == r2) {
						if (r1 instanceof Number) {
							return ((Number) r1).intValue();
						} else {
							return r1.toString().compareTo("");
						}
					} else {
						return 0;
					}
				}
				if (r1 instanceof Number) {// 数字
					Double n1 = ((Number) r1).doubleValue();
					Double n2 = ((Number) r2).doubleValue();
					if (isdesc)// 倒序
					{
						ret = n2.compareTo(n1);
					} else {
						ret = n1.compareTo(n2);
					}
				} else if (r1 instanceof Date)// 时间
				{
					if (isdesc)// 倒序
					{
						ret = ((Date) r2).compareTo((Date) r1);
					} else// 正序
					{
						ret = ((Date) r1).compareTo((Date) r2);
					}
				} else// 字符串
				{
					if (isdesc)// 倒序
						if (chinese) {
							return Collator.getInstance(Locale.CHINESE)
									.compare(r2.toString(), r1.toString());
						} else {
							ret = r2.toString().compareTo(r1.toString());
						}
					else // 正序
					if (chinese) {
						return Collator.getInstance(Locale.CHINESE).compare(
								r1.toString(), r2.toString());
					} else {
						ret = r1.toString().compareTo(r2.toString());
					}
				}
				return ret;
			}
		});
	}

	/**
	 * 通用排序.(正序,不筛选中文)
	 * 
	 * @param <E>
	 *            the element type
	 * @param list
	 *            列表
	 * @param field
	 *            需要排序的字段
	 */
	public static <E> void sort(List<E> list, final String field) {
		sort(list, field, false, false);
	}

	/**
	 * 通用排序.(不筛选中文)
	 * 
	 * @param <E>
	 *            the element type
	 * @param list
	 *            列表
	 * @param field
	 *            需要排序的字段
	 * @param isdesc
	 *            是否反序
	 */
	public static <E> void sort(List<E> list, final String field,
			final boolean isdesc) {
		sort(list, field, isdesc, false);
	}

	/**
	 * 通用排序.
	 * 
	 * @param <E>
	 *            the element type
	 * @param list
	 *            列表
	 * @param field
	 *            需要排序的字段
	 * @param isdesc
	 *            是否反序
	 * @param chinese
	 *            是否使用中文拼音排序
	 */
	@SuppressWarnings("unchecked")
	public static <E> void sort(List<E> list, final String field,
			final boolean isdesc, final boolean chinese) {
		try {
			if (null == list || list.size() == 0) {
				return;
			}
			final Method m = list.get(0).getClass().getMethod(
					"get" + StringTool.upFrist(field));
			Collections.sort(list, new Comparator() {
				@Override
				public int compare(Object a, Object b) {
					int ret = 0;
					try {
						Object r1 = m.invoke(a);
						Object r2 = m.invoke(b);
						if (null == r1 || null == r2) {// 比较值为空
							if (null == r1 && null != r2) {
								if (r2 instanceof Number) {
									return ((Number) r2).intValue();
								} else {
									return r2.toString().compareTo("");
								}
							} else if (null != r1 && null == r2) {
								if (r1 instanceof Number) {
									return ((Number) r1).intValue();
								} else {
									return r1.toString().compareTo("");
								}
							} else {
								return 0;
							}
						}
						if (r1 instanceof Number) {// 数字
							Double n1 = ((Number) r1).doubleValue();
							Double n2 = ((Number) r2).doubleValue();
							if (isdesc)// 倒序
							{
								ret = n2.compareTo(n1);
							} else {
								ret = n1.compareTo(n2);
							}
						} else if (r1 instanceof Date)// 时间
						{
							if (isdesc)// 倒序
							{
								ret = ((Date) r2).compareTo((Date) r1);
							} else// 正序
							{
								ret = ((Date) r1).compareTo((Date) r2);
							}
						} else if (r1 instanceof Boolean)// 布尔
						{
							if (isdesc)// 倒序
							{
								ret = ((Boolean) r2).compareTo((Boolean) r1);
							} else// 正序
							{
								ret = ((Boolean) r1).compareTo((Boolean) r2);
							}
						} else// 字符串
						{
							if (isdesc)// 倒序
								if (chinese) {
									return Collator.getInstance(Locale.CHINESE)
											.compare(r2.toString(),
													r1.toString());
								} else {
									ret = r2.toString()
											.compareTo(r1.toString());
								}
							else // 正序
							if (chinese) {
								return Collator.getInstance(Locale.CHINESE)
										.compare(r1.toString(), r2.toString());
							} else {
								ret = r1.toString().compareTo(r2.toString());
							}
						}
					} catch (IllegalAccessException ie) {
						log.debug("sort fail", ie);
					} catch (InvocationTargetException it) {
						log.debug("sort fail", it);
					}
					return ret;
				}
			});
		} catch (Exception e) {
			log.debug("sort fail", e);
		}
	}

	/**
	 * 将数组转为列表
	 * 
	 * @param array
	 *            the array
	 * @return the array list
	 */
	public static <E> ArrayList<E> toList(E... array) {
		ArrayList<E> arrayList = new ArrayList<E>();
		for (E e : array) {
			arrayList.add(e);
		}
		return arrayList;
	}

	/**
	 * 筛选唯一(去除重复项).
	 * 
	 * @param <E>
	 *            the element type
	 * @param list
	 *            列表
	 * @param field
	 *            需要筛选的字段
	 * @throws Exception
	 *             the exception
	 */
	@SuppressWarnings("unchecked")
	public static <E> void unique(List<E> list, final String field)
			throws Exception {
		Set uniqueFields = new HashSet();
		if (Tool.isNull(list))
		{
			return ;
		}
		Method m1 = list.get(0).getClass().getMethod(
				"get" + StringTool.upFrist(field));
		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			E e = (E) iterator.next();
			Object r1 = m1.invoke(e);
			if (uniqueFields.contains(r1)) {
				iterator.remove();
			} else {
				uniqueFields.add(r1);
			}
		}
	}
	
	/**
	 * 统计总和
	 *
	 * @param <E> the element type
	 * @param list the list
	 * @param field the field
	 * @return the double
	 * @throws Exception the exception
	 * @author 黄林
	 */
	public static <E> Double sum(List<E> list,final String field) throws Exception{
		Double sum=0d;
		if (Tool.isNull(list))
		{
			return sum;
		}
		Method m1 = list.get(0).getClass().getMethod(
				"get" + StringTool.upFrist(field));
		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			E e = (E) iterator.next();
			sum+=(Double)m1.invoke(e);
			
		}
		return sum;
	}
}
