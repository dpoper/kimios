package org.kimios.kernel.converter.impl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import org.apache.pdfbox.util.PDFMergerUtility;
import org.kimios.kernel.converter.ConverterImpl;
import org.kimios.kernel.converter.exception.ConverterException;
import org.kimios.kernel.converter.source.InputSource;
import org.kimios.kernel.converter.source.InputSourceFactory;
import org.kimios.kernel.converter.source.impl.DocumentVersionInputSource;
import org.kimios.kernel.dms.DocumentVersion;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Generate a barcode and merge it to given PDF
 */
public class BarcodeTransformer extends ConverterImpl {
    private static final String QRCODE_DATA_DELIMITER = "\n";

    @Override
    public InputSource convertInputSources(List<InputSource> sources) throws ConverterException {
        String qrcodeFilename = null;
        try {

            // get the pdf source

            InputSource sourcePdf = sources.get(0);
            InputStream in = sourcePdf.getInputStream();
            PdfReader givenPdf = new PdfReader(in);

            // set the target path

            String targetPath = temporaryRepository + "/" + FileNameGenerator.generate() + ".pdf";
            PDFMergerUtility merger = new PDFMergerUtility();
            merger.setDestinationFileName(targetPath);
            FileOutputStream out = new FileOutputStream(targetPath);
            PdfStamper stamper = new PdfStamper(givenPdf, out);

            // get metadatas, generate qrcode and write it to temporary repository

            String qrcodeData = buildQrcodeData((DocumentVersionInputSource) sourcePdf);
            qrcodeFilename = temporaryRepository + "/QRCode_" + FileNameGenerator.generate() + ".png";
            BitMatrix matrix = generateMatrix(qrcodeData, 100);
            writeImage(qrcodeFilename, "png", matrix);

            Image qrcodeImage = Image.getInstance(qrcodeFilename);
            qrcodeImage.setAbsolutePosition(0, 0);

            // add watermark and generate the new pdf

            PdfContentByte watermark = stamper.getOverContent(1);
            watermark.addImage(qrcodeImage);
            stamper.close();

            InputSource result = InputSourceFactory.getInputSource(targetPath);
            result.setHumanName("PDF_QRCode_" + FileNameGenerator.getTime() + ".pdf");
            return result;

        } catch (WriterException e) {
            e.printStackTrace();
            throw new ConverterException(e);

        } catch (IOException e) {
            e.printStackTrace();
            throw new ConverterException(e);

        } catch (DocumentException e) {
            e.printStackTrace();
            throw new ConverterException(e);

        } finally {
            new File(qrcodeFilename).delete();
        }

    }

    private String buildQrcodeData(DocumentVersionInputSource sourcePdf) {
        DocumentVersion version = sourcePdf.getVersion();
        StringBuilder buffer = new StringBuilder();
        buffer.append("DocumentId=").append(version.getDocumentUid()).append(QRCODE_DATA_DELIMITER);
        buffer.append("VersionId=").append(version.getUid()).append(QRCODE_DATA_DELIMITER);
        buffer.append("Length=").append(version.getLength()).append(QRCODE_DATA_DELIMITER);
        buffer.append("Author=").append(version.getAuthor()).append("@").append(version.getAuthorSource()).append(QRCODE_DATA_DELIMITER);
        buffer.append("CreateDate=").append(version.getCreationDate()).append(QRCODE_DATA_DELIMITER);
        buffer.append("UpdateDate=").append(version.getModificationDate()).append(QRCODE_DATA_DELIMITER);
        return buffer.toString();
    }

    private static BitMatrix generateMatrix(String data, int size) throws WriterException {
        return new QRCodeWriter().encode(data, BarcodeFormat.QR_CODE, size, size);
    }

    private static void writeImage(String outputFileName, String imageFormat, BitMatrix bitMatrix) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(new File(outputFileName));
        MatrixToImageWriter.writeToStream(bitMatrix, imageFormat, fileOutputStream);
        fileOutputStream.close();
    }

}
