package org.mel.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.sql.Connection;
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

import javax.sql.DataSource;

import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.Before;
import org.junit.Test;
import org.mel.test.QWERTYU2.Mapable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Camarades, ha arribat a les meves mans la 4ª edició de l'"Angular Ejamples",
 * fet de forma clandestina pel poble, i per al poble.
 * <p>
 * Us l'adjunto, en el format privatiu-burgès: fem-ne bon ús.
 *
 */
public class QWERTYU2 {

    final DataAccesFacade facade;

    public QWERTYU2() {
        final JDBCDataSource ds = new JDBCDataSource();
        ds.setUrl("jdbc:hsqldb:mem:a");
        ds.setUser("sa");
        ds.setPassword("");
        this.facade = new JdbcDataAccesFacade(ds);
    }

    @Before
    public void before() {
        facade.begin();
        try {
            SqlScriptExecutor sql = new SqlScriptExecutor(facade);
            sql.runFromClasspath("films.sql");
            facade.commit();
        } catch (Exception e) {
            facade.rollback();
            throw e;
        }
    }

    @Test
    public void testReal() throws Exception {

        Operations o = new Operations();
        facade.begin();
        try {

            Pizza_ p = new Pizza_();

            Pizza romana = new Pizza(100L, "romana", 12.5, EPizzaType.DELUX);
            facade.update(o.insert(p, romana));

            romana.setPrice(12.9);
            assertEquals("Pizza [idPizza=100, name=romana, price=12.9, type=DELUX]", romana.toString());
            facade.update(o.update(p, romana));

            Query<Pizza> q = o.query(p).append("select * from {} where {}", p, p.id.eq(100L));

            romana = q.getExecutor(facade).loadUnique();
            assertEquals("Pizza [idPizza=100, name=romana, price=12.9, type=DELUX]", romana.toString());

            assertEquals("[Pizza [idPizza=100, name=romana, price=12.9, type=DELUX]]",
                    q.getExecutor(facade).load().toString());

            Query<Double> scalar = o.query(p.price).append("select sum({}) as {} from {}", p.price, p.price, p);
            assertEquals(romana.getPrice(), scalar.getExecutor(facade).loadUnique());

            facade.update(o.delete(p, romana));
            assertEquals("[]", q.getExecutor(facade).load().toString());

            assertNull(scalar.getExecutor(facade).loadUnique());

            facade.commit();
        } catch (Exception e) {
            facade.rollback();
            throw e;
        }
    }

    @Test
    public void testName() throws Exception {

        {
            Pizza_ p_ = new Pizza_();
            assertEquals("pizzas", p_.getAliasedName());
            assertEquals("price", p_.price.getAliasedName());
        }
        {
            Pizza_ p_ = new Pizza_("p");
            assertEquals("pizzas p", p_.getAliasedName());
            assertEquals("p.price", p_.price.getAliasedName());

            IQueryObject q = Relational.and( //
                    p_.id.eq(100L), //
                    p_.name.ilike(ELike.CONTAINS, "alo"), //
                    p_.price.between(5.0, 18.5), //
                    p_.type.in(EPizzaType.REGULAR, EPizzaType.DELUX) //
            );
            assertEquals(
                    "id_pizza=? and upper(name) like upper(?) and price between ? and ? and kind in (?,?) -- [100(Long), %alo%(String), 5.0(Double), 18.5(Double), REGULAR(String), DELUX(String)]",
                    q.toString());
        }
        {

            Operations o = new Operations();
            Pizza romana = new Pizza(100L, "romana", 12.5, EPizzaType.DELUX);

            assertEquals(
                    "insert into pizzas (id_pizza, name, price, kind) values (?, ?, ?, ?) -- [100(Long), romana(String), 12.5(Double), DELUX(String)]",
                    o.insert(new Pizza_(), romana).toString());
            assertEquals(
                    "update pizzas set name=?, price=?, kind=? where id_pizza=? -- [romana(String), 12.5(Double), DELUX(String), 100(Long)]",
                    o.update(new Pizza_(), romana).toString());
            assertEquals("delete from pizzas where id_pizza=? -- [100(Long)]",
                    o.delete(new Pizza_(), romana).toString());
        }
        {
            Operations o = new Operations();
            Pizza_ p = new Pizza_();
            // Pizza romana = new Pizza(100L, "romana", 12.5, EPizzaType.DELUX);

            Query<Pizza> q = o.query(p);
            q.append("select sum({}) from {} ", p.price, p);
            q.append("where {} ", Relational.and( //
                    p.id.lt(100L), //
                    p.name.ilike(ELike.CONTAINS, "oma"), //
                    p.type.in(EPizzaType.REGULAR, EPizzaType.DELUX) //
            ));

            assertEquals(
                    "select sum(price) from pizzas where id_pizza<? and upper(name) like upper(?) and kind in (?,?)  " + //
                            "-- [100(Long), %oma%(String), REGULAR(String), DELUX(String)]", //
                    q.toString());

        }
    }

    public static class Pizza_ extends Table<Pizza> {

        public final Column<Pizza, Long> id = addPkColumn(Long.class, "idPizza", "id_pizza");
        public final Column<Pizza, String> name = addColumn(String.class, "name", "name");
        public final Column<Pizza, Double> price = addColumn(Double.class, "price", "price");
        public final Column<Pizza, EPizzaType> type = addColumn(EPizzaType.class, "type", "kind",
                new EnumColumnHandler<>(EPizzaType.class));

        public Pizza_() {
            super(Pizza.class, "pizzas");
        }

        public Pizza_(String alias) {
            super(Pizza.class, "pizzas", alias);
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

    static class Query<E> implements IQueryObject {

        final Mapable<E> mapable;

        final StringBuilder query;
        final List<Object> params;

        public Query(Mapable<E> mapable) {
            super();
            this.mapable = mapable;
            this.query = new StringBuilder();
            this.params = new ArrayList<>();
        }

        public Query<E> append(String queryFragment, Object... params) {

            int p = 0;
            int paramIndex = 0;
            while (true) {
                int pos = queryFragment.indexOf("{}", p);
                if (pos < 0) {
                    break;
                }

                this.query.append(queryFragment.substring(p, pos));
                Object param = params[paramIndex++];
                IQueryObject paramResult = evaluate(param);
                this.query.append(paramResult.getQuery());
                this.params.addAll(paramResult.getArgsList());

                p = pos + "{}".length();
            }
            this.query.append(queryFragment.substring(p));

            return this;
        }

        protected IQueryObject evaluate(Object param) {
            if (param instanceof IQueryObject) {
                return (IQueryObject) param;
            } else if (param instanceof Aliasable) {
                Aliasable t = (Aliasable) param;
                QueryObject r = new QueryObject();
                r.append(t.getAliasedName());
                return r;
            } else {
                throw new RuntimeException(param.getClass().getName());
            }
        }

        // (DataAccesFacade facade, QueryObject qo, Mapable<E> mapable) {
        public Executor<E> getExecutor(DataAccesFacade facade) {
            return new Executor<E>(facade, this, mapable);
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

    public static class Operations {

        public <E> Query<E> query(Mapable<E> mapable) {
            return new Query<>(mapable);
        }

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

    public static class Table<E> implements Aliasable, Mapable<E> {

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

        @Override
        public String getAliasedName() {
            if (alias == null) {
                return tableName;
            } else {
                return tableName + " " + alias;
            }
        }

        @Override
        public E map(ResultSet rs) throws SQLException {
            try {
                E r = entityClass.newInstance();
                for (Column<E, ?> c : columns) {
                    c.loadValue(r, rs);
                }
                return r;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static interface Mapable<T> {
        T map(ResultSet rs) throws SQLException;
    }

    public static class Column<E, T> implements Aliasable, Mapable<T> {

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

        @Override
        public T map(ResultSet rs) throws SQLException {
            return handler.readValue(rs, columnName);
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

class Executor<E> {

    final DataAccesFacade facade;
    final IQueryObject qo;
    final Mapable<E> mapable;

    public Executor(DataAccesFacade facade, IQueryObject qo, Mapable<E> mapable) {
        super();
        this.facade = facade;
        this.qo = qo;
        this.mapable = mapable;
    }

    public int update() {
        return facade.update(qo);
    }

    public E loadUnique() {
        return facade.loadUnique(qo, mapable);
    }

    public List<E> load() {
        return facade.load(qo, mapable);
    }

    // TODO
    // public void loadPage(Pager<E> pager) {
    // facade.loadPage(qo, mapable, pager);
    // }
}

interface DataAccesFacade {

    DataSource getDataSource();

    <T> T loadUnique(IQueryObject q, Mapable<T> mapable) throws TooManyResultsException, EmptyResultException;

    <T> List<T> load(IQueryObject q, Mapable<T> mapable);

    int update(IQueryObject q);

    // TODO <T> T extract(IQueryObject q, ResultSetExtractor<T> extractor);

    void begin();

    void commit();

    void rollback();

    boolean isValidTransaction();

    Connection getCurrentConnection();

}

class JdbcDataAccesFacade implements DataAccesFacade {

    static final Logger LOG = LoggerFactory.getLogger(JdbcDataAccesFacade.class);

    protected final DataSource ds;

    @Override
    public DataSource getDataSource() {
        return ds;
    }

    static class Tx {

        final Connection c;
        final Throwable opened;

        public Tx(Connection c) {
            super();
            this.c = c;
            this.opened = new Exception();
            configureConnection();
        }

        void configureConnection() {
            try {
                c.setAutoCommit(false);
                c.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            } catch (final SQLException e) {
                throw new LechugaException(e);
            }
        }

        public boolean isValid() throws SQLException {
            return !c.isClosed();
        }

        public Throwable getOpened() {
            return opened;
        }

    }

    static final ThreadLocal<Tx> threadton = new ThreadLocal<Tx>() {

        @Override
        protected Tx initialValue() {
            return null;
        };
    };

    public JdbcDataAccesFacade(DataSource ds) {
        super();
        this.ds = ds;
    }

    @Override
    public void begin() {
        createConnection();
        LOG.debug("=> begin");
    }

    @Override
    public Connection getCurrentConnection() {
        return getConnection();
    }

    protected void createConnection() {
        if (isValidTransaction()) {
            throw new LechugaException("transaction is yet active", threadton.get().getOpened());
        }
        try {
            threadton.set(new Tx(ds.getConnection()));
        } catch (final SQLException e) {
            throw new LechugaException(e);
        }
    }

    protected Connection getConnection() {
        try {
            Tx tx = threadton.get();
            if (tx == null || !tx.isValid()) {
                throw new LechugaException("not in a valid transaction");
            }
            return tx.c;
        } catch (final SQLException e) {
            throw new LechugaException(e);
        }
    }

    @Override
    public boolean isValidTransaction() {
        try {
            return threadton.get() != null && threadton.get().isValid();
        } catch (SQLException e) {
            throw new LechugaException(e);
        }
    }

    @Override
    public void commit() {
        final Connection c = getConnection();
        try {
            c.commit();
            c.close();
            LOG.debug("<= commit");
        } catch (final Exception e) {
            throw new LechugaException(e);
        }
    }

    @Override
    public void rollback() {
        final Connection c = getConnection();
        try {
            c.rollback();
            c.close();
            LOG.debug("<= rollback");
        } catch (final Exception e) {
            throw new LechugaException(e);
        }
    }

    protected void close() {
        final Connection c = getConnection();
        try {
            c.close();
            threadton.remove();
        } catch (final Exception e) {
            throw new LechugaException(e);
        } finally {
            threadton.remove();
        }
    }

    PreparedStatement prepareStatement(Connection c, IQueryObject q) throws SQLException {
        final PreparedStatement ps = c.prepareStatement(q.getQuery());
        try {
            Object[] args = q.getArgs();
            for (int i = 0; i < args.length; i++) {
                ps.setObject(i + 1, args[i]);
            }
            return ps;
        } catch (final SQLException e) {
            closeResources(null, ps);
            throw e;
        }
    }

    void closeResources(ResultSet rs, PreparedStatement ps) {
        if (rs != null) {
            try {
                rs.close();
            } catch (final SQLException e) {
                throw new LechugaException(e);
            }
        }
        if (ps != null) {
            try {
                ps.close();
            } catch (final SQLException e) {
                throw new LechugaException(e);
            }
        }
    }

    @Override
    public <T> T loadUnique(IQueryObject q, Mapable<T> mapable) throws TooManyResultsException, EmptyResultException {

        LOG.debug("{}", q);
        Connection c;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            c = getConnection();
            ps = prepareStatement(c, q);
            rs = ps.executeQuery();
            if (!rs.next()) {
                throw new EmptyResultException(q.toString());
            }
            final T r = mapable.map(rs);
            if (rs.next()) {
                throw new TooManyResultsException(q.toString());
            }
            return r;
        } catch (final EmptyResultException e) {
            throw e;
        } catch (final SQLException e) {
            throw new LechugaException(q.toString(), e);
        } finally {
            closeResources(rs, ps);
        }
    }

    @Override
    public <T> List<T> load(IQueryObject q, Mapable<T> mapable) {
        LOG.debug("{}", q);
        Connection c;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            c = getConnection();
            ps = prepareStatement(c, q);
            rs = ps.executeQuery();
            final List<T> r = new ArrayList<T>();
            while (rs.next()) {
                r.add(mapable.map(rs));
            }
            return r;
        } catch (final SQLException e) {
            throw new LechugaException(q.toString(), e);
        } finally {
            closeResources(rs, ps);
        }
    }

    @Override
    public int update(final IQueryObject q) {
        LOG.debug("{}", q);
        Connection c;
        PreparedStatement ps = null;
        try {
            c = getConnection();
            ps = prepareStatement(c, q);
            return ps.executeUpdate();
        } catch (final SQLException e) {
            throw new LechugaException(q.toString(), e);
        } finally {
            closeResources(null, ps);
        }
    }

    // TODO
    // @Override
    // public <T> T extract(final IQueryObject q, final ResultSetExtractor<T>
    // extractor) {
    // LOG.debug("{}", q);
    // Connection c;
    // PreparedStatement ps = null;
    // ResultSet rs = null;
    // try {
    // c = getConnection();
    // ps = c.prepareStatement(q.getSql(), ResultSet.TYPE_SCROLL_INSENSITIVE,
    // ResultSet.CONCUR_READ_ONLY);
    // Object[] args = q.getArgs();
    // for (int i = 0; i < args.length; i++) {
    // ps.setObject(i + 1, args[i]);
    // }
    // rs = ps.executeQuery();
    //
    // return extractor.extract(rs);
    //
    // } catch (final SQLException e) {
    // throw new LechugaException(q.toString(), e);
    // } finally {
    // closeResources(rs, ps);
    // }
    // }

}

class LechugaException extends RuntimeException {

    private static final long serialVersionUID = 8727129333282283655L;

    public LechugaException() {
        super();
    }

    public LechugaException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public LechugaException(final String message) {
        super(message);
    }

    public LechugaException(final Throwable cause) {
        super(cause);
    }

}

class UnexpectedResultException extends RuntimeException {

    private static final long serialVersionUID = 6193212451419056030L;

    public UnexpectedResultException(String message) {
        super(message);
    }

    public UnexpectedResultException(Exception e) {
        super(e);
    }

}

class TooManyResultsException extends UnexpectedResultException {

    private static final long serialVersionUID = 677361547286326224L;

    public TooManyResultsException(final String message) {
        super(message);
    }

}

class EmptyResultException extends UnexpectedResultException {

    private static final long serialVersionUID = 8727129333282283655L;

    public EmptyResultException(final String message) {
        super(message);
    }

}

class SqlScriptExecutor {

    public static final String DEFAULT_CHARSETNAME = "UTF-8";

    final DataAccesFacade facade;

    public SqlScriptExecutor(final DataAccesFacade facade) {
        super();
        this.facade = facade;
    }

    public void runFromClasspath(final String classPathFileName) {
        runFromClasspath(classPathFileName, DEFAULT_CHARSETNAME);
    }

    public void runFromClasspath(final String classPathFileName, final String charSetName) {
        final String text = FileUtils.loadFileFromClasspath(classPathFileName, charSetName);
        final List<String> stms = new SqlStatementParser(text).getStatementList();

        execute(stms);
    }

    public void execute(final List<String> stms) {
        for (final String stm : stms) {
            facade.update(new QueryObject(stm));
        }
    }

    public void execute(final String... stms) {
        execute(Arrays.asList(stms));
    }

}

class FileUtils {

    public static String loadFileFromClasspath(final String fileName, final String charSetName) {
        final InputStream is = loadFileFromClasspath(fileName);
        InputStreamReader reader;
        try {
            reader = new InputStreamReader(is, charSetName);
        } catch (final UnsupportedEncodingException e) {
            throw new LechugaException(e);
        }
        return readFromReader(reader);
    }

    public static InputStream loadFileFromClasspath(final String fileName) {
        final InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
        if (is == null) {
            throw new LechugaException("could not open file: " + fileName);
        }
        return is;
    }

    static String readFromReader(final Reader reader) {
        final BufferedReader r = new BufferedReader(reader);
        final StringBuilder strb = new StringBuilder();
        try {
            while (true) {
                final String line = r.readLine();
                if (line == null) {
                    break;
                }
                strb.append(line).append('\n');
            }
            reader.close();
        } catch (final IOException e) {
            throw new LechugaException(e);
        }
        return strb.toString();
    }

}

/**
 * Perfoms the same as the following regexp: <tt>
* (([^;]*?)?('.*?')?(/\\*(.|\\n)*?\\*\\/)?(--.*?\\n)?)*;\\s*
* </tt> , but preventing Regexp's <i>strange</i> errors, i.e. StackOverflowError
 * when working with large scripts.
 */
class SqlStatementParser {

    // String textSenseColon = "([^;]*?)?";
    // String literal = "('.*?')?";
    // String multiLineComment = "(/\\*(.|\\n)*?\\*/)?";
    // String singleLineComment = "(--.*?\\n)?";
    //
    // String regexp = "(" + textSenseColon + literal + multiLineComment +
    // singleLineComment + ")*;\\s*";

    public static final char DELIMITER = ';';
    public static final char LITERAL_DELIMITER = '\'';
    public static final String MULTI_LINE_COMMENT_START = "/*";
    public static final String MULTI_LINE_COMMENT_END = "*/";
    public static final String SINGLE_LINE_COMMENT_START = "--";
    public static final char SINGLE_LINE_COMMENT_END = '\n';

    final String script;
    final boolean trimStatements;
    int ssp, p;

    public SqlStatementParser(final String script, final boolean trimStatements) {
        super();
        this.script = script;
        this.trimStatements = trimStatements;
    }

    public SqlStatementParser(final String script) {
        this(script, true);
    }

    public List<String> getStatementList() {
        final List<String> stms = new ArrayList<String>();
        ssp = 0; // statement start pointer
        p = 0;

        while (noteof()) {
            while (noteof() && !isColon()) {
                if (isLiteral()) {
                    chupaLiteral();
                } else if (isMLComment()) {
                    chupaMLComment();
                } else if (isSLComment()) {
                    chupaSLComment();
                } else {
                    chupaText();
                }
            }
            if (noteof() && isColon()) {
                p++; // chupa ;
                String stm = script.substring(ssp, p);
                if (trimStatements) {
                    stm = stm.trim();
                }
                stms.add(stm);
                ssp = p;
            }
        }
        return stms;
    }

    protected boolean noteof() {
        return p < script.length();
    }

    protected boolean isText() {
        final char c = script.charAt(p);
        final char c2 = p + 1 < script.length() ? script.charAt(p + 1) : 0;
        return
        /**/c != DELIMITER &&
        /**/c != LITERAL_DELIMITER &&
        /**/!(c == MULTI_LINE_COMMENT_START.charAt(0) && c2 == MULTI_LINE_COMMENT_START.charAt(1)) &&
        /**/!(c == SINGLE_LINE_COMMENT_START.charAt(0) && c2 == SINGLE_LINE_COMMENT_START.charAt(1));
    }

    protected void chupaText() {
        while (noteof() && isText()) {
            p++;
        }
    }

    protected boolean isLiteral() {
        final char c = script.charAt(p);
        return c == LITERAL_DELIMITER;
    }

    protected void chupaLiteral() {
        p++; // chupa '
        while (!isLiteral()) {
            p++;
        }
        p++; // chupa '
    }

    protected boolean isMLComment() {
        final char c = script.charAt(p);
        final char c2 = p + 1 < script.length() ? script.charAt(p + 1) : 0;
        return c == MULTI_LINE_COMMENT_START.charAt(0) && c2 == MULTI_LINE_COMMENT_START.charAt(1);
    }

    protected boolean isMLCommentClosing() {
        final char c = script.charAt(p);
        final char c2 = p + 1 < script.length() ? script.charAt(p + 1) : 0;
        return c == MULTI_LINE_COMMENT_END.charAt(0) && c2 == MULTI_LINE_COMMENT_END.charAt(1);
    }

    protected void chupaMLComment() {
        p += MULTI_LINE_COMMENT_START.length(); // chupa
        while (!isMLCommentClosing()) {
            p++;
        }
        p += MULTI_LINE_COMMENT_END.length(); // chupa
    }

    protected boolean isSLComment() {
        final char c = script.charAt(p);
        final char c2 = p + 1 < script.length() ? script.charAt(p + 1) : 0;
        return c == SINGLE_LINE_COMMENT_START.charAt(0) && c2 == SINGLE_LINE_COMMENT_START.charAt(1);
    }

    protected void chupaSLComment() {
        p += SINGLE_LINE_COMMENT_START.length(); // chupa
        while (script.charAt(p) != SINGLE_LINE_COMMENT_END) {
            p++;
        }
        p++; // chupa \n
    }

    protected boolean isColon() {
        return script.charAt(p) == DELIMITER;
    }

}
