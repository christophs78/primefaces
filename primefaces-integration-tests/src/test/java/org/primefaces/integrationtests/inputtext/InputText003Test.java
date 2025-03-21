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
package org.primefaces.integrationtests.inputtext;

import org.primefaces.selenium.AbstractPrimePage;
import org.primefaces.selenium.AbstractPrimePageTest;
import org.primefaces.selenium.component.CommandButton;
import org.primefaces.selenium.component.InputText;

import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InputText003Test extends AbstractPrimePageTest {

    @Test
    @Order(1)
    @DisplayName("InputText: MaxLength using less than max # of characters")
    void maxLengthLessThan(Page page) {
        // Arrange
        InputText inputText = page.inputtext;
        assertEquals("", inputText.getValue());

        // Act
        inputText.setValue("four");
        page.button.click();

        // Assert
        assertEquals("MaxLength Counter", inputText.getAssignedLabelText());
        assertEquals("four", inputText.getValue());
        assertEquals("6 characters remaining.", page.display.getText());
        assertConfiguration(inputText.getWidgetConfiguration());
    }

    @Test
    @Order(2)
    @DisplayName("InputText: MaxLength using more than max # of characters")
    void maxLengthMoreThan(Page page) {
        // Arrange
        InputText inputText = page.inputtext;
        assertEquals("", inputText.getValue());

        // Act
        inputText.setValue("12345678901234");
        page.button.click();

        // Assert
        assertEquals("MaxLength Counter", inputText.getAssignedLabelText());
        assertEquals("1234567890", inputText.getValue());
        assertEquals("0 characters remaining.", page.display.getText());
        assertConfiguration(inputText.getWidgetConfiguration());
    }

    private void assertConfiguration(JSONObject cfg) {
        assertNoJavascriptErrors();
        System.out.println("InputText Config = " + cfg);
        assertEquals(10, cfg.getInt("maxlength"));
    }

    public static class Page extends AbstractPrimePage {
        @FindBy(id = "form:inputtext")
        InputText inputtext;

        @FindBy(id = "form:display")
        WebElement display;

        @FindBy(id = "form:button")
        CommandButton button;

        @Override
        public String getLocation() {
            return "inputtext/inputText003.xhtml";
        }
    }
}
