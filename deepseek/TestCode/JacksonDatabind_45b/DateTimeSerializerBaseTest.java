package com.fasterxml.jackson.databind.ser.std;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DateTimeSerializerBaseTest {

    private TimeZone originalTimeZone;

    @Before
    public void setUp() {
        originalTimeZone = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"));
    }

    @After
    public void tearDown() {
        TimeZone.setDefault(originalTimeZone);
    }

    // --- Concrete testable subclass ----------------------------------------

    private static class TestableDateSerializer extends DateTimeSerializerBase<Date> {

        TestableDateSerializer(boolean useTimestamp, DateFormat customFormat) {
            super(Date.class, useTimestamp, customFormat);
        }

        @Override
        public DateTimeSerializerBase<Date> withFormat(boolean timestamp, DateFormat customFormat) {
            return new TestableDateSerializer(timestamp, customFormat);
        }

        @Override
        protected long _timestamp(Date value) {
            return value.getTime();
        }
    }

    // --- Bean fixtures ------------------------------------------------------

    public static class StringShapeBean {
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public Date date = new Date(0L);
    }

    public static class NumericShapeBean {
        @JsonFormat(shape = JsonFormat.Shape.NUMBER)
        public Date date = new Date(0L);
    }

    public static class PatternBean {
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "UTC")
        public Date date = new Date(0L);
    }

    public static class TimezoneOnlyBean {
        @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC")
        public Date date = new Date(0L);
    }

    public static class PatternOnlyBean {
        @JsonFormat(pattern = "yyyy-MM-dd", timezone = "UTC")
        public Date date = new Date(0L);
    }

    // --- Direct serialize() tests ------------------------------------------

    @Test
    public void testSerializeWithTimestampTrue() throws Exception {
        TestableDateSerializer serializer = new TestableDateSerializer(true, null);
        StringWriter writer = new StringWriter();
        JsonGenerator generator = new JsonFactory().createGenerator(writer);

        Date date = new Date(123456789L);
        serializer.serialize(date, generator, null);

        generator.flush();
        assertEquals("123456789", writer.toString());
    }

    @Test
    public void testSerializeWithCustomFormat() throws Exception {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));

        TestableDateSerializer serializer = new TestableDateSerializer(false, format);
        StringWriter writer = new StringWriter();
        JsonGenerator generator = new JsonFactory().createGenerator(writer);

        serializer.serialize(new Date(0L), generator, null);

        generator.flush();
        assertEquals("\"1970-01-01\"", writer.toString());
    }

    @Test
    public void testSerializeWithoutCustomFormatUsesProviderDefault() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        mapper.setDateFormat(format);

        SerializerProvider provider = mapper.getSerializerProvider();
        TestableDateSerializer serializer = new TestableDateSerializer(false, null);
        StringWriter writer = new StringWriter();
        JsonGenerator generator = new JsonFactory().createGenerator(writer);

        serializer.serialize(new Date(0L), generator, provider);

        generator.flush();
        assertEquals("\"1969-12-31 19:00:00\"", writer.toString());
    }

    // --- createContextual() tests through ObjectMapper ----------------------

    @Test
    public void testRootDateWithTimestampsEnabledUsesNumber() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        assertEquals("0", mapper.writeValueAsString(new Date(0L)));
    }

    @Test
    public void testRootDateWithTimestampsDisabledUsesString() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        mapper.setDateFormat(format);

        assertEquals("\"1969-12-31 19:00:00\"", mapper.writeValueAsString(new Date(0L)));
    }

    @Test
    public void testStringShapeOverridesDefaultTimestampsEnabled() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        String json = mapper.writeValueAsString(new StringShapeBean());
        assertTrue(json.startsWith("\""));
        assertFalse("0".equals(json));
    }

    @Test
    public void testNumericShapeOverridesDefaultTimestampsDisabled() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        assertEquals("0", mapper.writeValueAsString(new NumericShapeBean()));
    }

    @Test
    public void testStringShapeUsesConfiguredDateFormat() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        mapper.setDateFormat(format);

        assertEquals("\"1969-12-31 19:00:00\"", mapper.writeValueAsString(new StringShapeBean()));
    }

    @Test
    public void testPatternAndTimezoneApplied() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        mapper.setDateFormat(format);

        assertEquals("\"1970-01-01\"", mapper.writeValueAsString(new PatternBean()));
    }

    @Test
    public void testTimezoneOnlyOverridesConfiguredDateFormatTimezone() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        mapper.setDateFormat(format);

        assertEquals("\"1970-01-01 00:00:00\"", mapper.writeValueAsString(new TimezoneOnlyBean()));
    }

    @Test
    public void testPatternWithoutShapeIsHonored() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        assertEquals("\"1970-01-01\"", mapper.writeValueAsString(new PatternOnlyBean()));
    }
}