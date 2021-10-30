@file:JvmName("ReflectUtil")
@file:Suppress("UNCHECKED_CAST")

package example.android.editable.ktx

import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method

/**
 * Locates a given field anywhere in the class inheritance hierarchy.
 *
 * [this]                 an object to search the field into.
 * [name]                 field name
 * [NoSuchFieldException] if the field cannot be located
 */
@Throws(NoSuchFieldException::class)
fun Any.findField(name: String) = javaClass.findField(name)

/**
 * Locates a given field anywhere in the class inheritance hierarchy.
 *
 * [this]                 a class to search the method into.
 * [name]                 field name
 * [NoSuchFieldException] if the field cannot be located
 */
@Throws(NoSuchFieldException::class)
fun Class<*>.findField(name: String): Field {
    var clazz: Class<*>? = this
    while (clazz != null) {
        try {
            return clazz.getDeclaredField(name).apply {
                if (!isAccessible) isAccessible = true
            }
        } catch (e: NoSuchFieldException) {
            // ignore and search next
        }
        clazz = clazz.superclass
    }
    throw NoSuchFieldException("Field $name not found in $this")
}

/**
 * Locates a given field anywhere in the class inheritance hierarchy And get its object [T].
 *
 * [this]                     an object to search the field into.
 * [name]                     field name
 * [NoSuchFieldException]     if the field cannot be located
 * [ClassCastException]       if the type coercion fails
 * [IllegalArgumentException] if the parameter is abnormal
 * [IllegalAccessException]   if not accessible
 */
@Throws(NoSuchMethodException::class, ClassCastException::class, IllegalArgumentException::class, IllegalAccessException::class)
fun <T : Any> Any.findFieldObject(name: String): T = javaClass.findField(name).get(this) as T

/**
 * Locates a given method anywhere in the class inheritance hierarchy.
 *
 * [this]                  an object to search the method into.
 * [name]                  method name
 * [parameterTypes]        method parameter types
 * [NoSuchMethodException] if the method cannot be located
 */
@Throws(NoSuchMethodException::class)
fun Any.findMethod(name: String, vararg parameterTypes: Class<*>) = javaClass.findMethod(name, *parameterTypes)

/**
 * Locates a given method anywhere in the class inheritance hierarchy.
 *
 * [this]                  a class to search the method into.
 * [name]                  method name
 * [parameterTypes]        method parameter types
 * [NoSuchMethodException] if the method cannot be located
 */
@Throws(NoSuchMethodException::class)
fun Class<*>.findMethod(name: String, vararg parameterTypes: Class<*>): Method {
    var clazz: Class<*>? = this
    while (clazz != null) {
        try {
            return clazz.getDeclaredMethod(name, *parameterTypes).apply {
                if (!isAccessible) isAccessible = true
            }
        } catch (e: NoSuchMethodException) {
            // ignore and search next
        }
        clazz = clazz.superclass
    }
    throw NoSuchMethodException("Method "
            + name
            + " with parameters "
            + listOf(*parameterTypes)
            + " not found in " + this)
}

/**
 * Locates a given constructor anywhere in the class inheritance hierarchy.
 *
 * [this]                  an object to search the constructor into.
 * [parameterTypes]        constructor parameter types
 * [NoSuchMethodException] if the constructor cannot be located
 */
@Throws(NoSuchMethodException::class)
fun Any.findConstructor(vararg parameterTypes: Class<*>): Constructor<*> {
    var clazz: Class<*>? = javaClass
    while (clazz != null) {
        try {
            return clazz.getDeclaredConstructor(*parameterTypes).apply {
                if (!isAccessible) isAccessible = true
            }
        } catch (e: NoSuchMethodException) {
            // ignore and search next
        }
        clazz = clazz.superclass
    }
    throw NoSuchMethodException("Constructor"
            + " with parameters "
            + listOf(*parameterTypes)
            + " not found in " + javaClass)
}