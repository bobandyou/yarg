package smoketest;

import com.haulmont.yarg.formatters.ReportFormatter;
import com.haulmont.yarg.formatters.factory.DefaultFormatterFactory;
import com.haulmont.yarg.formatters.factory.FormatterFactoryInput;
import com.haulmont.yarg.structure.BandData;
import com.haulmont.yarg.structure.BandOrientation;
import com.haulmont.yarg.structure.ReportOutputType;
import com.haulmont.yarg.structure.impl.ReportFieldFormatImpl;
import com.haulmont.yarg.structure.impl.ReportTemplateImpl;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.FileOutputStream;
import java.util.HashMap;

/**
 * @author degtyarjov
 * @version $Id$
 */
public class DocxSpecificTest extends AbstractFormatSpecificTest {
    @Test
    public void testDocxTableWithSplittedBandAlias() throws Exception {
        BandData root = new BandData("Root", null, BandOrientation.HORIZONTAL);
        HashMap<String, Object> rootData = new HashMap<String, Object>();
        root.setData(rootData);
        BandData ride = new BandData("ride", root, BandOrientation.HORIZONTAL);
        ride.setData(new RandomMap());
        root.addChild(ride);


        FileOutputStream outputStream = new FileOutputStream("./result/smoke/splitted-aliases-in-table.docx");
        ReportFormatter formatter = new DefaultFormatterFactory().createFormatter(new FormatterFactoryInput("docx", root,
                new ReportTemplateImpl("", "./modules/core/test/smoketest/splitted-aliases-in-table.docx", "./modules/core/test/smoketest/splitted-aliases-in-table.docx",
                        ReportOutputType.docx), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);
    }

    @Test
    public void testEmbeddedTables() throws Exception {
        BandData root = new BandData("Root", null, BandOrientation.HORIZONTAL);
        HashMap<String, Object> rootData = new HashMap<String, Object>();
        root.setData(rootData);

        BandData band1 = new BandData("Band1", root, BandOrientation.HORIZONTAL);
        band1.setData(new RandomMap());
        root.addChild(band1);

        FileOutputStream outputStream = new FileOutputStream("./result/smoke/embedded-table-hide.docx");
        ReportFormatter formatter = new DefaultFormatterFactory().createFormatter(new FormatterFactoryInput("docx", root,
                new ReportTemplateImpl("", "./modules/core/test/smoketest/embedded-table.docx", "./modules/core/test/smoketest/embedded-table.docx",
                        ReportOutputType.docx), outputStream));
        formatter.renderDocument();
        IOUtils.closeQuietly(outputStream);

        BandData control = new BandData("Control1", root, BandOrientation.HORIZONTAL);
        control.setData(new RandomMap());
        root.addChild(control);

        outputStream = new FileOutputStream("./result/smoke/embedded-table-show.docx");
        formatter = new DefaultFormatterFactory().createFormatter(new FormatterFactoryInput("docx", root,
                new ReportTemplateImpl("", "./modules/core/test/smoketest/embedded-table.docx", "./modules/core/test/smoketest/embedded-table.docx",
                        ReportOutputType.docx), outputStream));
        formatter.renderDocument();
        IOUtils.closeQuietly(outputStream);

    }

    @Test
    public void testUrl() throws Exception {
        BandData root = new BandData("Root", null, BandOrientation.HORIZONTAL);
        HashMap<String, Object> rootData = new HashMap<String, Object>();
        rootData.put("url", "https://www.google.ru/#newwindow=1&q=YARG");
        rootData.put("urlCaption", "URL");
        root.setData(rootData);

        FileOutputStream outputStream = new FileOutputStream("./result/smoke/url.docx");
        ReportFormatter formatter = new DefaultFormatterFactory().createFormatter(new FormatterFactoryInput("docx", root,
                new ReportTemplateImpl("", "./modules/core/test/smoketest/url.docx", "./modules/core/test/smoketest/url.docx",
                        ReportOutputType.docx), outputStream));
        formatter.renderDocument();
        IOUtils.closeQuietly(outputStream);
    }

    @Test
    public void testDocxTableWithAliasInHeader() throws Exception {
        BandData root = new BandData("Root", null, BandOrientation.HORIZONTAL);
        HashMap<String, Object> rootData = new HashMap<String, Object>();
        root.setData(rootData);
        BandData price = new BandData("Price", root, BandOrientation.HORIZONTAL);
        price.setData(new RandomMap());
        root.addChild(price);
        BandData price2 = new BandData("Price", root, BandOrientation.HORIZONTAL);
        price2.setData(new RandomMap());
        root.addChild(price2);
        BandData price3 = new BandData("Price", root, BandOrientation.HORIZONTAL);
        price3.setData(new RandomMap());
        root.addChild(price3);
        BandData price4 = new BandData("Price", root, BandOrientation.HORIZONTAL);
        price4.setData(new RandomMap());
        root.addChild(price4);
        BandData info = new BandData("Info", root, BandOrientation.HORIZONTAL);
        info.setData(new RandomMap());
        root.addChild(info);


        FileOutputStream outputStream = new FileOutputStream("./result/smoke/TemplateRateBook.docx");
        ReportFormatter formatter = new DefaultFormatterFactory().createFormatter(new FormatterFactoryInput("docx", root,
                new ReportTemplateImpl("", "./modules/core/test/smoketest/TemplateRateBook.docx", "./modules/core/test/smoketest/TemplateRateBook.docx", ReportOutputType.docx), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);
    }


    @Test
    public void testDocxWithSplittedAlias() throws Exception {
        BandData root = new BandData("Root", null, BandOrientation.HORIZONTAL);
        HashMap<String, Object> rootData = new HashMap<String, Object>();
        rootData.put("param1", "AAAAAA");
        root.setData(rootData);
        BandData cover = new BandData("Cover", root, BandOrientation.HORIZONTAL);
        cover.setData(new HashMap<String, Object>());
        cover.addData("index", "123");
        cover.addData("volume", "321");
        cover.addData("name", "AAA");
        BandData documents = new BandData("Documents", root, BandOrientation.HORIZONTAL);
        documents.setData(new HashMap<String, Object>());
        root.addChild(cover);
        root.addChild(documents);

        FileOutputStream outputStream = new FileOutputStream("./result/smoke/splitted-aliases.docx");
        ReportFormatter formatter = new DefaultFormatterFactory().createFormatter(new FormatterFactoryInput("docx", root,
                new ReportTemplateImpl("", "./modules/core/test/smoketest/splitted-aliases.docx", "./modules/core/test/smoketest/splitted-aliases.docx",
                        ReportOutputType.docx), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);
    }

    @Test
    public void testDocxWithColontitulesAndHtmlPageBreak() throws Exception {
        BandData root = new BandData("Root", null, BandOrientation.HORIZONTAL);
        HashMap<String, Object> rootData = new HashMap<String, Object>();
        rootData.put("param1", "AAAAAA");
        root.setData(rootData);
        BandData letterTable = new BandData("letterTable", root, BandOrientation.HORIZONTAL);
        BandData creatorInfo = new BandData("creatorInfo", root, BandOrientation.HORIZONTAL);
        HashMap<String, Object> letterTableData = new HashMap<String, Object>();
        String html = "<html><body>";
        html += "<table border=\"2px\">";
        for (int i = 0; i < 5; i++) {
            html += "<tr><td>123456712345671234567123456712345671234567123456712345" +
                    "67123456712345671234567123456712345671234567123456712345671234" +
                    "5671234567123456712345671234567123456712345671234567</td></tr>";
        }
        html += "</table>";
        html += "<br style=\"page-break-after: always\">";
        html += "<p>Second table</p>";
        html += "<table border=\"2px\">";
        for (int i = 0; i < 5; i++) {
            html += "<tr><td>1234567</td></tr>";
        }
        html += "</table>";


        html += "</body></html>";
        letterTableData.put("html", html);
        letterTable.setData(letterTableData);
        HashMap<String, Object> creatorInfoData = new HashMap<String, Object>();
        creatorInfoData.put("name", "12345");
        creatorInfoData.put("phone", "54321");
        creatorInfo.setData(creatorInfoData);
        root.addChild(letterTable);
        root.addChild(creatorInfo);
        root.getReportFieldFormats().put("letterTable.html", new ReportFieldFormatImpl("letterTable.html", "${html}"));

        FileOutputStream outputStream = new FileOutputStream("./result/smoke/colontitules.docx");
        DefaultFormatterFactory defaultFormatterFactory = new DefaultFormatterFactory();
        ReportFormatter formatter = defaultFormatterFactory.createFormatter(new FormatterFactoryInput("docx", root,
                new ReportTemplateImpl("", "./modules/core/test/smoketest/colontitules.docx", "./modules/core/test/smoketest/colontitules.docx",
                        ReportOutputType.docx), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);
    }
}
