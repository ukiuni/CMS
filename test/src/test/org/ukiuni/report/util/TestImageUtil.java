package test.org.ukiuni.report.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.Assert;
import org.junit.Test;
import org.ukiuni.report.util.ImageUtil;

public class TestImageUtil {

	@Test
	public void test() throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		long startTime = System.currentTimeMillis();
		new ImageUtil().trim(TestImageUtil.class.getResourceAsStream("/testImage.jpg"), 200, 200, out);
		long cost = System.currentTimeMillis() - startTime;
		System.out.println("cost = " + cost);
		ImageIO.read(new ByteArrayInputStream(out.toByteArray()));
		Assert.assertTrue("image trim cost must be shorter than 2000ms", cost < 2000);
		FileOutputStream fout = new FileOutputStream("test/result/resultImage.jpg");
		fout.write(out.toByteArray());
		fout.close();
	}

}
