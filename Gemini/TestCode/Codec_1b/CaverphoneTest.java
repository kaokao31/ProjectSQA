package org.apache.commons.codec.language;

import org.apache.commons.codec.EncoderException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class CaverphoneTest {

    private Caverphone caverphone;

    @Before
    public void setUp() {
        this.caverphone = new Caverphone();
    }

    @Test
    public void testCaverphoneNullAndEmpty() {
        assertEquals("1111111111", this.caverphone.caverphone(null));
        assertEquals("1111111111", this.caverphone.caverphone(""));
        assertEquals("1111111111", this.caverphone.caverphone("   "));
        assertEquals("1111111111", this.caverphone.caverphone("12345!@#$"));
    }

    @Test
    public void testEncodeString() {
        assertEquals("1111111111", this.caverphone.encode((String) null));
        assertEquals("1111111111", this.caverphone.encode(""));
        assertEquals(this.caverphone.caverphone("Stevenson"), this.caverphone.encode("Stevenson"));
        assertEquals(this.caverphone.caverphone("Peter"), this.caverphone.encode("Peter"));
    }

    @Test
    public void testEncodeObject() throws EncoderException {
        assertEquals("1111111111", this.caverphone.encode((Object) null));
        assertEquals(this.caverphone.caverphone("testing"), this.caverphone.encode((Object) "testing"));
        
        try {
            this.caverphone.encode(new Integer(5));
            fail("Expected EncoderException when encoding non-String Object");
        } catch (EncoderException e) {
            assertNotNull(e.getMessage());
        }

        try {
            this.caverphone.encode(new Object());
            fail("Expected EncoderException when encoding Object");
        } catch (EncoderException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testIsCaverphoneEqual() {
        assertTrue(this.caverphone.isCaverphoneEqual("Lee", "Leigh"));
        assertTrue(this.caverphone.isCaverphoneEqual("Stevenson", "Stephenson"));
        assertTrue(this.caverphone.isCaverphoneEqual("Smith", "Smyth"));
        assertTrue(this.caverphone.isCaverphoneEqual("", ""));
        assertTrue(this.caverphone.isCaverphoneEqual(null, null));
        assertTrue(this.caverphone.isCaverphoneEqual(null, ""));
        
        assertFalse(this.caverphone.isCaverphoneEqual("Peter", "Stevenson"));
        assertFalse(this.caverphone.isCaverphoneEqual("Thompson", "Williams"));
        assertFalse(this.caverphone.isCaverphoneEqual("A", "B"));
    }

    @Test
    public void testCaverphoneRulesCoverage() {
        // Test names/words that trigger specific transformation rules in Caverphone
        
        // Start replacements: cough, rough, tough, enough, trough, gn
        assertEquals(this.caverphone.caverphone("coughing"), this.caverphone.caverphone("cou2ffing"));
        assertEquals("KFFN111111", this.caverphone.caverphone("cough"));
        assertEquals("RFF1111111", this.caverphone.caverphone("rough"));
        assertEquals("TFF1111111", this.caverphone.caverphone("tough"));
        assertEquals("ANFF111111", this.caverphone.caverphone("enough"));
        assertEquals("TRF1111111", this.caverphone.caverphone("trough"));
        assertEquals("NN11111111", this.caverphone.caverphone("gnome"));

        // Ending 'e' removal
        assertEquals(this.caverphone.caverphone("love"), this.caverphone.caverphone("lov"));

        // mb -> m2 (ending)
        assertEquals("TM11111111", this.caverphone.caverphone("tomb"));
        assertEquals("KM11111111", this.caverphone.caverphone("climb"));

        // cq -> 2q, ci -> si, ce -> se, cy -> sy, tch -> 2ch, c -> k, q -> k, x -> k, v -> f, dg -> 2g
        assertEquals(this.caverphone.caverphone("acquire"), this.caverphone.caverphone("akquire"));
        assertEquals(this.caverphone.caverphone("city"), this.caverphone.caverphone("sity"));
        assertEquals(this.caverphone.caverphone("centre"), this.caverphone.caverphone("sentre"));
        assertEquals(this.caverphone.caverphone("cylinder"), this.caverphone.caverphone("sylinder"));
        assertEquals(this.caverphone.caverphone("catch"), this.caverphone.caverphone("cach"));
        assertEquals("KF11111111", this.caverphone.caverphone("cave"));
        assertEquals("K111111111", this.caverphone.caverphone("quick"));
        assertEquals("AKS1111111", this.caverphone.caverphone("axe"));
        assertEquals("FKSN111111", this.caverphone.caverphone("vixen"));
        assertEquals("AK11111111", this.caverphone.caverphone("edge"));

        // tio -> sio, tia -> sia, d -> t, ph -> fh, b -> p, sh -> s2, z -> s
        assertEquals(this.caverphone.caverphone("nation"), this.caverphone.caverphone("nasion"));
        assertEquals(this.caverphone.caverphone("spatial"), this.caverphone.caverphone("spasial"));
        assertEquals("TA11111111", this.caverphone.caverphone("day"));
        assertEquals("FA11111111", this.caverphone.caverphone("phase"));
        assertEquals("PA11111111", this.caverphone.caverphone("bay"));
        assertEquals("SA11111111", this.caverphone.caverphone("shoe"));
        assertEquals("SA11111111", this.caverphone.caverphone("zoo"));

        // Vowel handling: initial vs internal vowels
        assertEquals("AP11111111", this.caverphone.caverphone("ape"));
        assertEquals("EP11111111", this.caverphone.caverphone("epic"));
        assertEquals("AP11111111", this.caverphone.caverphone("open"));

        // j -> y, y3 -> Y3, y -> A
        assertEquals("Y111111111", this.caverphone.caverphone("joy"));
        assertEquals("YA11111111", this.caverphone.caverphone("yacht"));

        // 3gh3 -> 3kh3, gh -> 22, g -> k
        assertEquals("NKT1111111", this.caverphone.caverphone("night"));
        assertEquals("KT11111111", this.caverphone.caverphone("gate"));

        // Repeated consonants: s+, t+, p+, k+, f+, m+, n+
        assertEquals(this.caverphone.caverphone("hiss"), this.caverphone.caverphone("his"));
        assertEquals(this.caverphone.caverphone("matter"), this.caverphone.caverphone("mater"));
        assertEquals(this.caverphone.caverphone("happy"), this.caverphone.caverphone("hapy"));
        assertEquals(this.caverphone.caverphone("coffee"), this.caverphone.caverphone("cofe"));
        assertEquals(this.caverphone.caverphone("hammer"), this.caverphone.caverphone("hamer"));
        assertEquals(this.caverphone.caverphone("manner"), this.caverphone.caverphone("maner"));

        // w3 -> W3, wh3 -> Wh3, w -> 2, h -> 2
        assertEquals("WA11111111", this.caverphone.caverphone("way"));
        assertEquals("WA11111111", this.caverphone.caverphone("why"));
        assertEquals("A111111111", this.caverphone.caverphone("how"));

        // r3 -> R3, r -> 2, l3 -> L3, l -> 2
        assertEquals("RA11111111", this.caverphone.caverphone("ray"));
        assertEquals("A111111111", this.caverphone.caverphone("car"));
        assertEquals("LA11111111", this.caverphone.caverphone("lay"));
        assertEquals("A111111111", this.caverphone.caverphone("call"));
    }

    @Test
    public void testLengthTruncationAndPadding() {
        // Output must always be exactly 10 characters long
        String shortWord = this.caverphone.caverphone("a");
        assertEquals(10, shortWord.length());

        String longWord = this.caverphone.caverphone("supercalifragilisticexpialidocious");
        assertEquals(10, longWord.length());

        String tenCharExpected = this.caverphone.caverphone("Alexanderson");
        assertEquals(10, tenCharExpected.length());
    }

    @Test
    public void testKnownPairs() {
        // Common Caverphone phonetic equivalence checks
        assertEquals(this.caverphone.caverphone("Smith"), this.caverphone.caverphone("Smyth"));
        assertEquals(this.caverphone.caverphone("Thompson"), this.caverphone.caverphone("Thomson"));
        assertEquals(this.caverphone.caverphone("White"), this.caverphone.caverphone("Whyte"));
        assertEquals(this.caverphone.caverphone("Clarke"), this.caverphone.caverphone("Clark"));
    }
}