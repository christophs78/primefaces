/*
 * The MIT License
 *
 * Copyright (c) 2009-2025 PrimeTek Informatics
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.primefaces.component.api;

import org.primefaces.component.column.Column;
import org.primefaces.component.columngroup.ColumnGroup;
import org.primefaces.component.columns.Columns;
import org.primefaces.component.row.Row;
import org.primefaces.model.ColumnMeta;
import org.primefaces.util.ComponentUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import jakarta.faces.FacesException;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.visit.VisitContext;
import jakarta.faces.component.visit.VisitResult;
import jakarta.faces.context.FacesContext;

public interface ColumnAware {

    default void forEachColumn(Predicate<UIColumn> callback) {
        forEachColumn(true, true, false, callback);
    }

    default void forEachColumn(boolean unwrapDynamicColumns, boolean skipUnrendered, boolean skipColumnGroups, Predicate<UIColumn> callback) {
        forEachColumn(FacesContext.getCurrentInstance(), (UIComponent) this, unwrapDynamicColumns, skipUnrendered, skipColumnGroups, callback);
    }

    /**
     * NOTE: this is for internal usage only!
     *
     * @param context the {@link FacesContext}
     * @param root the {@link UIComponent} where the search starts from
     * @param unwrapDynamicColumns if the callback should be called for each item of p:columns with {@link DynamicColumn},
     *                              or just once with {@link Columns}
     * @param skipUnrendered If unrendered components should be skipped
     * @param skipColumnGroups If {@link ColumnGroup} components should be skipped
     * @param callback The callback, which will be invoked for each column. If it returns false, the algorithm will be cancelled
     * @return false when the algorithm was cancelled
     */
    default boolean forEachColumn(FacesContext context, UIComponent root, boolean unwrapDynamicColumns, boolean skipUnrendered, boolean skipColumnGroups,
            Predicate<UIColumn> callback) {
        for (int i = 0; i < root.getChildCount(); i++) {
            UIComponent child = root.getChildren().get(i);
            if (!(child instanceof Columns) && skipUnrendered && !child.isRendered()) {
                continue;
            }

            if (child instanceof Columns) {
                Columns columns = (Columns) child;
                if (unwrapDynamicColumns) {
                    for (int j = 0; j < columns.getRowCount(); j++) {
                        DynamicColumn dynaColumn = new DynamicColumn(j, columns, context);
                        dynaColumn.applyStatelessModel();

                        if (skipUnrendered && !dynaColumn.isRendered()) {
                            continue;
                        }

                        if (!callback.test(dynaColumn)) {
                            return false;
                        }
                    }
                }
                else {
                    if (!callback.test(columns)) {
                        return false;
                    }
                }
            }
            else if (child instanceof Column) {
                Column column = (Column) child;
                if (!callback.test(column)) {
                    return false;
                }
            }
            else if (child instanceof ColumnGroup && !skipColumnGroups) {
                // columnGroup must contain p:row(s) as child
                for (int j = 0; j < child.getChildCount(); j++) {
                    UIComponent row = child.getChildren().get(j);
                    if (skipUnrendered && !row.isRendered()) {
                        continue;
                    }

                    if (!forEachColumn(context, row, unwrapDynamicColumns, skipUnrendered, skipColumnGroups, callback)) {
                        return false;
                    }
                }
            }
            else if (child instanceof ColumnAware) {
                ColumnAware columnAware = (ColumnAware) child;
                for (int j = 0; j < ((UIComponent) columnAware).getChildCount(); j++) {
                    UIComponent columnAwareChild = ((UIComponent) columnAware).getChildren().get(j);
                    if (skipUnrendered && !columnAwareChild.isRendered()) {
                        continue;
                    }

                    if (!forEachColumn(context, columnAwareChild, unwrapDynamicColumns, skipUnrendered, skipColumnGroups, callback)) {
                        return false;
                    }
                }
            }
            else if (ComponentUtils.isUIRepeat(child)) {
                VisitContext visitContext = VisitContext.createVisitContext(context, null,
                        ComponentUtils.VISIT_HINTS_SKIP_UNRENDERED);
                child.visitTree(visitContext, (ctx, target) -> {
                    if (ComponentUtils.isUIRepeat(target)) {
                        return VisitResult.ACCEPT;
                    }

                    // for now just support basic p:column in ui:repeat
                    if (target instanceof Column) {
                        Column column = (Column) target;
                        if (!callback.test(column)) {
                            return VisitResult.COMPLETE;
                        }
                    }

                    return VisitResult.REJECT;
                });
            }
        }

        return true;
    }

    /**
     * Return each {@link Row} in a Column Group.
     * @param context  the {@link FacesContext}
     * @param cg the {@link ColumnGroup} to get
     * @param skipUnrendered If unrendered components should be skipped
     * @param callback The callback, which will be invoked for each row. If it returns false, the algorithm will be cancelled
     * @return false when the algorithm was cancelled
     */
    default boolean forEachColumnGroupRow(FacesContext context, ColumnGroup cg, boolean skipUnrendered, Predicate<Row> callback) {
        if (cg == null || cg.getChildCount() == 0) {
            return false;
        }
        for (UIComponent child : cg.getChildren()) {
            if (ComponentUtils.isUIRepeat(child)) {
                VisitContext visitContext = VisitContext.createVisitContext(context, null,
                        ComponentUtils.VISIT_HINTS_SKIP_UNRENDERED);
                child.visitTree(visitContext, (ctx, target) -> {
                    if (ComponentUtils.isUIRepeat(target)) {
                        return VisitResult.ACCEPT;
                    }

                    if (target instanceof Row) {
                        Row row = (Row) target;
                        if (!callback.test(row)) {
                            return VisitResult.COMPLETE;
                        }
                    }

                    return VisitResult.REJECT;
                });
            }
            else if (child instanceof Row) {
                Row row = (Row) child;
                if (skipUnrendered && !row.isRendered()) {
                    continue;
                }

                if (!callback.test(row)) {
                    return false;
                }
            }
        }
        return true;
    }

    default void invokeOnColumn(String columnKey, Consumer<UIColumn> callback) {
        forEachColumn((column) -> {
            if (column.getColumnKey().equals(columnKey)) {
                callback.accept(column);
                return false;
            }
            return true;
        });
    }

    default void invokeOnColumn(String columnKey, int rowIndex, Consumer<UIColumn> callback) {
        forEachColumn((column) -> {
            if (column.getColumnKey((UIComponent) this, rowIndex).equals(columnKey)) {
                callback.accept(column);
                return false;
            }
            return true;
        });
    }

    default UIColumn findColumn(String columnKey) {
        if ("globalFilter".equals(columnKey)) {
            return null;
        }

        List<UIColumn> columns = getColumns();

        //body columns
        for (int i = 0; i < columns.size(); i++) {
            UIColumn column = columns.get(i);
            if (Objects.equals(column.getColumnKey(), columnKey)) {
                return column;
            }
        }

        //header columns
        if (getFrozenColumnsCount() > 0) {
            UIColumn column = findColumnInGroup(columnKey, getColumnGroup("frozenHeader"));
            if (column == null) {
                column = findColumnInGroup(columnKey, getColumnGroup("scrollableHeader"));
            }

            if (column != null) {
                return column;
            }
        }
        else {
            return findColumnInGroup(columnKey, getColumnGroup("header"));
        }

        throw new FacesException("Cannot find column with key: " + columnKey);
    }

    default int getFrozenColumnsCount() {
        return 0;
    }

    default UIColumn findColumnInGroup(String columnKey, ColumnGroup group) {
        if (group == null) {
            return null;
        }

        for (int i = 0; i < group.getChildCount(); i++) {
            UIComponent row = group.getChildren().get(i);
            for (int j = 0; j < row.getChildCount(); j++) {
                UIComponent rowChild = row.getChildren().get(j);
                if (rowChild instanceof Column) {
                    Column column = (Column) rowChild;
                    if (Objects.equals(column.getColumnKey(), columnKey)) {
                        return column;
                    }
                }
                else if (rowChild instanceof Columns) {
                    Columns columns = (Columns) rowChild;
                    List<DynamicColumn> dynamicColumns = columns.getDynamicColumns();
                    for (UIColumn column : dynamicColumns) {
                        if (Objects.equals(column.getColumnKey(), columnKey)) {
                            return column;
                        }
                    }
                }
            }
        }

        return null;
    }

    default ColumnGroup getColumnGroup(String type) {
        for (int i = 0; i < ((UIComponent) this).getChildCount(); i++) {
            UIComponent child = ((UIComponent) this).getChildren().get(i);
            if (child instanceof ColumnGroup) {
                ColumnGroup colGroup = (ColumnGroup) child;
                if (Objects.equals(type, colGroup.getType())) {
                    return colGroup;
                }
            }
        }

        return null;
    }

    List<UIColumn> getColumns();

    void setColumns(List<UIColumn> columns);

    default List<UIColumn> collectColumns() {
        List<UIColumn> columns = new ArrayList<>();
        FacesContext context = FacesContext.getCurrentInstance();

        for (int i = 0; i < ((UIComponent) this).getChildCount(); i++) {
            UIComponent child = ((UIComponent) this).getChildren().get(i);
            if (child instanceof Column) {
                columns.add((Column) child);
            }
            else if (child instanceof Columns) {
                Columns uiColumns = (Columns) child;
                uiColumns.setRowIndex(-1);
                for (int j = 0; j < uiColumns.getRowCount(); j++) {
                    DynamicColumn dynaColumn = new DynamicColumn(j, uiColumns, context);
                    columns.add(dynaColumn);
                }
            }
        }

        Map<String, ColumnMeta> columnMeta = getColumnMeta();

        // sort by displayOrder
        columns.sort((c1, c2) -> {
            if (c1 instanceof DynamicColumn) {
                ((DynamicColumn) c1).applyStatelessModel();
            }

            Integer dp1 = c1.getDisplayPriority();
            ColumnMeta cm1 = columnMeta.get(c1.getColumnKey());
            if (cm1 != null && cm1.getDisplayPriority() != null) {
                dp1 = cm1.getDisplayPriority();
            }

            if (c2 instanceof DynamicColumn) {
                ((DynamicColumn) c2).applyStatelessModel();
            }

            Integer dp2 = c2.getDisplayPriority();
            ColumnMeta cm2 = columnMeta.get(c2.getColumnKey());
            if (cm2 != null && cm2.getDisplayPriority() != null) {
                dp2 = cm2.getDisplayPriority();
            }

            return dp1.compareTo(dp2);
        });

        return columns;
    }

    default int getColumnsCount() {
        return getColumnsCount(true);
    }

    default int getColumnsCount(boolean visibleOnly) {
        final LongAdder columnsCount = new LongAdder();

        forEachColumn(true, true, true, column -> {
            if (!visibleOnly || column.isVisible()) {
                columnsCount.increment();
            }
            return true;
        });

        return columnsCount.intValue();
    }

    default int getColumnsCountWithSpan() {
        return getColumnsCountWithSpan(true);
    }

    default int getColumnsCountWithSpan(boolean visibleOnly) {
        final LongAdder columnsCountWithSpan = new LongAdder();

        forEachColumn(true, true, true, column -> {
            if (!visibleOnly || column.isVisible()) {
                columnsCountWithSpan.add(column.getColspan());
            }
            return true;
        });

        return columnsCountWithSpan.intValue();
    }

    Map<String, ColumnMeta> getColumnMeta();

    void setColumnMeta(Map<String, ColumnMeta> columnMeta);

    default String getOrderedColumnKeys() {
        return getColumns()
                .stream()
                .map(e -> e.getColumnKey())
                .collect(Collectors.joining(","));
    }
}
