package com.eryansky.core.rpc.utils;

import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 字段上注解查找工具类
 */
public class FieldAnnotationUtils {

    /**
     * 字段注解信息返回VO
     */
    public static class FieldAnnotationInfo {
        /**
         * 类变量
         */
        private Object obj;
        /**
         * 携带该注解的字段对象
         */
        private Field field;
        /**
         * 注解对象
         */
        private Annotation annotation;

        public Object getObj() {
            return obj;
        }

        public void setObj(Object obj) {
            this.obj = obj;
        }

        public Field getField() {
            return field;
        }

        public void setField(Field field) {
            this.field = field;
        }

        public Annotation getAnnotation() {
            return annotation;
        }

        public void setAnnotation(Annotation annotation) {
            this.annotation = annotation;
        }
    }

    /**
     * 判断类变量中是否字段上存在指定的注解，如果存在，则返回字段和注解信息
     *
     * @param obj            类变量
     * @param annotationType 注解类型
     * @return
     */
    public static List<FieldAnnotationUtils.FieldAnnotationInfo> parseFieldAnnotationInfo(Object obj, Class annotationType) {
        return parseFieldAnnotationInfo(obj, annotationType, null);
    }

    /**
     * 判断类变量中是否字段上存在指定的注解，如果存在，则返回字段和注解信息
     *
     * @param obj              类变量
     * @param annotationType   注解类型
     * @param filterFieldClazz 注解适用的字段类型（不适用的字段类型即使字段上面添加改注解也不生效）
     * @return
     */
    public static List<FieldAnnotationUtils.FieldAnnotationInfo> parseFieldAnnotationInfo(Object obj, Class annotationType, Set<Class> filterFieldClazz) {
        if (obj == null) {
            return null;
        }
        List<FieldAnnotationUtils.FieldAnnotationInfo> resultList = new ArrayList<>();
        /**
         * 获取该对象的所有字段，进行遍历判断
         */
        ReflectionUtils.doWithFields(obj.getClass(), field -> {
            // 判断该字段上是否存在注解
            Annotation annotation = AnnotatedElementUtils.findMergedAnnotation(field, annotationType);
            if (annotation != null) { // 如果存在指定注解
                boolean flag = true;
                if (filterFieldClazz != null && !filterFieldClazz.isEmpty()) { // 如果指定了适用的字段的类型
                    for (Class c : filterFieldClazz) {
                        if (c.isAssignableFrom(field.getType())) { // 判断该字段类型是否符合使用类型，使用isAssignableFrom方法是为了父类也进行判断
                            break;
                        }
                        flag = false;
                    }
                }
                if (flag) { // 如果该字段类型符合，则返回字段注解信息
                    FieldAnnotationUtils.FieldAnnotationInfo fieldAnnotationInfo = new FieldAnnotationUtils.FieldAnnotationInfo();
                    fieldAnnotationInfo.setObj(obj);
                    fieldAnnotationInfo.setField(field);
                    fieldAnnotationInfo.setAnnotation(annotation);
                    resultList.add(fieldAnnotationInfo);
                }
            }
        });
        return resultList;
    }
}
