package net.sf.jabref.importer.fileformat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.sf.jabref.Globals;
import net.sf.jabref.JabRefPreferences;
import net.sf.jabref.bibtex.BibtexEntryAssert;
import net.sf.jabref.model.entry.BibEntry;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MedlinePlainImporterTest {

    private MedlinePlainImporter importer;

    private BufferedReader readerForString(String string) {
        return new BufferedReader(new StringReader(string));
    }

    @Before
    public void setUp() {
        Globals.prefs = JabRefPreferences.getInstance();
        importer = new MedlinePlainImporter();
    }

    @Test
    public void testIsRecognizedFormat() throws IOException, URISyntaxException {
        List<String> list = Arrays.asList("CopacImporterTest1.txt", "CopacImporterTest2.txt", "IsiImporterTest1.isi",
                "IsiImporterTestInspec.isi", "IsiImporterTestWOS.isi", "IsiImporterTestMedline.isi");
        for (String str : list) {
            Path file = Paths.get(MedlinePlainImporter.class.getResource(str).toURI());
            Assert.assertFalse(importer.isRecognizedFormat(file, Charset.defaultCharset()));
        }
    }

    @Test
    public void testIsNotRecognizedFormat() throws Exception {
        List<String> list = Arrays.asList("MedlinePlainImporterTestMultipleEntries.txt",
                "MedlinePlainImporterTestCompleteEntry.txt", "MedlinePlainImporterTestMultiAbstract.txt",
                "MedlinePlainImporterTestMultiTitle.txt", "MedlinePlainImporterTestDOI.txt",
                "MedlinePlainImporterTestInproceeding.txt");
        for (String str : list) {
            Path file = Paths.get(MedlinePlainImporter.class.getResource(str).toURI());
            Assert.assertTrue(importer.isRecognizedFormat(file, Charset.defaultCharset()));
        }
    }

    @Test
    public void doesNotRecognizeEmptyFiles() throws IOException {
        Assert.assertFalse(importer.isRecognizedFormat(readerForString("")));
    }

    @Test
    public void testImportMultipleEntriesInSingleFile() throws IOException, URISyntaxException {
        Path inputFile = Paths.get(
                MedlinePlainImporter.class.getResource("MedlinePlainImporterTestMultipleEntries.txt").toURI());

        List<BibEntry> entries = importer.importDatabase(inputFile, Charset.defaultCharset()).getDatabase()
                .getEntries();
        Assert.assertEquals(7, entries.size());

        BibEntry testEntry = entries.get(0);
        Assert.assertEquals("article", testEntry.getType());
        Assert.assertNull(testEntry.getField("month"));
        Assert.assertEquals("Long, Vicky and Marland, Hilary", testEntry.getField("author"));
        Assert.assertEquals(
                "From danger and motherhood to health and beauty: health advice for the factory girl in early twentieth-century Britain.",
                testEntry.getField("title"));

        testEntry = entries.get(1);
        Assert.assertEquals("conference", testEntry.getType());
        Assert.assertEquals("06", testEntry.getField("month"));
        Assert.assertNull(testEntry.getField("author"));
        Assert.assertNull(testEntry.getField("title"));

        testEntry = entries.get(2);
        Assert.assertEquals("book", testEntry.getType());
        Assert.assertEquals(
                "This is a Testtitle: This title should be appended: This title should also be appended. Another append to the Title? LastTitle",
                testEntry.getField("title"));

        testEntry = entries.get(3);
        Assert.assertEquals("techreport", testEntry.getType());
        Assert.assertNotNull(testEntry.getField("doi"));

        testEntry = entries.get(4);
        Assert.assertEquals("inproceedings", testEntry.getType());
        Assert.assertEquals("Inproceedings book title", testEntry.getField("booktitle"));

        BibEntry expectedEntry5 = new BibEntry();
        expectedEntry5.setType("proceedings");
        expectedEntry5.setField("keywords", "Female");
        BibtexEntryAssert.assertEquals(expectedEntry5, entries.get(5));

        BibEntry expectedEntry6 = new BibEntry();
        expectedEntry6.setType("misc");
        expectedEntry6.setField("keywords", "Female");
        BibtexEntryAssert.assertEquals(expectedEntry6, entries.get(6));
    }

    @Test
    public void testEmptyFileImport() throws IOException {
        List<BibEntry> emptyEntries = importer.importDatabase(readerForString("")).getDatabase().getEntries();
        Assert.assertEquals(Collections.emptyList(), emptyEntries);
    }

    @Test
    public void testImportSingleEntriesInSingleFiles() throws IOException, URISyntaxException {
        List<String> testFiles = Arrays.asList("MedlinePlainImporterTestCompleteEntry",
                "MedlinePlainImporterTestMultiAbstract", "MedlinePlainImporterTestMultiTitle",
                "MedlinePlainImporterTestDOI", "MedlinePlainImporterTestInproceeding");
        for (String testFile : testFiles) {
            String medlineFile = testFile + ".txt";
            String bibtexFile = testFile + ".bib";
            assertImportOfMedlineFileEqualsBibtexFile(medlineFile, bibtexFile);
        }
    }

    private void assertImportOfMedlineFileEqualsBibtexFile(String medlineFile, String bibtexFile)
            throws IOException, URISyntaxException {
        Path file = Paths.get(MedlinePlainImporter.class.getResource(medlineFile).toURI());
        try (InputStream nis = MedlinePlainImporter.class.getResourceAsStream(bibtexFile)) {
            List<BibEntry> entries = importer.importDatabase(file, Charset.defaultCharset()).getDatabase().getEntries();
            Assert.assertNotNull(entries);
            Assert.assertEquals(1, entries.size());
            BibtexEntryAssert.assertEquals(nis, entries.get(0));
        }
    }

    @Test
    public void testMultiLineComments() throws IOException {
        BufferedReader reader = readerForString(
                "PMID-22664220" + "\n" + "CON - Comment1" + "\n" + "CIN - Comment2" + "\n" + "EIN - Comment3" + "\n"
                        + "EFR - Comment4" + "\n" + "CRI - Comment5" + "\n" + "CRF - Comment6" + "\n" + "PRIN- Comment7"
                        + "\n" + "PROF- Comment8" + "\n" + "RPI - Comment9" + "\n" + "RPF - Comment10" + "\n"
                        + "RIN - Comment11" + "\n" + "ROF - Comment12" + "\n" + "UIN - Comment13" + "\n"
                        + "UOF - Comment14" + "\n" + "SPIN- Comment15" + "\n" + "ORI - Comment16");
        List<BibEntry> actualEntries = importer.importDatabase(reader).getDatabase().getEntries();

        BibEntry expectedEntry = new BibEntry();
        expectedEntry.setField("comment",
                "Comment1" + "\n" + "Comment2" + "\n" + "Comment3" + "\n" + "Comment4" + "\n" + "Comment5" + "\n"
                        + "Comment6" + "\n" + "Comment7" + "\n" + "Comment8" + "\n" + "Comment9" + "\n" + "Comment10"
                        + "\n" + "Comment11" + "\n" + "Comment12" + "\n" + "Comment13" + "\n" + "Comment14" + "\n"
                        + "Comment15" + "\n" + "Comment16");
        BibtexEntryAssert.assertEquals(Arrays.asList(expectedEntry), actualEntries);
    }

    @Test
    public void testKeyWords() throws IOException {
        try (BufferedReader reader = readerForString("PMID-22664795" + "\n" + "MH  - Female" + "\n" + "OT  - Male")) {
            List<BibEntry> actualEntries = importer.importDatabase(reader).getDatabase().getEntries();

            BibEntry expectedEntry = new BibEntry();
            expectedEntry.setField("keywords", "Female, Male");
            BibtexEntryAssert.assertEquals(Arrays.asList(expectedEntry), actualEntries);
        }
    }

    @Test
    public void testAllArticleTypes() throws IOException {
        try (BufferedReader reader = readerForString("PMID-22664795" + "\n" +
                "MH  - Female\n" +
                "PT  - journal article" + "\n" +
                "PT  - classical article" + "\n" +
                "PT  - corrected and republished article" + "\n" +
                "PT  - introductory journal article" + "\n" + "PT  - newspaper article")) {
            List<BibEntry> actualEntries = importer.importDatabase(reader).getDatabase().getEntries();

            BibEntry expectedEntry = new BibEntry();
            expectedEntry.setType("article");
            expectedEntry.setField("keywords", "Female");
            BibtexEntryAssert.assertEquals(Arrays.asList(expectedEntry), actualEntries);
        }
    }

    @Test
    public void testGetFormatName() {
        Assert.assertEquals("MedlinePlain", importer.getFormatName());
    }

    @Test
    public void testGetCLIId() {
        Assert.assertEquals("medlineplain", importer.getId());
    }

}
