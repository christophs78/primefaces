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
package org.primefaces.component.sidebar;

import org.primefaces.expression.SearchExpressionUtils;
import org.primefaces.renderkit.CoreRenderer;
import org.primefaces.util.WidgetBuilder;

import java.io.IOException;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

public class SidebarRenderer extends CoreRenderer {

    @Override
    public void decode(FacesContext context, UIComponent component) {
        decodeBehaviors(context, component);
    }

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        Sidebar sidebar = (Sidebar) component;

        if (sidebar.isContentLoadRequest(context)) {
            renderChildren(context, sidebar);
        }
        else {
            encodeMarkup(context, sidebar);
            encodeScript(context, sidebar);
        }
    }

    protected void encodeMarkup(FacesContext context, Sidebar sidebar) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String style = sidebar.getStyle();
        String styleClass = sidebar.getStyleClass();
        styleClass = styleClass == null ? Sidebar.STYLE_CLASS : Sidebar.STYLE_CLASS + " " + styleClass;
        styleClass = sidebar.isFullScreen() ? styleClass + " " + Sidebar.FULL_BAR_CLASS : styleClass;
        styleClass += " ui-sidebar-" + sidebar.getPosition();

        writer.startElement("div", sidebar);
        writer.writeAttribute("id", sidebar.getClientId(context), null);
        writer.writeAttribute("class", styleClass, null);
        if (style != null) {
            writer.writeAttribute("style", style, null);
        }

        if (sidebar.isShowCloseIcon()) {
            encodeCloseIcon(context);
        }

        writer.startElement("div", null);
        writer.writeAttribute("class", Sidebar.CONTENT_CLASS, null);
        writer.writeAttribute("id", sidebar.getClientId(context) + "_content", null);

        if (!sidebar.isDynamic()) {
            renderChildren(context, sidebar);
        }

        writer.endElement("div");
        writer.endElement("div");
    }

    protected void encodeCloseIcon(FacesContext context) throws IOException {
        ResponseWriter writer = context.getResponseWriter();

        writer.startElement("a", null);
        writer.writeAttribute("href", "#", null);
        writer.writeAttribute("class", Sidebar.TITLE_BAR_CLOSE_CLASS, null);

        writer.startElement("span", null);
        writer.writeAttribute("class", Sidebar.CLOSE_ICON_CLASS, null);
        writer.endElement("span");

        writer.endElement("a");
    }

    private void encodeScript(FacesContext context, Sidebar sidebar) throws IOException {
        WidgetBuilder wb = getWidgetBuilder(context);
        wb.init("Sidebar", sidebar)
                .attr("visible", sidebar.isVisible(), false)
                .attr("modal", sidebar.isModal(), true)
                .attr("blockScroll", sidebar.isBlockScroll(), false)
                .attr("baseZIndex", sidebar.getBaseZIndex(), 0)
                .attr("dynamic", sidebar.isDynamic(), false)
                .attr("showCloseIcon", sidebar.isShowCloseIcon(), true)
                .attr("appendTo", SearchExpressionUtils.resolveOptionalClientIdForClientSide(context, sidebar, sidebar.getAppendTo()))
                .callback("onHide", "function()", sidebar.getOnHide())
                .callback("onShow", "function()", sidebar.getOnShow());

        encodeClientBehaviors(context, sidebar);

        wb.finish();
    }

    @Override
    public void encodeChildren(FacesContext context, UIComponent component) throws IOException {
        //Do nothing
    }

    @Override
    public boolean getRendersChildren() {
        return true;
    }
}
