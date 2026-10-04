package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class CommandLineTest {

    private CommandLine cmd;

    @Before
    public void setUp() {
        cmd = new CommandLine();
    }

    @Test
    public void testEmptyCommandLine() {
        assertNotNull(cmd.getArgs());
        assertEquals(0, cmd.getArgs().length);
        assertNotNull(cmd.getArgList());
        assertEquals(0, cmd.getArgList().size());
        assertNotNull(cmd.getOptions());
        assertEquals(0, cmd.getOptions().length);
        assertNotNull(cmd.iterator());
        assertFalse(cmd.iterator().hasNext());
    }

    @Test
    public void testAddAndGetArgs() {
        cmd.addArg("arg1");
        cmd.addArg("arg2");
        cmd.addArg(null);

        List<String> argList = cmd.getArgList();
        assertEquals(3, argList.size());
        assertEquals("arg1", argList.get(0));
        assertEquals("arg2", argList.get(1));
        assertNull(argList.get(2));

        String[] args = cmd.getArgs();
        assertEquals(3, args.length);
        assertEquals("arg1", args[0]);
        assertEquals("arg2", args[1]);
        assertNull(args[2]);
    }

    @Test
    public void testHasOptionByString() {
        Option optA = OptionBuilder.withLongOpt("opt-a").hasArg().create('a');
        cmd.addOption(optA);

        assertTrue(cmd.hasOption("a"));
        assertTrue(cmd.hasOption("opt-a"));
        assertTrue(cmd.hasOption("-a"));
        assertTrue(cmd.hasOption("--opt-a"));
        assertFalse(cmd.hasOption("b"));
        assertFalse(cmd.hasOption("non-existent"));
        assertFalse(cmd.hasOption((String) null));
    }

    @Test
    public void testHasOptionByChar() {
        Option optA = new Option("a", "alpha", false, "Option A");
        cmd.addOption(optA);

        assertTrue(cmd.hasOption('a'));
        assertFalse(cmd.hasOption('b'));
    }

    @Test
    public void testHasOptionByOptionObject() {
        Option optA = new Option("a", "alpha", false, "Option A");
        Option optB = new Option("b", "beta", false, "Option B");
        cmd.addOption(optA);

        assertTrue(cmd.hasOption(optA));
        assertFalse(cmd.hasOption(optB));
        assertFalse(cmd.hasOption((Option) null));
    }

    @Test
    public void testGetOptionValueString() {
        Option opt = OptionBuilder.hasArg().create('f');
        opt.addValueForProcessing("foo.txt");
        cmd.addOption(opt);

        assertEquals("foo.txt", cmd.getOptionValue("f"));
        assertEquals("foo.txt", cmd.getOptionValue('f'));
        assertEquals("foo.txt", cmd.getOptionValue(opt));
        assertNull(cmd.getOptionValue("non-existent"));
        assertNull(cmd.getOptionValue('x'));
    }

    @Test
    public void testGetOptionValueWithDefault() {
        Option opt = OptionBuilder.hasArg().create('f');
        opt.addValueForProcessing("file.txt");
        cmd.addOption(opt);

        assertEquals("file.txt", cmd.getOptionValue("f", "default.txt"));
        assertEquals("file.txt", cmd.getOptionValue('f', "default.txt"));
        assertEquals("file.txt", cmd.getOptionValue(opt, "default.txt"));

        assertEquals("default.txt", cmd.getOptionValue("non-existent", "default.txt"));
        assertEquals("default.txt", cmd.getOptionValue('x', "default.txt"));
        assertEquals("default.txt", cmd.getOptionValue(new Option("z", "missing"), "default.txt"));
    }

    @Test
    public void testGetOptionValues() {
        Option opt = OptionBuilder.hasArgs(3).create('m');
        opt.addValueForProcessing("v1");
        opt.addValueForProcessing("v2");
        opt.addValueForProcessing("v3");
        cmd.addOption(opt);

        String[] expected = new String[]{"v1", "v2", "v3"};
        assertArrayEquals(expected, cmd.getOptionValues("m"));
        assertArrayEquals(expected, cmd.getOptionValues('m'));
        assertArrayEquals(expected, cmd.getOptionValues(opt));

        assertNull(cmd.getOptionValues("unknown"));
        assertNull(cmd.getOptionValues('u'));
        assertNull(cmd.getOptionValues(new Option("u", "unknown")));
    }

    @Test
    public void testGetOptionObject() throws Exception {
        Option opt = OptionBuilder.hasArg().withType(Number.class).create('n');
        opt.addValueForProcessing("123");
        cmd.addOption(opt);

        Object val = cmd.getOptionObject("n");
        assertNotNull(val);
        assertTrue(val instanceof Number);
        assertEquals(123L, ((Number) val).longValue());

        Object charVal = cmd.getOptionObject('n');
        assertNotNull(charVal);
        assertEquals(123L, ((Number) charVal).longValue());

        assertNull(cmd.getOptionObject("missing"));
        assertNull(cmd.getOptionObject('m'));
    }

    @Test
    public void testGetParsedOptionValue() throws Exception {
        Option opt = OptionBuilder.hasArg().withType(Number.class).create('n');
        opt.addValueForProcessing("456");
        cmd.addOption(opt);

        Object val = cmd.getParsedOptionValue("n");
        assertNotNull(val);
        assertEquals(456L, ((Number) val).longValue());

        Object charVal = cmd.getParsedOptionValue('n');
        assertNotNull(charVal);
        assertEquals(456L, ((Number) charVal).longValue());

        Object optVal = cmd.getParsedOptionValue(opt);
        assertNotNull(optVal);
        assertEquals(456L, ((Number) optVal).longValue());

        assertNull(cmd.getParsedOptionValue("unknown"));
        assertNull(cmd.getParsedOptionValue('u'));
        assertNull(cmd.getParsedOptionValue(new Option("u", "unknown")));
    }

    @Test
    public void testGetOptionProperties() {
        Option opt = OptionBuilder.hasArgs(2).withValueSeparator('=').create('D');
        opt.addValueForProcessing("key1=value1");
        opt.addValueForProcessing("key2=value2");
        cmd.addOption(opt);

        Properties props = cmd.getOptionProperties("D");
        assertNotNull(props);
        assertEquals("value1", props.getProperty("key1"));
        assertEquals("value2", props.getProperty("key2"));

        Properties propsOpt = cmd.getOptionProperties(opt);
        assertNotNull(propsOpt);
        assertEquals("value1", propsOpt.getProperty("key1"));
        assertEquals("value2", propsOpt.getProperty("key2"));

        Properties emptyProps = cmd.getOptionProperties("unknown");
        assertNotNull(emptyProps);
        assertTrue(emptyProps.isEmpty());

        Properties emptyOptProps = cmd.getOptionProperties(new Option("z", "missing"));
        assertNotNull(emptyOptProps);
        assertTrue(emptyOptProps.isEmpty());
    }

    @Test
    public void testGetOptionPropertiesOddArguments() {
        Option opt = OptionBuilder.hasArgs().withValueSeparator().create('D');
        opt.addValueForProcessing("singleKey");
        cmd.addOption(opt);

        Properties props = cmd.getOptionProperties("D");
        assertNotNull(props);
        assertEquals("true", props.getProperty("singleKey"));
    }

    @Test
    public void testMultipleOptionsIteration() {
        Option optA = new Option("a", "first");
        Option optB = new Option("b", "second");
        cmd.addOption(optA);
        cmd.addOption(optB);

        Option[] options = cmd.getOptions();
        assertEquals(2, options.length);
        assertEquals(optA, options[0]);
        assertEquals(optB, options[1]);

        Iterator<Option> it = cmd.iterator();
        assertTrue(it.hasNext());
        assertEquals(optA, it.next());
        assertTrue(it.hasNext());
        assertEquals(optB, it.next());
        assertFalse(it.hasNext());
    }

    @Test
    public void testSerialization() throws Exception {
        Option opt = OptionBuilder.hasArg().create('s');
        opt.addValueForProcessing("serializeTest");
        cmd.addOption(opt);
        cmd.addArg("extraArg");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(cmd);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        CommandLine deserialized = (CommandLine) ois.readObject();
        ois.close();

        assertNotNull(deserialized);
        assertTrue(deserialized.hasOption("s"));
        assertEquals("serializeTest", deserialized.getOptionValue("s"));
        assertEquals(1, deserialized.getArgs().length);
        assertEquals("extraArg", deserialized.getArgs()[0]);
    }
}