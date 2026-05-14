package com.ops.zen.utils;


import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author xyn
 * @Date 2021/3/18 15:49
 * @Description
 */
public class CollectionUtils {

    public static <T> List<T> toList(T[] array) {
        return new ArrayList<>(Arrays.asList(array));
    }

    public static <T> T random(List<T> list) {
        Assert.isTrue(list != null && list.size() > 0,
                "列表不能为null，size必须大于0");
        return list.get(new Random().nextInt(list.size()));
    }


    /**
     * elements中排除exceptElements再随机获取
     *
     * @param elements
     * @param exceptElements
     * @return
     */
    public static String random(List<String> elements, List<String> exceptElements) {
        List<String> selectList = elements.stream().filter(el -> !exceptElements.contains(el)).collect(Collectors.toList());
        if (selectList.size() == 0) {
            return null;
        } else {
            return random(selectList);
        }
    }

    /**
     * Null-safe check if the specified collection is empty.
     * <p>
     * Null returns true.
     *
     * @param coll the collection to check, may be null
     * @return true if empty or null
     * @since Commons Collections 3.2
     */
    public static boolean isEmpty(Collection coll) {
        return (coll == null || coll.isEmpty());
    }

    /**
     * Null-safe check if the specified collection is not empty.
     * <p>
     * Null returns false.
     *
     * @param coll the collection to check, may be null
     * @return true if non-null and non-empty
     * @since Commons Collections 3.2
     */
    public static boolean isNotEmpty(Collection coll) {
        return !CollectionUtils.isEmpty(coll);
    }


    public static boolean isEmpty(Iterator it) {
        return !it.hasNext();
    }

    public static boolean isNotEmpty(Iterator it) {
        return !CollectionUtils.isEmpty(it);
    }

    /**
     * 求a和b的并集
     *
     * @param a
     * @param b
     * @param <T>
     * @return
     */
    public static <T> List<T> union(List<T> a, List<T> b) {
        List<T> all = new ArrayList<>();
        if (isNotEmpty(a)) {
            all.addAll(a);
        }
        if (isNotEmpty(b)) {
            all.addAll(b);
        }
        return all.stream().distinct().collect(Collectors.toList());
    }
}
