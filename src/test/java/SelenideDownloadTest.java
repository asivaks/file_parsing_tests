import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;
import static org.assertj.core.api.Assertions.assertThat;



public class SelenideDownloadTest {

    static {
        //Configuration.browser = "chrome";
        //Configuration.browserVersion = "99.0";
        Configuration.browser = "firefox";
        Configuration.holdBrowserOpen = true;
    }
    @Test
    void downloadTest() throws Exception {
        open("https://github.com/selenide/selenide/blob/master/README.md");
        File downloadedFile = $("#raw-url").download(); //should contain href link
              //autocloseable interface
        try (InputStream is = new FileInputStream(downloadedFile)) {
            assertThat(new String(is.readAllBytes(), StandardCharsets.UTF_8)).contains("What is Selenide?");
        }
        }

    @Test
    void uploadTest() {
        open("https://the-internet.herokuapp.com/upload");
                                      // .uploadFile(new File("/Users/kosh/IdeaProjects/qaguru_files/src/test/resources/files/example.txt")); //bad practice
        $("input[type=file]").uploadFromClasspath("files/example.txt");
        $("#file-submit").click();
        $("div.example").shouldHave(text("File Uploaded!"));
        $("div#uploaded-files").shouldHave(text("example.txt "));

    }

/*
        InputStream is = new FileInputStream(downloadedFile);
        try {
            byte[] fileContent = is.readAllBytes();
            String strContent = new String(fileContent, StandardCharsets.UTF_8);
            assertThat(strContent).contains("JUnit 5");
        } finally {
            is.close();
        }
        String str = "";
        str.apply();
    }

        @Test
        void pdfParsingTest() {
        InputStream stream = getClass().getClassLoader().getResourceAsStream("pdf/junit-user-guide-5.8.2.pdf");
            PDF pdf = null;
            try {
                pdf = new PDF(stream);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Assertions.assertEquals(167, pdf.numberOfPages);
            assertThat(pdf, ContainsExactText(""))

*/



}
