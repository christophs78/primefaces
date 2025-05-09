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
package org.primefaces.component.datatable.feature;

import org.primefaces.component.datatable.DataTable;
import org.primefaces.component.datatable.DataTableRenderer;
import org.primefaces.component.datatable.DataTableState;
import org.primefaces.component.rowexpansion.RowExpansion;
import org.primefaces.context.PrimeRequestContext;
import org.primefaces.util.LangUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

public class RowExpandFeature implements DataTableFeature {

    @Override
    public void decode(FacesContext context, DataTable table) {
        Map<String, String> params = context.getExternalContext().getRequestParameterMap();
        String rowExpansionState = params.get(table.getClientId(context) + "_rowExpansionState");

        Set<String> rowKeys = Collections.emptySet();
        if (LangUtils.isNotBlank(rowExpansionState)) {
            rowKeys = LangUtils.newLinkedHashSet(rowExpansionState.split(","));
        }

        table.setExpandedRowKeys(rowKeys);

        if (table.isMultiViewState()) {
            DataTableState ts = table.getMultiViewState(true);
            ts.setExpandedRowKeys(rowKeys);
        }
    }

    @Override
    public void encode(FacesContext context, DataTableRenderer renderer, DataTable table) throws IOException {
        Map<String, String> params = context.getExternalContext().getRequestParameterMap();
        int expandedRowIndex = Integer.parseInt(params.get(table.getClientId(context) + "_expandedRowIndex"));

        table.loadLazyDataIfRequired();

        encodeExpansion(context, renderer, table, expandedRowIndex);
        table.setRowIndex(-1);
    }

    public void encodeExpansion(FacesContext context, DataTableRenderer renderer, DataTable table, int rowIndex) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String rowIndexVar = table.getRowIndexVar();
        table.setRowIndex(rowIndex);

        RowExpansion rowExpansion = table.getRowExpansion();
        if (rowExpansion != null && rowExpansion.isRendered()) {
            String styleClass = PrimeRequestContext.getCurrentInstance(context).getStyleClassBuilder()
                .add(DataTable.EXPANDED_ROW_CONTENT_CLASS)
                .add("ui-widget-content")
                .add(rowExpansion.getStyleClass())
                .build();

            if (rowIndexVar != null) {
                context.getExternalContext().getRequestMap().put(rowIndexVar, rowIndex);
            }

            writer.startElement("tr", null);
            writer.writeAttribute("class", styleClass, null);

            writer.startElement("td", null);
            writer.writeAttribute("colspan", table.getColumnsCount(), null);

            rowExpansion.encodeAll(context);

            writer.endElement("td");

            writer.endElement("tr");
        }
    }

    @Override
    public boolean shouldDecode(FacesContext context, DataTable table) {
        return context.getExternalContext().getRequestParameterMap().containsKey(table.getClientId(context) + "_rowExpansionState");
    }

    @Override
    public boolean shouldEncode(FacesContext context, DataTable table) {
        return context.getExternalContext().getRequestParameterMap().containsKey(table.getClientId(context) + "_rowExpansion");
    }

}
