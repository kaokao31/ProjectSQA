package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

public class TokeniserStateTest {

    @Test
    public void testTokeniserStateReadTransitions() {
        // Test various states and their transition characters to ensure all switch cases and read() methods are covered.
        CharacterReader r;
        Tokeniser t;

        // 1. Data state
        r = new CharacterReader("</script>");
        t = new Tokeniser(r);
        TokeniserState.Data.read(t, r);
        assertEquals(TokeniserState.TagOpen, t.getState());

        r = new CharacterReader("&#97;");
        t = new Tokeniser(r);
        TokeniserState.Data.read(t, r);
        // Entity or character reference transition check

        r = new CharacterReader("\u0000");
        t = new Tokeniser(r);
        TokeniserState.Data.read(t, r);

        r = new CharacterReader("a");
        t = new Tokeniser(r);
        TokeniserState.Data.read(t, r);

        // 2. CharacterReferenceInData
        r = new CharacterReader("abc");
        t = new Tokeniser(r);
        TokeniserState.CharacterReferenceInData.read(t, r);

        // 3. TagOpen
        r = new CharacterReader("div>"), t = new Tokeniser(r);
        TokeniserState.TagOpen.read(t, r);

        r = new CharacterReader("/div>"), t = new Tokeniser(r);
        TokeniserState.TagOpen.read(t, r);

        r = new CharacterReader("!div>"), t = new Tokeniser(r);
        TokeniserState.TagOpen.read(t, r);

        r = new CharacterReader("?div>"), t = new Tokeniser(r);
        TokeniserState.TagOpen.read(t, r);

        r = new CharacterReader(" A>"), t = new Tokeniser(r);
        TokeniserState.TagOpen.read(t, r);

        r = new CharacterReader(">");
        t = new Tokeniser(r);
        TokeniserState.TagOpen.read(t, r);

        r = new CharacterReader("\u0000");
        t = new Tokeniser(r);
        TokeniserState.TagOpen.read(t, r);

        r = new CharacterReader("x");
        t = new Tokeniser(r);
        TokeniserState.TagOpen.read(t, r);

        // 4. EndTagOpen
        r = new CharacterReader("div>"), t = new Tokeniser(r);
        TokeniserState.EndTagOpen.read(t, r);

        r = new CharacterReader(">");
        t = new Tokeniser(r);
        TokeniserState.EndTagOpen.read(t, r);

        r = new CharacterReader("\u0000");
        t = new Tokeniser(r);
        TokeniserState.EndTagOpen.read(t, r);

        r = new CharacterReader("x");
        t = new Tokeniser(r);
        TokeniserState.EndTagOpen.read(t, r);

        // 5. TagName
        r = new CharacterReader(" \n\r\f/>"), t = new Tokeniser(r);
        TokeniserState.TagName.read(t, r); // Whitespace -> BeforeAttributeName
        
        r = new CharacterReader("/");
        t = new Tokeniser(r);
        TokeniserState.TagName.read(t, r); // SelfClosingStartTag

        r = new CharacterReader(">");
        t = new Tokeniser(r);
        TokeniserState.TagName.read(t, r); // Data

        r = new CharacterReader("\u0000");
        t = new Tokeniser(r);
        TokeniserState.TagName.read(t, r);

        r = new CharacterReader("\uffff");
        t = new Tokeniser(r);
        TokeniserState.TagName.read(t, r);

        r = new CharacterReader("a");
        t = new Tokeniser(r);
        TokeniserState.TagName.read(t, r);

        // 6. BeforeAttributeName
        r = new CharacterReader(" \n\r\f/>"), t = new Tokeniser(r);
        TokeniserState.BeforeAttributeName.read(t, r);
        
        r = new CharacterReader("/");
        t = new Tokeniser(r);
        TokeniserState.BeforeAttributeName.read(t, r);

        r = new CharacterReader("=");
        t = new Tokeniser(r);
        TokeniserState.BeforeAttributeName.read(t, r);

        r = new CharacterReader(">");
        t = new Tokeniser(r);
        TokeniserState.BeforeAttributeName.read(t, r);

        r = new CharacterReader("\u0000");
        t = new Tokeniser(r);
        TokeniserState.BeforeAttributeName.read(t, r);

        r = new CharacterReader("A");
        t = new Tokeniser(r);
        TokeniserState.BeforeAttributeName.read(t, r);

        r = new CharacterReader("a");
        t = new Tokeniser(r);
        TokeniserState.BeforeAttributeName.read(t, r);

        // 7. AttributeName
        r = new CharacterReader(" \n\r\f/>="), t = new Tokeniser(r);
        TokeniserState.AttributeName.read(t, r);

        r = new CharacterReader("/");
        t = new Tokeniser(r);
        TokeniserState.AttributeName.read(t, r);

        r = new CharacterReader("=");
        t = new Tokeniser(r);
        TokeniserState.AttributeName.read(t, r);

        r = new CharacterReader(">");
        t = new Tokeniser(r);
        TokeniserState.AttributeName.read(t, r);

        r = new CharacterReader("\u0000");
        t = new Tokeniser(r);
        TokeniserState.AttributeName.read(t, r);

        r = new CharacterReader("A");
        t = new Tokeniser(r);
        TokeniserState.AttributeName.read(t, r);

        r = new CharacterReader("\uffff");
        t = new Tokeniser(r);
        TokeniserState.AttributeName.read(t, r);

        // 8. AfterAttributeName
        r = new CharacterReader(" \n\r\f/>="), t = new Tokeniser(r);
        TokeniserState.AfterAttributeName.read(t, r);

        r = new CharacterReader("/");
        t = new Tokeniser(r);
        TokeniserState.AfterAttributeName.read(t, r);

        r = new CharacterReader("=");
        t = new Tokeniser(r);
        TokeniserState.AfterAttributeName.read(t, r);

        r = new CharacterReader(">");
        t = new Tokeniser(r);
        TokeniserState.AfterAttributeName.read(t, r);

        r = new CharacterReader("\u0000");
        t = new Tokeniser(r);
        TokeniserState.AfterAttributeName.read(t, r);

        r = new CharacterReader("A");
        t = new Tokeniser(r);
        TokeniserState.AfterAttributeName.read(t, r);

        r = new CharacterReader("a");
        t = new Tokeniser(r);
        TokeniserState.AfterAttributeName.read(t, r);

        // 9. BeforeAttributeValue
        r = new CharacterReader(" \n\r\f/>="), t = new Tokeniser(r);
        TokeniserState.BeforeAttributeValue.read(t, r);

        r = new CharacterReader("\"");
        t = new Tokeniser(r);
        TokeniserState.BeforeAttributeValue.read(t, r);

        r = new CharacterReader("'");
        t = new Tokeniser(r);
        TokeniserState.BeforeAttributeValue.read(t, r);

        r = new CharacterReader(">");
        t = new Tokeniser(r);
        TokeniserState.BeforeAttributeValue.read(t, r);

        r = new CharacterReader("\u0000");
        t = new Tokeniser(r);
        TokeniserState.BeforeAttributeValue.read(t, r);

        r = new CharacterReader("a");
        t = new Tokeniser(r);
        TokeniserState.BeforeAttributeValue.read(t, r);

        // 10. AttributeValue_DoubleQuoted
        r = new CharacterReader("\""), t = new Tokeniser(r);
        TokeniserState.AttributeValue_DoubleQuoted.read(t, r);

        r = new CharacterReader("&"), t = new Tokeniser(r);
        TokeniserState.AttributeValue_DoubleQuoted.read(t, r);

        r = new CharacterReader("\u0000"), t = new Tokeniser(r);
        TokeniserState.AttributeValue_DoubleQuoted.read(t, r);

        r = new CharacterReader("a"), t = new Tokeniser(r);
        TokeniserState.AttributeValue_DoubleQuoted.read(t, r);

        // 11. AttributeValue_SingleQuoted
        r = new CharacterReader("'"), t = new Tokeniser(r);
        TokeniserState.AttributeValue_SingleQuoted.read(t, r);

        r = new CharacterReader("&"), t = new Tokeniser(r);
        TokeniserState.AttributeValue_SingleQuoted.read(t, r);

        r = new CharacterReader("\u0000"), t = new Tokeniser(r);
        TokeniserState.AttributeValue_SingleQuoted.read(t, r);

        r = new CharacterReader("a"), t = new Tokeniser(r);
        TokeniserState.AttributeValue_SingleQuoted.read(t, r);

        // 12. AttributeValue_Unquoted
        r = new CharacterReader(" \n\r\f/>"), t = new Tokeniser(r);
        TokeniserState.AttributeValue_Unquoted.read(t, r);

        r = new CharacterReader("&"), t = new Tokeniser(r);
        TokeniserState.AttributeValue_Unquoted.read(t, r);

        r = new CharacterReader(">"), t = new Tokeniser(r);
        TokeniserState.AttributeValue_Unquoted.read(t, r);

        r = new CharacterReader("\u0000"), t = new Tokeniser(r);
        TokeniserState.AttributeValue_Unquoted.read(t, r);

        r = new CharacterReader("a"), t = new Tokeniser(r);
        TokeniserState.AttributeValue_Unquoted.read(t, r);

        // 13. AfterAttributeValue_Quoted
        r = new CharacterReader(" \n\r\f/>"), t = new Tokeniser(r);
        TokeniserState.AfterAttributeValue_Quoted.read(t, r);

        r = new CharacterReader("/"), t = new Tokeniser(r);
        TokeniserState.AfterAttributeValue_Quoted.read(t, r);

        r = new CharacterReader(">"), t = new Tokeniser(r);
        TokeniserState.AfterAttributeValue_Quoted.read(t, r);

        r = new CharacterReader("\u0000"), t = new Tokeniser(r);
        TokeniserState.AfterAttributeValue_Quoted.read(t, r);

        r = new CharacterReader("a"), t = new Tokeniser(r);
        TokeniserState.AfterAttributeValue_Quoted.read(t, r);

        // 14. SelfClosingStartTag
        r = new CharacterReader(">"), t = new Tokeniser(r);
        TokeniserState.SelfClosingStartTag.read(t, r);

        r = new CharacterReader("\u0000"), t = new Tokeniser(r);
        TokeniserState.SelfClosingStartTag.read(t, r);

        r = new CharacterReader("a"), t = new Tokeniser(r);
        TokeniserState.SelfClosingStartTag.read(t, r);

        // 15. BogusComment
        r = new CharacterReader(">"), t = new Tokeniser(r);
        TokeniserState.BogusComment.read(t, r);

        r = new CharacterReader("\u0000"), t = new Tokeniser(r);
        TokeniserState.BogusComment.read(t, r);

        r = new CharacterReader("a"), t = new Tokeniser(r);
        TokeniserState.BogusComment.read(t, r);

        // 16. MarkupDeclarationOpen
        r = new CharacterReader("--"), t = new Tokeniser(r);
        TokeniserState.MarkupDeclarationOpen.read(t, r);

        r = new CharacterReader("DOCTYPE"), t = new Tokeniser(r);
        // Note: MarkupDeclarationOpen checks case insensitively or specific strings
        r = new CharacterReader("[CDATA[");
        t = new Tokeniser(r);
        TokeniserState.MarkupDeclarationOpen.read(t, r);

        r = new CharacterReader("abc");
        t = new Tokeniser(r);
        TokeniserState.MarkupDeclarationOpen.read(t, r);

        // 17. CommentStart
        r = new CharacterReader("-"), t = new Tokeniser(r);
        TokeniserState.CommentStart.read(t, r);

        r = new CharacterReader(">"), t = new Tokeniser(r);
        TokeniserState.CommentStart.read(t, r);

        r = new CharacterReader("\u0000"), t = new Tokeniser(r);
        TokeniserState.CommentStart.read(t, r);

        r = new CharacterReader("a"), t = new Tokeniser(r);
        TokeniserState.CommentStart.read(t, r);

        // 18. CommentStartDash
        r = new CharacterReader("-"), t = new Tokeniser(r);
        TokeniserState.CommentStartDash.read(t, r);

        r = new CharacterReader(">"), t = new Tokeniser(r);
        TokeniserState.CommentStartDash.read(t, r);

        r = new CharacterReader("\u0000"), t = new Tokeniser(r);
        TokeniserState.CommentStartDash.read(t, r);

        r = new CharacterReader("a"), t = new Tokeniser(r);
        TokeniserState.CommentStartDash.read(t, r);

        // 19. Comment
        r = new CharacterReader("-"), t = new Tokeniser(r);
        TokeniserState.Comment.read(t, r);

        r = new CharacterReader("\u0000"), t = new Tokeniser(r);
        TokeniserState.Comment.read(t, r);

        r = new CharacterReader("a"), t = new Tokeniser(r);
        TokeniserState.Comment.read(t, r);

        // 20. CommentEndDash
        r = new CharacterReader("-"), t = new Tokeniser(r);
        TokeniserState.CommentEndDash.read(t, r);

        r = new CharacterReader("\u0000"), t = new Tokeniser(r);
        TokeniserState.CommentEndDash.read(t, r);

        r = new CharacterReader("a"), t = new Tokeniser(r);
        TokeniserState.CommentEndDash.read(t, r);

        // 21. CommentEnd
        r = new CharacterReader(">"), t = new Tokeniser(r);
        TokeniserState.CommentEnd.read(t, r);

        r = new CharacterReader("-"), t = new Tokeniser(r);
        TokeniserState.CommentEnd.read(t, r);

        r = new CharacterReader("\u0000"), t = new Tokeniser(r);
        TokeniserState.CommentEnd.read(t, r);

        r = new CharacterReader("a"), t = new Tokeniser(r);
        TokeniserState.CommentEnd.read(t, r);

        // 22. CommentEndBang
        r = new CharacterReader(">"), t = new Tokeniser(r);
        TokeniserState.CommentEndBang.read(t, r);

        r = new CharacterReader("-"), t = new Tokeniser(r);
        TokeniserState.CommentEndBang.read(t, r);

        r = new CharacterReader("\u0000"), t = new Tokeniser(r);
        TokeniserState.CommentEndBang.read(t, r);

        r = new CharacterReader("a"), t = new Tokeniser(r);
        TokeniserState.CommentEndBang.read(t, r);

        // 23. Doctype
        r = new CharacterReader(" \n\r\f>"), t = new Tokeniser(r);
        TokeniserState.Doctype.read(t, r);

        r = new CharacterReader("\u0000"), t = new Tokeniser(r);
        TokeniserState.Doctype.read(t, r);

        r = new CharacterReader("a"), t = new Tokeniser(r);
        TokeniserState.Doctype.read(t, r);

        // 24. BeforeDoctypeName
        r = new CharacterReader(" \n\r\f>"), t = new Tokeniser(r);
        TokeniserState.BeforeDoctypeName.read(t, r);

        r = new CharacterReader("\u0000"), t = new Tokeniser(r);
        TokeniserState.BeforeDoctypeName.read(t, r);

        r = new CharacterReader("A"), t = new Tokeniser(r);
        TokeniserState.BeforeDoctypeName.read(t, r);

        r = new CharacterReader("a"), t = new Tokeniser(r);
        TokeniserState.BeforeDoctypeName.read(t, r);

        // 25. DoctypeName
        r = new CharacterReader(" \n\r\f>"), t = new Tokeniser(r);
        TokeniserState.DoctypeName.read(t, r);

        r = new CharacterReader("\u0000"), t = new Tokeniser(r);
        TokeniserState.DoctypeName.read(t, r);

        r = new CharacterReader("A"), t = new Tokeniser(r);
        TokeniserState.DoctypeName.read(t, r);

        r = new CharacterReader("a"), t = new Tokeniser(r);
        TokeniserState.DoctypeName.read(t, r);

        // 26. AfterDoctypeName
        r = new CharacterReader(" \n\r\f>"), t = new Tokeniser(r);
        TokeniserState.AfterDoctypeName.read(t, r);

        r = new CharacterReader("P"), t = new Tokeniser(r); // PUBLIC
        TokeniserState.AfterDoctypeName.read(t, r);

        r = new CharacterReader("S"), t = new Tokeniser(r); // SYSTEM
        TokeniserState.AfterDoctypeName.read(t, r);

        r = new CharacterReader("\u0000"), t = new Tokeniser(r);
        TokeniserState.AfterDoctypeName.read(t, r);

        r = new CharacterReader("a"), t = new Tokeniser(r);
        TokeniserState.AfterDoctypeName.read(t, r);

        // 27. AfterDoctypePublicKeyword
        r = new CharacterReader(" \n\r\f>"), t = new Tokeniser(r);
        TokeniserState.AfterDoctypePublicKeyword.read(t, r);

        r = new CharacterReader("\""), t = new Tokeniser(r);
        TokeniserState.AfterDoctypePublicKeyword.read(t, r);

        r = new CharacterReader("'"), t = new Tokeniser(r);
        TokeniserState.AfterDoctypePublicKeyword.read(t, r);

        r = new CharacterReader("\u0000"), t = new Tokeniser(r);
        TokeniserState.AfterDoctypePublicKeyword.read(t, r);

        r = new CharacterReader("a"), t = new Tokeniser(r);
        TokeniserState.AfterDoctypePublicKeyword.read(t, r);

        // 28. BeforeDoctypePublicIdentifier
        r = new CharacterReader(" \n\r\f>"), t = new Tokeniser(r);
        TokeniserState.BeforeDoctypePublicIdentifier.read(t, r);

        r = new CharacterReader("\""), t = new Tokeniser(r);
        TokeniserState.BeforeDoctypePublicIdentifier.read(t, r);

        r = new CharacterReader("'"), t = new Tokeniser(r);
        TokeniserState.BeforeDoctypePublicIdentifier.read(t, r);

        r = new CharacterReader("\u0000"), t = new Tokeniser(r);
        TokeniserState.BeforeDoctypePublicIdentifier.read(t, r);

        r = new CharacterReader("a"), t = new Tokeniser(r);
        TokeniserState.BeforeDoctypePublicIdentifier.read(t, r);

        // 29. DoctypePublicIdentifier_DoubleQuoted
        r = new CharacterReader("\""), t = new Tokeniser(r);
        TokeniserState.DoctypePublicIdentifier_DoubleQuoted.read(t, r);

        r = new CharacterReader("\u0000"), t = new Tokeniser(r);
        TokeniserState.DoctypePublicIdentifier_DoubleQuoted.read(t, r);

        r = new CharacterReader("a"), t = new Tokeniser(r);
        TokeniserState.DoctypePublicIdentifier_DoubleQuoted.read(t, r);

        // 30. DoctypePublicIdentifier_SingleQuoted
        r = new CharacterReader("'"), t = new Tokeniser(r);
        TokeniserState.DoctypePublicIdentifier_SingleQuoted.read(t, r);

        r = new CharacterReader("\u0000"), t = new Tokeniser(r);
        TokeniserState.DoctypePublicIdentifier_SingleQuoted.read(t, r);

        r = new CharacterReader("a"), t = new Tokeniser(r);
        TokeniserState.DoctypePublicIdentifier_SingleQuoted.read(t, r);

        // 31. AfterDoctypePublicIdentifier
        r = new CharacterReader(" \n\r\f>"), t = new Tokeniser(r);
        TokeniserState.AfterDoctypePublicIdentifier.read(t, r);

        r = new CharacterReader("\""), t = new Tokeniser(r);
        TokeniserState.AfterDoctypePublicIdentifier.read(t, r);

        r = new CharacterReader("'"), t = new Tokeniser(r);
        TokeniserState.AfterDoctypePublicIdentifier.read(t, r);

        r = new CharacterReader("\u0000"), t = new Tokeniser(r);
        TokeniserState.AfterDoctypePublicIdentifier.read(t, r);

        r = new CharacterReader("a"), t = new Tokeniser(r);
        TokeniserState.AfterDoctypePublicIdentifier.read(t, r);

        // 32. BetweenDoctypePublicAndSystemIdentifiers
        r = new CharacterReader(" \n\r\f>"), t = new Tokeniser(r);
        TokeniserState.BetweenDoctypePublicAndSystemIdentifiers.read(t, r);

        r = new CharacterReader("\""), t = new Tokeniser(r);
        TokeniserState.BetweenDoctypePublicAndSystemIdentifiers.read(t, r);

        r = new CharacterReader("'"), t = new Tokeniser(r);
        TokeniserState.BetweenDoctypePublicAndSystemIdentifiers.read(t, r);

        r = new CharacterReader("\u0000"), t = new Tokeniser(r);
        TokeniserState.BetweenDoctypePublicAndSystemIdentifiers.read(t, r);

        r = new CharacterReader("a"), t = new Tokeniser(r);
        TokeniserState.BetweenDoctypePublicAndSystemIdentifiers.read(t, r);

        // 33. AfterDoctypeSystemKeyword
        r = new CharacterReader(" \n\r\f>"), t = new Tokeniser(r);
        TokeniserState.AfterDoctypeSystemKeyword.read(t, r);

        r = new CharacterReader("\""), t = new Tokeniser(r);
        TokeniserState.AfterDoctypeSystemKeyword.read(t, r);

        r = new CharacterReader("'"), t = new Tokeniser(r);
        TokeniserState.AfterDoctypeSystemKeyword.read(t, r);

        r = new CharacterReader("\u0000"), t = new Tokeniser(r);
        TokeniserState.AfterDoctypeSystemKeyword.read(t, r);

        r = new CharacterReader("a"), t = new Tokeniser(r);
        TokeniserState.AfterDoctypeSystemKeyword.read(t, r);

        // 34. BeforeDoctypeSystemIdentifier
        r = new CharacterReader(" \n\r\f>"), t = new Tokeniser(r);
        TokeniserState.BeforeDoctypeSystemIdentifier.read(t, r);

        r = new CharacterReader("\""), t = new Tokeniser(r);
        TokeniserState.BeforeDoctypeSystemIdentifier.read(t, r);

        r = new CharacterReader("'"), t = new Tokeniser(r);
        TokeniserState.BeforeDoctypeSystemIdentifier.read(t, r);

        r = new CharacterReader("\u0000"), t = new Tokeniser(r);
        TokeniserState.BeforeDoctypeSystemIdentifier.read(t, r);

        r = new CharacterReader("a"), t = new Tokeniser(r);
        TokeniserState.BeforeDoctypeSystemIdentifier.read(t, r);

        // 35. DoctypeSystemIdentifier_DoubleQuoted
        r = new CharacterReader("\""), t = new Tokeniser(r);
        TokeniserState.DoctypeSystemIdentifier_DoubleQuoted.read(t, r);

        r = new CharacterReader("\u0000"), t = new Tokeniser(r);
        TokeniserState.DoctypeSystemIdentifier_DoubleQuoted.read(t, r);

        r = new CharacterReader("a"), t = new Tokeniser(r);
        TokeniserState.DoctypeSystemIdentifier_DoubleQuoted.read(t, r);

        // 36. DoctypeSystemIdentifier_SingleQuoted
        r = new CharacterReader("'"), t = new Tokeniser(r);
        TokeniserState.DoctypeSystemIdentifier_SingleQuoted.read(t, r);

        r = new CharacterReader("\u0000"), t = new Tokeniser(r);
        TokeniserState.DoctypeSystemIdentifier_SingleQuoted.read(t, r);

        r = new CharacterReader("a"), t = new Tokeniser(r);
        TokeniserState.DoctypeSystemIdentifier_SingleQuoted.read(t, r);

        // 37. AfterDoctypeSystemIdentifier
        r = new CharacterReader(" \n\r\f>"), t = new Tokeniser(r);
        TokeniserState.AfterDoctypeSystemIdentifier.read(t, r);

        r = new CharacterReader("\u0000"), t = new Tokeniser(r);
        TokeniserState.AfterDoctypeSystemIdentifier.read(t, r);

        r = new CharacterReader("a"), t = new Tokeniser(r);
        TokeniserState.AfterDoctypeSystemIdentifier.read(t, r);

        // 38. BogusDoctype
        r = new CharacterReader(">"), t = new Tokeniser(r);
        TokeniserState.BogusDoctype.read(t, r);

        r = new CharacterReader("\u0000"), t = new Tokeniser(r);
        TokeniserState.BogusDoctype.read(t, r);

        r = new CharacterReader("a"), t = new Tokeniser(r);
        TokeniserState.BogusDoctype.read(t, r);

        // 39. CdataSection
        r = new CharacterReader("]]>"), t = new Tokeniser(r);
        TokeniserState.CdataSection.read(t, r);

        r = new CharacterReader("a"), t = new Tokeniser(r);
        TokeniserState.CdataSection.read(t, r);
    }
}