/**
 *  Copyright (c) 2012-2022 https://www.eryansky.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.common.utils.collections;

import java.util.*;
import java.util.function.Predicate;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;

import com.eryansky.common.utils.reflection.ReflectionUtils;

/**
 * Collections工具集.
 * 在JDK的Collections和Guava的Collections2后, 命名为Collections3.
 * 
 * @author Eryan
 */
public class Collections3 {

	/**
	 * 提取集合中的对象的两个属性(通过Getter函数), 组合成Map.
	 * 
	 * @param collection 来源集合.
	 * @param keyPropertyName 要提取为Map中的Key值的属性名.
	 * @param valuePropertyName 要提取为Map中的Value值的属性名.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
    public static Map extractToMap(final Collection collection, final String keyPropertyName,
			final String valuePropertyName) {
		Map map = new HashMap(collection.size());

		try {
			for (Object obj : collection) {
				map.put(PropertyUtils.getProperty(obj, keyPropertyName),
						PropertyUtils.getProperty(obj, valuePropertyName));
			}
		} catch (Exception e) {
			throw ReflectionUtils.convertReflectionExceptionToUnchecked(e);
		}

		return map;
	}

	/**
	 * 提取集合中的对象的一个属性(通过Getter函数), 组合成List.
	 * 
	 * @param collection 来源集合.
	 * @param propertyName 要提取的属性名.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
    public static List extractToList(final Collection collection, final String propertyName) {
		List list = new ArrayList(collection.size());

		try {
			for (Object obj : collection) {
				list.add(PropertyUtils.getProperty(obj, propertyName));
			}
		} catch (Exception e) {
			throw ReflectionUtils.convertReflectionExceptionToUnchecked(e);
		}

		return list;
	}

	/**
	 * 提取集合中的对象的一个属性(通过Getter函数), 组合成Set.
	 *
	 * @param collection 来源集合.
	 * @param propertyName 要提取的属性名.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Set extractToSet(final Collection collection, final String propertyName) {
		Set set = new HashSet(collection.size());

		try {
			for (Object obj : collection) {
				set.add(PropertyUtils.getProperty(obj, propertyName));
			}
		} catch (Exception e) {
			throw ReflectionUtils.convertReflectionExceptionToUnchecked(e);
		}

		return set;
	}

	/**
	 * 提取集合中的对象的一个属性(通过Getter函数), 组合成由分割符分隔的字符串.
	 * 
	 * @param collection 来源集合.
	 * @param propertyName 要提取的属性名.
	 * @param separator 分隔符.
	 */
	@SuppressWarnings("rawtypes")
    public static String extractToString(final Collection collection, final String propertyName, final String separator) {
		List list = extractToList(collection, propertyName);
		return StringUtils.join(list, separator);
	}

	/**
	 * 转换Collection所有元素(通过toString())为String, 中间以 separator分隔。
	 */
	public static String convertToString(@SuppressWarnings("rawtypes") final Collection collection, final String separator) {
		return StringUtils.join(collection, separator);
	}

	/**
	 * 转换Collection所有元素(通过toString())为String, 每个元素的前面加入prefix，后面加入postfix，如<div>mymessage</div>。
	 */
	public static String convertToString(@SuppressWarnings("rawtypes") final Collection collection, final String prefix, final String postfix) {
		StringBuilder builder = new StringBuilder();
		for (Object o : collection) {
			builder.append(prefix).append(o).append(postfix);
		}
		return builder.toString();
	}

	/**
	 * 判断是否为空.
	 */
	@SuppressWarnings("rawtypes")
    public static boolean isEmpty(Collection collection) {
		return (collection == null || collection.isEmpty());
	}

    /**
     * 判断是否不为空.
     */
    public static boolean isNotEmpty(Collection collection) {
        return !isEmpty(collection);
    }

	/**
	 * 取得Collection的第一个元素，如果collection为空返回null.
	 */
	public static <T> T getFirst(Collection<T> collection) {
		if (isEmpty(collection)) {
			return null;
		}

		return collection.iterator().next();
	}

	/**
	 * 查找并删除第一个元素
	 * @param collection
	 * @param test
	 * @param <T>
	 * @return
	 */
	public static <T> T findAndRemoveFirst(Iterable<? extends T> collection, Predicate<? super T> test) {
		T value = null;
		for (Iterator<? extends T> it = collection.iterator(); it.hasNext();)
			if (test.test(value = it.next())) {
				it.remove();
				return value;
			}
		return null;
	}
	/**
	 * 获取Collection的最后一个元素 ，如果collection为空返回null.
	 */
	public static <T> T getLast(Collection<T> collection) {
		if (isEmpty(collection)) {
			return null;
		}

		//当类型为List时，直接取得最后一个元素 。
		if (collection instanceof List) {
			List<T> list = (List<T>) collection;
			return list.get(list.size() - 1);
		}

		//其他类型通过iterator滚动到最后一个元素.
		Iterator<T> iterator = collection.iterator();
		while (true) {
			T current = iterator.next();
			if (!iterator.hasNext()) {
				return current;
			}
		}
	}

	/**
	 * 返回a+b的新List.
	 */
	public static <T> List<T> union(final Collection<T> a, final Collection<T> b) {
		List<T> result = new ArrayList<T>(a);
		result.addAll(b);
		return result;
	}

	/**
	 * 返回a-b的新List.
	 */
	public static <T> List<T> subtract(final Collection<T> a, final Collection<T> b) {
		List<T> list = new ArrayList<T>(a);
		for (T element : b) {
			list.remove(element);
		}

		return list;
	}

    /**
     * 获取list2在list1中的无重复补集
     * 注意：需要重写List集合中对象的hashcode和equals方法
     * A={2,3} comple B={1,1,2,6} => C={1,6}
     * @param a
     * @param b
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T>  comple(Collection<T> a,Collection<T> b){
        Set<T> set = new LinkedHashSet();
        set.addAll(a);
        set.removeAll(intersection(a, b));
        return new ArrayList(set);
    }
	/**
	 * 返回a与b的交集的新List.
	 */
	public static <T> List<T> intersection(Collection<T> a, Collection<T> b) {
		List<T> list = new ArrayList<T>();

		for (T element : a) {
			if (b.contains(element)) {
				list.add(element);
			}
		}
		return list;
	}

    /**
     * 返回a与b的无重复并集的新List.
     */
    public static <T> List<T>  aggregate(Collection<T> a,Collection<T> b){
        List<T> list = new ArrayList<T>();
        if (a != null) {
            Iterator it = a.iterator();
            while (it.hasNext()) {
                T o = (T)it.next();
                if (!list.contains(o)) {
                    list.add(o);
                }
            }
        }
        if (b != null) {
            Iterator it = b.iterator();
            while (it.hasNext()) {
                T o = (T) it.next();
                if (!list.contains(o))
                    list.add(o);
            }
        }
        return list;
    }


	/**
	 * 得到分页后的数据
	 *
	 * @param a
	 * @param pageNo 页码
	 * @param pageSize 页大小
	 * @return 分页后结果
	 */
	public static <T> List<T> getPagedList(List<T> a,int pageNo,int pageSize) {
		int fromIndex = (pageNo - 1) * pageSize;
		if (fromIndex >= a.size()) {
			return Collections.emptyList();
		}

		int toIndex = pageNo * pageSize;
		if (toIndex >= a.size()) {
			toIndex = a.size();
		}
		return a.subList(fromIndex, toIndex);
	}

	/**
	 * List分组
	 * @param list
	 * @param groupSize 分组大小
	 * @return
	 */
	public static List<List> splitList(List list , int groupSize){
		int length = list.size();
		// 计算可以分成多少组
		int num = ( length + groupSize - 1 )/groupSize ; // TODO
		List<List> newList = new ArrayList<>(num);
		for (int i = 0; i < num; i++) {
			// 开始位置
			int fromIndex = i * groupSize;
			// 结束位置
			int toIndex = (i+1) * groupSize < length ? ( i+1 ) * groupSize : length ;
			newList.add(list.subList(fromIndex,toIndex)) ;
		}
		return  newList ;
	}

	/**
	 * 将一组数据平均分成n组
	 *
	 * @param source 要分组的数据源
	 * @param n      平均分成n组
	 * @param <T>
	 * @return
	 */
	public static <T> List<List<T>> averageAssign(List<T> source, int n) {
		List<List<T>> result = new ArrayList<>();
		int remainder = source.size() % n;  //(先计算出余数)
		int number = source.size() / n;  //然后是商
		int offset = 0;//偏移量
		for (int i = 0; i < n; i++) {
			List<T> value = null;
			if (remainder > 0) {
				value = source.subList(i * number + offset, (i + 1) * number + offset + 1);
				remainder--;
				offset++;
			} else {
				value = source.subList(i * number + offset, (i + 1) * number + offset);
			}
			result.add(value);
		}
		return result;
	}

	/**
	 * 将一组数据固定分组，每组n个元素
	 * @param source 要分组的数据源
	 * @param n      每组n个元素
	 * @param <T>
	 * @return
	 */
	public static <T> List<List<T>> fixedGrouping(List<T> source, int n) {

		if (null == source || source.size() == 0 || n <= 0)
			return Collections.emptyList();
		List<List<T>> result = new ArrayList<>();

		int sourceSize = source.size();
		int size = (sourceSize % n) == 0 ? (sourceSize / n) : ((source.size() / n) + 1);
		for (int i = 0; i < size; i++) {
			List<T> subset = new ArrayList<T>();
			for (int j = i * n; j < (i + 1) * n; j++) {
				if (j < sourceSize) {
					subset.add(source.get(j));
				}
			}
			result.add(subset);
		}
		return result;
	}

	/**
	 * 将一组数据固定分组，每组n个元素
	 *
	 * @param source 要分组的数据源
	 * @param n      每组n个元素
	 * @param <T>
	 * @return
	 */
	public static <T> List<List<T>> fixedGrouping2(List<T> source, int n) {

		if (null == source || source.size() == 0 || n <= 0)
			return Collections.emptyList();
		List<List<T>> result = new ArrayList<>();
		int remainder = source.size() % n;
		int size = (source.size() / n);
		for (int i = 0; i < size; i++) {
			List<T> subset = null;
			subset = source.subList(i * n, (i + 1) * n);
			result.add(subset);
		}
		if (remainder > 0) {
			List<T> subset = null;
			subset = source.subList(size * n, size * n + remainder);
			result.add(subset);
		}
		return result;
	}

	/*
	 * List分割
	 */
	public static List<List<String>> groupList(List<String> list) {
		List<List<String>> listGroup = new ArrayList<>();
		int listSize = list.size();
		//子集合的长度
		int toIndex = 2;
		for (int i = 0; i < list.size(); i += 2) {
			if (i + 2 > listSize) {
				toIndex = listSize - i;
			}
			List<String> newList = list.subList(i, i + toIndex);
			listGroup.add(newList);
		}
		return listGroup;
	}

	/**
	 * Byte[] to byte[]
	 *
	 * @param oBytes
	 * @return
	 */
	public static byte[] toPrimitives(Byte[] oBytes) {
		byte[] bytes = new byte[oBytes.length];
		for (int i = 0; i < oBytes.length; i++) {
			bytes[i] = oBytes[i];
		}
		return bytes;
	}

	/**
	 * byte[] to Byte[]
	 *
	 * @param bytesPrim
	 * @return
	 */
	public static Byte[] toObjects(byte[] bytesPrim) {
		Byte[] bytes = new Byte[bytesPrim.length];
		int i = 0;
		for (byte b : bytesPrim) bytes[i++] = b; //Autoboxing
		return bytes;

	}

}