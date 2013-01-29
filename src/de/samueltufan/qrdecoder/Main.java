package de.samueltufan.qrdecoder;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

public class Main 
{
	public static void main(String[] args) 
	{
		for (int i=0; i<=8; i++)
		{
			String result =  decodeQRTag("src/data/0" + i + ".jpg", true);
			
			if (result != null)
			{
				System.out.println(result + "!");
			}
			else
			{
				System.err.println("No QR-Code found");
			}
		}

	}
	
	public static String decodeQRTag(String filePath, boolean tryHard)
	{
		File f = new File(filePath);

		BufferedImage image = null;
		
		try 
		{
			image = ImageIO.read(f);
		} catch (IOException e1)
		{
			e1.printStackTrace();
		}

		//convert the image to a binary bitmap source
		LuminanceSource source = new BufferedImageLuminanceSource(image);
		BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

		//decode the barcode
		QRCodeReader reader = new QRCodeReader();

		Result result = null;
		
		Map<DecodeHintType, ?> hints = new HashMap<DecodeHintType, Object>();
		if (tryHard)
		{
			hints.put(DecodeHintType.TRY_HARDER, null);
		}
		
		try 
		{
			result = reader.decode(bitmap, hints);
		} 
		catch (ReaderException e) 
		{}

		//byte[] b = result.getRawBytes();
		if (result != null)
		{
			return result.getText();
		}
		else
		{
			return null;
		}
	}
}
