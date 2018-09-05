package org.mel.test;

import static org.junit.Assert.assertEquals;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import org.junit.Test;

public class QWERTYU2 {

    @Test
    public void testName() throws Exception {

        {
            Pizza_ p_ = new Pizza_();
            assertEquals("pizza", p_.getAliasedName());
            assertEquals("price", p_.price.getAliasedName());
        }
        {
            Pizza_ p_ = new Pizza_("p");
            assertEquals("pizza p", p_.getAliasedName());
            assertEquals("p.price", p_.price.getAliasedName());

            IQueryObject q = Relational.and( //
                    p_.id.eq(100L), //
                    p_.name.ilike(ELike.CONTAINS, "alo"), //
                    p_.price.between(5.0, 18.5), //
                    p_.type.in(EPizzaType.REGULAR, EPizzaType.DELUX) //
            );
            assertEquals(
                    "id_pizza=? and upper(name) like upper(?) and price between ? and ? and type in (?,?) -- [100(Long), %alo%(String), 5.0(Double), 18.5(Double), REGULAR(String), DELUX(String)]",
                    q.toString());
        }
        {

            Operations o = new Operations();
            Pizza romana = new Pizza(100L, "romana", 12.5, EPizzaType.DELUX);

            assertEquals(
                    "insert into pizza (id_pizza, name, price, type) values (?, ?, ?, ?) -- [100(Long), romana(String), 12.5(Double), DELUX(String)]",
                    o.insert(new Pizza_(), romana).toString());
            assertEquals(
                    "update pizza set name=?, price=?, type=? where id_pizza=? -- [romana(String), 12.5(Double), DELUX(String), 100(Long)]",
                    o.update(new Pizza_(), romana).toString());
            assertEquals("delete from pizza where id_pizza=? -- [100(Long)]",
                    o.delete(new Pizza_(), romana).toString());
        }
    }

    public static class Pizza_ extends Table<Pizza> {

        public final Column<Pizza, Long> id = addPkColumn(Long.class, "idPizza", "id_pizza");
        public final Column<Pizza, String> name = addColumn(String.class, "name", "name");
        public final Column<Pizza, Double> price = addColumn(Double.class, "price", "price");
        public final Column<Pizza, EPizzaType> type = addColumn(EPizzaType.class, "type", "type",
                new EnumColumnHandler<>(EPizzaType.class));

        public Pizza_() {
            super(Pizza.class, "pizza");
        }

        public Pizza_(String alias) {
            super(Pizza.class, "pizza", alias);
        }
    }

    public static enum EPizzaType {
        REGULAR, DELUX;
    }

    public static class Pizza {

        Long idPizza;
        String name;
        Double price;
        EPizzaType type;

        public Pizza() {
            super();
        }

        public Pizza(Long idPizza, String name, Double price, EPizzaType type) {
            super();
            this.idPizza = idPizza;
            this.name = name;
            this.price = price;
            this.type = type;
        }

        public Long getIdPizza() {
            return idPizza;
        }

        public void setIdPizza(Long idPizza) {
            this.idPizza = idPizza;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Double getPrice() {
            return price;
        }

        public void setPrice(Double price) {
            this.price = price;
        }

        public EPizzaType getType() {
            return type;
        }

        public void setType(EPizzaType type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return "Pizza [idPizza=" + idPizza + ", name=" + name + ", price=" + price + ", type=" + type + "]";
        }
    }

    public static class Operations {

        public <E> IQueryObject insert(Table<E> table, E entity) {
            QueryObject q = new QueryObject();
            q.append("insert into ");
            q.append(table.getTableName());
            q.append(" (");
            {
                StringJoiner j = new StringJoiner(", ");
                for (Column<E, ?> c : table.getColumns()) {
                    j.add(c.getColumnName());
                }
                q.append(j.toString());
            }
            q.append(") values (");
            {
                StringJoiner j = new StringJoiner(", ");
                for (Column<E, ?> c : table.getColumns()) {
                    j.add("?");
                    q.addArg(c.storeValue(entity));
                }
                q.append(j.toString());
            }
            q.append(")");
            return q;
        }

        public <E> IQueryObject update(Table<E> table, E entity) {
            QueryObject q = new QueryObject();
            q.append("update ");
            q.append(table.getTableName());
            q.append(" set ");
            {
                StringJoiner j = new StringJoiner(", ");
                for (Column<E, ?> c : table.getColumns()) {
                    if (!c.isPk) {
                        j.add(c.getColumnName() + "=?");
                        q.addArg(c.storeValue(entity));
                    }
                }
                q.append(j.toString());
            }
            q.append(" where ");
            {
                StringJoiner j = new StringJoiner(" and ");
                for (Column<E, ?> c : table.getColumns()) {
                    if (c.isPk) {
                        j.add(c.getColumnName() + "=?");
                        q.addArg(c.storeValue(entity));
                    }
                }
                q.append(j.toString());
            }
            return q;
        }

        public <E> IQueryObject delete(Table<E> table, E entity) {
            QueryObject q = new QueryObject();
            q.append("delete from ");
            q.append(table.getTableName());
            q.append(" where ");
            {
                StringJoiner j = new StringJoiner(" and ");
                for (Column<E, ?> c : table.getColumns()) {
                    if (c.isPk) {
                        j.add(c.getColumnName() + "=?");
                        q.addArg(c.storeValue(entity));
                    }
                }
                q.append(j.toString());
            }
            return q;
        }
    }

    public static interface Aliasable {
        String getAliasedName();
    }

    public static class Table<E> {

        final String alias;
        final Class<E> entityClass;
        final String tableName;
        final List<Column<E, ?>> columns;

        public Table(Class<E> entityClass, String tableName, String alias) {
            super();
            this.entityClass = entityClass;
            this.tableName = tableName;
            this.columns = new ArrayList<>();
            this.alias = alias;
        }

        public Table(Class<E> entityClass, String tableName) {
            this(entityClass, tableName, null);
        }

        protected <T> Column<E, T> addColumn(Class<T> columnClass, String propertyPath, String columnName) {
            Column<E, T> c = new Column<>(this, columnClass, propertyPath, columnName, false,
                    Handlers.getHandlerFor(columnClass));
            this.columns.add(c);
            return c;
        }

        protected <T> Column<E, T> addPkColumn(Class<T> columnClass, String propertyPath, String columnName) {
            Column<E, T> c = new Column<>(this, columnClass, propertyPath, columnName, true,
                    Handlers.getHandlerFor(columnClass));
            this.columns.add(c);
            return c;
        }

        protected <T> Column<E, T> addColumn(Class<T> columnClass, String propertyPath, String columnName,
                ColumnHandler<T> handler) {
            Column<E, T> c = new Column<>(this, columnClass, propertyPath, columnName, false, handler);
            this.columns.add(c);
            return c;
        }

        protected <T> Column<E, T> addPkColumn(Class<T> columnClass, String propertyPath, String columnName,
                ColumnHandler<T> handler) {
            Column<E, T> c = new Column<>(this, columnClass, propertyPath, columnName, true, handler);
            this.columns.add(c);
            return c;
        }

        public Class<E> getEntityClass() {
            return entityClass;
        }

        public String getTableName() {
            return tableName;
        }

        public List<Column<E, ?>> getColumns() {
            return columns;
        }

        public String getAlias() {
            return alias;
        }

        public String getAliasedName() {
            if (alias == null) {
                return tableName;
            } else {
                return tableName + " " + alias;
            }
        }
    }

    public static class Column<E, T> implements Aliasable {

        final Table<E> parentTable;
        final Class<T> columnClass;
        final String columnName;
        final boolean isPk;
        final ColumnHandler<T> handler;
        final Accessor accessor;

        public Column(Table<E> parentTable, Class<T> columnClass, String propertyPath, String columnName, boolean isPk,
                ColumnHandler<T> handler) {
            super();
            this.parentTable = parentTable;
            this.columnClass = columnClass;
            this.accessor = new Accessor(parentTable.getEntityClass(), propertyPath);
            this.columnName = columnName;
            this.isPk = isPk;
            this.handler = handler;
        }

        public String getColumnName() {
            return columnName;
        }

        @Override
        public String getAliasedName() {
            if (parentTable.getAlias() == null) {
                return columnName;
            } else {
                return parentTable.getAlias() + "." + columnName;
            }
        }

        public void loadValue(E entity, ResultSet rs) throws SQLException {
            T value = handler.readValue(rs, columnName);
            accessor.set(entity, value);
        }

        @SuppressWarnings("unchecked")
        public Object storeValue(E entity) {
            T value = (T) accessor.get(entity);
            return handler.getJdbcValue(value);
        }

        protected IQueryObject binaryOp(String op, T value) {
            QueryObject q = new QueryObject();
            q.append(getColumnName());
            q.append(op);
            q.append("?");
            q.addArg(handler.getJdbcValue(value));
            return q;
        }

        public IQueryObject eq(T value) {
            return binaryOp("=", value);
        }

        public IQueryObject ne(T value) {
            return binaryOp("<>", value);
        }

        public IQueryObject lt(T value) {
            return binaryOp("<", value);
        }

        public IQueryObject gt(T value) {
            return binaryOp(">", value);
        }

        public IQueryObject le(T value) {
            return binaryOp("<=", value);
        }

        public IQueryObject ge(T value) {
            return binaryOp(">=", value);
        }

        //

        protected IQueryObject binaryOp(String op, Column<?, T> c) {
            QueryObject q = new QueryObject();
            q.append(getColumnName());
            q.append(op);
            q.append(c.getColumnName());
            return q;
        }

        public IQueryObject eq(Column<?, T> c) {
            return binaryOp("=", c);
        }

        public IQueryObject ne(Column<?, T> c) {
            return binaryOp("<>", c);
        }

        public IQueryObject lt(Column<?, T> c) {
            return binaryOp("<", c);
        }

        public IQueryObject gt(Column<?, T> c) {
            return binaryOp(">", c);
        }

        public IQueryObject le(Column<?, T> c) {
            return binaryOp("<=", c);
        }

        public IQueryObject ge(Column<?, T> c) {
            return binaryOp(">=", c);
        }

        //

        protected IQueryObject unaryOp(String prefix, String postfix) {
            QueryObject q = new QueryObject();
            q.append(prefix);
            q.append(getColumnName());
            q.append(postfix);
            return q;
        }

        public IQueryObject isNull() {
            return unaryOp("", " IS NULL");
        }

        public IQueryObject isNotNull() {
            return unaryOp("", " IS NOT NULL");
        }

        //

        public IQueryObject in(List<T> values) {

            QueryObject r = new QueryObject();
            r.append(getColumnName());
            r.append(" in (");
            for (int i = 0; i < values.size(); i++) {
                if (i > 0) {
                    r.append(",");
                }
                r.append("?");
                r.addArg(handler.getJdbcValue(values.get(i)));
            }
            r.append(")");
            return r;
        }

        @SuppressWarnings("unchecked")
        public IQueryObject in(T... values) {
            return in(Arrays.asList(values));
        }

        public IQueryObject notIn(List<T> values) {
            return Relational.not(in(values));
        }

        @SuppressWarnings("unchecked")
        public IQueryObject notIn(T... values) {
            return Relational.not(in(Arrays.asList(values)));
        }

        public IQueryObject between(T value1, T value2) {
            QueryObject r = new QueryObject();
            r.append(getColumnName());
            r.append(" between ? and ?");
            r.addArg(handler.getJdbcValue(value1));
            r.addArg(handler.getJdbcValue(value2));
            return r;
        }

        public IQueryObject like(ELike like, String value) {
            QueryObject r = new QueryObject();
            r.append(getColumnName());
            r.append(" like ?");
            r.addArg(like.process(value));
            return r;
        }

        public IQueryObject ilike(ELike like, String value) {
            QueryObject r = new QueryObject();
            r.append("upper(");
            r.append(getColumnName());
            r.append(") like upper(?)");
            r.addArg(like.process(value));
            return r;
        }

    }

    public static interface ColumnHandler<T> {

        /**
         * donat el valor de bean, retorna el valor a tipus de jdbc, apte per a setejar
         * en un {@link QueryObject} o en {@link PreparedStatement}.
         */
        default Object getJdbcValue(T value) {
            return value;
        }

        /**
         * retorna el valor de bean a partir del {@link ResultSet}.
         */
        T readValue(ResultSet rs, String columnName) throws SQLException;
    }

    public static class EnumColumnHandler<T extends Enum<T>> implements ColumnHandler<T> {

        final Class<T> enumClass;

        public EnumColumnHandler(Class<T> enumClass) {
            super();
            this.enumClass = enumClass;
        }

        @Override
        public Object getJdbcValue(T value) {
            return value.name();
        }

        @Override
        public T readValue(ResultSet rs, String columnName) throws SQLException {
            String name = rs.getString(columnName);
            return Enum.valueOf(enumClass, name);
        }
    }

    static class Handlers {

        public static final ColumnHandler<String> STRING = (rs, c) -> ResultSetUtils.getString(rs, c);
        public static final ColumnHandler<Date> DATE = (rs, c) -> ResultSetUtils.getTimestamp(rs, c);
        public static final ColumnHandler<byte[]> BYTE_ARRAY = (rs, c) -> ResultSetUtils.getBytes(rs, c);
        public static final ColumnHandler<BigDecimal> BIG_DECIMAL = (rs, c) -> ResultSetUtils.getBigDecimal(rs, c);

        public static final ColumnHandler<Boolean> BOOLEAN = (rs, c) -> ResultSetUtils.getBoolean(rs, c);
        public static final ColumnHandler<Byte> BYTE = (rs, c) -> ResultSetUtils.getByte(rs, c);
        public static final ColumnHandler<Short> SHORT = (rs, c) -> ResultSetUtils.getShort(rs, c);
        public static final ColumnHandler<Integer> INTEGER = (rs, c) -> ResultSetUtils.getInteger(rs, c);
        public static final ColumnHandler<Long> LONG = (rs, c) -> ResultSetUtils.getLong(rs, c);
        public static final ColumnHandler<Float> FLOAT = (rs, c) -> ResultSetUtils.getFloat(rs, c);
        public static final ColumnHandler<Double> DOUBLE = (rs, c) -> ResultSetUtils.getDouble(rs, c);

        public static final ColumnHandler<Boolean> PBOOLEAN = (rs, c) -> rs.getBoolean(c);
        public static final ColumnHandler<Byte> PBYTE = (rs, c) -> rs.getByte(c);
        public static final ColumnHandler<Short> PSHORT = (rs, c) -> rs.getShort(c);
        public static final ColumnHandler<Integer> PINTEGER = (rs, c) -> rs.getInt(c);
        public static final ColumnHandler<Long> PLONG = (rs, c) -> rs.getLong(c);
        public static final ColumnHandler<Float> PFLOAT = (rs, c) -> rs.getFloat(c);
        public static final ColumnHandler<Double> PDOUBLE = (rs, c) -> rs.getDouble(c);

        static final Map<Class<?>, ColumnHandler<?>> HANDLERS = new LinkedHashMap<>();
        static {
            HANDLERS.put(String.class, STRING);
            HANDLERS.put(Date.class, DATE);
            HANDLERS.put(byte[].class, BYTE_ARRAY);
            HANDLERS.put(BigDecimal.class, BIG_DECIMAL);

            HANDLERS.put(Boolean.class, BOOLEAN);
            HANDLERS.put(Byte.class, BYTE);
            HANDLERS.put(Short.class, SHORT);
            HANDLERS.put(Integer.class, INTEGER);
            HANDLERS.put(Long.class, LONG);
            HANDLERS.put(Float.class, FLOAT);
            HANDLERS.put(Double.class, DOUBLE);

            HANDLERS.put(boolean.class, PBOOLEAN);
            HANDLERS.put(byte.class, PBYTE);
            HANDLERS.put(short.class, PSHORT);
            HANDLERS.put(int.class, PINTEGER);
            HANDLERS.put(long.class, PLONG);
            HANDLERS.put(float.class, PFLOAT);
            HANDLERS.put(double.class, PDOUBLE);
        }

        /**
         * Els tipus aquí contemplats especifiquen els que poden tenir les propietats de
         * les entitats a tractar. Per a algun tipus diferent, anotar amb
         * {@link CustomHandler}.
         */
        @SuppressWarnings("unchecked")
        public static <T> ColumnHandler<T> getHandlerFor(Class<T> type) {
            if (!HANDLERS.containsKey(type)) {
                throw new RuntimeException("unsupported column type: " + type.getName() + ": please specify a concrete "
                        + ColumnHandler.class.getName());
            }
            return (ColumnHandler<T>) HANDLERS.get(type);
        }

    }
}

class ResultSetUtils {

    public static <T extends Enum<T>> T getEnum(final Class<T> enumClass, final ResultSet rs) throws SQLException {

        final String v = rs.getString(1);
        if (v == null) {
            return null;
        }
        return Enum.valueOf(enumClass, v);
    }

    public static <T extends Enum<T>> T getEnum(final Class<T> enumClass, final ResultSet rs, String columnLabel)
            throws SQLException {

        final String v = rs.getString(columnLabel);
        if (v == null) {
            return null;
        }
        return Enum.valueOf(enumClass, v);
    }

    public static Byte getByte(final ResultSet rs) throws SQLException {
        final byte v = rs.getByte(1);
        if (rs.wasNull()) {
            return null;
        }
        return v;
    }

    public static Short getShort(final ResultSet rs) throws SQLException {
        final short v = rs.getShort(1);
        if (rs.wasNull()) {
            return null;
        }
        return v;
    }

    public static Integer getInteger(final ResultSet rs) throws SQLException {
        final int v = rs.getInt(1);
        if (rs.wasNull()) {
            return null;
        }
        return v;
    }

    public static Long getLong(final ResultSet rs) throws SQLException {
        final long v = rs.getLong(1);
        if (rs.wasNull()) {
            return null;
        }
        return v;
    }

    public static Float getFloat(final ResultSet rs) throws SQLException {
        final float v = rs.getFloat(1);
        if (rs.wasNull()) {
            return null;
        }
        return v;
    }

    public static Double getDouble(final ResultSet rs) throws SQLException {
        final double v = rs.getDouble(1);
        if (rs.wasNull()) {
            return null;
        }
        return v;
    }

    public static Byte getByte(final ResultSet rs, final String columnLabel) throws SQLException {
        final byte v = rs.getByte(columnLabel);
        if (rs.wasNull()) {
            return null;
        }
        return v;
    }

    public static Short getShort(final ResultSet rs, final String columnLabel) throws SQLException {
        final short v = rs.getShort(columnLabel);
        if (rs.wasNull()) {
            return null;
        }
        return v;
    }

    public static Integer getInteger(final ResultSet rs, final String columnLabel) throws SQLException {
        final int v = rs.getInt(columnLabel);
        if (rs.wasNull()) {
            return null;
        }
        return v;
    }

    public static Long getLong(final ResultSet rs, final String columnLabel) throws SQLException {
        final long v = rs.getLong(columnLabel);
        if (rs.wasNull()) {
            return null;
        }
        return v;
    }

    public static Float getFloat(final ResultSet rs, final String columnLabel) throws SQLException {
        final float v = rs.getFloat(columnLabel);
        if (rs.wasNull()) {
            return null;
        }
        return v;
    }

    public static Double getDouble(final ResultSet rs, final String columnLabel) throws SQLException {
        final double v = rs.getDouble(columnLabel);
        if (rs.wasNull()) {
            return null;
        }
        return v;
    }

    public static String getString(final ResultSet rs) throws SQLException {
        return rs.getString(1);
    }

    public static boolean getBoolean(final ResultSet rs) throws SQLException {
        return rs.getBoolean(1);
    }

    public static byte[] getBytes(final ResultSet rs) throws SQLException {
        return rs.getBytes(1);
    }

    public static Timestamp getTimestamp(final ResultSet rs) throws SQLException {
        return rs.getTimestamp(1);
    }

    public static String getString(final ResultSet rs, final String columnLabel) throws SQLException {
        return rs.getString(columnLabel);
    }

    public static boolean getBoolean(final ResultSet rs, final String columnLabel) throws SQLException {
        return rs.getBoolean(columnLabel);
    }

    public static byte[] getBytes(final ResultSet rs, final String columnLabel) throws SQLException {
        return rs.getBytes(columnLabel);
    }

    public static Timestamp getTimestamp(final ResultSet rs, final String columnLabel) throws SQLException {
        return rs.getTimestamp(columnLabel);
    }

    public static BigDecimal getBigDecimal(final ResultSet rs) throws SQLException {
        return rs.getBigDecimal(1);
    }

    public static BigDecimal getBigDecimal(final ResultSet rs, final String columnLabel) throws SQLException {
        return rs.getBigDecimal(columnLabel);
    }

    public static Character getChar(final ResultSet rs) throws SQLException {
        String v = rs.getString(1);
        if (v == null || v.isEmpty()) {
            return null;
        }
        return v.charAt(0);
    }

    public static Character getChar(final ResultSet rs, final String columnLabel) throws SQLException {
        String v = rs.getString(columnLabel);
        if (v == null || v.isEmpty()) {
            return null;
        }
        return v.charAt(0);
    }

}

interface IQueryObject {

    String getQuery();

    Object[] getArgs();

    List<Object> getArgsList();

}

class QueryObject implements IQueryObject {

    final StringBuilder query;
    final List<Object> params;

    public QueryObject() {
        super();
        this.query = new StringBuilder();
        this.params = new ArrayList<Object>();
    }

    public QueryObject(String sqlFragment) {
        this();
        this.query.append(sqlFragment);
    }

    public void append(String sqlFragment) {
        this.query.append(sqlFragment);
    }

    public void append(IQueryObject qo) {
        this.query.append(qo.getQuery());
        this.params.addAll(qo.getArgsList());
    }

    public void addArg(Object paramValue) {
        this.params.add(paramValue);
    }

    public void addArgs(List<Object> paramValues) {
        this.params.addAll(paramValues);
    }

    @Override
    public String getQuery() {
        return query.toString();
    }

    @Override
    public Object[] getArgs() {
        return params.toArray();
    }

    @Override
    public List<Object> getArgsList() {
        return params;
    }

    @Override
    public String toString() {
        return QueryObjectUtils.toString(this);
    }

}

class QueryObjectUtils {

    public static String toString(final IQueryObject q) {
        final StringBuilder r = new StringBuilder();
        r.append(q.getQuery());
        r.append(" -- [");
        int c = 0;
        for (final Object o : q.getArgs()) {
            if (c > 0) {
                r.append(", ");
            }
            r.append(o);
            if (o != null) {
                r.append("(");
                r.append(o.getClass().getSimpleName());
                r.append(")");
            }
            c++;
        }
        r.append("]");
        return r.toString();
    }

}

class Accessor {

    protected final Class<?> beanClass;
    // id.login
    protected final String propertyPath;
    protected final String[] propertyNameParts;
    protected final List<PropertyDescriptor> propertyPathList = new ArrayList<>();

    public Accessor(Class<?> beanClass, String propertyPath) {
        super();
        this.propertyPath = propertyPath;
        this.beanClass = beanClass;
        this.propertyNameParts = propertyPath.split("\\.");

        try {
            Class<?> c = beanClass;
            for (String part : propertyNameParts) {
                PropertyDescriptor pd = findProp(c, part);
                this.propertyPathList.add(pd);
                c = pd.getPropertyType();
            }
        } catch (Exception e) {
            throw new RuntimeException("describing " + beanClass.getName() + "#" + propertyPath, e);
        }
    }

    protected static PropertyDescriptor findProp(Class<?> beanClass, String propertyName) {
        BeanInfo info;
        try {
            info = Introspector.getBeanInfo(beanClass);
        } catch (IntrospectionException e) {
            throw new RuntimeException("describing " + beanClass.getName(), e);
        }
        PropertyDescriptor[] pds = info.getPropertyDescriptors();
        for (PropertyDescriptor pd : pds) {
            if (pd.getName().equals("class") || pd.getName().contains("$")) {
                continue;
            }
            if (pd.getName().equals(propertyName)) {
                return pd;
            }
        }
        throw new RuntimeException("property not found: '" + beanClass.getName() + "#" + propertyName + "'");
    }

    public Object get(Object bean) {
        return get(bean, 0);
    }

    public void set(Object bean, Object propertyValue) {
        set(bean, 0, propertyValue);
    }

    public Object get(Object bean, int startIndex) {
        try {
            Object o = bean;
            for (int i = startIndex; i < propertyPathList.size(); i++) {
                o = propertyPathList.get(i).getReadMethod().invoke(o);
                if (o == null) {
                    return null;
                }
            }
            return o;
        } catch (Exception e) {
            throw new RuntimeException(this.beanClass.getName() + "#" + this.propertyPath + " for instance: " + bean,
                    e);
        }
    }

    public void set(Object bean, int startIndex, Object propertyValue) {
        try {
            Object o = bean;
            for (int i = startIndex; i < propertyPathList.size() - 1; i++) {
                PropertyDescriptor p = propertyPathList.get(i);
                Object o2 = p.getReadMethod().invoke(o);
                if (o2 == null) {
                    o2 = p.getPropertyType().newInstance();
                    p.getWriteMethod().invoke(o, o2);
                }
                o = o2;
            }
            propertyPathList.get(propertyPathList.size() - 1).getWriteMethod().invoke(o, propertyValue);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Class<?> getBeanClass() {
        return beanClass;
    }

    public String getPropertyName() {
        return propertyPath;
    }

    public Class<?> getPropertyFinalType() {
        return propertyPathList.get(propertyPathList.size() - 1).getPropertyType();
    }

    @Override
    public String toString() {
        return beanClass + "#" + propertyPath;
    }

}

enum ELike {

    /**
     * A -- A
     */
    EXACT_MATCH(false, false),

    /**
     * A -- A%
     */
    BEGINS_WITH(false, true),

    /**
     * A -- %A%
     */
    CONTAINS(true, true),

    /**
     * A -- %A
     */
    ENDS_WITH(true, false);

    private final boolean beginWithWildchar;
    private final boolean endsWithWildchar;

    private ELike(final boolean beginWithWildchar, final boolean endsWithWildchar) {
        this.beginWithWildchar = beginWithWildchar;
        this.endsWithWildchar = endsWithWildchar;
    }

    /**
     * renderitza la "regexp" SQL
     */
    public String process(final String value) {
        String str = value;
        if (beginWithWildchar) {
            str = '%' + str;
        }
        if (endsWithWildchar) {
            str = str + '%';
        }
        return str;
    }

}

class Relational {

    protected static IQueryObject composition(String op, List<IQueryObject> qs) {
        QueryObject r = new QueryObject();
        for (int i = 0; i < qs.size(); i++) {
            if (i > 0) {
                r.append(op);
            }
            r.append(qs.get(i));
        }
        return r;
    }

    public static IQueryObject and(List<IQueryObject> qs) {
        return composition(" and ", qs);
    }

    public static IQueryObject or(List<IQueryObject> qs) {
        return composition(" or ", qs);
    }

    public static IQueryObject and(IQueryObject... qs) {
        return and(Arrays.asList(qs));
    }

    public static IQueryObject or(IQueryObject... qs) {
        return or(Arrays.asList(qs));
    }

    public static IQueryObject not(IQueryObject q) {
        QueryObject r = new QueryObject();
        r.append("not(");
        r.append(q);
        r.append(")");
        return r;
    }
}
