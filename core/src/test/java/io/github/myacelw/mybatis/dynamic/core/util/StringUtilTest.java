package io.github.myacelw.mybatis.dynamic.core.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StringUtilTest {

    @Test
    void toUnderlineCase() {
        assertEquals("xml_file", StringUtil.toUnderlineCase("xmlFile"));
        assertEquals("xml_file", StringUtil.toUnderlineCase("XmlFile"));
        assertEquals("xml_file", StringUtil.toUnderlineCase("XMLFile"));
        assertEquals("xml_file", StringUtil.toUnderlineCase("xml_file"));
        assertEquals("xml_file", StringUtil.toUnderlineCase("Xml_File"));
        assertEquals("xml_file", StringUtil.toUnderlineCase("XML_File"));
        assertEquals("xml", StringUtil.toUnderlineCase("XML"));
        assertEquals("x_file", StringUtil.toUnderlineCase("XFile"));

    }
}