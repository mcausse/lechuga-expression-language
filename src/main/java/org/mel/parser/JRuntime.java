package org.mel.parser;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class JRuntime {

    @SuppressWarnings({ "rawtypes" })
    public static Collection<?> toKeysList(Object elements) {
        if (elements == null) {
            throw new RuntimeException();
        } else if (elements instanceof Collection) {
            /*
             * LIST
             */
            return (Collection<?>) elements;
        } else if (elements instanceof Map) {
            /*
             * MAP
             */
            Map m = (Map) elements;
            return m.keySet();
        } else if (elements.getClass().isArray()) {
            /*
             * ARRAY
             */
            return Arrays.asList((Object[]) elements);
        } else {
            throw new RuntimeException(String.valueOf(elements)); // TODO
        }
    }

    public static Object getByKey(Object o, Object key) {

        if (o == null) {
            throw new RuntimeException(); // TODO +operador .?
        } else if (o instanceof Map) {
            /*
             * MAP
             */
            Map<?, ?> m = (Map<?, ?>) o;
            if (!m.containsKey(key)) {
                throw new RuntimeException("key not found: " + key);
            }
            return m.get(key);
        } else if (o instanceof Collection && key instanceof Number) {
            /*
             * LIST
             */
            Number nindex = (Number) key;
            int index = nindex.intValue();
            Collection<?> c = (Collection<?>) o;
            return new ArrayList<Object>(c).get(index);
        } else if (o.getClass().isArray() && key instanceof Number) {
            /*
             * ARRAY
             */
            Number nindex = (Number) key;
            int index = nindex.intValue();
            return Array.get(o, index);
        } else {
            /*
             * OBJECT
             */
            if (!(key instanceof String)) {
                throw new RuntimeException();
            }

            Class<? extends Object> beanClass = o.getClass();

            /*
             * PROVA PROPIETAT DE JAVA BEAN
             */
            BeanInfo info;
            try {
                info = Introspector.getBeanInfo(beanClass);
            } catch (IntrospectionException e) {
                throw new RuntimeException("describing " + beanClass.getName(), e);
            }
            PropertyDescriptor[] pds = info.getPropertyDescriptors();
            for (PropertyDescriptor pd : pds) {
                if (pd.getName().equals(key)) {
                    try {
                        return pd.getReadMethod().invoke(o);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            /*
             * PROVA METHOD
             */
            for (Method m : beanClass.getMethods()) {
                if (m.getName().equals(key) && m.getParameterTypes().length == 0) {
                    try {
                        return m.invoke(o);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            throw new RuntimeException();
        }

    }

    static final List<Class<? extends Number>> ntypes = new ArrayList<Class<? extends Number>>();

    static {
        ntypes.add(Byte.class);
        ntypes.add(Short.class);
        ntypes.add(Integer.class);
        ntypes.add(Long.class);
        ntypes.add(Float.class);
        ntypes.add(Double.class);
    }

    static int findUpperType(Object n1, Object n2) {
        int index1 = findType(n1);
        int index2 = findType(n2);
        return index1 >= index2 ? index1 : index2;
    }

    static int findType(Object n) {
        return ntypes.indexOf(n.getClass());
    }

    static Number getNumber(Object o) {
        if (o == null || !(o instanceof Number)) {
            throw new RuntimeException("unexpected value: " + String.valueOf(o) + " (" + o.getClass().getName() + ")");
        }
        return (Number) o;
    }

    public static Byte toByte(Object o) {
        return getNumber(o).byteValue();
    }

    public static Short toShort(Object o) {
        return getNumber(o).shortValue();
    }

    public static Integer toInteger(Object o) {
        return getNumber(o).intValue();
    }

    public static Long toLong(Object o) {
        return getNumber(o).longValue();
    }

    public static Float toFloat(Object o) {
        return getNumber(o).floatValue();
    }

    public static Double toDouble(Object o) {
        return getNumber(o).doubleValue();
    }

    public static String toString(Object o) {
        return String.valueOf(o);
    }

    public static Object neg(Object o) {

        try {
            Number n = getNumber(o);

            int type = findType(n);

            switch (type) {
            case 0:
                return -n.byteValue();
            case 1:
                return -n.shortValue();
            case 2:
                return -n.intValue();
            case 3:
                return -n.longValue();
            case 4:
                return -n.floatValue();
            case 5:
                return -n.doubleValue();
            default:
                throw new RuntimeException();
            }
        } catch (Exception e) {
            throw new RuntimeException("-" + String.valueOf(o), e);
        }
    }

    public static Object add(Object o1, Object o2) {

        if (o1 != null && o1 instanceof String) {
            String s1 = (String) o1;
            String s2 = String.valueOf(o2);
            return s1 + s2;
        }

        try {
            Number n1 = getNumber(o1);
            Number n2 = getNumber(o2);

            int type = findUpperType(n1, n2);

            switch (type) {
            case 0:
                return n1.byteValue() + n2.byteValue();
            case 1:
                return Short.valueOf((short) (n1.shortValue() + n2.shortValue()));
            case 2:
                return n1.intValue() + n2.intValue();
            case 3:
                return n1.longValue() + n2.longValue();
            case 4:
                return n1.floatValue() + n2.floatValue();
            case 5:
                return n1.doubleValue() + n2.doubleValue();
            default:
                throw new RuntimeException();
            }
        } catch (Exception e) {
            throw new RuntimeException(String.valueOf(o1) + "+" + String.valueOf(o2), e);
        }
    }

    public static Number sub(Object o1, Object o2) {

        try {
            Number n1 = getNumber(o1);
            Number n2 = getNumber(o2);

            int type = findUpperType(n1, n2);

            switch (type) {
            case 0:
                return n1.byteValue() - n2.byteValue();
            case 1:
                return n1.shortValue() - n2.shortValue();
            case 2:
                return n1.intValue() - n2.intValue();
            case 3:
                return n1.longValue() - n2.longValue();
            case 4:
                return n1.floatValue() - n2.floatValue();
            case 5:
                return n1.doubleValue() - n2.doubleValue();
            default:
                throw new RuntimeException();
            }
        } catch (Exception e) {
            throw new RuntimeException(String.valueOf(o1) + "+" + String.valueOf(o2), e);
        }
    }

    public static Number mul(Object o1, Object o2) {

        try {
            Number n1 = getNumber(o1);
            Number n2 = getNumber(o2);

            int type = findUpperType(n1, n2);

            switch (type) {
            case 0:
                return n1.byteValue() * n2.byteValue();
            case 1:
                return n1.shortValue() * n2.shortValue();
            case 2:
                return n1.intValue() * n2.intValue();
            case 3:
                return n1.longValue() * n2.longValue();
            case 4:
                return n1.floatValue() * n2.floatValue();
            case 5:
                return n1.doubleValue() * n2.doubleValue();
            default:
                throw new RuntimeException();
            }
        } catch (Exception e) {
            throw new RuntimeException(String.valueOf(o1) + "+" + String.valueOf(o2), e);
        }
    }

    public static Number div(Object o1, Object o2) {

        try {
            Number n1 = getNumber(o1);
            Number n2 = getNumber(o2);

            int type = findUpperType(n1, n2);

            switch (type) {
            case 0:
                return n1.byteValue() / n2.byteValue();
            case 1:
                return n1.shortValue() / n2.shortValue();
            case 2:
                return n1.intValue() / n2.intValue();
            case 3:
                return n1.longValue() / n2.longValue();
            case 4:
                return n1.floatValue() / n2.floatValue();
            case 5:
                return n1.doubleValue() / n2.doubleValue();
            default:
                throw new RuntimeException();
            }
        } catch (Exception e) {
            throw new RuntimeException(String.valueOf(o1) + "+" + String.valueOf(o2), e);
        }
    }

    public static Number mod(Object o1, Object o2) {

        try {
            Number n1 = getNumber(o1);
            Number n2 = getNumber(o2);

            int type = findUpperType(n1, n2);

            switch (type) {
            case 0:
                return n1.byteValue() % n2.byteValue();
            case 1:
                return n1.shortValue() % n2.shortValue();
            case 2:
                return n1.intValue() % n2.intValue();
            case 3:
                return n1.longValue() % n2.longValue();
            case 4:
                return n1.floatValue() % n2.floatValue();
            case 5:
                return n1.doubleValue() % n2.doubleValue();
            default:
                throw new RuntimeException();
            }
        } catch (Exception e) {
            throw new RuntimeException(String.valueOf(o1) + "+" + String.valueOf(o2), e);
        }
    }

    public static Boolean gt(Object o1, Object o2) {

        try {
            Number n1 = getNumber(o1);
            Number n2 = getNumber(o2);

            int type = findUpperType(n1, n2);

            switch (type) {
            case 0:
                return n1.byteValue() > n2.byteValue();
            case 1:
                return n1.shortValue() > n2.shortValue();
            case 2:
                return n1.intValue() > n2.intValue();
            case 3:
                return n1.longValue() > n2.longValue();
            case 4:
                return n1.floatValue() > n2.floatValue();
            case 5:
                return n1.doubleValue() > n2.doubleValue();
            default:
                throw new RuntimeException();
            }
        } catch (Exception e) {
            throw new RuntimeException(String.valueOf(o1) + "+" + String.valueOf(o2), e);
        }
    }

    public static Boolean lt(Object o1, Object o2) {

        try {
            Number n1 = getNumber(o1);
            Number n2 = getNumber(o2);

            int type = findUpperType(n1, n2);

            switch (type) {
            case 0:
                return n1.byteValue() < n2.byteValue();
            case 1:
                return n1.shortValue() < n2.shortValue();
            case 2:
                return n1.intValue() < n2.intValue();
            case 3:
                return n1.longValue() < n2.longValue();
            case 4:
                return n1.floatValue() < n2.floatValue();
            case 5:
                return n1.doubleValue() < n2.doubleValue();
            default:
                throw new RuntimeException();
            }
        } catch (Exception e) {
            throw new RuntimeException(String.valueOf(o1) + "+" + String.valueOf(o2), e);
        }
    }

    public static Boolean ge(Object o1, Object o2) {

        try {
            Number n1 = getNumber(o1);
            Number n2 = getNumber(o2);

            int type = findUpperType(n1, n2);

            switch (type) {
            case 0:
                return n1.byteValue() >= n2.byteValue();
            case 1:
                return n1.shortValue() >= n2.shortValue();
            case 2:
                return n1.intValue() >= n2.intValue();
            case 3:
                return n1.longValue() >= n2.longValue();
            case 4:
                return n1.floatValue() >= n2.floatValue();
            case 5:
                return n1.doubleValue() >= n2.doubleValue();
            default:
                throw new RuntimeException();
            }
        } catch (Exception e) {
            throw new RuntimeException(String.valueOf(o1) + "+" + String.valueOf(o2), e);
        }
    }

    public static Boolean le(Object o1, Object o2) {

        try {
            Number n1 = getNumber(o1);
            Number n2 = getNumber(o2);

            int type = findUpperType(n1, n2);

            switch (type) {
            case 0:
                return n1.byteValue() <= n2.byteValue();
            case 1:
                return n1.shortValue() <= n2.shortValue();
            case 2:
                return n1.intValue() <= n2.intValue();
            case 3:
                return n1.longValue() <= n2.longValue();
            case 4:
                return n1.floatValue() <= n2.floatValue();
            case 5:
                return n1.doubleValue() <= n2.doubleValue();
            default:
                throw new RuntimeException();
            }
        } catch (Exception e) {
            throw new RuntimeException(String.valueOf(o1) + "+" + String.valueOf(o2), e);
        }
    }

    static boolean safeEquals(Object x, Object y) {
        return x == y || x != null && x.equals(y);
    }

    public static Boolean eq(Object o1, Object o2) {
        return safeEquals(o1, o2);
    }

    public static Boolean ne(Object o1, Object o2) {
        return !safeEquals(o1, o2);
    }

    static Boolean getBoolean(Object o) {
        if (o == null || !(o instanceof Boolean)) {
            throw new RuntimeException();
        }
        return (Boolean) o;
    }

    public static Boolean and(Object o1, Object o2) {
        Boolean b1 = getBoolean(o1);
        Boolean b2 = getBoolean(o2);
        return b1 && b2;
    }

    public static Boolean or(Object o1, Object o2) {
        Boolean b1 = getBoolean(o1);
        Boolean b2 = getBoolean(o2);
        return b1 || b2;
    }

    public static Boolean not(Object o) {
        Boolean b = getBoolean(o);
        return !b;
    }

}
