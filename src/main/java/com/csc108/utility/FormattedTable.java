package com.csc108.utility;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class FormattedTable {
	private List<List<Object>> tableData;
	private List<Integer> fieldWidth;
	private static String delimiter = "  ";

	@Getter
	@Setter
	private boolean printSeparator;

	@Getter
	@Setter
	private char separator;

	public FormattedTable() {
		tableData = new ArrayList<List<Object>>();
		printSeparator = true;
		separator = '-';
		fieldWidth = new ArrayList<Integer>();
	}

	public void AddRow(List<Object> row_) {
		tableData.add(row_);
	}

	/*
	 * note: java generic is not as c#, it is impossible to get the T.class from
	 * runtime if rows_ == null 0r size(rows_) == 0
	 */
	public <T> void addRows(List<T> rows_) {
		if (rows_.size() == 0)
			return;

		List<Object> header = new ArrayList<Object>();
		tableData.add(header);

		// Field[] properties = T.class == typeof(Object) ?
		// rows_.get(0).getClass().getFields() : typeof(T).GetProperties();
		Field[] fields = rows_.get(0).getClass().getDeclaredFields();
		AccessibleObject.setAccessible(fields, true);

		for (Field p : fields) {
			if (!Modifier.isStatic(p.getModifiers()))
				header.add(p.getName());
		}

		try {
			for (T r : rows_) {
				List<Object> data = new ArrayList<Object>();
				tableData.add(data);
				for (Field p : fields) {
					if (!Modifier.isStatic(p.getModifiers()))
						data.add(p.get(r) == null ? "N/A" : p.get(r).toString());
				}
			}
		} catch (Exception e_) {
			throw new RuntimeException(e_);
		}
	}

	private void calcFieldWidth(List<Object> row_) {
		int length = row_.size();

		while (fieldWidth.size() < length) {
			fieldWidth.add(0);
		}

		for (int i = 0; i < length; ++i) {
			if (row_.get(i) == null) {
				row_.set(i, "NULL");
			}
			fieldWidth.set(i, Math.max(fieldWidth.get(i), row_.get(i).toString().length()));
		}
	}

	public void Transpose() {
		int nRow = tableData.size();
		int nColumn = tableData.get(0).size();

		List<List<Object>> origData = this.tableData;
		tableData = new ArrayList<List<Object>>(nColumn);

		for (int i = 0; i < nColumn; ++i) {
			List<Object> row = new ArrayList<Object>(nRow);
			for (int j = 0; j < nRow; ++j) {
				List<Object> origRow = origData.get(j);
				row.add(origRow.get(i));
			}
			tableData.add(row);
		}
		printSeparator = false;
	}

	@Override
	public String toString() {
		fieldWidth.clear();
		for (List<Object> row : tableData) {
			calcFieldWidth(row);
		}

		boolean separatorPrinted = false;
		StringBuilder builder = new StringBuilder();

		for (List<Object> row : tableData) {
			int length = row.size();
			for (int i = 0; i < length; ++i) {
				builder.append(String.format("%-" + fieldWidth.get(i) + "s", row.get(i).toString()));
				if (i != length - 1) {
					builder.append(delimiter);
				}
			}
			builder.append("\n");
			if (printSeparator && !separatorPrinted) {
				for (int i = 0; i < length; ++i) {
					builder.append(StringUtils.repeat(separator, fieldWidth.get(i)));
					if (i != length - 1) {
						builder.append(delimiter);
					}
				}
				builder.append("\n");
				separatorPrinted = true;
			}
		}
		return builder.toString();
	}
	
	public static String toString(Object o_) {
		return toString(o_, true, false);
	}

	/**
     * 
     */
	public static String toString(Object o_, boolean firstCharUppercase, boolean transpose) {
		FormattedTable table = new FormattedTable();
		List<Object> header = new ArrayList<Object>();
		List<Object> value = new ArrayList<Object>();
		table.AddRow(header);
		table.AddRow(value);

		Field[] properties = o_.getClass().getDeclaredFields();
		AccessibleObject.setAccessible(properties, true);
		String name = null;
		for (Field p : properties) {
			if(Modifier.isStatic(p.getModifiers()))
				continue;
			
			if (firstCharUppercase)
				name = p.getName().charAt(0) + "".toUpperCase() + p.getName().substring(1);
			else
				name = p.getName();

			header.add(name);
			Object obj;
			try {
				obj = p.get(o_);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
			value.add(obj.toString());
		}

		if (transpose) {
			table.Transpose();
		}
		return table.toString();
	}
}
