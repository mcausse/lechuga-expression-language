// package org.mel.test;
//
// import java.math.BigDecimal;
// import java.sql.PreparedStatement;
// import java.sql.ResultSet;
// import java.sql.SQLException;
// import java.sql.Timestamp;
// import java.util.ArrayList;
// import java.util.Date;
// import java.util.LinkedHashMap;
// import java.util.List;
// import java.util.Map;
//
// public class QWERTYU {
//
// public static class Pizza {
//
// }
//
// public static class Pizza_ {
//
// public static final Table<Pizza> table = new Table<>(Pizza.class, "pizza");
// public static final Column<Pizza, Long> id = table.addPkColumn(Long.class,
// "id_pizza");
// public static final Column<Pizza, String> name =
// table.addColumn(String.class, "name");
// public static final Column<Pizza, Double> price =
// table.addColumn(Double.class, "price");
// }
//
// public static interface ColumnHandler<T> {
//
// /**
// * donat el valor de bean, retorna el valor a tipus de jdbc, apte per a
// setejar
// * en un {@link QueryObject} o en {@link PreparedStatement}.
// */
// default Object getJdbcValue(T value) {
// return value;
// }
//
// /**
// * retorna el valor de bean a partir del {@link ResultSet}.
// */
// T readValue(ResultSet rs, String columnName) throws SQLException;
// }
//
// public static class AliasedTable<E> extends Table<E> {
//
// final String tableAlias;
//
// @SuppressWarnings("unchecked")
// public AliasedTable(String tableAlias, Table<E> tableToAliase) {
// super(tableToAliase.entityClass, tableToAliase.tableName);
// this.tableAlias = tableAlias;
// for (Column<E, ?> e : tableToAliase.columns) {
// Class<Object> columnClass = (Class<Object>) e.columnClass;
// ColumnHandler<Object> handler = (ColumnHandler<Object>) e.handler;
// columns.add(new Column<E, Object>(this, columnClass, e.columnName, e.isPk,
// handler));
// }
// }
//
// }
//
// public static class Table<E> {
//
// final Class<E> entityClass;
// final String tableName;
// final List<Column<E, ?>> columns;
//
// public Table(Class<E> entityClass, String tableName) {
// super();
// this.entityClass = entityClass;
// this.tableName = tableName;
// this.columns = new ArrayList<>();
// }
//
// public <T> Column<E, T> addColumn(Class<T> columnClass, String columnName) {
// Column<E, T> c = new Column<>(this, columnClass, columnName, false,
// Handlers.getHandlerFor(columnClass));
// this.columns.add(c);
// return c;
// }
//
// public <T> Column<E, T> addPkColumn(Class<T> columnClass, String columnName)
// {
// Column<E, T> c = new Column<>(this, columnClass, columnName, true,
// Handlers.getHandlerFor(columnClass));
// this.columns.add(c);
// return c;
// }
//
// public <T> Column<E, T> addColumn(Class<T> columnClass, String columnName,
// ColumnHandler<T> handler) {
// Column<E, T> c = new Column<>(this, columnClass, columnName, false, handler);
// this.columns.add(c);
// return c;
// }
//
// public <T> Column<E, T> addPkColumn(Class<T> columnClass, String columnName,
// ColumnHandler<T> handler) {
// Column<E, T> c = new Column<>(this, columnClass, columnName, true, handler);
// this.columns.add(c);
// return c;
// }
// }
//
// public static class Column<E, T> {
//
// final Table<E> parentTable;
// final Class<T> columnClass;
// final String columnName;
// final boolean isPk;
// final ColumnHandler<T> handler;
//
// public Column(Table<E> parentTable, Class<T> columnClass, String columnName,
// boolean isPk,
// ColumnHandler<T> handler) {
// super();
// this.parentTable = parentTable;
// this.columnClass = columnClass;
// this.columnName = columnName;
// this.isPk = isPk;
// this.handler = handler;
// }
//
// }
//
// static class Handlers {
//
// public static final ColumnHandler<String> STRING = (rs, c) ->
// ResultSetUtils.getString(rs, c);
// public static final ColumnHandler<Date> DATE = (rs, c) ->
// ResultSetUtils.getTimestamp(rs, c);
// public static final ColumnHandler<byte[]> BYTE_ARRAY = (rs, c) ->
// ResultSetUtils.getBytes(rs, c);
// public static final ColumnHandler<BigDecimal> BIG_DECIMAL = (rs, c) ->
// ResultSetUtils.getBigDecimal(rs, c);
//
// public static final ColumnHandler<Boolean> BOOLEAN = (rs, c) ->
// ResultSetUtils.getBoolean(rs, c);
// public static final ColumnHandler<Byte> BYTE = (rs, c) ->
// ResultSetUtils.getByte(rs, c);
// public static final ColumnHandler<Short> SHORT = (rs, c) ->
// ResultSetUtils.getShort(rs, c);
// public static final ColumnHandler<Integer> INTEGER = (rs, c) ->
// ResultSetUtils.getInteger(rs, c);
// public static final ColumnHandler<Long> LONG = (rs, c) ->
// ResultSetUtils.getLong(rs, c);
// public static final ColumnHandler<Float> FLOAT = (rs, c) ->
// ResultSetUtils.getFloat(rs, c);
// public static final ColumnHandler<Double> DOUBLE = (rs, c) ->
// ResultSetUtils.getDouble(rs, c);
//
// public static final ColumnHandler<Boolean> PBOOLEAN = (rs, c) ->
// rs.getBoolean(c);
// public static final ColumnHandler<Byte> PBYTE = (rs, c) -> rs.getByte(c);
// public static final ColumnHandler<Short> PSHORT = (rs, c) -> rs.getShort(c);
// public static final ColumnHandler<Integer> PINTEGER = (rs, c) ->
// rs.getInt(c);
// public static final ColumnHandler<Long> PLONG = (rs, c) -> rs.getLong(c);
// public static final ColumnHandler<Float> PFLOAT = (rs, c) -> rs.getFloat(c);
// public static final ColumnHandler<Double> PDOUBLE = (rs, c) ->
// rs.getDouble(c);
//
// static final Map<Class<?>, ColumnHandler<?>> HANDLERS = new
// LinkedHashMap<>();
// static {
// HANDLERS.put(String.class, STRING);
// HANDLERS.put(Date.class, DATE);
// HANDLERS.put(byte[].class, BYTE_ARRAY);
// HANDLERS.put(BigDecimal.class, BIG_DECIMAL);
//
// HANDLERS.put(Boolean.class, BOOLEAN);
// HANDLERS.put(Byte.class, BYTE);
// HANDLERS.put(Short.class, SHORT);
// HANDLERS.put(Integer.class, INTEGER);
// HANDLERS.put(Long.class, LONG);
// HANDLERS.put(Float.class, FLOAT);
// HANDLERS.put(Double.class, DOUBLE);
//
// HANDLERS.put(boolean.class, PBOOLEAN);
// HANDLERS.put(byte.class, PBYTE);
// HANDLERS.put(short.class, PSHORT);
// HANDLERS.put(int.class, PINTEGER);
// HANDLERS.put(long.class, PLONG);
// HANDLERS.put(float.class, PFLOAT);
// HANDLERS.put(double.class, PDOUBLE);
// }
//
// /**
// * Els tipus aquí contemplats especifiquen els que poden tenir les propietats
// de
// * les entitats a tractar. Per a algun tipus diferent, anotar amb
// * {@link CustomHandler}.
// */
// @SuppressWarnings("unchecked")
// public static <T> ColumnHandler<T> getHandlerFor(Class<T> type) {
// if (!HANDLERS.containsKey(type)) {
// throw new RuntimeException("unsupported column type: " + type.getName() + ":
// please specify a concrete "
// + ColumnHandler.class.getName());
// }
// return (ColumnHandler<T>) HANDLERS.get(type);
// }
//
// }
// }
//
// class ResultSetUtils {
//
// public static <T extends Enum<T>> T getEnum(final Class<T> enumClass, final
// ResultSet rs) throws SQLException {
//
// final String v = rs.getString(1);
// if (v == null) {
// return null;
// }
// return Enum.valueOf(enumClass, v);
// }
//
// public static <T extends Enum<T>> T getEnum(final Class<T> enumClass, final
// ResultSet rs, String columnLabel)
// throws SQLException {
//
// final String v = rs.getString(columnLabel);
// if (v == null) {
// return null;
// }
// return Enum.valueOf(enumClass, v);
// }
//
// public static Byte getByte(final ResultSet rs) throws SQLException {
// final byte v = rs.getByte(1);
// if (rs.wasNull()) {
// return null;
// }
// return v;
// }
//
// public static Short getShort(final ResultSet rs) throws SQLException {
// final short v = rs.getShort(1);
// if (rs.wasNull()) {
// return null;
// }
// return v;
// }
//
// public static Integer getInteger(final ResultSet rs) throws SQLException {
// final int v = rs.getInt(1);
// if (rs.wasNull()) {
// return null;
// }
// return v;
// }
//
// public static Long getLong(final ResultSet rs) throws SQLException {
// final long v = rs.getLong(1);
// if (rs.wasNull()) {
// return null;
// }
// return v;
// }
//
// public static Float getFloat(final ResultSet rs) throws SQLException {
// final float v = rs.getFloat(1);
// if (rs.wasNull()) {
// return null;
// }
// return v;
// }
//
// public static Double getDouble(final ResultSet rs) throws SQLException {
// final double v = rs.getDouble(1);
// if (rs.wasNull()) {
// return null;
// }
// return v;
// }
//
// public static Byte getByte(final ResultSet rs, final String columnLabel)
// throws SQLException {
// final byte v = rs.getByte(columnLabel);
// if (rs.wasNull()) {
// return null;
// }
// return v;
// }
//
// public static Short getShort(final ResultSet rs, final String columnLabel)
// throws SQLException {
// final short v = rs.getShort(columnLabel);
// if (rs.wasNull()) {
// return null;
// }
// return v;
// }
//
// public static Integer getInteger(final ResultSet rs, final String
// columnLabel) throws SQLException {
// final int v = rs.getInt(columnLabel);
// if (rs.wasNull()) {
// return null;
// }
// return v;
// }
//
// public static Long getLong(final ResultSet rs, final String columnLabel)
// throws SQLException {
// final long v = rs.getLong(columnLabel);
// if (rs.wasNull()) {
// return null;
// }
// return v;
// }
//
// public static Float getFloat(final ResultSet rs, final String columnLabel)
// throws SQLException {
// final float v = rs.getFloat(columnLabel);
// if (rs.wasNull()) {
// return null;
// }
// return v;
// }
//
// public static Double getDouble(final ResultSet rs, final String columnLabel)
// throws SQLException {
// final double v = rs.getDouble(columnLabel);
// if (rs.wasNull()) {
// return null;
// }
// return v;
// }
//
// public static String getString(final ResultSet rs) throws SQLException {
// return rs.getString(1);
// }
//
// public static boolean getBoolean(final ResultSet rs) throws SQLException {
// return rs.getBoolean(1);
// }
//
// public static byte[] getBytes(final ResultSet rs) throws SQLException {
// return rs.getBytes(1);
// }
//
// public static Timestamp getTimestamp(final ResultSet rs) throws SQLException
// {
// return rs.getTimestamp(1);
// }
//
// public static String getString(final ResultSet rs, final String columnLabel)
// throws SQLException {
// return rs.getString(columnLabel);
// }
//
// public static boolean getBoolean(final ResultSet rs, final String
// columnLabel) throws SQLException {
// return rs.getBoolean(columnLabel);
// }
//
// public static byte[] getBytes(final ResultSet rs, final String columnLabel)
// throws SQLException {
// return rs.getBytes(columnLabel);
// }
//
// public static Timestamp getTimestamp(final ResultSet rs, final String
// columnLabel) throws SQLException {
// return rs.getTimestamp(columnLabel);
// }
//
// public static BigDecimal getBigDecimal(final ResultSet rs) throws
// SQLException {
// return rs.getBigDecimal(1);
// }
//
// public static BigDecimal getBigDecimal(final ResultSet rs, final String
// columnLabel) throws SQLException {
// return rs.getBigDecimal(columnLabel);
// }
//
// public static Character getChar(final ResultSet rs) throws SQLException {
// String v = rs.getString(1);
// if (v == null || v.isEmpty()) {
// return null;
// }
// return v.charAt(0);
// }
//
// public static Character getChar(final ResultSet rs, final String columnLabel)
// throws SQLException {
// String v = rs.getString(columnLabel);
// if (v == null || v.isEmpty()) {
// return null;
// }
// return v.charAt(0);
// }
//
// }
