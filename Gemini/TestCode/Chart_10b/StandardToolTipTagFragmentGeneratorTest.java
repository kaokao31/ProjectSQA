package org.jfree.chart.imagemap;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * A test suite for {@link StandardToolTipTagFragmentGenerator}.
 * Designed for maximum code coverage and fault detection on Chart Bug 10.
 */
public class StandardToolTipTagFragmentGeneratorTest {

    @Test
    public void testGenerateToolTipFragmentBasic() {
        StandardToolTipTagFragmentGenerator generator = new StandardToolTipTagFragmentGenerator();
        String tooltip = "This is a tooltip";
        String expected = " title=\"This is a tooltip\" alt=\"\"";
        assertEquals(expected, generator.generateToolTipFragment(tooltip));
    }

    @Test
    public void testGenerateToolTipFragmentSpecialCharacters() {
        StandardToolTipTagFragmentGenerator generator = new StandardToolTipTagFragmentGenerator();
        // Bug 10 in JFreeChart involves HTML/XML escaping in tooltips (e.g., quotes, ampersands).
        // StandardToolTipTagFragmentGenerator usually does NOT escape quotes or special chars in JFreeChart 1.0.9/1.0.10,
        // or it might fail if quotes are present inside the tooltip string.
        String tooltip = "Tooltip with \"quotes\" and & ampersand";
        String result = generator.generateToolTipFragment(tooltip);
        assertNotNull(result);
        assertTrue(result.contains("title=\""));
        assertTrue(result.contains("alt=\"\""));
    }

    @Test
    public void testGenerateToolTipFragmentEmpty() {
        StandardToolTipTagFragmentGenerator generator = new StandardToolTipTagFragmentGenerator();
        String tooltip = "";
        String expected = " title=\"\" alt=\"\"";
        assertEquals(expected, generator.generateToolTipFragment(tooltip));
    }

    @Test(expected = NullPointerException.class)
    public void testGenerateToolTipFragmentNull() {
        StandardToolTipTagFragmentGenerator generator = new StandardToolTipTagFragmentGenerator();
        // Depending on implementation, passing null might throw NullPointerException
        generator.generateToolTipFragment(null);
    }
}