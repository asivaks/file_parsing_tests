import com.codeborne.pdftest.PDF;
import com.codeborne.xlstest.XLS;
import com.codeborne.selenide.Configuration;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.opencsv.CSVReader;
import org.apache.commons.io.FileUtils;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.io.Zip;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;;

public class FileParsingTest {
    ClassLoader classLoader = getClass().getClassLoader();

    /*
    //This method guards against writing files to the file system outside of the target folder.
    //was used in unzipToTemp, but better to do it with Selenium
    public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }


// better to do it with Selenium
    @BeforeAll
    static void unzipToTemp() throws Exception {
        // ClassLoader classLoader = getClass().getClassLoader();
        // classloader method is not static and could not be called from another static method eg BeforeAll
        // should be used
        ClassLoader classLoader = FileParsingTest.class.getClassLoader();

        String fileZip = "files/zip_example.zip";
        File destDir = new File("files/temp");
        File myTempDir = Files.createTempDirectory();
        byte[] buffer = new byte[1024];
        try (
                InputStream inputStream = classLoader.getResourceAsStream(fileZip);
                ZipInputStream zipInputStream = new ZipInputStream(inputStream);
        ) {
            ZipEntry entry;
                    //get next entry and check that it is not null
            while ((entry = zipInputStream.getNextEntry()) != null) {
                //File newFile = newFile(destDir, entry);
                File newFile = new File(destDir, entry.getName());
                FileOutputStream fileOutputStream = new FileOutputStream(newFile);
                int len;
                while ((len = zipInputStream.read(buffer)) > 0) {
                    fileOutputStream.write(buffer, 0, len);
                }
                fileOutputStream.close();
            }
        }
    }

 */

    //unzip all files from files/zip_example.zip to files/temp folder
    //https://www.youtube.com/watch?v=zAspD6KO3DU
    @BeforeAll
    static void unzipToTempSelenium() throws FileNotFoundException, IOException {
        // ClassLoader classLoader = getClass().getClassLoader();
        // classloader method is not static and could not be called from another static method eg BeforeAll
        // should be used
        ClassLoader classLoader = FileParsingTest.class.getClassLoader();
        String fileZip = "files/zip_example.zip";
        // need to use stream
        //Zip.unzip(new FileInputStream("files/zip_example.zip"), new File("./temp"));
        InputStream inputStream = classLoader.getResourceAsStream(fileZip);
        Zip.unzip(inputStream, new File("./src/test/resources/files/temp"));
    }

    //delete files/temp folder after all tests
    @AfterAll
    static void deleteTemp() throws IOException {
        String tempDirPath = "./src/test/resources/files/temp";
        File tempDir = new File(tempDirPath);
        FileUtils.deleteDirectory(tempDir);
    }



    @Test
    void parsePdfTest() throws Exception {
        Configuration.holdBrowserOpen = true;
        open("https://junit.org/junit5/docs/current/user-guide/");
        File downloadedPdf = $(byText("PDF download")).download();
        PDF pdf = new PDF(downloadedPdf);
        assertThat(pdf.author).contains("Marc Philipp");
        assertThat(pdf.numberOfPages).isEqualTo(166);
    }

    @Test
    void parseXlsTest() throws Exception {
        Configuration.holdBrowserOpen = true;
        open("http://romashka2008.ru/");
        //File downloadedPdf = $(byText("Скачать Прайс-лист Excel")).download();
        File downloadedXls = $$("a[href*='prajs']").find(visible).download();
        XLS xls = new XLS(downloadedXls);
        String valueOf11B = xls.excel
                .getSheetAt(0)
                .getRow(11)
                .getCell(1 )
                .getStringCellValue();
        assertThat(valueOf11B.contains("Сахалинская обл, Южно-Сахалинск"));
        System.out.println();
        //assertThat()
    }

    @Test
    void parseCsvTest() throws Exception {
        //Configuration.holdBrowserOpen = true;
        ClassLoader classLoader = getClass().getClassLoader();
        try (
                InputStream inputStream = classLoader.getResourceAsStream("files/csv_example.csv");
                CSVReader reader = new CSVReader(new InputStreamReader(inputStream))
        ) {
            List<String[]> content = reader.readAll();
            var csv0line = content.get(0);
            var csv0element = csv0line[0];
            // there is ZWNBSP at the beginning of the file, who knows why Azure Data Explorer CSV export puts it there
            csv0element = csv0element.replaceAll("\\p{C}", ""); //remove non printable characters
            var csv0elementShouldBe = "Timestamp";
            assertEquals(csv0elementShouldBe,csv0element);
            for (int i = 0; i < csv0line.length; i++) {
                csv0line[i] = csv0line[i].replaceAll("\\p{C}", "");
            }
            assertThat(csv0line).contains("Timestamp","Source","EventName","CountryId","Platform","AppVersion","DeviceType","OsVersion");
        }
    }

    @Test
    void parseZipTest() throws Exception {
        //needed in few tests, moved into class field
        //ClassLoader classLoader = getClass().getClassLoader();
        List<String> fileNames = Lists.newArrayList
                ("csv_example.csv",
                            "example.txt",
                            "json_example.json",
                            "junit-user-guide-5.8.2.pdf")
                ;

        try (
                InputStream inputStream = classLoader.getResourceAsStream("files/zip_example.zip");
                ZipInputStream zipInputStream = new ZipInputStream(inputStream)
        ) {
            List<String> fileNamesFound = new ArrayList<String>();
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                fileNamesFound.add(entry.getName());
            }

            /*
            System.out.println("\nfileNamesFound");
            for (String temp : fileNames) {
                System.out.println(temp);
            }
            */

            assertThat(fileNamesFound).containsExactlyInAnyOrderElementsOf(fileNames);

        }
    }

    @Test
    void parseJsonTest() throws Exception {
        Gson gson = new Gson();
        String[] booleanValues = {"true", "false"};
        String[] platformValues = {"iOS", "Android"};
        try (
                InputStream inputStream = classLoader.getResourceAsStream("files/temp/json_example.json");
        ) {
            String json = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            JsonObject jsonObject = gson.fromJson(json, JsonObject.class);

            //assertThat(jsonObject.get("isSuspect").getAsString()).isIn(booleanValues);
            //var isSusp = jsonObject.get("isSuspect").getAsBoolean();
            assertThat(jsonObject.get("isSuspect").getAsBoolean()).isNotNull();

            /*
            // will not work because null could not be getAsString
            String latRaw = jsonObject.get("coordinates").getAsJsonObject().get("lat").getAsString();
            String lonRaw = jsonObject.get("coordinates").getAsJsonObject().get("lon").getAsString();
            if ((latRaw == "null") && (lonRaw == "null")) {fail("lat&lon are null");}
            else if (latRaw == "null") {fail("lat is null");}
            else if (lonRaw == "null") {fail("lon is null");}
             */

            //assert lat&lon not null
            assertFalse(jsonObject.get("coordinates").getAsJsonObject().get("lat").isJsonNull(), "lat is null");
            assertFalse(jsonObject.get("coordinates").getAsJsonObject().get("lon").isJsonNull(), "lon is null");

            double lat = jsonObject.get("coordinates").getAsJsonObject().get("lat").getAsFloat();
            //System.out.println(lat);
            assertThat(lat).isNotZero();
            double lon = jsonObject.get("coordinates").getAsJsonObject().get("lon").getAsFloat();
            //System.out.println(lon);
            assertThat(lon).isNotZero();

            String platfrom = jsonObject.get("platform").getAsString();
            assertThat(jsonObject.get("platform").getAsString()).isIn(platformValues);
        }
    }
}
