package org.ukiuni.report.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.UUID;

import org.ukiuni.report.entity.Account;
import org.ukiuni.report.entity.IconImage;
import org.ukiuni.report.entity.Report;
import org.ukiuni.report.entity.ReportImage;
import org.ukiuni.report.util.DBUtil;
import org.ukiuni.report.util.ImageUtil;
import org.ukiuni.report.util.StreamUtil;

public class ImageService {
	public DBUtil dbUtil = DBUtil.create("org.ukiuni.report");

	public IconImage regist(Account register, InputStream in) throws IOException {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		new ImageUtil().trim(in, 120, 120, bout);

		IconImage iconImage = new IconImage();
		iconImage.setKey(UUID.randomUUID().toString());
		iconImage.setRegister(register);
		iconImage.setCreatedAt(new Date());
		iconImage.setContent(bout.toByteArray());
		dbUtil.persist(iconImage);
		return iconImage;
	}

	public IconImage loadIconByKey(String key) {
		return dbUtil.findSingleEquals(IconImage.class, "key", key);
	}

	public ReportImage loadReportImageByKey(String key) {
		return dbUtil.findSingleEquals(ReportImage.class, "key", key);
	}

	public ReportImage regist(Account register, Report report, InputStream in) throws IOException {
		ReportImage reportImage = new ReportImage();
		reportImage.setKey(UUID.randomUUID().toString());
		reportImage.setReportKey(report.getKey());
		reportImage.setRegister(register);
		reportImage.setCreatedAt(new Date());
		reportImage.setContent(StreamUtil.toByteArray(in));
		dbUtil.persist(reportImage);
		return reportImage;
	}
}
