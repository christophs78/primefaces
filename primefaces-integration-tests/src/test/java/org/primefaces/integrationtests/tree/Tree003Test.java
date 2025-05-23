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
package org.primefaces.integrationtests.tree;

import org.primefaces.selenium.AbstractPrimePage;
import org.primefaces.selenium.PrimeExpectedConditions;
import org.primefaces.selenium.PrimeSelenium;
import org.primefaces.selenium.component.Messages;
import org.primefaces.selenium.component.Tree;
import org.primefaces.selenium.component.model.tree.TreeNode;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Keys;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class Tree003Test extends AbstractTreeTest {

    @Test
    @Order(1)
    @DisplayName("Tree: Drag and drop still allows tab and arrow keys to select")
    void tabbing(Page page) {
        // Arrange
        Tree tree = page.tree;
        assertNotNull(tree);
        PrimeSelenium.waitGui().until(PrimeExpectedConditions.visibleInViewport(page.tree.getWrappedElement()));

        // Act
        Actions actions = new Actions(page.getWebDriver());
        Action actionSelect = actions.sendKeys(Keys.TAB).sendKeys(Keys.ARROW_DOWN).sendKeys(Keys.ARROW_RIGHT).build();
        actionSelect.perform();

        // Assert
        List<TreeNode> children = tree.getChildren();
        assertNotNull(children);
        assertEquals(3, children.size());

        TreeNode second = children.get(1);
        assertEquals("Events", second.getLabelText());
        assertDisplayed(second.getWebElement());

        List<TreeNode> secondChildren = second.getChildren();
        assertNotNull(secondChildren);
        assertEquals(3, secondChildren.size());

        assertDisplayed(secondChildren.get(0).getWebElement());
        assertDisplayed(secondChildren.get(1).getWebElement());
        assertDisplayed(secondChildren.get(2).getWebElement());

        assertConfiguration(tree.getWidgetConfiguration());
    }

    public static class Page extends AbstractPrimePage {
        @FindBy(id = "form:tree")
        Tree tree;

        @FindBy(id = "form:msgs")
        Messages messages;

        @Override
        public String getLocation() {
            return "tree/tree003.xhtml";
        }
    }
}
