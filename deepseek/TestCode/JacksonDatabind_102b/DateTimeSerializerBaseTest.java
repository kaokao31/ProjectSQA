package com.fasterxml.jackson.databind.ser.std;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.DateTimeSerializerBase;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import static org.junit.Assert.*;

/**
 * Test suite for DateTimeSerializerBase.
 * Targets maximum coverage and fault detection (Defects4J bug 102).
 */
public class DateTimeSerializerBaseTest {

    private ObjectMapper mapper;
    private TestDateTimeSerializer serializer;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        // Disable default date formatting to use timestamps
        mapper.configure(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
        serializer = new TestDateTimeSerializer();
    }

    // ------------------------------------------------------------
    // Concrete subclass for testing abstract DateTimeSerializerBase
    // ------------------------------------------------------------
    @SuppressWarnings("serial")
    public static class TestDateTimeSerializer extends DateTimeSerializerBase<Date> {
        public TestDateTimeSerializer() {
            super(Date.class, null, false);
        }

        @Override
        public void serialize(Date value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            // Delegate to base implementation
            if (_asTimestamp(value)) {
                gen.writeNumber(_timestamp(value));
            } else {
                gen.writeString(_format(value));
            }
        }

        // Helper to format as string (simplified)
        private String _format(Date value) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            return sdf.format(value);
        }

        @Override
        protected long _timestamp(Date value) {
            return value.getTime();
        }

        @Override
        protected boolean _asTimestamp(Date value) {
            // Default: use timestamp unless overridden by annotation
            return true;
        }
    }

    // ------------------------------------------------------------
    // Tests for serialize with Date
    // ------------------------------------------------------------
    @Test
    public void testSerializeDateAsTimestamp() throws Exception {
        Date date = new Date(123456789L);
        String json = mapper.writeValueAsString(date);
        assertEquals("123456789", json);
    }

    @Test
    public void testSerializeDateAsString() throws Exception {
        // Configure mapper to use string format
        ObjectMapper stringMapper = new ObjectMapper();
        stringMapper.configure(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        stringMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
        Date date = new Date(123456789L);
        String json = stringMapper.writeValueAsString(date);
        assertTrue(json.startsWith("\""));
        assertTrue(json.endsWith("\""));
    }

    @Test
    public void testSerializeNullDate() throws Exception {
        String json = mapper.writeValueAsString(null);
        assertEquals("null", json);
    }

    @Test
    public void testSerializeDateEpoch() throws Exception {
        Date epoch = new Date(0);
        String json = mapper.writeValueAsString(epoch);
        assertEquals("0", json);
    }

    @Test
    public void testSerializeDateMaxLong() throws Exception {
        Date max = new Date(Long.MAX_VALUE);
        String json = mapper.writeValueAsString(max);
        assertEquals(String.valueOf(Long.MAX_VALUE), json);
    }

    @Test
    public void testSerializeDateMinLong() throws Exception {
        Date min = new Date(Long.MIN_VALUE);
        String json = mapper.writeValueAsString(min);
        assertEquals(String.valueOf(Long.MIN_VALUE), json);
    }

    // ------------------------------------------------------------
    // Tests for serialize with Calendar
    // ------------------------------------------------------------
    @Test
    public void testSerializeCalendarAsTimestamp() throws Exception {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(987654321L);
        String json = mapper.writeValueAsString(cal);
        assertEquals("987654321", json);
    }

    @Test
    public void testSerializeCalendarAsString() throws Exception {
        ObjectMapper stringMapper = new ObjectMapper();
        stringMapper.configure(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        stringMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(987654321L);
        String json = stringMapper.writeValueAsString(cal);
        assertTrue(json.startsWith("\""));
        assertTrue(json.endsWith("\""));
    }

    @Test
    public void testSerializeNullCalendar() throws Exception {
        Calendar cal = null;
        String json = mapper.writeValueAsString(cal);
        assertEquals("null", json);
    }

    @Test
    public void testSerializeCalendarEpoch() throws Exception {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(0);
        String json = mapper.writeValueAsString(cal);
        assertEquals("0", json);
    }

    // ------------------------------------------------------------
    // Tests for Calendar subclass (Defects4J bug 102 scenario)
    // ------------------------------------------------------------
    @Test
    public void testSerializeCustomCalendarSubclass() throws Exception {
        // Create a Calendar subclass that overrides getTimeInMillis()
        Calendar custom = new GregorianCalendar() {
            @Override
            public long getTimeInMillis() {
                // Return a different value to test if serializer uses overridden method
                return 123456789L;
            }
        };
        // The serializer should use the overridden getTimeInMillis()
        String json = mapper.writeValueAsString(custom);
        assertEquals("123456789", json);
    }

    @Test
    public void testSerializeCalendarWithTimeZone() throws Exception {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.setTimeInMillis(1000000L);
        String json = mapper.writeValueAsString(cal);
        assertEquals("1000000", json);
    }

    // ------------------------------------------------------------
    // Tests for _timestamp method (via reflection or direct call)
    // ------------------------------------------------------------
    @Test
    public void testTimestampDate() throws Exception {
        Date date = new Date(555555L);
        long ts = serializer._timestamp(date);
        assertEquals(555555L, ts);
    }

    @Test
    public void testTimestampCalendar() throws Exception {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(777777L);
        // _timestamp(Calendar) is not directly exposed; test via serialization
        String json = mapper.writeValueAsString(cal);
        assertEquals("777777", json);
    }

    // ------------------------------------------------------------
    // Tests for _asTimestamp method
    // ------------------------------------------------------------
    @Test
    public void testAsTimestampTrue() throws Exception {
        Date date = new Date();
        assertTrue(serializer._asTimestamp(date));
    }

    @Test
    public void testAsTimestampFalse() throws Exception {
        // Override _asTimestamp to return false for testing
        TestDateTimeSerializer stringSerializer = new TestDateTimeSerializer() {
            @Override
            protected boolean _asTimestamp(Date value) {
                return false;
            }
        };
        // Use a custom ObjectMapper with this serializer
        ObjectMapper customMapper = new ObjectMapper();
        customMapper.configOverride(Date.class).setSerializer(stringSerializer);
        Date date = new Date(123L);
        String json = customMapper.writeValueAsString(date);
        // Should be a string representation
        assertTrue(json.startsWith("\""));
    }

    // ------------------------------------------------------------
    // Edge cases: java.sql.Date and java.sql.Timestamp
    // ------------------------------------------------------------
    @Test
    public void testSerializeSqlDate() throws Exception {
        java.sql.Date sqlDate = new java.sql.Date(123456789L);
        String json = mapper.writeValueAsString(sqlDate);
        assertEquals("123456789", json);
    }

    @Test
    public void testSerializeSqlTimestamp() throws Exception {
        java.sql.Timestamp ts = new java.sql.Timestamp(987654321L);
        String json = mapper.writeValueAsString(ts);
        assertEquals("987654321", json);
    }

    // ------------------------------------------------------------
    // Test with custom JsonFormat annotation (shape STRING)
    // ------------------------------------------------------------
    @Test
    public void testSerializeWithJsonFormatString() throws Exception {
        // Use a bean with @JsonFormat annotation
        BeanWithDateFormat bean = new BeanWithDateFormat();
        bean.date = new Date(111111L);
        String json = mapper.writeValueAsString(bean);
        // Should be a string due to annotation
        assertTrue(json.contains("\"date\":\"1970-01-01T00:01:51.111+0000\""));
    }

    static class BeanWithDateFormat {
        @com.fasterxml.jackson.annotation.JsonFormat(shape = com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        public Date date;
    }

    // ------------------------------------------------------------
    // Test for null handling in serialize method
    // ------------------------------------------------------------
    @Test
    public void testSerializeNullWithSerializer() throws Exception {
        // Directly test the serializer's serialize method with null
        JsonGenerator gen = mapper.getFactory().createGenerator(System.out);
        SerializerProvider prov = mapper.getSerializerProvider();
        try {
            serializer.serialize(null, gen, prov);
            // Should not throw exception; base class handles null
        } catch (Exception e) {
            fail("Serializing null should not throw exception");
        }
    }

    // ------------------------------------------------------------
    // Additional coverage: boundary values for Calendar
    // ------------------------------------------------------------
    @Test
    public void testSerializeCalendarMaxLong() throws Exception {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(Long.MAX_VALUE);
        String json = mapper.writeValueAsString(cal);
        assertEquals(String.valueOf(Long.MAX_VALUE), json);
    }

    @Test
    public void testSerializeCalendarMinLong() throws Exception {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(Long.MIN_VALUE);
        String json = mapper.writeValueAsString(cal);
        assertEquals(String.valueOf(Long.MIN_VALUE), json);
    }
}