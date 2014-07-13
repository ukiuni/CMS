package org.ukiuni.report.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.UUID;

import javax.persistence.NoResultException;

import org.ukiuni.report.entity.Account;
import org.ukiuni.report.entity.IconImage;
import org.ukiuni.report.util.DBUtil;
import org.ukiuni.report.util.ImageUtil;

public class IconImageService {
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

	public IconImage loadByKey(String key) {
		try {
			return dbUtil.findSingleEquals(IconImage.class, "key", key);
		} catch (NoResultException e) {
			return null;
		}
	}
}
