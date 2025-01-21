package se.replyto.camel.int001.converter;

import java.io.IOException;
import java.io.InputStream;
import org.apache.camel.Converter;
import org.apache.camel.TypeConversionException;

import com.hierynomus.smbj.share.File;

@Converter(generateLoader = true)
public class SmbFileConverter {

	@Converter
    public static InputStream toInputStream(File smbFile) throws IOException {
		
		if (smbFile == null) {
			 throw new TypeConversionException(smbFile, InputStream.class, null);
        }
        return smbFile.getInputStream();
		}
		
}
