package net.sf.jabref.importer.fileformat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import net.sf.jabref.Globals;
import net.sf.jabref.JabRefPreferences;
import net.sf.jabref.bibtex.BibtexEntryAssert;
import net.sf.jabref.model.entry.BibEntry;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class InspecImportTest {

    private InspecImporter inspecImp;

    @Before
    public void setUp() throws Exception {
        Globals.prefs = JabRefPreferences.getInstance();
        this.inspecImp = new InspecImporter();
    }

    @Test
    public void testIsRecognizedFormatAccept() throws IOException, URISyntaxException {
        List<String> testList = Arrays.asList("InspecImportTest.txt", "InspecImportTest2.txt");
        for (String str : testList) {
            Path file = Paths.get(InspecImportTest.class.getResource(str).toURI());
            assertTrue(inspecImp.isRecognizedFormat(file, Charset.defaultCharset()));
        }
    }

    @Test
    public void testIsRecognizedFormatReject() throws IOException, URISyntaxException {
        List<String> testList = Arrays.asList("CopacImporterTest1.txt", "CopacImporterTest2.txt",
                "IEEEImport1.txt", "IsiImporterTest1.isi", "IsiImporterTestInspec.isi", "IsiImporterTestWOS.isi",
                "IsiImporterTestMedline.isi", "RisImporterTest1.ris", "InspecImportTestFalse.txt");
        for (String str : testList) {
            Path file = Paths.get(InspecImportTest.class.getResource(str).toURI());
            assertFalse(inspecImp.isRecognizedFormat(file, Charset.defaultCharset()));
        }
    }

    @Test
    public void testCompleteBibtexEntryOnJournalPaperImport() throws IOException, URISyntaxException {

        BibEntry shouldBeEntry = new BibEntry();
        shouldBeEntry.setType("article");
        shouldBeEntry.setField("title", "The SIS project : software reuse with a natural language approach");
        shouldBeEntry.setField("author", "Prechelt, Lutz");
        shouldBeEntry.setField("year", "1992");
        shouldBeEntry.setField("abstract", "Abstrakt");
        shouldBeEntry.setField("keywords", "key");
        shouldBeEntry.setField("journal", "10000");
        shouldBeEntry.setField("pages", "20");
        shouldBeEntry.setField("volume", "19");

        Path file = Paths.get(InspecImportTest.class.getResource("InspecImportTest2.txt").toURI());
        List<BibEntry> entries = inspecImp.importDatabase(file, Charset.defaultCharset()).getDatabase().getEntries();
        assertEquals(1, entries.size());
        BibEntry entry = entries.get(0);
        BibtexEntryAssert.assertEquals(shouldBeEntry, entry);
    }

    @Test
    public void importConferencePaperGivesInproceedings() throws IOException {
        String testInput = "Record.*INSPEC.*\n" +
                "\n" +
                "RT ~ Conference-Paper\n" +
                "AU ~ Prechelt, Lutz";
        BibEntry shouldBeEntry = new BibEntry();
        shouldBeEntry.setType("Inproceedings");
        shouldBeEntry.setField("author", "Prechelt, Lutz");

        try (BufferedReader reader = new BufferedReader(new StringReader(testInput))) {
            List<BibEntry> entries = inspecImp.importDatabase(reader).getDatabase().getEntries();
            assertEquals(1, entries.size());
            BibEntry entry = entries.get(0);
            BibtexEntryAssert.assertEquals(shouldBeEntry, entry);
        }
    }

    @Test
    public void importMiscGivesMisc() throws IOException {
        String testInput = "Record.*INSPEC.*\n" +
                "\n" +
                "AU ~ Prechelt, Lutz \n" +
                "RT ~ Misc";
        BibEntry shouldBeEntry = new BibEntry();
        shouldBeEntry.setType("Misc");
        shouldBeEntry.setField("author", "Prechelt, Lutz");

        try (BufferedReader reader = new BufferedReader(new StringReader(testInput))) {
            List<BibEntry> entries = inspecImp.importDatabase(reader).getDatabase().getEntries();
            assertEquals(1, entries.size());
            BibEntry entry = entries.get(0);
            BibtexEntryAssert.assertEquals(shouldBeEntry, entry);
        }
    }

    @Test
    public void testGetFormatName() {
        assertEquals("INSPEC", inspecImp.getFormatName());
    }

    @Test
    public void testGetCLIId() {
        assertEquals("inspec", inspecImp.getId());
    }

}
