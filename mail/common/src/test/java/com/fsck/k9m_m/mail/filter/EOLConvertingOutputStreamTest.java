package com.fsck.k9m_m.mail.filter;


import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;

import static org.junit.Assert.assertEquals;


public class EOLConvertingOutputStreamTest {
    private EOLConvertingOutputStream subject;
    private ByteArrayOutputStream out;

    @Before
    public void setUp() throws Exception {
        out = new ByteArrayOutputStream();
        subject = new EOLConvertingOutputStream(out);
    }

    @Test
    public void testFilterWithoutCRorLF() throws Exception {
        subject.write("Unchanged".getBytes());
        subject.flush();
        assertEquals("Unchanged", out.toString());
    }

    @Test
    public void testFilterWithCRLF() throws Exception {
        subject.write("Filter\r\nNext Line".getBytes());
        subject.flush();
        assertEquals("Filter\r\nNext Line", out.toString());
    }

    @Test
    public void testFilterWithJustCR() throws Exception {
        subject.write("\n\n\n".getBytes());
        subject.flush();
        assertEquals("\r\n\r\n\r\n", out.toString());
    }

    @Test
    public void testFilterWithCR() throws Exception {
        subject.write("Filter\rNext Line".getBytes());
        subject.flush();
        assertEquals("Filter\r\nNext Line", out.toString());
    }

    @Test
    public void testFilterWithLF() throws Exception {
        subject.write("Filter\nNext Line".getBytes());
        subject.flush();
        assertEquals("Filter\r\nNext Line", out.toString());
    }

    @Test
    public void testFlushWithCR() throws Exception {
        subject.write("Flush\r".getBytes());
        subject.flush();
        assertEquals("Flush\r\n", out.toString());
        subject.write("\n\n\n".getBytes());
        assertEquals("Flush\r\n\r\n\r\n", out.toString());
    }

    @Test
    public void testFlushWithCRNotFollowedByLF() throws Exception {
        subject.write("Flush\r".getBytes());
        subject.flush();
        subject.write("Next line".getBytes());
        assertEquals("Flush\r\nNext line", out.toString());
    }

    @Test
    public void testFlushWithLF() throws Exception {
        subject.write("Flush\n".getBytes());
        subject.flush();
        subject.write("\n".getBytes());
        assertEquals("Flush\r\n\r\n", out.toString());
    }

    @Test
    public void testFlushWithCrFollowedByLf() throws Exception {
        subject.write("Flush\r".getBytes());
        subject.flush();
        subject.write("\n".getBytes());
        assertEquals("Flush\r\n", out.toString());
    }

    @Test
    public void endWithCrLfAndFlush_withoutNewline_shouldAddNewline() throws Exception {
        subject.write("The end".getBytes());
        
        subject.endWithCrLfAndFlush();
        
        assertEquals("The end\r\n", out.toString());
    }

    @Test
    public void endWithCrLfAndFlush_endingWithNewline_shouldNotAddAdditionalNewline() throws Exception {
        subject.write("The end\r\n".getBytes());
        
        subject.endWithCrLfAndFlush();
        
        assertEquals("The end\r\n", out.toString());
    }

    @Test
    public void endWithCrLfAndFlush_endingWithCr_shouldCompleteNewline() throws Exception {
        subject.write("The end\r".getBytes());
        
        subject.endWithCrLfAndFlush();
        
        assertEquals("The end\r\n", out.toString());
    }

    @Test
    public void endWithCrLfAndFlush_endingWithLf_shouldCompleteNewline() throws Exception {
        subject.write("The end\n".getBytes());
        
        subject.endWithCrLfAndFlush();
        
        assertEquals("The end\r\n", out.toString());
    }
}
