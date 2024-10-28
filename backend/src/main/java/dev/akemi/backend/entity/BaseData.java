package dev.akemi.backend.entity;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.function.Consumer;

/**
 * BaseData接口定义了一个基础的数据转换功能，用于将当前对象的字段值转换为指定类型的视图对象（View Object）。
 */
public interface BaseData {

    /**
     * 将当前对象转换为指定类型的视图对象，并接受一个操作该对象的Consumer操作。
     *
     * @param clazz 目标视图对象的Class对象，用于指定转换后的类型。
     * @param consumer Consumer操作，用于对转换后的视图对象进行额外的操作。
     * @param <V> 视图对象的类型参数。
     * @return 转换后的视图对象。
     */
    default <V> V asViewObject(Class<V> clazz, Consumer<V> consumer) {
        V v = this.asViewObject(clazz); // 调用无Consumer参数的转换方法
        consumer.accept(v); // 执行Consumer操作
        return v; // 返回视图对象
    }

    /**
     * 将当前对象转换为指定类型的视图对象。
     *
     * @param clazz 目标视图对象的Class对象，用于指定转换后的类型。
     * @param <V> 视图对象的类型参数。
     * @return 转换后的视图对象。
     * @throws RuntimeException 当反射操作失败时抛出运行时异常。
     */
    default <V> V asViewObject(Class<V> clazz) {
        try {
            Field[] declaredFields = clazz.getDeclaredFields(); // 获取目标类的所有声明字段
            Constructor<V> constructor = clazz.getConstructor(); // 获取目标类的无参构造函数
            V v = constructor.newInstance(); // 通过无参构造函数实例化视图对象
            for (Field declaredField : declaredFields) { // 遍历目标类的每个字段
                convert(declaredField, v); // 调用私有的convert方法进行字段赋值转换
            }
            return v; // 返回转换后的视图对象
        } catch (ReflectiveOperationException exception) {
            throw new RuntimeException(exception); // 捕获反射异常并包装为运行时异常抛出
        }
    }

    /**
     * 将当前对象的指定字段值复制到目标视图对象的同名字段中。
     *
     * @param field 目标视图对象的字段。
     * @param vo 目标视图对象。
     */
    private void convert(Field field, Object vo) {
        try {
            Field source = this.getClass().getDeclaredField(field.getName()); // 获取当前对象中与目标字段同名的字段
            source.setAccessible(true); // 设置源字段为可访问
            field.setAccessible(true); // 设置目标字段为可访问
            field.set(vo, source.get(this)); // 将源字段的值赋给目标视图对象的同名字段
        } catch (IllegalAccessException | NoSuchFieldException e) {
            // 捕获字段不存在或不可访问的异常，不进行任何处理
        }
    }
}
